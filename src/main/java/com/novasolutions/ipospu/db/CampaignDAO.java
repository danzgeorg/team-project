package com.novasolutions.ipospu.db;

import com.novasolutions.ipospu.model.CampaignProduct;
import com.novasolutions.ipospu.model.PromotionCampaign;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class CampaignDAO {

    // The live Aiven schema stores the campaign discount on campaign_products,
    // while promotion_campaigns keeps the campaign metadata and click count.

    public List<PromotionCampaign> getAllCampaigns() {
        String sql = """
                SELECT id, name, start_date, end_date, status, click_count, created_at
                FROM ipos_pu.promotion_campaigns
                ORDER BY created_at DESC, id DESC
                """;

        try (Connection conn = DatabaseConnection.getInstance().getPuConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            List<PromotionCampaign> campaigns = new ArrayList<>();
            while (rs.next()) {
                campaigns.add(mapCampaign(conn, rs));
            }
            return campaigns;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load campaigns: " + e.getMessage(), e);
        }
    }

    public List<PromotionCampaign> getActiveCampaigns() {
        String sql = """
                SELECT id, name, start_date, end_date, status, click_count, created_at
                FROM ipos_pu.promotion_campaigns
                WHERE status = 'ACTIVE'
                  AND start_date <= CURDATE()
                  AND end_date >= CURDATE()
                ORDER BY end_date ASC, created_at DESC
                """;

        try (Connection conn = DatabaseConnection.getInstance().getPuConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            List<PromotionCampaign> campaigns = new ArrayList<>();
            while (rs.next()) {
                campaigns.add(mapCampaign(conn, rs));
            }
            return campaigns;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load active campaigns: " + e.getMessage(), e);
        }
    }

    public PromotionCampaign getCampaign(long campaignId) {
        String sql = """
                SELECT id, name, start_date, end_date, status, click_count, created_at
                FROM ipos_pu.promotion_campaigns
                WHERE id = ?
                """;

        try (Connection conn = DatabaseConnection.getInstance().getPuConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, campaignId);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                return null;
            }
            return mapCampaign(conn, rs);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load campaign: " + e.getMessage(), e);
        }
    }

    public long createCampaign(String name, LocalDate startDate, LocalDate endDate,
                               String status,
                               Map<Integer, Double> productDiscounts) {
        String campaignSql = """
                INSERT INTO ipos_pu.promotion_campaigns
                    (name, start_date, end_date, status, click_count, created_at)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        String productSql = """
                INSERT INTO ipos_pu.campaign_products
                    (campaign_id, product_id, discount_percent, items_added_count, items_purchased_count)
                VALUES (?, ?, ?, 0, 0)
                """;

        try (Connection conn = DatabaseConnection.getInstance().getPuConnection()) {
            conn.setAutoCommit(false);

            long campaignId;
            try (PreparedStatement ps = conn.prepareStatement(campaignSql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, name);
                ps.setObject(2, startDate);
                ps.setObject(3, endDate);
                ps.setString(4, status);
                ps.setInt(5, 0);
                ps.setObject(6, LocalDateTime.now());
                ps.executeUpdate();

                ResultSet keys = ps.getGeneratedKeys();
                if (!keys.next()) {
                    throw new SQLException("No generated campaign ID returned");
                }
                campaignId = keys.getLong(1);
            }

            insertCampaignProducts(conn, campaignId, productDiscounts);
            conn.commit();
            return campaignId;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create campaign: " + e.getMessage(), e);
        }
    }

    public boolean updateCampaign(long campaignId, String name, LocalDate startDate, LocalDate endDate,
                                  String status,
                                  Map<Integer, Double> productDiscounts) {
        String updateSql = """
                UPDATE ipos_pu.promotion_campaigns
                SET name = ?, start_date = ?, end_date = ?, status = ?
                WHERE id = ?
                """;

        String deleteProductsSql = "DELETE FROM ipos_pu.campaign_products WHERE campaign_id = ?";

        try (Connection conn = DatabaseConnection.getInstance().getPuConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
                ps.setString(1, name);
                ps.setObject(2, startDate);
                ps.setObject(3, endDate);
                ps.setString(4, status);
                ps.setLong(5, campaignId);
                if (ps.executeUpdate() == 0) {
                    conn.rollback();
                    return false;
                }
            }

            try (PreparedStatement ps = conn.prepareStatement(deleteProductsSql)) {
                ps.setLong(1, campaignId);
                ps.executeUpdate();
            }

            insertCampaignProducts(conn, campaignId, productDiscounts);
            conn.commit();
            return true;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update campaign: " + e.getMessage(), e);
        }
    }

    public boolean deactivateCampaign(long campaignId) {
        String sql = "UPDATE ipos_pu.promotion_campaigns SET status = 'INACTIVE' WHERE id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getPuConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, campaignId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to deactivate campaign: " + e.getMessage(), e);
        }
    }

    public boolean deleteCampaign(long campaignId) {
        String deleteProductsSql = "DELETE FROM ipos_pu.campaign_products WHERE campaign_id = ?";
        String deleteCampaignSql = "DELETE FROM ipos_pu.promotion_campaigns WHERE id = ?";

        try (Connection conn = DatabaseConnection.getInstance().getPuConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement ps = conn.prepareStatement(deleteProductsSql)) {
                ps.setLong(1, campaignId);
                ps.executeUpdate();
            }

            try (PreparedStatement ps = conn.prepareStatement(deleteCampaignSql)) {
                ps.setLong(1, campaignId);
                boolean deleted = ps.executeUpdate() > 0;
                conn.commit();
                return deleted;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete campaign: " + e.getMessage(), e);
        }
    }

    public boolean deleteCampaignByName(String name) {
        PromotionCampaign campaign = findCampaignByName(name);
        return campaign != null && deleteCampaign(campaign.id());
    }

    public PromotionCampaign findCampaignByName(String name) {
        String sql = """
                SELECT id, name, start_date, end_date, status, click_count, created_at
                FROM ipos_pu.promotion_campaigns
                WHERE name = ?
                ORDER BY id DESC
                LIMIT 1
                """;

        try (Connection conn = DatabaseConnection.getInstance().getPuConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                return null;
            }
            return mapCampaign(conn, rs);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find campaign by name: " + e.getMessage(), e);
        }
    }

    private void insertCampaignProducts(Connection conn, long campaignId, Map<Integer, Double> productDiscounts)
            throws SQLException {
        if (productDiscounts == null || productDiscounts.isEmpty()) {
            return;
        }

        String sql = """
                INSERT INTO ipos_pu.campaign_products
                    (campaign_id, product_id, discount_percent, items_added_count, items_purchased_count)
                VALUES (?, ?, ?, 0, 0)
                """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (Map.Entry<Integer, Double> entry : productDiscounts.entrySet()) {
                ps.setLong(1, campaignId);
                ps.setInt(2, entry.getKey());
                ps.setDouble(3, entry.getValue());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private PromotionCampaign mapCampaign(Connection conn, ResultSet rs) throws SQLException {
        long campaignId = rs.getLong("id");
        List<CampaignProduct> products = loadCampaignProducts(conn, campaignId);
        // Keep a representative discount on the campaign record for UI summaries.
        double discountPercent = products.isEmpty() ? 0.0 : products.get(0).discountPercent();
        return new PromotionCampaign(
                campaignId,
                rs.getString("name"),
                rs.getObject("start_date", LocalDate.class),
                rs.getObject("end_date", LocalDate.class),
                discountPercent,
                rs.getString("status"),
                rs.getInt("click_count"),
                rs.getObject("created_at", LocalDateTime.class),
                products
        );
    }

    public void incrementClickCount(long campaignId) {
        String sql = "UPDATE ipos_pu.promotion_campaigns SET click_count = click_count + 1 WHERE id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getPuConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, campaignId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to increment campaign click count: " + e.getMessage(), e);
        }
    }

    public void incrementItemsAdded(int productId, int quantity) {
        String sql = """
                UPDATE ipos_pu.campaign_products cp
                JOIN ipos_pu.promotion_campaigns pc ON pc.id = cp.campaign_id
                SET cp.items_added_count = cp.items_added_count + ?
                WHERE cp.product_id = ?
                  AND pc.status = 'ACTIVE'
                  AND pc.start_date <= CURDATE()
                  AND pc.end_date >= CURDATE()
                """;

        try (Connection conn = DatabaseConnection.getInstance().getPuConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, quantity);
            ps.setInt(2, productId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to increment promo item-added counter: " + e.getMessage(), e);
        }
    }

    public void incrementItemsPurchased(int productId, int quantity) {
        String sql = """
                UPDATE ipos_pu.campaign_products cp
                JOIN ipos_pu.promotion_campaigns pc ON pc.id = cp.campaign_id
                SET cp.items_purchased_count = cp.items_purchased_count + ?
                WHERE cp.product_id = ?
                  AND pc.status = 'ACTIVE'
                  AND pc.start_date <= CURDATE()
                  AND pc.end_date >= CURDATE()
                """;

        try (Connection conn = DatabaseConnection.getInstance().getPuConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, quantity);
            ps.setInt(2, productId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to increment promo item-purchased counter: " + e.getMessage(), e);
        }
    }

    private List<CampaignProduct> loadCampaignProducts(Connection conn, long campaignId) throws SQLException {
        String sql = """
                SELECT cp.id, cp.campaign_id, cp.product_id, cp.discount_percent, csi.item_name AS product_name
                FROM ipos_pu.campaign_products cp
                LEFT JOIN ipos_ca.ca_stock_items csi ON csi.stock_item_id = cp.product_id
                WHERE cp.campaign_id = ?
                ORDER BY cp.id ASC
                """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, campaignId);
            ResultSet rs = ps.executeQuery();
            List<CampaignProduct> products = new ArrayList<>();
            while (rs.next()) {
                products.add(new CampaignProduct(
                        rs.getLong("id"),
                        rs.getLong("campaign_id"),
                        rs.getInt("product_id"),
                        rs.getString("product_name") != null ? rs.getString("product_name") : String.valueOf(rs.getInt("product_id")),
                        rs.getDouble("discount_percent")
                ));
            }
            return Collections.unmodifiableList(products);
        }
    }
}
