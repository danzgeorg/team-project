package com.novasolutions.ipospu.model;

import java.time.LocalDateTime;

public class CartItem {

    private final long id;
    private final Long memberId;
    private final String sessionId;
    private final Product product;
    private int quantity;
    private final LocalDateTime addedAt;

    public CartItem(long id, Long memberId, String sessionId,
                    Product product, int quantity, LocalDateTime addedAt) {
        this.id        = id;
        this.memberId  = memberId;
        this.sessionId = sessionId;
        this.product   = product;
        this.quantity  = quantity;
        this.addedAt   = addedAt;
    }

    public long getId()              { return id; }
    public Long getMemberId()        { return memberId; }
    public String getSessionId()     { return sessionId; }
    public Product getProduct()      { return product; }
    public int getQuantity()         { return quantity; }
    public void setQuantity(int q)   { this.quantity = q; }
    public LocalDateTime getAddedAt(){ return addedAt; }
    public double getLineTotal()     { return product.getPrice() * quantity; }
}