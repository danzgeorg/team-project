package com.novasolutions.ipospu.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Development utility for resetting PU-owned tables.
 * Only touches ipos_pu data — CA and SA tables are not affected.
 */
public class DatabaseResetDAO {

    public static boolean resetPuTables() {
        String disableFk          = "SET FOREIGN_KEY_CHECKS = 0";
        String truncateApplications = "TRUNCATE TABLE commercial_applications";
        String truncateMembers    = "TRUNCATE TABLE members";
        String enableFk           = "SET FOREIGN_KEY_CHECKS = 1";

        try (Connection connection = DatabaseConnection.getInstance().getPuConnection();
             PreparedStatement ps1 = connection.prepareStatement(disableFk);
             PreparedStatement ps2 = connection.prepareStatement(truncateApplications);
             PreparedStatement ps3 = connection.prepareStatement(truncateMembers);
             PreparedStatement ps4 = connection.prepareStatement(enableFk)) {

            ps1.execute();
            ps2.execute();
            ps3.execute();
            ps4.execute();

            return true;

        } catch (SQLException e) {
            System.err.println("[ERROR] Failed to reset PU tables: " + e.getMessage());
            return false;
        }
    }
}
