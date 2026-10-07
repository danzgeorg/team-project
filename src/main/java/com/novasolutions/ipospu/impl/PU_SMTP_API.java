package com.novasolutions.ipospu.impl;

import com.novasolutions.ipospu.db.DatabaseConnection;
import com.novasolutions.ipospu.interfaces.provided.SMTP_API;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Properties;

/**
 * Implementation of SMTP_API provided by IPOS-PU.
 * Sends email via Resend when configured.
 * If delivery fails, stores the email in the email_queue table for later retry.
 */
public class PU_SMTP_API implements SMTP_API {

    private static final String RESEND_API_URL = "https://api.resend.com/emails";

    private final ResendConfig resendConfig = loadResendConfig();
    private final HttpClient httpClient = HttpClient.newHttpClient();

    /**
     * Sends an email to the specified recipient using Resend when configured.
     * If Resend delivery fails, the email is queued with PENDING status for retry.
     *
     * @param recipient the email address of the recipient
     * @param subject   the subject line
     * @param body      the body content
     * @return true if stored/sent successfully, false on failure
     */
    @Override
    public boolean sendEmail(String recipient, String subject, String body) {
        if (recipient == null || recipient.isBlank()) return false;
        if (subject == null || subject.isBlank()) return false;
        if (body == null) return false;

        if (!resendConfig.isConfigured()) {
            System.err.println("[WARN] Resend is not configured. Queueing email for retry.");
            return storeForRetry(recipient, subject, body);
        }

        try {
            sendViaResend(recipient, subject, body);
            return recordSent(recipient, subject, body);
        } catch (Exception e) {
            System.err.println("[ERROR] Failed to send email via Resend: " + e.getMessage());
            return storeForRetry(recipient, subject, body);
        }
    }

    private void sendViaResend(String recipient, String subject, String body) throws Exception {
        String payload = """
                {
                  "from": "%s",
                  "to": ["%s"],
                  "subject": "%s",
                  "html": "%s"
                }
                """.formatted(
                escapeJson(resendConfig.from()),
                escapeJson(recipient),
                escapeJson(subject),
                escapeJson(toHtml(body))
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(RESEND_API_URL))
                .header("Authorization", "Bearer " + resendConfig.apiKey())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payload, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        int status = response.statusCode();
        if (status < 200 || status >= 300) {
            throw new IllegalStateException("HTTP " + status + ": " + response.body());
        }
    }

    private boolean recordSent(String recipient, String subject, String body) {
        String sql = """
                INSERT INTO email_queue (recipient, subject, body, status, created_at, sent_at)
                VALUES (?, ?, ?, 'SENT', ?, ?)
                """;

        try (Connection conn = DatabaseConnection.getInstance().getPuConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            LocalDateTime now = LocalDateTime.now();
            ps.setString(1, recipient);
            ps.setString(2, subject);
            ps.setString(3, body);
            ps.setObject(4, now);
            ps.setObject(5, now);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("[ERROR] Email was sent but could not be logged to email_queue: " + e.getMessage());
            return true;
        }
    }

    /**
     * Stores a failed email in the queue with status PENDING for later retry.
     */
    private boolean storeForRetry(String recipient, String subject, String body) {
        String sql = """
                INSERT INTO email_queue (recipient, subject, body, status, created_at)
                VALUES (?, ?, ?, 'PENDING', ?)
                """;
        try (Connection conn = DatabaseConnection.getInstance().getPuConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, recipient);
            ps.setString(2, subject);
            ps.setString(3, body);
            ps.setObject(4, LocalDateTime.now());
            ps.executeUpdate();
            return false;

        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    private ResendConfig loadResendConfig() {
        Properties props = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("db.properties")) {
            if (input != null) {
                props.load(input);
            }
            String apiKey = firstNonBlank(
                    System.getenv("RESEND_API_KEY"),
                    props.getProperty("resend.api_key")
            );
            String from = firstNonBlank(
                    System.getenv("RESEND_FROM"),
                    props.getProperty("resend.from"),
                    "onboarding@resend.dev"
            );
            return new ResendConfig(apiKey, from);
        } catch (Exception e) {
            System.err.println("[WARN] Failed to load Resend config: " + e.getMessage());
            return ResendConfig.empty();
        }
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    private String toHtml(String body) {
        return "<pre style=\"font-family:Arial,sans-serif;white-space:pre-wrap;\">" + escapeHtml(body) + "</pre>";
    }

    private String escapeHtml(String value) {
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

    private String escapeJson(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }

    private record ResendConfig(String apiKey, String from) {
        static ResendConfig empty() {
            return new ResendConfig(null, "onboarding@resend.dev");
        }

        boolean isConfigured() {
            return apiKey != null && !apiKey.isBlank() && from != null && !from.isBlank();
        }
    }
}
