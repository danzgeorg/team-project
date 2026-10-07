package com.novasolutions.ipospu.service;

import com.novasolutions.ipospu.model.Member;

public class LoginResult {
    private final boolean success;
    private final String message;
    private final Member member;

    private LoginResult(boolean success, String message, Member member) {
        this.success = success;
        this.message = message;
        this.member = member;
    }

    public static LoginResult success(String message, Member member) {
        return new LoginResult(true, message, member);
    }

    public static LoginResult guest(String message) {
        return new LoginResult(true, message, Member.guest());
    }

    public static LoginResult failure(String message) {
        return new LoginResult(false, message, null);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public Member getMember() {
        return member;
    }
}
