package com.novasolutions.ipospu.model.report;

public class SalesReportRow {

    private final String itemId;
    private final String description;
    private final int soldPacks;
    private final double unitPrice;
    private final double total;

    public SalesReportRow(String itemId, String description, int soldPacks,
                          double unitPrice, double total) {
        this.itemId      = itemId;
        this.description = description;
        this.soldPacks   = soldPacks;
        this.unitPrice   = unitPrice;
        this.total       = total;
    }

    public String getItemId()      { return itemId; }
    public String getDescription() { return description; }
    public int getSoldPacks()      { return soldPacks; }
    public double getUnitPrice()   { return unitPrice; }
    public double getTotal()       { return total; }
}
