package com.novasolutions.ipospu.service;

import com.novasolutions.ipospu.db.CartDAO;
import com.novasolutions.ipospu.db.MemberDAO;
import com.novasolutions.ipospu.db.ProductDAO;
import com.novasolutions.ipospu.model.Member;
import com.novasolutions.ipospu.model.Product;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static com.novasolutions.ipospu.service.OrderService.CheckoutResult.*;
import static org.junit.jupiter.api.Assertions.*;

public class OrderServiceTest {

    private final OrderService      orderService      = new OrderService();
    private final MembershipService membershipService = new MembershipService();
    private final MemberDAO         memberDAO         = new MemberDAO();
    private final CartDAO           cartDAO           = new CartDAO();
    private final ProductDAO        productDAO        = new ProductDAO();

    private static final long   VALID_CARD      = 4111_1111_1111_1111L;
    private static final String VALID_EXPIRY    = "12/28";
    private static final String EXPIRED_EXPIRY  = "01/20";
    private static final String VALID_ADDRESS   = "123 Test Street, London, E1 6RF";
    private static final long   PHANTOM_MEMBER_ID = 999_999L;

    private String testEmail;
    private Member testMember;

    @BeforeEach
    void setUp() {
        testEmail = "ordertest_" + System.currentTimeMillis() + "@example.com";
        RegistrationResult reg = membershipService.registerNonCommercial("Order Test User", testEmail);
        assertTrue(reg.isSuccess(), "Test setup failed: could not register test member");

        LoginResult login = membershipService.authenticate(testEmail, reg.getPassword());
        assertTrue(login.isSuccess(), "Test setup failed: could not authenticate test member");
        testMember = login.getMember();
    }

    @AfterEach
    void cleanup() {
        if (testEmail != null) {
            memberDAO.deleteMemberByEmail(testEmail);
        }
    }

    private Member buildApprovedMember(long id, String memberType, int orderCount) {
        return new Member(
                id,
                "Test User",
                "testorder_service@example.com",
                "hashedpassword",
                memberType,
                "APPROVED",
                null,
                orderCount,
                false,
                null,
                LocalDateTime.now()
        );
    }

    // ─────────────────────────────────────────────────────────────
    // checkout() tests
    // ─────────────────────────────────────────────────────────────

    @Test
    void checkout_validInputsWithEmptyCart_returnsCartEmpty() {
        Member member = buildApprovedMember(PHANTOM_MEMBER_ID, "NON_COMMERCIAL", 0);

        OrderService.CheckoutOutcome outcome = orderService.checkout(member, VALID_CARD, VALID_EXPIRY, VALID_ADDRESS);

        assertEquals(CART_EMPTY, outcome.result());
        assertEquals("Your cart is empty.", outcome.message());
        assertEquals(-1L, outcome.orderId());
        assertEquals(VALID_ADDRESS, outcome.deliveryAddress());
    }

    @Test
    void checkout_invalidCardNumber_cartCheckedBeforePayment_returnsCartEmpty() {
        Member member = buildApprovedMember(PHANTOM_MEMBER_ID, "NON_COMMERCIAL", 0);

        OrderService.CheckoutOutcome outcome = orderService.checkout(member, 0L, VALID_EXPIRY, VALID_ADDRESS);

        assertEquals(CART_EMPTY, outcome.result());
    }

    @Test
    void checkout_nullMember_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () ->
                orderService.checkout(null, VALID_CARD, VALID_EXPIRY, VALID_ADDRESS));
    }

    @Test
    void checkout_blankDeliveryAddress_returnsCartEmptyPreservingBlankAddress() {
        Member member = buildApprovedMember(PHANTOM_MEMBER_ID, "NON_COMMERCIAL", 0);

        OrderService.CheckoutOutcome outcome = orderService.checkout(member, VALID_CARD, VALID_EXPIRY, "");

        assertEquals(CART_EMPTY, outcome.result());
        assertEquals("", outcome.deliveryAddress());
    }

    @Test
    void checkout_memberAtLoyaltyDiscountBoundary_cartCheckedBeforeLoyaltyLogic() {
        Member member = buildApprovedMember(PHANTOM_MEMBER_ID, "NON_COMMERCIAL", 9);

        OrderService.CheckoutOutcome outcome = orderService.checkout(member, VALID_CARD, VALID_EXPIRY, VALID_ADDRESS);

        assertEquals(CART_EMPTY, outcome.result());
        assertEquals("Your cart is empty.", outcome.message());
    }

    @Test
    void checkout_guestMemberWithFreshSession_returnsCartEmptyWithGuestEmail() {
        Member guest = Member.guest();

        OrderService.CheckoutOutcome outcome = orderService.checkout(
                guest, VALID_CARD, VALID_EXPIRY, VALID_ADDRESS, "guest@example.com");

        assertEquals(CART_EMPTY, outcome.result());
        assertEquals("Your cart is empty.", outcome.message());
        assertEquals("guest@example.com", outcome.contactEmail());
    }

    @Test
    void checkout_memberWithItemInCart_expiredCard_returnsPaymentFailed() {
        List<Product> products = productDAO.getAllProducts();
        Assumptions.assumeTrue(!products.isEmpty(),
                "ipos_ca must have at least one product for this test to run");

        Product product = products.stream()
                .filter(p -> p.getStockQuantity() > 0)
                .findFirst()
                .orElse(null);
        Assumptions.assumeTrue(product != null,
                "At least one in-stock product is required for this test to run");

        cartDAO.addItem(testMember.id(), product.getId(), 1);

        OrderService.CheckoutOutcome outcome = orderService.checkout(
                testMember, VALID_CARD, EXPIRED_EXPIRY, VALID_ADDRESS);

        assertEquals(PAYMENT_FAILED, outcome.result());
        assertEquals("Payment could not be processed. Please check your card details.", outcome.message());
        assertEquals(-1L, outcome.orderId());
    }
}
