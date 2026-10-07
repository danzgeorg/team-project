package com.novasolutions.ipospu.db;

import java.sql.*;

public class CommercialApplicationDAO {

    public boolean pendingApplicationExistsForEmail(String email) {
        String sql = "SELECT 1 FROM commercial_applications WHERE email = ? AND status = 'PENDING'";

        try (Connection connection = DatabaseConnection.getInstance().getPuConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, email);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next();
            }

        } catch (SQLException e) {
            System.err.println("[ERROR] Failed to check pending application for email: " + email);
            System.err.println(e.getMessage());
        }
        return false;
    }

    public boolean submitApplication(String applicantName, String companyName,
                                     String placeholderPasswordHash,
                                     String companiesHouseNumber, String directorNames,
                                     String businessType, String businessAddress,
                                     String email) {

        String insertMember =
                "INSERT INTO members (full_name, email, password_hash, member_type, membership_status, company_name, created_at) " +
                        "VALUES (?, ?, ?, 'COMMERCIAL', 'PENDING', ?, NOW(6))";

        String insertApplication =
                "INSERT INTO commercial_applications (member_id, companies_house_number, director_names, business_type, business_address, email, submitted_at) " +
                        "VALUES (?, ?, ?, ?, ?, ?, NOW(6))";

        Connection connection = null;

        try {
            connection = DatabaseConnection.getInstance().getPuConnection();
            connection.setAutoCommit(false);

            long memberId;

            try (PreparedStatement ps = connection.prepareStatement(insertMember, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, applicantName);
                ps.setString(2, email);
                ps.setString(3, placeholderPasswordHash);
                ps.setString(4, companyName);
                ps.executeUpdate();

                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        memberId = keys.getLong(1);
                    } else {
                        throw new SQLException("Failed to retrieve generated member ID");
                    }
                }
            }

            try (PreparedStatement ps = connection.prepareStatement(insertApplication)) {
                ps.setLong(1, memberId);
                ps.setString(2, companiesHouseNumber);
                ps.setString(3, directorNames);
                ps.setString(4, businessType);
                ps.setString(5, businessAddress);
                ps.setString(6, email);
                ps.executeUpdate();
            }

            connection.commit();
            System.out.println("Commercial application submitted for: " + email + " (member_id=" + memberId + ")");
            return true;

        } catch (SQLException e) {

            if (connection != null) {
                try {
                    connection.rollback();
                    System.err.println("[ERROR] Transaction rolled back for email: " + email);
                } catch (SQLException rollbackEx) {
                    System.err.println("[ERROR] Failed to rollback transaction for email: " + email);
                    System.err.println(rollbackEx.getMessage());
                }
            }

            System.err.println("[ERROR] Failed to submit commercial application for email: " + email);
            System.err.println(e.getMessage());

            return false;

        } finally {
            if (connection != null) {
                try {
                    connection.setAutoCommit(true);
                    connection.close();
                } catch (SQLException e) {
                    System.err.println("[ERROR] Failed to close DB connection");
                    System.err.println(e.getMessage());
                }
            }
        }
    }
}