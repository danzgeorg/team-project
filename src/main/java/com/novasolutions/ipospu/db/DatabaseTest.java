package com.novasolutions.ipospu.db;

import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseTest {

    public static void main(String[] args) {
        try (Connection conn = DatabaseConnection.getInstance().getPuConnection()) {
            if (conn != null) {
                System.out.println("ipos_pu connected successfully");
            }
        } catch (SQLException e) {
            System.err.println("ipos_pu connection failed: " + e.getMessage());
        }

        try (Connection conn = DatabaseConnection.getInstance().getCaConnection()) {
            if (conn != null) {
                System.out.println("ipos_ca connected successfully");
            }
        } catch (SQLException e) {
            System.err.println("ipos_ca connection failed: " + e.getMessage());
        }
    }
}