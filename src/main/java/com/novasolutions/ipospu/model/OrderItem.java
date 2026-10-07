package com.novasolutions.ipospu.model;

public record OrderItem(long id, long orderId, int stockItemId, String productName, int quantity, double unitPrice,
                        double lineTotal) {

}