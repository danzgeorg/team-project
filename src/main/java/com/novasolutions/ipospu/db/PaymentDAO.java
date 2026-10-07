package com.novasolutions.ipospu.db;

import com.novasolutions.ipospu.model.PaymentRecord;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PaymentDAO {

    public List<PaymentRecord> getAllPayments() {
        String sql = """
                SELECT p.id,
                       p.order_id,
                       o.total_amount AS order_amount,
                       COALESCE(m.email, o.guest_email) AS customer_email,
                       o.guest_address,
                       p.amount,
                       p.card_number_masked,
                       p.expiry,
                       p.status,
                       p.transaction_id,
                       p.processed_at
                FROM ipos_pu.payments p
                LEFT JOIN ipos_pu.orders o ON o.id = p.order_id
                LEFT JOIN ipos_pu.members m ON m.id = o.member_id
                ORDER BY p.processed_at DESC, p.id DESC
                """;

        try (Connection conn = DatabaseConnection.getInstance().getPuConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            List<PaymentRecord> payments = new ArrayList<>();
            while (rs.next()) {
                Long orderId = rs.getLong("order_id");
                if (rs.wasNull()) {
                    orderId = null;
                }

                Double orderAmount = rs.getDouble("order_amount");
                if (rs.wasNull()) {
                    orderAmount = null;
                }

                payments.add(new PaymentRecord(
                        rs.getLong("id"),
                        orderId,
                        orderAmount,
                        rs.getString("customer_email"),
                        rs.getString("guest_address"),
                        rs.getDouble("amount"),
                        rs.getString("card_number_masked"),
                        rs.getString("expiry"),
                        rs.getString("status"),
                        rs.getString("transaction_id"),
                        rs.getObject("processed_at", LocalDateTime.class)
                ));
            }
            return payments;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch payments: " + e.getMessage(), e);
        }
    }
}
