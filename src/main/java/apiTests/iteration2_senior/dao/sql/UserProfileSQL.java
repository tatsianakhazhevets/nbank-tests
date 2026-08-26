package apiTests.iteration2_senior.dao.sql;

import apiTests.iteration2_senior.dao.CustomerDao;

import java.sql.*;

public class UserProfileSQL extends BaseSQL {

    public CustomerDao getProfileByUsername(String username) {

        String sql = """
                SELECT id, username, password, name, role, created_at, updated_at
                FROM customers
                WHERE username = ?
                ORDER BY id DESC
                LIMIT 1
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    throw new RuntimeException("Profile not found for user: " + username
                    );
                }

                return CustomerDao.builder()
                        .id(resultSet.getInt("id"))
                        .username(resultSet.getString("username"))
                        .password(resultSet.getString("password"))
                        .role(resultSet.getString("role"))
                        .name(resultSet.getString("name"))
                        .build();
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to get user profile from DB", e);
        }
    }
}