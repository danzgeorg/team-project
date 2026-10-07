package com.novasolutions.ipospu.service;

public class RegistrationResult {
    private final boolean success;
    private final String message;
    private final String password;

    private RegistrationResult(boolean success, String message, String password) {
        this.success = success;
        this.message = message;
        this.password = password;
    }

    public static RegistrationResult success(String password) {
        return new RegistrationResult(true, null, password);
    }

    public static RegistrationResult failure(String errorMessage) {
        return new RegistrationResult(false, errorMessage, null);
    }


    public boolean isSuccess() {
        return success;
    }

    public String getPassword() {
        return password;
    }

    public String getMessage() {
        return message;
    }
}
