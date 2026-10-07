package com.novasolutions.ipospu.service;

// I use a dedicated result type for the commercial application flow.
// RegistrationResult carries a password field that has no meaning here,
// so a separate class keeps the intent clear at each call site.
public class CommercialApplicationResult {

    private final boolean success;
    private final String message;

    private CommercialApplicationResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public static CommercialApplicationResult success(String message) {
        return new CommercialApplicationResult(true, message);
    }

    public static CommercialApplicationResult failure(String message) {
        return new CommercialApplicationResult(false, message);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }
}
