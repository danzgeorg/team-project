package com.novasolutions.ipospu.service;

import com.novasolutions.ipospu.db.CampaignDAO;
import com.novasolutions.ipospu.model.CampaignProduct;
import com.novasolutions.ipospu.model.CartItem;
import com.novasolutions.ipospu.model.Product;
import com.novasolutions.ipospu.model.PromotionCampaign;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PromotionServiceTest {

    private final PromotionService promotionService = new PromotionService();
    private final CampaignDAO campaignDAO = new CampaignDAO();
    private String createdCampaignName;

    @AfterEach
    void cleanup() {
        if (createdCampaignName != null) {
            campaignDAO.deleteCampaignByName(createdCampaignName);
            createdCampaignName = null;
        }
    }

    @Test
    void createCampaign_persistsCampaignAndProducts() {
        PromotionCampaign seedCampaign = getSeedCampaign();
        int productId = seedCampaign.products().get(0).productId();

        createdCampaignName = "JUnit Promo " + System.currentTimeMillis();

        PromotionService.PromotionActionResult result = promotionService.createCampaign(
                createdCampaignName,
                LocalDate.now().plusDays(30),
                LocalDate.now().plusDays(37),
                Map.of(productId, 12.5)
        );

        assertTrue(result.success());
        assertNotNull(result.campaign());
        assertEquals(createdCampaignName, result.campaign().name());
        assertEquals(1, result.campaign().products().size());
        assertEquals(productId, result.campaign().products().get(0).productId());
    }

    @Test
    void calculatePromotionDiscount_usesActiveCampaignDiscount() {
        PromotionCampaign seedCampaign = getSeedCampaign();
        CampaignProduct product = seedCampaign.products().get(0);

        Product cartProduct = new Product(
                product.productId(),
                "TEST-CODE",
                product.productName(),
                "Box",
                "unit",
                1,
                100.0,
                10
        );

        CartItem item = new CartItem(1L, 1L, null, cartProduct, 2, LocalDateTime.now());

        double discount = promotionService.calculatePromotionDiscount(List.of(item));
        assertEquals(100.0 * 2 * (seedCampaign.discountPercent() / 100.0), discount, 0.01);
    }

    @Test
    void deactivateCampaign_marksCampaignInactive() {
        PromotionCampaign seedCampaign = getSeedCampaign();
        int productId = seedCampaign.products().get(0).productId();

        createdCampaignName = "JUnit Deactivate " + System.currentTimeMillis();

        PromotionService.PromotionActionResult created = promotionService.createCampaign(
                createdCampaignName,
                LocalDate.now().plusDays(30),
                LocalDate.now().plusDays(37),
                Map.of(productId, 10.0)
        );

        assertTrue(created.success());
        assertNotNull(created.campaign());

        PromotionService.PromotionActionResult deactivated =
                promotionService.deactivateCampaign(created.campaign().id());

        assertTrue(deactivated.success());
        assertNotNull(deactivated.campaign());
        assertEquals("INACTIVE", deactivated.campaign().status());
    }

    @Test
    void createCampaign_rejectsOverlappingProductCampaigns() {
        PromotionCampaign seedCampaign = getSeedCampaign();
        int productId = seedCampaign.products().get(0).productId();

        PromotionService.PromotionActionResult result = promotionService.createCampaign(
                "Overlap Test " + System.currentTimeMillis(),
                seedCampaign.startDate(),
                seedCampaign.endDate(),
                Map.of(productId, 15.0)
        );

        assertFalse(result.success());
        assertTrue(result.message().toLowerCase().contains("conflict"));
    }

    private PromotionCampaign getSeedCampaign() {
        List<PromotionCampaign> active = promotionService.getActiveCampaigns();
        Assumptions.assumeTrue(!active.isEmpty(), "Seeded active campaigns are required for promotion tests");
        Assumptions.assumeTrue(!active.get(0).products().isEmpty(), "Seeded active campaign must have products");
        return active.get(0);
    }

}
