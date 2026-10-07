package com.novasolutions.ipospu.service;

public class ChangePasswordResult {
    private final boolean success;
    private final String message;

    private ChangePasswordResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public static ChangePasswordResult success(String message) {
        return new ChangePasswordResult(true, message);
    }

    public static ChangePasswordResult failure(String message) {
        return new ChangePasswordResult(false, message);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }
}
