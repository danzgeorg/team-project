package com.novasolutions.ipospu.db;

import com.novasolutions.ipospu.model.Product;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {

    public List<Product> getAllProducts() {
        String sql = """
                SELECT stock_item_id, item_code, item_name, package_type,
                       unit, unit_per_pack, package_cost, quantity_in_stock
                FROM ca_stock_items
                """;

        try (Connection connection = DatabaseConnection.getInstance().getCaConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            List<Product> products = new ArrayList<>();
            while (rs.next()) {
                products.add(mapRow(rs));
            }
            return products;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch products from ipos_ca: " + e.getMessage(), e);
        }
    }

    private Product mapRow(ResultSet rs) throws SQLException {
        return new Product(
                rs.getInt("stock_item_id"),
                rs.getString("item_code"),
                rs.getString("item_name"),
                rs.getString("package_type"),
                rs.getString("unit"),
                rs.getInt("unit_per_pack"),
                rs.getDouble("package_cost"),
                rs.getInt("quantity_in_stock")
        );
    }
}
