package com.novasolutions.ipospu.db;

import com.novasolutions.ipospu.model.report.CampaignEngagementRow;
import com.novasolutions.ipospu.model.report.CampaignProductSaleRow;
import com.novasolutions.ipospu.model.report.CampaignReportRow;
import com.novasolutions.ipospu.model.report.SalesReportRow;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ReportDAO {

    // ─── Report 1: Sales Report (Appendix 8) ────────────────────────────────────

    public List<SalesReportRow> getSalesReport(LocalDate from, LocalDate to) {
        String sql = """
                SELECT
                    csi.item_code                   AS item_id,
                    csi.item_name                   AS description,
                    SUM(oi.quantity)                AS sold_packs,
                    oi.unit_price                   AS unit_price,
                    SUM(oi.line_total)              AS total
                FROM ipos_pu.order_items oi
                JOIN ipos_pu.orders o ON o.id = oi.order_id
                JOIN ipos_ca.ca_stock_items csi ON csi.stock_item_id = oi.product_id
                WHERE o.created_at >= ?
                  AND o.created_at <  DATE_ADD(?, INTERVAL 1 DAY)
                  AND o.status NOT IN ('REFUNDED')
                GROUP BY csi.item_code, csi.item_name, oi.unit_price
                ORDER BY csi.item_code
                """;

        try (Connection conn = DatabaseConnection.getInstance().getPuConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setObject(1, from.atStartOfDay());
            ps.setObject(2, to.atStartOfDay());

            List<SalesReportRow> rows = new ArrayList<>();
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                rows.add(new SalesReportRow(
                        rs.getString("item_id"),
                        rs.getString("description"),
                        rs.getInt("sold_packs"),
                        rs.getDouble("unit_price"),
                        rs.getDouble("total")
                ));
            }
            return rows;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to generate sales report: " + e.getMessage(), e);
        }
    }

    // ─── Report 2: Campaign Report headers (Appendix 9) ─────────────────────────

    public List<CampaignReportRow> getCampaignReportHeaders(LocalDate from, LocalDate to) {
        // Live DB: promotion_campaigns has no discount_percent column (it's per-product in campaign_products)
        String sql = """
                SELECT
                    pc.id,
                    pc.name,
                    pc.start_date,
                    pc.end_date,
                    COALESCE(MIN(cp.discount_percent), 0) AS discount_percent,
                    COUNT(cp.product_id) AS item_count
                FROM ipos_pu.promotion_campaigns pc
                LEFT JOIN ipos_pu.campaign_products cp ON cp.campaign_id = pc.id
                WHERE pc.start_date <= ?
                  AND pc.end_date   >= ?
                GROUP BY pc.id, pc.name, pc.start_date, pc.end_date
                ORDER BY pc.start_date
                """;

        try (Connection conn = DatabaseConnection.getInstance().getPuConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setObject(1, to);
            ps.setObject(2, from);

            List<CampaignReportRow> rows = new ArrayList<>();
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                rows.add(new CampaignReportRow(
                        rs.getLong("id"),
                        rs.getString("name"),
                        rs.getObject("start_date", LocalDate.class),
                        rs.getObject("end_date", LocalDate.class),
                        rs.getDouble("discount_percent"),
                        rs.getInt("item_count"),
                        new ArrayList<>()   // products populated separately by service
                ));
            }
            return rows;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to generate campaign report headers: " + e.getMessage(), e);
        }
    }

    // ─── Report 2: Per-campaign product sales (Appendix 9) ──────────────────────

    public List<CampaignProductSaleRow> getCampaignProductSales(long campaignId,
                                                                 LocalDate from,
                                                                 LocalDate to) {
        String sql = """
                SELECT
                    csi.item_code                   AS item_id,
                    csi.item_name                   AS description,
                    cp.discount_percent             AS discount,
                    COALESCE(SUM(oi.quantity), 0)   AS items_sold,
                    COALESCE(SUM(oi.line_total), 0) AS total_sales
                FROM ipos_pu.campaign_products cp
                JOIN ipos_ca.ca_stock_items csi ON csi.stock_item_id = cp.product_id
                LEFT JOIN ipos_pu.order_items oi ON oi.product_id = cp.product_id
                LEFT JOIN ipos_pu.orders o ON o.id = oi.order_id
                    AND o.created_at >= ?
                    AND o.created_at <  DATE_ADD(?, INTERVAL 1 DAY)
                    AND o.status NOT IN ('REFUNDED')
                WHERE cp.campaign_id = ?
                GROUP BY csi.item_code, csi.item_name, cp.discount_percent
                ORDER BY csi.item_code
                """;

        try (Connection conn = DatabaseConnection.getInstance().getPuConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setObject(1, from.atStartOfDay());
            ps.setObject(2, to.atStartOfDay());
            ps.setLong(3, campaignId);

            List<CampaignProductSaleRow> rows = new ArrayList<>();
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                rows.add(new CampaignProductSaleRow(
                        rs.getString("item_id"),
                        rs.getString("description"),
                        rs.getDouble("discount"),
                        rs.getInt("items_sold"),
                        rs.getDouble("total_sales")
                ));
            }
            return rows;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to generate campaign product sales: " + e.getMessage(), e);
        }
    }

    // ─── Report 3: Campaign Engagement Report (Appendix 10) ─────────────────────

    public List<CampaignEngagementRow> getCampaignEngagement(long campaignId) {
        // Live DB has no campaign_tracking table.
        // Campaign-level click count is in promotion_campaigns.click_count.
        // Per-product counters are in campaign_products.items_added_count / items_purchased_count.
        String sql = """
                SELECT
                    pc.id                                   AS campaign_id,
                    pc.name                                 AS campaign_name,
                    CAST(NULL AS UNSIGNED)                  AS product_id,
                    CAST(NULL AS CHAR)                      AS product_name,
                    pc.click_count,
                    0                                       AS items_added_count,
                    0                                       AS items_purchased_count,
                    CAST(NULL AS DECIMAL(10,2))             AS conversion_rate
                FROM ipos_pu.promotion_campaigns pc
                WHERE pc.id = ?

                UNION ALL

                SELECT
                    pc.id,
                    pc.name,
                    cp.product_id,
                    csi.item_name,
                    cp.items_added_count,
                    cp.items_added_count,
                    cp.items_purchased_count,
                    CASE
                        WHEN cp.items_added_count = 0 THEN 0.00
                        ELSE ROUND(cp.items_purchased_count * 100.0 / cp.items_added_count, 2)
                    END
                FROM ipos_pu.campaign_products cp
                JOIN ipos_pu.promotion_campaigns pc ON pc.id = cp.campaign_id
                LEFT JOIN ipos_ca.ca_stock_items csi ON csi.stock_item_id = cp.product_id
                WHERE cp.campaign_id = ?
                ORDER BY product_id IS NULL DESC, product_id ASC
                """;

        try (Connection conn = DatabaseConnection.getInstance().getPuConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, campaignId);   // first SELECT (campaign-level row)
            ps.setLong(2, campaignId);   // second SELECT (per-product rows)

            List<CampaignEngagementRow> rows = new ArrayList<>();
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int productId = rs.getInt("product_id");
                boolean productIsNull = rs.wasNull();

                double conversionRate = rs.getDouble("conversion_rate");
                boolean conversionIsNull = rs.wasNull();

                rows.add(new CampaignEngagementRow(
                        rs.getLong("campaign_id"),
                        rs.getString("campaign_name"),
                        productIsNull ? null : productId,
                        rs.getString("product_name"),
                        rs.getInt("click_count"),
                        rs.getInt("items_added_count"),
                        rs.getInt("items_purchased_count"),
                        conversionIsNull ? null : conversionRate
                ));
            }
            return rows;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to generate campaign engagement report: " + e.getMessage(), e);
        }
    }
}
