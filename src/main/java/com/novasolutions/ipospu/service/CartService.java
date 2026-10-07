package com.novasolutions.ipospu.service;

import com.novasolutions.ipospu.db.CartDAO;
import com.novasolutions.ipospu.impl.InventoryDBAdapter;
import com.novasolutions.ipospu.model.CartItem;
import com.novasolutions.ipospu.model.Member;

import java.util.List;

public class CartService {

    private final CartDAO cartDAO               = new CartDAO();
    private final InventoryDBAdapter inventory  = new InventoryDBAdapter();
    private final PromotionService promotionService = new PromotionService();

    public enum AddResult { SUCCESS, OUT_OF_STOCK, INSUFFICIENT_STOCK }

    /**
     * Adds a product to the member's cart after checking stock availability.
     */
    public AddResult addToCart(Member member, int stockItemId, int quantity) {
        if (!inventory.checkStock(stockItemId, quantity)) {
            int available = getAvailableStock(stockItemId);
            return available <= 0 ? AddResult.OUT_OF_STOCK : AddResult.INSUFFICIENT_STOCK;
        }
        if (member.isGuest()) {
            cartDAO.addItemBySession(member.guestSessionId(), stockItemId, quantity);
        } else {
            cartDAO.addItem(member.id(), stockItemId, quantity);
        }
        promotionService.recordItemAdded(stockItemId, quantity);
        return AddResult.SUCCESS;
    }

    public List<CartItem> getCart(Member member) {
        return member.isGuest()
                ? cartDAO.getCartItemsBySession(member.guestSessionId())
                : cartDAO.getCartItems(member.id());
    }

    /**
     * Updates item quantity. Returns false if requested quantity exceeds stock.
     */
    public boolean updateQuantity(long cartItemId, int stockItemId, int newQuantity) {
        if (newQuantity <= 0) return false;
        if (!inventory.checkStock(stockItemId, newQuantity)) return false;
        cartDAO.updateQuantity(cartItemId, newQuantity);
        return true;
    }

    public void removeFromCart(long cartItemId) {
        cartDAO.removeItem(cartItemId);
    }

    public void clearCart(Member member) {
        if (member.isGuest()) {
            cartDAO.clearCartBySession(member.guestSessionId());
        } else {
            cartDAO.clearCart(member.id());
        }
    }

    public double getCartTotal(List<CartItem> items) {
        return items.stream().mapToDouble(CartItem::getLineTotal).sum();
    }

    private int getAvailableStock(int stockItemId) {
        // checkStock(id, 1) tells us if at least 1 is available
        return inventory.checkStock(stockItemId, 1) ? 1 : 0;
    }
}
