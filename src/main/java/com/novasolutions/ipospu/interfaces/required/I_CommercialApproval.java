package com.novasolutions.ipospu.interfaces.required;

/**
 * Required interface provided by IPOS-SA.
 * Allows IPOS-PU to submit commercial membership applications
 * to InfoPharma for diligence checks and to query their status.
 */
public interface I_CommercialApproval {

    /**
     * Submits a commercial membership application to IPOS-SA for
     * approval by InfoPharma staff.
     *
     * @param applicationData a String containing the applicant's company details:
     *                        Companies House registration number, Company Director(s),
     *                        type of business, business address, and email address.
     *                        Must not be null or empty.
     * @return a String containing the application reference ID assigned by IPOS-SA,
     *         or an error message if the submission failed.
     */
    String submitCommercialApplication(String applicationData);

    /**
     * Queries the current status of a commercial membership application.
     *
     * @param applicationID the unique identifier of the application as returned
     *                      by submitCommercialApplication(). Must correspond to an existing application.
     * @return a String indicating the current status (e.g. "pending", "approved", "rejected").
     */
    String getApplicationStatus(int applicationID);
}
