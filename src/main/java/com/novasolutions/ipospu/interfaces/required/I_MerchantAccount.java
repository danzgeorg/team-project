package com.novasolutions.ipospu.interfaces.required;

/**
 * Required interface provided by IPOS-SA.
 * Allows IPOS-PU and IPOS-CA to access the product catalogue,
 * place orders, retrieve invoices, and check outstanding balances.
 */
public interface I_MerchantAccount {

    /**
     * Searches the product catalogue for items matching a keyword.
     *
     * @param keyword the search term (e.g. "paracetamol"). Must not be null or empty.
     * @return a String containing the matching catalogue entries,
     *         or an empty result if no matches are found.
     */
    String searchCatalogue(String keyword);

    /**
     * Retrieves the full details of a specific catalogue item.
     *
     * @param itemID the unique product identifier in the catalogue.
     *               Must correspond to an existing item.
     * @return a String containing item details (product ID, description, unit price, availability).
     */
    String getCatalogueItem(int itemID);

    /**
     * Places a new order with IPOS-SA.
     *
     * @param orderData a String containing the order details (items, quantities, delivery address).
     *                  Must not be null or empty.
     * @return a String containing the order confirmation reference,
     *         or an error message if the order could not be placed.
     */
    String placeOrder(String orderData);

    /**
     * Retrieves the invoice for a specified order.
     *
     *
     * @param orderID the unique order identifier. Must correspond to an existing order.
     * @return a String containing the invoice details.
     */
    String getInvoice(int orderID);

    /**
     * Retrieves the outstanding balance for a specified merchant.
     *
     * @param merchantID the unique merchant identifier. Must correspond to a registered merchant.
     * @return the outstanding balance as a float value in GBP.
     */
    float getOutstandingBalance(String merchantID);
}
