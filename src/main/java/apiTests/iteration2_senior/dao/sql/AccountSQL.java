package apiTests.iteration2_senior.dao.sql;

import apiTests.iteration2_senior.dao.AccountDao;

import java.sql.*;

public class AccountSQL extends BaseSQL {

    public AccountDao getByAccountNumber(String accountNumber) {

        String sql = """
                SELECT id, account_number, balance, customer_id
                FROM accounts
                WHERE account_number = ?
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, accountNumber);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    throw new RuntimeException("Account not found: " + accountNumber);
                }

                return AccountDao.builder()
                        .id(resultSet.getInt("id"))
                        .accountNumber(resultSet.getString("account_number"))
                        .balance(resultSet.getDouble("balance"))
                        .customerId(resultSet.getLong("customer_id"))
                        .build();
                }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get account from DB", e);
        }
    }
}