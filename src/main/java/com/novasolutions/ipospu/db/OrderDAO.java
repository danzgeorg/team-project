package com.novasolutions.ipospu.db;

import com.novasolutions.ipospu.model.CartItem;
import com.novasolutions.ipospu.model.Order;
import com.novasolutions.ipospu.model.OrderItem;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class OrderDAO {

    /**
     * Inserts a new order and its line items. Returns the generated order ID.
     */
    public long createOrder(Long memberId, String guestEmail, String guestAddress,
                            List<CartItem> items, double totalAmount, double discountAmount) {

        String orderSql = """
                INSERT INTO ipos_pu.orders
                    (member_id, guest_email, guest_address, status, total_amount, discount_amount, created_at)
                VALUES (?, ?, ?, 'RECEIVED', ?, ?, ?)
                """;

        String itemSql = """
                INSERT INTO ipos_pu.order_items
                    (order_id, product_id, quantity, unit_price, line_total)
                VALUES (?, ?, ?, ?, ?)
                """;

        try (Connection conn = DatabaseConnection.getInstance().getPuConnection()) {
            conn.setAutoCommit(false);

            long orderId;
            try (PreparedStatement ps = conn.prepareStatement(orderSql, Statement.RETURN_GENERATED_KEYS)) {
                if (memberId != null) ps.setLong(1, memberId); else ps.setNull(1, Types.BIGINT);
                ps.setString(2, guestEmail);
                ps.setString(3, guestAddress);
                ps.setDouble(4, totalAmount);
                ps.setDouble(5, discountAmount);
                ps.setObject(6, LocalDateTime.now());
                ps.executeUpdate();

                ResultSet keys = ps.getGeneratedKeys();
                keys.next();
                orderId = keys.getLong(1);
            }

            try (PreparedStatement ps = conn.prepareStatement(itemSql)) {
                for (CartItem item : items) {
                    ps.setLong(1, orderId);
                    ps.setInt(2, item.getProduct().getId());
                    ps.setInt(3, item.getQuantity());
                    ps.setDouble(4, item.getProduct().getPrice());
                    ps.setDouble(5, item.getLineTotal());
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            conn.commit();
            return orderId;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to create order: " + e.getMessage(), e);
        }
    }

    /**
     * Increments the member's order count by 1 (used for 10th-order loyalty discount tracking).
     */
    public void incrementOrderCount(long memberId) {
        String sql = "UPDATE ipos_pu.members SET order_count = order_count + 1 WHERE id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getPuConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, memberId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to increment order count: " + e.getMessage(), e);
        }
    }

    /**
     * Updates the status of a single order.
     */
    public void updateOrderStatus(long orderId, String newStatus) {
        String sql = "UPDATE ipos_pu.orders SET status = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getPuConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newStatus);
            ps.setLong(2, orderId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update order status: " + e.getMessage(), e);
        }
    }

    /**
     * Returns all orders (admin view) with their line items, newest first.
     */
    public List<Order> getAllOrders() {
        String orderSql = """
                SELECT id, member_id, guest_email, guest_address, status,
                       total_amount, discount_amount, created_at
                FROM ipos_pu.orders
                ORDER BY created_at DESC
                """;

        String itemSql = """
                SELECT oi.id, oi.order_id, oi.product_id, oi.quantity, oi.unit_price, oi.line_total,
                       csi.item_name
                FROM ipos_pu.order_items oi
                LEFT JOIN ipos_ca.ca_stock_items csi ON csi.stock_item_id = oi.product_id
                WHERE oi.order_id = ?
                """;

        try (Connection conn = DatabaseConnection.getInstance().getPuConnection()) {
            List<Order> orders = new ArrayList<>();

            try (PreparedStatement ps = conn.prepareStatement(orderSql)) {
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    long orderId = rs.getLong("id");
                    List<OrderItem> items = new ArrayList<>();

                    try (PreparedStatement ips = conn.prepareStatement(itemSql)) {
                        ips.setLong(1, orderId);
                        ResultSet irs = ips.executeQuery();
                        while (irs.next()) {
                            items.add(new OrderItem(
                                    irs.getLong("id"),
                                    orderId,
                                    irs.getInt("product_id"),
                                    irs.getString("item_name"),
                                    irs.getInt("quantity"),
                                    irs.getDouble("unit_price"),
                                    irs.getDouble("line_total")
                            ));
                        }
                    }

                    Long memberId = rs.getLong("member_id");
                    if (rs.wasNull()) memberId = null;

                    orders.add(new Order(
                            orderId,
                            memberId,
                            rs.getString("guest_email"),
                            rs.getString("guest_address"),
                            rs.getString("status"),
                            rs.getDouble("total_amount"),
                            rs.getDouble("discount_amount"),
                            rs.getObject("created_at", LocalDateTime.class),
                            items
                    ));
                }
            }
            return orders;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch all orders: " + e.getMessage(), e);
        }
    }

    /**
     * Returns all orders for a member with their line items.
     */
    public List<Order> getOrdersForMember(long memberId) {
        String orderSql = """
                SELECT id, member_id, guest_email, guest_address, status,
                       total_amount, discount_amount, created_at
                FROM ipos_pu.orders
                WHERE member_id = ?
                ORDER BY created_at DESC
                """;

        String itemSql = """
                SELECT oi.id, oi.order_id, oi.product_id, oi.quantity, oi.unit_price, oi.line_total,
                       csi.item_name
                FROM ipos_pu.order_items oi
                LEFT JOIN ipos_ca.ca_stock_items csi ON csi.stock_item_id = oi.product_id
                WHERE oi.order_id = ?
                """;

        try (Connection conn = DatabaseConnection.getInstance().getPuConnection()) {
            List<Order> orders = new ArrayList<>();

            try (PreparedStatement ps = conn.prepareStatement(orderSql)) {
                ps.setLong(1, memberId);
                ResultSet rs = ps.executeQuery();

                while (rs.next()) {
                    long orderId = rs.getLong("id");
                    List<OrderItem> items = new ArrayList<>();

                    try (PreparedStatement ips = conn.prepareStatement(itemSql)) {
                        ips.setLong(1, orderId);
                        ResultSet irs = ips.executeQuery();
                        while (irs.next()) {
                            items.add(new OrderItem(
                                    irs.getLong("id"),
                                    orderId,
                                    irs.getInt("product_id"),
                                    irs.getString("item_name"),
                                    irs.getInt("quantity"),
                                    irs.getDouble("unit_price"),
                                    irs.getDouble("line_total")
                            ));
                        }
                    }

                    orders.add(new Order(
                            orderId,
                            rs.getLong("member_id"),
                            rs.getString("guest_email"),
                            rs.getString("guest_address"),
                            rs.getString("status"),
                            rs.getDouble("total_amount"),
                            rs.getDouble("discount_amount"),
                            rs.getObject("created_at", LocalDateTime.class),
                            items
                    ));
                }
            }

            return orders;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch orders: " + e.getMessage(), e);
        }
    }
}