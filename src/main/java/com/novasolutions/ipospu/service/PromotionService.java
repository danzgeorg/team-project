package com.novasolutions.ipospu.service;

import com.novasolutions.ipospu.db.CampaignDAO;
import com.novasolutions.ipospu.model.CampaignProduct;
import com.novasolutions.ipospu.model.CartItem;
import com.novasolutions.ipospu.model.PromotionCampaign;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class PromotionService {

    public record PromotionActionResult(boolean success, String message, PromotionCampaign campaign) {}

    private final CampaignDAO campaignDAO = new CampaignDAO();

    public PromotionActionResult createCampaign(String name, LocalDate startDate, LocalDate endDate,
                                                Map<Integer, Double> productDiscounts) {
        String validation = validateCampaign(name, startDate, endDate, productDiscounts);
        if (validation != null) {
            return new PromotionActionResult(false, validation, null);
        }

        List<Integer> productIds = new ArrayList<>(productDiscounts.keySet());
        String conflict = validateNoConflict(null, startDate, endDate, productIds);
        if (conflict != null) {
            return new PromotionActionResult(false, conflict, null);
        }

        Map<Integer, Double> deduped = normaliseProductDiscounts(productDiscounts);
        String status = isCurrentlyActive(startDate, endDate) ? "ACTIVE" : "DRAFT";
        long campaignId = campaignDAO.createCampaign(name.trim(), startDate, endDate, status, deduped);
        PromotionCampaign campaign = campaignDAO.getCampaign(campaignId);
        return new PromotionActionResult(true, "Campaign created successfully", campaign);
    }

    public PromotionActionResult updateCampaign(long campaignId, String name, LocalDate startDate, LocalDate endDate,
                                                Map<Integer, Double> productDiscounts) {
        String validation = validateCampaign(name, startDate, endDate, productDiscounts);
        if (validation != null) {
            return new PromotionActionResult(false, validation, null);
        }

        List<Integer> productIds = new ArrayList<>(productDiscounts.keySet());
        String conflict = validateNoConflict(campaignId, startDate, endDate, productIds);
        if (conflict != null) {
            return new PromotionActionResult(false, conflict, null);
        }

        Map<Integer, Double> deduped = normaliseProductDiscounts(productDiscounts);
        String status = isCurrentlyActive(startDate, endDate) ? "ACTIVE" : "DRAFT";
        boolean updated = campaignDAO.updateCampaign(campaignId, name.trim(), startDate, endDate, status, deduped);
        if (!updated) {
            return new PromotionActionResult(false, "Campaign not found", null);
        }
        return new PromotionActionResult(true, "Campaign updated successfully", campaignDAO.getCampaign(campaignId));
    }

    public PromotionActionResult deleteCampaign(long campaignId) {
        boolean deleted = campaignDAO.deleteCampaign(campaignId);
        return deleted
                ? new PromotionActionResult(true, "Campaign deleted", null)
                : new PromotionActionResult(false, "Campaign not found", null);
    }

    public PromotionActionResult deactivateCampaign(long campaignId) {
        boolean updated = campaignDAO.deactivateCampaign(campaignId);
        return updated
                ? new PromotionActionResult(true, "Campaign deactivated", campaignDAO.getCampaign(campaignId))
                : new PromotionActionResult(false, "Campaign not found", null);
    }

    public List<PromotionCampaign> getAllCampaigns() {
        return campaignDAO.getAllCampaigns();
    }

    public List<PromotionCampaign> getActiveCampaigns() {
        return campaignDAO.getActiveCampaigns();
    }

    public boolean hasActiveCampaigns() {
        return !getActiveCampaigns().isEmpty();
    }

    public void recordCampaignView(long campaignId) {
        campaignDAO.incrementClickCount(campaignId);
    }

    public void recordItemAdded(int productId, int quantity) {
        if (quantity > 0) {
            campaignDAO.incrementItemsAdded(productId, quantity);
        }
    }

    public void recordItemsPurchased(List<CartItem> items) {
        if (items == null || items.isEmpty()) {
            return;
        }
        for (CartItem item : items) {
            campaignDAO.incrementItemsPurchased(item.getProduct().getId(), item.getQuantity());
        }
    }

    public double calculatePromotionDiscount(List<CartItem> items) {
        if (items == null || items.isEmpty()) {
            return 0.0;
        }

        List<PromotionCampaign> activeCampaigns = getActiveCampaigns();
        if (activeCampaigns.isEmpty()) {
            return 0.0;
        }

        // If multiple active campaigns match the same product, use the best discount.
        Map<Integer, Double> bestDiscountByProduct = activeCampaigns.stream()
                .flatMap(campaign -> campaign.products().stream())
                .collect(Collectors.toMap(
                        CampaignProduct::productId,
                        CampaignProduct::discountPercent,
                        Math::max
                ));

        double discount = 0.0;
        for (CartItem item : items) {
            Double discountPercent = bestDiscountByProduct.get(item.getProduct().getId());
            if (discountPercent != null && discountPercent > 0) {
                discount += item.getLineTotal() * (discountPercent / 100.0);
            }
        }
        return discount;
    }

    public List<String> describeCampaignProducts(PromotionCampaign campaign) {
        if (campaign == null || campaign.products() == null) {
            return List.of();
        }

        return campaign.products().stream()
                .map(product -> String.format("%s (%s%%)", product.productName(), trimTrailingZeros(product.discountPercent())))
                .toList();
    }

    private String validateCampaign(String name, LocalDate startDate, LocalDate endDate,
                                    Map<Integer, Double> productDiscounts) {
        if (name == null || name.isBlank()) {
            return "Campaign name is required";
        }
        if (startDate == null || endDate == null) {
            return "Start and end dates are required";
        }
        if (endDate.isBefore(startDate)) {
            return "End date must be on or after the start date";
        }
        if (productDiscounts == null || productDiscounts.isEmpty()) {
            return "At least one product must be selected";
        }
        for (Map.Entry<Integer, Double> entry : productDiscounts.entrySet()) {
            double d = entry.getValue();
            if (d <= 0 || d > 100) {
                return "Discount for product " + entry.getKey() + " must be between 0 and 100";
            }
        }
        return null;
    }

    private Map<Integer, Double> normaliseProductDiscounts(Map<Integer, Double> productDiscounts) {
        // LinkedHashMap preserves insertion order and deduplicates by key
        return new LinkedHashMap<>(productDiscounts);
    }

    private String validateNoConflict(Long currentCampaignId, LocalDate startDate, LocalDate endDate, List<Integer> productIds) {
        // Deduplicate while preserving order
        List<Integer> candidateProducts = new ArrayList<>(new LinkedHashSet<>(productIds));
        List<PromotionCampaign> existingCampaigns = campaignDAO.getAllCampaigns();

        for (PromotionCampaign existing : existingCampaigns) {
            if (currentCampaignId != null && existing.id() == currentCampaignId) {
                continue;
            }
            if ("INACTIVE".equals(existing.status())) {
                continue;
            }
            if (!dateRangesOverlap(startDate, endDate, existing.startDate(), existing.endDate())) {
                continue;
            }

            List<String> conflicts = existing.products().stream()
                    .filter(product -> candidateProducts.contains(product.productId()))
                    .map(CampaignProduct::productName)
                    .distinct()
                    .toList();

            if (!conflicts.isEmpty()) {
                return "Promotion conflict with '" + existing.name() + "' for: " + String.join(", ", conflicts);
            }
        }

        return null;
    }

    private boolean dateRangesOverlap(LocalDate startA, LocalDate endA, LocalDate startB, LocalDate endB) {
        return !startA.isAfter(endB) && !endA.isBefore(startB);
    }

    private boolean isCurrentlyActive(LocalDate startDate, LocalDate endDate) {
        LocalDate today = LocalDate.now();
        return !today.isBefore(startDate) && !today.isAfter(endDate);
    }

    private String trimTrailingZeros(double value) {
        if (value == (long) value) {
            return String.format("%d", (long) value);
        }
        return String.format("%s", value);
    }
}
