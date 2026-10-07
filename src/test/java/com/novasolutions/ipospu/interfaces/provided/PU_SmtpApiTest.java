package com.novasolutions.ipospu.interfaces.provided;

import com.novasolutions.ipospu.db.DatabaseConnection;
import com.novasolutions.ipospu.impl.PU_SMTP_API;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Component tests for the IPOS-PU sales/email notification path.
 *
 * These tests verify the SMTP_API provided interface by instantiating
 * PU_SMTP_API and interacting with it through the SMTP_API interface.
 *
 * This supports the IPOS-PU notification behaviour used for:
 * - order confirmation emails after successful checkout;
 * - order status update emails;
 * - validation of malformed email notification input.
 */
class PU_SmtpApiTest {

    private SMTP_API smtpApi;

    @BeforeEach
    void setUp() {
        smtpApi = new PU_SMTP_API();
        System.out.println();
        System.out.println("Running IPOS-PU SMTP_API component test...");
    }

    @Test
    @DisplayName("CT-SN-01: Valid order confirmation email is accepted")
    void sendEmail_validInput_isDeliveredOrQueued() {
        String recipient = "ct-sn-01-" + System.currentTimeMillis() + "@example.com";
        String subject = "Order Confirmation - #999999";
        String body = "Thank you for your order. Status: RECEIVED.";

        try {
            boolean result = smtpApi.sendEmail(recipient, subject, body);
            boolean storedInEmailQueue = recipientExistsInEmailQueue(recipient);

            assertTrue(
                    result || storedInEmailQueue,
                    "Valid email input should either be sent successfully or stored in email_queue for retry."
            );

            String outcome = result
                    ? "The email was accepted by sendEmail."
                    : "The email was stored in email_queue for retry.";

            printPass(
                    "CT-SN-01",
                    "Valid order confirmation email accepted",
                    outcome
            );

        } finally {
            deleteFromEmailQueueByRecipient(recipient);
        }
    }

    @Test
    @DisplayName("CT-SN-02: Blank recipient is rejected")
    void sendEmail_blankRecipient_returnsFalse() {
        int rowsBefore = countEmailQueueRows();

        boolean result = smtpApi.sendEmail(
                "",
                "Order Confirmation",
                "Body"
        );

        int rowsAfter = countEmailQueueRows();

        assertFalse(result, "sendEmail should reject a blank recipient.");
        assertEquals(rowsBefore, rowsAfter, "Blank recipient should not create an email_queue row.");

        printPass(
                "CT-SN-02",
                "Blank recipient rejected",
                "The recipient field was blank, so the email was correctly rejected."
        );
    }

    @Test
    @DisplayName("CT-SN-03: Null email body is rejected")
    void sendEmail_nullBody_returnsFalse() {
        String recipient = "ct-sn-03-" + System.currentTimeMillis() + "@example.com";

        boolean result = smtpApi.sendEmail(
                recipient,
                "Order Confirmation",
                null
        );

        boolean storedInEmailQueue = recipientExistsInEmailQueue(recipient);

        assertFalse(result, "sendEmail should reject a null body.");
        assertFalse(storedInEmailQueue, "Null body should not create an email_queue row.");

        printPass(
                "CT-SN-03",
                "Null email body rejected",
                "The email body was missing, so the notification was correctly rejected."
        );
    }

    private boolean recipientExistsInEmailQueue(String recipient) {
        String sql = "SELECT COUNT(*) FROM email_queue WHERE recipient = ?";

        try (Connection conn = DatabaseConnection.getInstance().getPuConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, recipient);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            System.out.println("Could not check email_queue for recipient: " + recipient);
            System.out.println(e.getMessage());
            return false;
        }
    }

    private int countEmailQueueRows() {
        String sql = "SELECT COUNT(*) FROM email_queue";

        try (Connection conn = DatabaseConnection.getInstance().getPuConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            System.out.println("Could not count email_queue rows.");
            System.out.println(e.getMessage());
        }

        return -1;
    }

    private void deleteFromEmailQueueByRecipient(String recipient) {
        String sql = "DELETE FROM email_queue WHERE recipient = ?";

        try (Connection conn = DatabaseConnection.getInstance().getPuConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, recipient);
            ps.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Cleanup warning: email_queue row may remain for " + recipient);
            System.out.println(e.getMessage());
        }
    }

    private void printPass(String testId, String testName, String description) {
        System.out.println("✅ PASS " + testId + " - " + testName);
        System.out.println("  " + description);
        System.out.println("  Interface tested: SMTP_API");
        System.out.println("  Implementation tested: PU_SMTP_API");
    }
}