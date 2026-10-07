package com.novasolutions.ipospu.db;

import com.novasolutions.ipospu.model.CartItem;
import com.novasolutions.ipospu.model.Product;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CartDAO {

    /**
     * Returns all cart items for a logged-in member, joining product data from ipos_ca.
     * Cross-schema join works because both schemas are on the same Aiven server.
     */
    public List<CartItem> getCartItems(long memberId) {
        String sql = """
                SELECT ci.id, ci.member_id, ci.session_id, ci.quantity, ci.added_at,
                       csi.stock_item_id, csi.item_code, csi.item_name, csi.package_type,
                       csi.unit, csi.unit_per_pack, csi.package_cost, csi.quantity_in_stock
                FROM ipos_pu.cart_items ci
                JOIN ipos_ca.ca_stock_items csi ON csi.stock_item_id = ci.product_id
                WHERE ci.member_id = ?
                ORDER BY ci.added_at DESC
                """;

        try (Connection conn = DatabaseConnection.getInstance().getPuConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, memberId);
            ResultSet rs = ps.executeQuery();
            List<CartItem> items = new ArrayList<>();
            while (rs.next()) items.add(mapRow(rs));
            return items;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to load cart items: " + e.getMessage(), e);
        }
    }

    public List<CartItem> getCartItemsBySession(String sessionId) {
        String sql = """
                SELECT ci.id, ci.member_id, ci.session_id, ci.quantity, ci.added_at,
                       csi.stock_item_id, csi.item_code, csi.item_name, csi.package_type,
                       csi.unit, csi.unit_per_pack, csi.package_cost, csi.quantity_in_stock
                FROM ipos_pu.cart_items ci
                JOIN ipos_ca.ca_stock_items csi ON csi.stock_item_id = ci.product_id
                WHERE ci.session_id = ?
                ORDER BY ci.added_at DESC
                """;

        try (Connection conn = DatabaseConnection.getInstance().getPuConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, sessionId);
            ResultSet rs = ps.executeQuery();
            List<CartItem> items = new ArrayList<>();
            while (rs.next()) items.add(mapRow(rs));
            return items;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to load guest cart items: " + e.getMessage(), e);
        }
    }

    /**
     * Adds a product to the cart. If the product is already in the cart, increments quantity.
     */
    public void addItem(long memberId, int stockItemId, int quantity) {
        // Check if already in cart
        String checkSql = "SELECT id, quantity FROM ipos_pu.cart_items WHERE member_id = ? AND product_id = ?";

        try (Connection conn = DatabaseConnection.getInstance().getPuConnection();
             PreparedStatement check = conn.prepareStatement(checkSql)) {

            check.setLong(1, memberId);
            check.setInt(2, stockItemId);
            ResultSet rs = check.executeQuery();

            if (rs.next()) {
                // Already in cart — increment
                long existingId = rs.getLong("id");
                int newQty = rs.getInt("quantity") + quantity;
                String updateSql = "UPDATE ipos_pu.cart_items SET quantity = ? WHERE id = ?";
                try (PreparedStatement upd = conn.prepareStatement(updateSql)) {
                    upd.setInt(1, newQty);
                    upd.setLong(2, existingId);
                    upd.executeUpdate();
                }
            } else {
                // New cart item
                String insertSql = """
                        INSERT INTO ipos_pu.cart_items (member_id, session_id, product_id, quantity, added_at)
                        VALUES (?, NULL, ?, ?, ?)
                        """;
                try (PreparedStatement ins = conn.prepareStatement(insertSql)) {
                    ins.setLong(1, memberId);
                    ins.setInt(2, stockItemId);
                    ins.setInt(3, quantity);
                    ins.setObject(4, LocalDateTime.now());
                    ins.executeUpdate();
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to add item to cart: " + e.getMessage(), e);
        }
    }

    public void addItemBySession(String sessionId, int stockItemId, int quantity) {
        String checkSql = "SELECT id, quantity FROM ipos_pu.cart_items WHERE session_id = ? AND product_id = ?";

        try (Connection conn = DatabaseConnection.getInstance().getPuConnection();
             PreparedStatement check = conn.prepareStatement(checkSql)) {

            check.setString(1, sessionId);
            check.setInt(2, stockItemId);
            ResultSet rs = check.executeQuery();

            if (rs.next()) {
                long existingId = rs.getLong("id");
                int newQty = rs.getInt("quantity") + quantity;
                String updateSql = "UPDATE ipos_pu.cart_items SET quantity = ? WHERE id = ?";
                try (PreparedStatement upd = conn.prepareStatement(updateSql)) {
                    upd.setInt(1, newQty);
                    upd.setLong(2, existingId);
                    upd.executeUpdate();
                }
            } else {
                String insertSql = """
                        INSERT INTO ipos_pu.cart_items (member_id, session_id, product_id, quantity, added_at)
                        VALUES (NULL, ?, ?, ?, ?)
                        """;
                try (PreparedStatement ins = conn.prepareStatement(insertSql)) {
                    ins.setString(1, sessionId);
                    ins.setInt(2, stockItemId);
                    ins.setInt(3, quantity);
                    ins.setObject(4, LocalDateTime.now());
                    ins.executeUpdate();
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to add guest cart item: " + e.getMessage(), e);
        }
    }

    /**
     * Updates the quantity of a specific cart item.
     */
    public void updateQuantity(long cartItemId, int quantity) {
        String sql = "UPDATE ipos_pu.cart_items SET quantity = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getPuConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, quantity);
            ps.setLong(2, cartItemId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update cart quantity: " + e.getMessage(), e);
        }
    }

    /**
     * Removes a single item from the cart.
     */
    public void removeItem(long cartItemId) {
        String sql = "DELETE FROM ipos_pu.cart_items WHERE id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getPuConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, cartItemId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to remove cart item: " + e.getMessage(), e);
        }
    }

    /**
     * Clears all cart items for a member (called after checkout).
     */
    public void clearCart(long memberId) {
        String sql = "DELETE FROM ipos_pu.cart_items WHERE member_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getPuConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, memberId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to clear cart: " + e.getMessage(), e);
        }
    }

    public void clearCartBySession(String sessionId) {
        String sql = "DELETE FROM ipos_pu.cart_items WHERE session_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getPuConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, sessionId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to clear guest cart: " + e.getMessage(), e);
        }
    }

    private CartItem mapRow(ResultSet rs) throws SQLException {
        Product product = new Product(
                rs.getInt("stock_item_id"),
                rs.getString("item_code"),
                rs.getString("item_name"),
                rs.getString("package_type"),
                rs.getString("unit"),
                rs.getInt("unit_per_pack"),
                rs.getDouble("package_cost"),
                rs.getInt("quantity_in_stock")
        );
        return new CartItem(
                rs.getLong("id"),
                rs.getObject("member_id") != null ? rs.getLong("member_id") : null,
                rs.getString("session_id"),
                product,
                rs.getInt("quantity"),
                rs.getObject("added_at", LocalDateTime.class)
        );
    }
}
