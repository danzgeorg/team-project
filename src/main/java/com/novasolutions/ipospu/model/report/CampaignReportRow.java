package com.novasolutions.ipospu.model.report;

import java.time.LocalDate;
import java.util.List;

public class CampaignReportRow {

    private final long campaignId;
    private final String name;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final double discountPercent;
    private final int itemCount;
    private final List<CampaignProductSaleRow> products;

    public CampaignReportRow(long campaignId, String name, LocalDate startDate,
                             LocalDate endDate, double discountPercent,
                             int itemCount, List<CampaignProductSaleRow> products) {
        this.campaignId      = campaignId;
        this.name            = name;
        this.startDate       = startDate;
        this.endDate         = endDate;
        this.discountPercent = discountPercent;
        this.itemCount       = itemCount;
        this.products        = products;
    }

    public long getCampaignId()              { return campaignId; }
    public String getName()                  { return name; }
    public LocalDate getStartDate()          { return startDate; }
    public LocalDate getEndDate()            { return endDate; }
    public double getDiscountPercent()       { return discountPercent; }
    public int getItemCount()                { return itemCount; }
    public List<CampaignProductSaleRow> getProducts() { return products; }

    public double getTotalCampaignSales() {
        return products.stream().mapToDouble(CampaignProductSaleRow::getTotalSales).sum();
    }
}
