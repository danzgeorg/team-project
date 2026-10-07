package com.novasolutions.ipospu.model;

import java.time.LocalDateTime;

// I use a record here to keep the Member model immutable and concise.
// Each field maps directly to a column in the `members` table.
public record Member(
        long id,               // I use long because the DB column is bigint
        String fullName,       // I renamed this from 'name' to match the 'full_name' column
        String email,
        String passwordHash,
        String memberType,     // I store this as a String — valid values: NON_COMMERCIAL, COMMERCIAL
        String membershipStatus, // I store this as a String — valid values: APPROVED, PENDING, etc.
        String companyName,    // I include this for commercial members; may be null for non-commercial
        int orderCount,        // I track how many orders this member has placed; defaults to 0 in DB
        boolean isFirstLogin,  // I use this flag to prompt a password change on first login
        String guestSessionId, // I use this only for guest browsing/cart sessions
        LocalDateTime createdAt // I capture when the account was created
) {
    public static Member guest() {
        return new Member(
                -1L,
                "Guest User",
                "guest@ipos-pu.local",
                "",
                "GUEST",
                "GUEST",
                null,
                0,
                false,
                java.util.UUID.randomUUID().toString(),
                LocalDateTime.now()
        );
    }

    public boolean isGuest() {
        return "GUEST".equalsIgnoreCase(memberType);
    }
}
