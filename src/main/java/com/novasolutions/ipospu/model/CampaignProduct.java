package com.novasolutions.ipospu.model;

public record CampaignProduct(
        long id,
        long campaignId,
        int productId,
        String productName,
        double discountPercent
) {
}
