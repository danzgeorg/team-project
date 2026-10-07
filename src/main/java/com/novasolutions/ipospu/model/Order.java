package com.novasolutions.ipospu.model;

import java.time.LocalDateTime;
import java.util.List;

public class Order {

    private final long id;
    private final Long memberId;
    private final String guestEmail;
    private final String guestAddress;
    private String status;
    private final double totalAmount;
    private final double discountAmount;
    private final LocalDateTime createdAt;
    private final List<OrderItem> items;

    public Order(long id, Long memberId, String guestEmail, String guestAddress,
                 String status, double totalAmount, double discountAmount,
                 LocalDateTime createdAt, List<OrderItem> items) {
        this.id             = id;
        this.memberId       = memberId;
        this.guestEmail     = guestEmail;
        this.guestAddress   = guestAddress;
        this.status         = status;
        this.totalAmount    = totalAmount;
        this.discountAmount = discountAmount;
        this.createdAt      = createdAt;
        this.items          = items;
    }

    public long getId()              { return id; }
    public Long getMemberId()        { return memberId; }
    public String getGuestEmail()    { return guestEmail; }
    public String getGuestAddress()  { return guestAddress; }
    public String getStatus()        { return status; }
    public void setStatus(String s)  { this.status = s; }
    public double getTotalAmount()   { return totalAmount; }
    public double getDiscountAmount(){ return discountAmount; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public List<OrderItem> getItems(){ return items; }
}