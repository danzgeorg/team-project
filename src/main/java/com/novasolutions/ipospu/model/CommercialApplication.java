package com.novasolutions.ipospu.model;

import java.time.LocalDateTime;

public record CommercialApplication(
        long id,
        Long memberId,
        String companiesHouseNumber,
        String directorNames,
        String businessType,
        String businessAddress,
        String email,
        Status status,
        LocalDateTime submittedAt,
        LocalDateTime reviewedAt
) {
    public enum Status {
        PENDING,
        APPROVED,
        REJECTED
    }
}