package com.novasolutions.ipospu.impl;

import com.novasolutions.ipospu.db.DatabaseConnection;
import com.novasolutions.ipospu.interfaces.required.I_Inventory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Adapter implementing I_Inventory by querying IPOS-CA's ipos_ca schema
 * on the shared Aiven MySQL server via the ca connection in DatabaseConnection.
 *
 * Cross-subsystem communication: IPOS-PU reads and writes to ipos_ca.ca_stock_items.
 * This is the mechanism that earns cross-subsystem marks.
 *
 * IMPORTANT — column name assumptions:
 * The column names below (CA_TABLE, COL_ID, COL_NAME, COL_PRICE, COL_STOCK)
 * are based on the name "ca_stock_items" confirmed by Chahra (Team 23) in the
 * cross-team Discord coordination (01/04/26). The exact column names have NOT
 * been verified — update the constants below once Aiven access is confirmed.
 */
public class InventoryDBAdapter implements I_Inventory {
    
    private static final String CA_TABLE  = "ipos_ca.ca_stock_items";
    private static final String COL_ID    = "stock_item_id";
    private static final String COL_NAME  = "item_name";
    private static final String COL_PRICE = "package_cost";
    private static final String COL_STOCK = "quantity_in_stock";
    // -----------------------------------------------------------------------

    /**
     * Checks whether sufficient stock is available for a given item in ipos_ca.
     *
     * @param itemID   the stock item ID in ipos_ca.ca_stock_items
     * @param quantity the quantity requested, must be > 0
     * @return true if stock_quantity >= quantity, false otherwise
     */
    @Override
    public boolean checkStock(int itemID, int quantity) {
        if (quantity <= 0) throw new IllegalArgumentException("Quantity must be greater than 0");

        String sql = "SELECT " + COL_STOCK + " FROM " + CA_TABLE + " WHERE " + COL_ID + " = ?";

        try (Connection conn = DatabaseConnection.getInstance().getCaConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, itemID);
            ResultSet rs = ps.executeQuery();

            if (!rs.next()) return false;
            return rs.getInt(COL_STOCK) >= quantity;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Deducts the specified quantity from ipos_ca.ca_stock_items following a purchase.
     * Should only be called after checkStock() has confirmed availability.
     *
     * @param itemID   the stock item ID in ipos_ca.ca_stock_items
     * @param quantity the quantity to deduct, must be > 0
     */
    @Override
    public void deductStock(int itemID, int quantity) {
        if (quantity <= 0) throw new IllegalArgumentException("Quantity must be greater than 0");

        String checkSql  = "SELECT " + COL_STOCK + " FROM " + CA_TABLE + " WHERE " + COL_ID + " = ?";
        String updateSql = "UPDATE " + CA_TABLE + " SET " + COL_STOCK + " = " + COL_STOCK + " - ? WHERE " + COL_ID + " = ?";

        try (Connection conn = DatabaseConnection.getInstance().getCaConnection()) {

            // Verify stock before deducting
            try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
                ps.setInt(1, itemID);
                ResultSet rs = ps.executeQuery();
                if (!rs.next()) throw new IllegalArgumentException("Item not found: " + itemID);
                int available = rs.getInt(COL_STOCK);
                if (available < quantity) {
                    throw new IllegalStateException("Insufficient stock: requested " + quantity + ", available " + available);
                }
            }

            try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
                ps.setInt(1, quantity);
                ps.setInt(2, itemID);
                ps.executeUpdate();
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to deduct stock for item " + itemID, e);
        }
    }

    /**
     * Retrieves all available products from ipos_ca.ca_stock_items.
     *
     * @return a formatted string listing all products with ID, name, price, and stock level
     */
    @Override
    public String getProductCatalogue() {
        String sql = "SELECT " + COL_ID + ", " + COL_NAME + ", " + COL_PRICE + ", " + COL_STOCK
                + " FROM " + CA_TABLE
                + " WHERE " + COL_STOCK + " > 0"
                + " ORDER BY " + COL_NAME;

        StringBuilder sb = new StringBuilder();

        try (Connection conn = DatabaseConnection.getInstance().getCaConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                sb.append(rs.getInt(COL_ID)).append("|")
                  .append(rs.getString(COL_NAME)).append("|")
                  .append(rs.getBigDecimal(COL_PRICE)).append("|")
                  .append(rs.getInt(COL_STOCK)).append("\n");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return sb.toString();
    }
}