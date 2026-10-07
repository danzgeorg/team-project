package com.novasolutions.ipospu.interfaces.provided;

/**
 * Interface provided by IPOS-PU.
 * Handles email communication via an external SMTP server.
 * This service is made available to all three IPOS subsystems
 * (IPOS-SA, IPOS-CA, and IPOS-PU) as specified in the Student's Brief.
 */
public interface SMTP_API {

    /**
     * Sends an email to the specified recipient.
     *
     * @param recipient the email address of the recipient.
     *                  Must be a valid, well-formed email address.
     * @param subject   the subject line of the email. Must not be null or empty.
     * @param body      the body content of the email. Must not be null.
     * @return true if the email was sent successfully,
     *         false if the SMTP server is unavailable or delivery fails.
     *         If delivery fails, the email details (timestamp, recipient,
     *         subject, body) should be stored in a database table for
     *         later retry, as specified in the Student's Brief (footnote 14).
     */
    boolean sendEmail(String recipient, String subject, String body);
}
