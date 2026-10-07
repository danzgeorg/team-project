package com.novasolutions.ipospu.model;

import java.time.LocalDateTime;

public record PaymentRecord(
        long id,
        Long orderId,
        Double orderAmount,
        String customerEmail,
        String deliveryAddress,
        double amount,
        String cardNumberMasked,
        String expiry,
        String status,
        String transactionId,
        LocalDateTime processedAt
) {
}
