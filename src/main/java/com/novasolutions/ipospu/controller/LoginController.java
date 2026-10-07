package com.novasolutions.ipospu.controller;

import com.novasolutions.ipospu.service.LoginResult;
import com.novasolutions.ipospu.service.MembershipService;

public class LoginController {

    private final MembershipService membershipService = new MembershipService();

    /**
     * UC-02: Attempts to authenticate a member.
     *
     * @param email the member's email address
     * @param password the plain-text password entered by the user
     * @return a LoginResult indicating success or failure; contains a user-facing
     * message describing the outcome
     */
    public LoginResult login(String email, String password) {
        return membershipService.authenticate(email, password);
    }

    public LoginResult loginAsGuest() {
        return membershipService.authenticateGuest();
    }
}
