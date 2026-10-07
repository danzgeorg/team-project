package com.novasolutions.ipospu.model;

public class Product {

    private final int id;
    private final String code;
    private final String name;
    private final String packageType;
    private final String unit;
    private final int unitsPerPack;
    private final double price;
    private final int stockQuantity;

    public Product(int id, String code, String name, String packageType,
                   String unit, int unitsPerPack, double price, int stockQuantity) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.packageType = packageType;
        this.unit = unit;
        this.unitsPerPack = unitsPerPack;
        this.price = price;
        this.stockQuantity = stockQuantity;
    }

    public int getId()           { return id; }
    public String getCode()      { return code; }
    public String getName()      { return name; }
    public String getPackageType() { return packageType; }
    public String getUnit()      { return unit; }
    public int getUnitsPerPack() { return unitsPerPack; }
    public double getPrice()     { return price; }
    public int getStockQuantity() { return stockQuantity; }
}
