package com.novasolutions.ipospu.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record PromotionCampaign(
        long id,
        String name,
        LocalDate startDate,
        LocalDate endDate,
        double discountPercent,
        String status,
        int clickCount,
        LocalDateTime createdAt,
        List<CampaignProduct> products
) {
}
