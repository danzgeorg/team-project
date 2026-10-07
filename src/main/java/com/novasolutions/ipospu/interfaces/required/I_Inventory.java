package com.novasolutions.ipospu.interfaces.required;

/**
 * Required interface provided by IPOS-CA.
 * Allows IPOS-PU to validate stock availability and deduct
 * inventory quantities following successful online purchases.
 */
public interface I_Inventory {

    /**
     * Checks whether sufficient stock is available for a given item.
     *
     * @param itemID   the unique product identifier. Must correspond to an existing item
     *                 in the merchant's inventory.
     * @param quantity the quantity to check availability for. Must be a positive integer greater than 0.
     * @return true if the requested quantity is available in stock, false otherwise.
     */
    boolean checkStock(int itemID, int quantity);

    /**
     * Deducts the specified quantity from the merchant's inventory
     * following a successful online purchase.
     *
     * @param itemID   the unique product identifier. Must correspond to an existing item.
     * @param quantity the quantity to deduct. Must be a positive integer and must not exceed
     *                 the current stock level (checkStock should be called first).
     */
    void deductStock(int itemID, int quantity);

    /**
     * Retrieves the full product catalogue from the merchant's inventory.
     *
     * @return a String containing all available products with their details
     *         (ID, description, price, stock level).
     */
    String getProductCatalogue();
}
