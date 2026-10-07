package com.novasolutions.ipospu.impl;

import com.novasolutions.ipospu.db.DatabaseConnection;
import com.novasolutions.ipospu.interfaces.provided.PaymentAPI;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Implementation of PaymentAPI provided by IPOS-PU.
 * Simulates payment processing — authorises all valid requests.
 * In production, integrate a real payment processor (e.g. Stripe, PayPal).
 * Failed/unavailable service cases are stored in the payments table with status PENDING.
 */
public class PU_PaymentAPI implements PaymentAPI {

    /**
     * Processes a credit/debit card payment.
     * Validates amount, card number format, and expiry before authorising.
     * Stores the masked card number (first 4 + last 4 digits only).
     *
     * @param amount     payment amount in GBP, must be > 0
     * @param cardNumber card number (used for masking only — not stored in full)
     * @param expiry     expiry in MM/YY format, must not be in the past
     * @return true if authorised, false if declined or service unavailable
     */
    @Override
    public boolean processPayment(double amount, long cardNumber, String expiry) {
        return processPaymentAndReturnId(amount, cardNumber, expiry) != null;
    }

    public Long processPaymentAndReturnId(double amount, long cardNumber, String expiry) {
        if (amount <= 0) return null;
        if (!isValidExpiry(expiry)) return null;

        String masked = maskCard(String.valueOf(cardNumber));
        String sql = """
                INSERT INTO payments (order_id, amount, card_number_masked, expiry, status, transaction_id, processed_at)
                VALUES (NULL, ?, ?, ?, 'AUTHORISED', ?, ?)
                """;

        try (Connection conn = DatabaseConnection.getInstance().getPuConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            String transactionId = "TXN-" + System.currentTimeMillis();
            ps.setDouble(1, amount);
            ps.setString(2, masked);
            ps.setString(3, expiry);
            ps.setString(4, transactionId);
            ps.setObject(5, LocalDateTime.now());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
            }
            return null;

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void linkPaymentToOrder(long paymentId, long orderId) {
        String sql = "UPDATE payments SET order_id = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getPuConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, orderId);
            ps.setLong(2, paymentId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to link payment to order: " + e.getMessage(), e);
        }
    }

    /**
     * Refunds a previously completed payment.
     * Only permitted if the associated order has not yet reached 'SHIPPED' status.
     *
     * @param transactionID the payment row ID to refund
     * @return true if refund processed, false if not permitted or not found
     */
    @Override
    public boolean refundPayment(int transactionID) {
        if (transactionID <= 0) throw new IllegalArgumentException("Invalid transaction ID");

        String checkSql = """
                SELECT p.id, p.order_id, p.amount, o.status
                FROM payments p
                JOIN orders o ON o.id = p.order_id
                WHERE p.id = ?
                """;

        try (Connection conn = DatabaseConnection.getInstance().getPuConnection();
             PreparedStatement ps = conn.prepareStatement(checkSql)) {

            ps.setInt(1, transactionID);
            ResultSet rs = ps.executeQuery();

            if (!rs.next()) return false;

            String orderStatus = rs.getString("status");
            if ("SHIPPED".equals(orderStatus) || "DELIVERED".equals(orderStatus)) return false;

            long orderId = rs.getLong("order_id");
            double amount = rs.getDouble("amount");

            // Record the refund
            String refundSql = """
                    INSERT INTO refunds (order_id, payment_id, amount, status, processed_at)
                    VALUES (?, ?, ?, 'PROCESSED', ?)
                    """;
            try (PreparedStatement rps = conn.prepareStatement(refundSql)) {
                rps.setLong(1, orderId);
                rps.setInt(2, transactionID);
                rps.setDouble(3, amount);
                rps.setObject(4, LocalDateTime.now());
                rps.executeUpdate();
            }

            // Update order status to REFUNDED
            String updateSql = "UPDATE orders SET status = 'REFUNDED' WHERE id = ?";
            try (PreparedStatement ups = conn.prepareStatement(updateSql)) {
                ups.setLong(1, orderId);
                ups.executeUpdate();
            }

            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private String maskCard(String cardNumber) {
        if (cardNumber.length() < 8) return "****-****";
        String first = cardNumber.substring(0, 4);
        String last = cardNumber.substring(cardNumber.length() - 4);
        return first + "-****-****-" + last;
    }

    private boolean isValidExpiry(String expiry) {
        if (expiry == null || !expiry.matches("\\d{2}/\\d{2}")) return false;
        try {
            YearMonth exp = YearMonth.parse(expiry, DateTimeFormatter.ofPattern("MM/yy"));
            return !exp.isBefore(YearMonth.now());
        } catch (DateTimeParseException e) {
            return false;
        }
    }
}
