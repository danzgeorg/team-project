package com.novasolutions.ipospu.controller;

import com.novasolutions.ipospu.service.CommercialApplicationResult;
import com.novasolutions.ipospu.service.MembershipService;

public class CommercialRegistrationController {

    private final MembershipService membershipService = new MembershipService();

    /**
     * UC-01b: Submits a commercial membership application for SA review.
     * A PENDING COMMERCIAL member is created alongside the application row.
     * No login-ready account is produced until the SA approves.
     *
     * @param applicantName        full name of the person submitting
     * @param companyName          trading name of the business
     * @param companiesHouseNumber registered company number
     * @param directorNames        names of the directors
     * @param businessType         type of business
     * @param businessAddress      registered business address
     * @param email                contact email
     * @return CommercialApplicationResult indicating success or failure
     */
    public CommercialApplicationResult submitCommercialApplication(
            String applicantName, String companyName,
            String companiesHouseNumber, String directorNames,
            String businessType, String businessAddress, String email) {
        return membershipService.submitCommercialApplication(
                applicantName, companyName,
                companiesHouseNumber, directorNames,
                businessType, businessAddress, email);
    }
}
