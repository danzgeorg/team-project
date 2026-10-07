package com.novasolutions.ipospu.controller;

import com.novasolutions.ipospu.service.MembershipService;
import com.novasolutions.ipospu.service.RegistrationResult;

public class RegistrationController {

    private final MembershipService membershipService = new MembershipService();

    /**
     * UC-01a: Registers a new non-commercial member.
     *
     * @param name the member's full name
     * @param email the member's email address
     * @return a RegistrationResult indicating success or failure; contains the
     * generated password on success or an error message on failure
     */
    public RegistrationResult registerNonCommercial(String name, String email) {
        return membershipService.registerNonCommercial(name, email);
    }
}