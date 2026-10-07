package com.novasolutions.ipospu.service;

import com.novasolutions.ipospu.db.CartDAO;
import com.novasolutions.ipospu.db.OrderDAO;
import com.novasolutions.ipospu.impl.InventoryDBAdapter;
import com.novasolutions.ipospu.impl.PU_PaymentAPI;
import com.novasolutions.ipospu.impl.PU_SMTP_API;
import com.novasolutions.ipospu.model.CartItem;
import com.novasolutions.ipospu.model.Member;

import java.util.List;

public class OrderService {

    private final OrderDAO            orderDAO   = new OrderDAO();
    private final CartDAO             cartDAO    = new CartDAO();
    private final InventoryDBAdapter  inventory  = new InventoryDBAdapter();
    private final PU_PaymentAPI       paymentAPI = new PU_PaymentAPI();
    private final PU_SMTP_API         smtpAPI    = new PU_SMTP_API();
    private final PromotionService     promotionService = new PromotionService();

    private static final double LOYALTY_DISCOUNT = 0.10;
    private static final int    LOYALTY_EVERY_N  = 10;

    public enum CheckoutResult {
        SUCCESS,
        CART_EMPTY,
        STOCK_UNAVAILABLE,
        PAYMENT_FAILED
    }

    public record CheckoutOutcome(CheckoutResult result, String message, long orderId, String deliveryAddress,
                                  String contactEmail) {}

    /**
     * Executes the full checkout flow for a logged-in member:
     * 1. Validate cart is not empty
     * 2. Validate stock for all items
     * 3. Apply 10th-order loyalty discount if applicable
     * 4. Process payment
     * 5. Create order record
     * 6. Deduct stock from ipos_ca
     * 7. Send confirmation email
     * 8. Clear cart
     * 9. Increment member order count
     */
    public CheckoutOutcome checkout(Member member, long cardNumber, String expiry, String deliveryAddress) {
        return checkout(member, cardNumber, expiry, deliveryAddress, null);
    }

    public CheckoutOutcome checkout(Member member, long cardNumber, String expiry, String deliveryAddress, String guestEmail) {
        List<CartItem> items = member.isGuest()
                ? cartDAO.getCartItemsBySession(member.guestSessionId())
                : cartDAO.getCartItems(member.id());

        if (items.isEmpty()) {
            return new CheckoutOutcome(CheckoutResult.CART_EMPTY, "Your cart is empty.", -1, deliveryAddress, guestEmail);
        }

        // Validate stock for all items
        for (CartItem item : items) {
            if (!inventory.checkStock(item.getProduct().getId(), item.getQuantity())) {
                return new CheckoutOutcome(CheckoutResult.STOCK_UNAVAILABLE,
                        "'" + item.getProduct().getName() + "' no longer has sufficient stock.", -1, deliveryAddress, guestEmail);
            }
        }

        // Promotions are applied first, then the loyalty discount is calculated on the reduced subtotal.
        double subtotal = items.stream().mapToDouble(CartItem::getLineTotal).sum();
        double promotionDiscount = promotionService.calculatePromotionDiscount(items);
        double discount = promotionDiscount;
        int nextOrderCount = member.orderCount() + 1;

        if (!member.isGuest() && member.memberType().equals("NON_COMMERCIAL") && nextOrderCount % LOYALTY_EVERY_N == 0) {
            discount += (subtotal - promotionDiscount) * LOYALTY_DISCOUNT;
        }

        double total = subtotal - discount;

        // Process payment
        Long paymentId = paymentAPI.processPaymentAndReturnId(total, cardNumber, expiry);
        if (paymentId == null) {
            return new CheckoutOutcome(CheckoutResult.PAYMENT_FAILED,
                    "Payment could not be processed. Please check your card details.", -1, deliveryAddress, guestEmail);
        }

        String contactEmail = member.isGuest() ? guestEmail : member.email();

        // Create order record
        long orderId = orderDAO.createOrder(
                member.isGuest() ? null : member.id(),
                contactEmail,
                deliveryAddress,
                items,
                total,
                discount
        );
        paymentAPI.linkPaymentToOrder(paymentId, orderId);

        // Deduct stock from ipos_ca
        for (CartItem item : items) {
            inventory.deductStock(item.getProduct().getId(), item.getQuantity());
        }

        // Send confirmation email
        String emailBody = buildConfirmationEmail(member, contactEmail, orderId, items, subtotal, promotionDiscount, discount, total, deliveryAddress);
        smtpAPI.sendEmail(contactEmail, "Order Confirmation — #" + orderId, emailBody);

        // Track promotional purchases for campaign reporting.
        promotionService.recordItemsPurchased(items);

        // Clear cart and update order count
        if (member.isGuest()) {
            cartDAO.clearCartBySession(member.guestSessionId());
        } else {
            cartDAO.clearCart(member.id());
            orderDAO.incrementOrderCount(member.id());
        }

        String msg = discount > 0
                ? String.format("Order placed! You saved £%.2f with available discounts.", discount)
                : "Order placed successfully!";

        return new CheckoutOutcome(CheckoutResult.SUCCESS, msg, orderId, deliveryAddress, contactEmail);
    }

    private String buildConfirmationEmail(Member member, String contactEmail, long orderId, List<CartItem> items,
                                          double subtotal, double promotionDiscount, double discount, double total,
                                          String deliveryAddress) {
        StringBuilder sb = new StringBuilder();
        sb.append("Dear ").append(member.fullName()).append(",\n\n");
        sb.append("Thank you for your order. Here is your summary:\n\n");
        sb.append("Order ID: #").append(orderId).append("\n");
        sb.append("Email: ").append(contactEmail).append("\n");
        sb.append("Delivery Address: ").append(deliveryAddress).append("\n");
        sb.append("─────────────────────────────\n");
        for (CartItem item : items) {
            sb.append(String.format("%-30s x%d   £%.2f%n",
                    item.getProduct().getName(), item.getQuantity(), item.getLineTotal()));
        }
        sb.append("─────────────────────────────\n");
        if (promotionDiscount > 0) {
            sb.append(String.format("Promotion Discount:      -£%.2f%n", promotionDiscount));
        }
        if (discount > promotionDiscount) {
            sb.append(String.format("Subtotal:               £%.2f%n", subtotal));
            sb.append(String.format("Loyalty Discount (10%%): -£%.2f%n", discount - promotionDiscount));
        }
        sb.append(String.format("Total:                  £%.2f%n", total));
        sb.append("\nOrder Status: RECEIVED\n");
        sb.append("Track your order: Log in to IPOS-PU and go to 'My Orders' to view live status updates.\n\n");
        sb.append("Your order is being processed. Thank you for using IPOS-PU.\n\n");
        sb.append("Nova Solutions — IPOS-PU Portal");
        return sb.toString();
    }
}
