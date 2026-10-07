package com.novasolutions.ipospu.db;

import com.novasolutions.ipospu.model.Member;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MemberDAO {

    public Member findById(long memberId) {
        String sql = """
                SELECT id, full_name, email, password_hash, member_type, membership_status,
                       company_name, order_count, is_first_login, created_at
                FROM members
                WHERE id = ?
                """;

        try (Connection connection = DatabaseConnection.getInstance().getPuConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setLong(1, memberId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return new Member(
                            resultSet.getLong("id"),
                            resultSet.getString("full_name"),
                            resultSet.getString("email"),
                            resultSet.getString("password_hash"),
                            resultSet.getString("member_type"),
                            resultSet.getString("membership_status"),
                            resultSet.getString("company_name"),
                            resultSet.getInt("order_count"),
                            resultSet.getBoolean("is_first_login"),
                            null,
                            resultSet.getTimestamp("created_at").toLocalDateTime()
                    );
                }
            }

        } catch (SQLException e) {
            System.err.println("[ERROR] Failed to fetch member by ID: " + memberId);
            System.err.println(e.getMessage());
        }

        return null;
    }

    public Member findByEmail(String email) {
        String sql = """
                SELECT id, full_name, email, password_hash, member_type, membership_status,
                       company_name, order_count, is_first_login, created_at
                FROM members
                WHERE email = ?
                """;

        try (Connection connection = DatabaseConnection.getInstance().getPuConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, email);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return new Member(
                            resultSet.getLong("id"),
                            resultSet.getString("full_name"),
                            resultSet.getString("email"),
                            resultSet.getString("password_hash"),
                            resultSet.getString("member_type"),
                            resultSet.getString("membership_status"),
                            resultSet.getString("company_name"),
                            resultSet.getInt("order_count"),
                            resultSet.getBoolean("is_first_login"),
                            null,
                            resultSet.getTimestamp("created_at").toLocalDateTime()
                    );
                }
            }

        } catch (SQLException e) {
            System.err.println("[ERROR] Failed to fetch member by email: " + email);
            System.err.println(e.getMessage());
        }

        return null;
    }

    public boolean emailExists(String email) {
        String sql = "SELECT 1 FROM members WHERE email = ?";

        try (Connection connection = DatabaseConnection.getInstance().getPuConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, email);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next();
            }

        } catch (SQLException e) {
            System.err.println("[ERROR] Failed to check email existence: " + email);
            System.err.println(e.getMessage());
        }
        return false;
    }

    public boolean createMember(String fullName, String email, String passwordHash,
                                String memberType, String membershipStatus) {

        String sql = """
                INSERT INTO members (full_name, email, password_hash, member_type, membership_status, created_at)
                VALUES (?, ?, ?, ?, ?, NOW())
                """;

        try (Connection connection = DatabaseConnection.getInstance().getPuConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, fullName);
            preparedStatement.setString(2, email);
            preparedStatement.setString(3, passwordHash);
            preparedStatement.setString(4, memberType);
            preparedStatement.setString(5, membershipStatus);

            int rowsAffected = preparedStatement.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.err.println("[ERROR] Failed to create member: " + email);
            System.err.println(e.getMessage());
        }

        return false;
    }

    public boolean deleteMemberByEmail(String email) {
        String deleteCartSql = "DELETE ci FROM cart_items ci JOIN members m ON m.id = ci.member_id WHERE m.email = ?";
        String deleteApplicationsSql = "DELETE ca FROM commercial_applications ca JOIN members m ON m.id = ca.member_id WHERE m.email = ?";
        String deleteMemberSql = "DELETE FROM members WHERE email = ?";

        try (Connection connection = DatabaseConnection.getInstance().getPuConnection()) {
            connection.setAutoCommit(false);

            try (PreparedStatement preparedStatement = connection.prepareStatement(deleteCartSql)) {
                preparedStatement.setString(1, email);
                preparedStatement.executeUpdate();
            }

            try (PreparedStatement preparedStatement = connection.prepareStatement(deleteApplicationsSql)) {
                preparedStatement.setString(1, email);
                preparedStatement.executeUpdate();
            }

            try (PreparedStatement preparedStatement = connection.prepareStatement(deleteMemberSql)) {
                preparedStatement.setString(1, email);
                int rowsAffected = preparedStatement.executeUpdate();
                connection.commit();
                return rowsAffected > 0;
            }

        } catch (SQLException e) {
            System.err.println("[ERROR] Failed to delete member: " + email);
            System.err.println(e.getMessage());
        }
        return false;
    }

    public boolean setNewPassword(String email, String newPasswordHash) {
        String sql = "UPDATE members SET password_hash = ?, is_first_login = false WHERE email = ?";

        try (Connection connection = DatabaseConnection.getInstance().getPuConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, newPasswordHash);
            preparedStatement.setString(2, email);

            int rowsAffected = preparedStatement.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.err.println("[ERROR] Failed to update password for: " + email);
            System.err.println(e.getMessage());
        }
        return false;
    }
}
