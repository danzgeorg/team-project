package com.novasolutions.ipospu.controller;

import com.novasolutions.ipospu.interfaces.provided.IPromotionsPortal;
import com.novasolutions.ipospu.model.CartItem;
import com.novasolutions.ipospu.model.PromotionCampaign;
import com.novasolutions.ipospu.service.PromotionService;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class PromotionsController implements IPromotionsPortal {

    private final PromotionService promotionService = new PromotionService();

    @Override
    public PromotionCampaign createCampaign(String name, LocalDate startDate, LocalDate endDate,
                                            Map<Integer, Double> productDiscounts) {
        PromotionService.PromotionActionResult result =
                promotionService.createCampaign(name, startDate, endDate, productDiscounts);
        if (!result.success()) {
            throw new IllegalArgumentException(result.message());
        }
        return result.campaign();
    }

    @Override
    public PromotionCampaign updateCampaign(long campaignId, String name, LocalDate startDate, LocalDate endDate,
                                            Map<Integer, Double> productDiscounts) {
        PromotionService.PromotionActionResult result =
                promotionService.updateCampaign(campaignId, name, startDate, endDate, productDiscounts);
        if (!result.success()) {
            throw new IllegalArgumentException(result.message());
        }
        return result.campaign();
    }

    public void deleteCampaign(long campaignId) {
        PromotionService.PromotionActionResult result = promotionService.deleteCampaign(campaignId);
        if (!result.success()) {
            throw new IllegalArgumentException(result.message());
        }
    }

    @Override
    public PromotionCampaign deactivateCampaign(long campaignId) {
        PromotionService.PromotionActionResult result = promotionService.deactivateCampaign(campaignId);
        if (!result.success()) {
            throw new IllegalArgumentException(result.message());
        }
        return result.campaign();
    }

    @Override
    public List<PromotionCampaign> getActivePromotions() {
        return promotionService.getActiveCampaigns();
    }

    @Override
    public List<PromotionCampaign> getAllCampaigns() {
        return promotionService.getAllCampaigns();
    }

    public void recordCampaignView(long campaignId) {
        promotionService.recordCampaignView(campaignId);
    }

    @Override
    public double calculatePromotionDiscount(List<CartItem> items) {
        return promotionService.calculatePromotionDiscount(items);
    }
}
