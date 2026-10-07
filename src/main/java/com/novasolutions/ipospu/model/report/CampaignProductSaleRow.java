package com.novasolutions.ipospu.model.report;

public class CampaignProductSaleRow {

    private final String itemId;
    private final String description;
    private final double discountPercent;
    private final int itemsSold;
    private final double totalSales;

    public CampaignProductSaleRow(String itemId, String description,
                                  double discountPercent, int itemsSold, double totalSales) {
        this.itemId          = itemId;
        this.description     = description;
        this.discountPercent = discountPercent;
        this.itemsSold       = itemsSold;
        this.totalSales      = totalSales;
    }

    public String getItemId()          { return itemId; }
    public String getDescription()     { return description; }
    public double getDiscountPercent() { return discountPercent; }
    public int getItemsSold()          { return itemsSold; }
    public double getTotalSales()      { return totalSales; }
}
