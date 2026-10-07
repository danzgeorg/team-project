package com.novasolutions.ipospu.model.report;

public class CampaignEngagementRow {

    private final long campaignId;
    private final String campaignName;
    private final Integer productId;       // null = campaign-level row
    private final String productName;      // null = campaign-level row
    private final int clickCount;
    private final int itemsAddedCount;
    private final int itemsPurchasedCount;
    private final Double conversionRate;   // null = campaign-level row (N/A)

    public CampaignEngagementRow(long campaignId, String campaignName,
                                 Integer productId, String productName,
                                 int clickCount, int itemsAddedCount,
                                 int itemsPurchasedCount, Double conversionRate) {
        this.campaignId           = campaignId;
        this.campaignName         = campaignName;
        this.productId            = productId;
        this.productName          = productName;
        this.clickCount           = clickCount;
        this.itemsAddedCount      = itemsAddedCount;
        this.itemsPurchasedCount  = itemsPurchasedCount;
        this.conversionRate       = conversionRate;
    }

    public long getCampaignId()          { return campaignId; }
    public String getCampaignName()      { return campaignName; }
    public Integer getProductId()        { return productId; }
    public String getProductName()       { return productName; }
    public int getClickCount()           { return clickCount; }
    public int getItemsAddedCount()      { return itemsAddedCount; }
    public int getItemsPurchasedCount()  { return itemsPurchasedCount; }
    public Double getConversionRate()    { return conversionRate; }

    public boolean isCampaignLevel()     { return productId == null; }
}
