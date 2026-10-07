package com.novasolutions.ipospu.controller;

import com.novasolutions.ipospu.model.Product;
import com.novasolutions.ipospu.service.ProductService;

import java.util.List;

public class CatalogueController {

    private final ProductService productService = new ProductService();

    public List<Product> loadProducts() {
        return productService.getAllProducts();
    }
}
