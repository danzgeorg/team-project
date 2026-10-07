package com.novasolutions.ipospu.service;

import com.novasolutions.ipospu.db.ReportDAO;
import com.novasolutions.ipospu.model.report.CampaignEngagementRow;
import com.novasolutions.ipospu.model.report.CampaignProductSaleRow;
import com.novasolutions.ipospu.model.report.CampaignReportRow;
import com.novasolutions.ipospu.model.report.SalesReportRow;

import java.time.LocalDate;
import java.util.List;

public class ReportService {

    private final ReportDAO reportDAO = new ReportDAO();

    // ─── Report 1: Sales Report (Appendix 8) ────────────────────────────────────

    /**
     * Returns per-item online sales for the given date range.
     * Matches the layout of Appendix 8 in the brief.
     */
    public List<SalesReportRow> getSalesReport(LocalDate from, LocalDate to) {
        return reportDAO.getSalesReport(from, to);
    }

    // ─── Report 2: Advertising Campaigns Report (Appendix 9) ────────────────────

    /**
     * Returns all campaigns active within the given date range, each with a
     * per-product breakdown of items sold and revenue generated during that period.
     * Matches the layout of Appendix 9 in the brief.
     */
    public List<CampaignReportRow> getCampaignReport(LocalDate from, LocalDate to) {
        List<CampaignReportRow> campaigns = reportDAO.getCampaignReportHeaders(from, to);
        for (CampaignReportRow campaign : campaigns) {
            List<CampaignProductSaleRow> products =
                    reportDAO.getCampaignProductSales(campaign.getCampaignId(), from, to);
            campaign.getProducts().addAll(products);
        }
        return campaigns;
    }

    // ─── Report 3: Customer Campaign Engagement Report (Appendix 10) ────────────

    /**
     * Returns engagement counters for a specific campaign:
     * - one campaign-level row (click_count, no conversion rate)
     * - one row per product (item hits, purchases, conversion rate)
     * Matches the layout of Appendix 10 in the brief.
     */
    public List<CampaignEngagementRow> getCampaignEngagement(long campaignId) {
        return reportDAO.getCampaignEngagement(campaignId);
    }
}
