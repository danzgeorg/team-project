package com.novasolutions.ipospu.service;

import com.novasolutions.ipospu.db.ProductDAO;
import com.novasolutions.ipospu.model.Product;

import java.util.List;

public class ProductService {

    private final ProductDAO productDAO = new ProductDAO();

    public List<Product> getAllProducts() {
        return productDAO.getAllProducts();
    }
}
