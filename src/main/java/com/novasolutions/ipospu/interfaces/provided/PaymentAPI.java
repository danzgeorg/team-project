package com.novasolutions.ipospu.interfaces.provided;

/**
 * Interface provided by IPOS-PU.
 * Handles payment processing via an external payment service
 * (e.g. PayPal or alternative processor).
 */
public interface PaymentAPI {

    /**
     * Processes a credit/debit card payment for the specified amount.
     *
     * @param amount     the total payment amount in GBP. Must be a positive value greater than 0.
     * @param cardNumber the card number used for payment. Must be a valid card number.
     *                   Only the first four and last four digits should be stored by the system.
     * @param expiry     the card expiry date in MM/YY format. Must not be expired at time of processing.
     * @return true if the payment was authorised and confirmed,
     *         false if the payment was declined or the service is unavailable.
     *         If the service is unavailable, the payment details (payee, amount,
     *         timestamp, card number) should be stored in a database table as
     *         specified in the Student's Brief (footnote 14).
     */
    boolean processPayment(double amount, long cardNumber, String expiry);

    /**
     * Refunds a previously completed payment.
     *
     * @param transactionID the unique identifier of the original transaction to be refunded.
     *                      Must correspond to an existing completed transaction.
     *                      Refunds are only permitted before the order has reached 'Shipped' status.
     * @return true if the refund was processed successfully,
     *         false if the transaction was not found or the refund was not permitted.
     */
    boolean refundPayment(int transactionID);
}
