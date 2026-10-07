package com.novasolutions.ipospu.interfaces.provided;

import com.novasolutions.ipospu.model.PromotionCampaign;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface IPromotionsPortal {

    PromotionCampaign createCampaign(String name, LocalDate startDate, LocalDate endDate,
                                     Map<Integer, Double> productDiscounts);

    PromotionCampaign updateCampaign(long campaignId, String name, LocalDate startDate, LocalDate endDate,
                                     Map<Integer, Double> productDiscounts);

    PromotionCampaign deactivateCampaign(long campaignId);

    List<PromotionCampaign> getActivePromotions();

    List<PromotionCampaign> getAllCampaigns();

    double calculatePromotionDiscount(List<com.novasolutions.ipospu.model.CartItem> items);
}
