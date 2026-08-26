package apiTests.iteration2_senior.dao.sql;

import apiTests.iteration2_senior.dao.TransactionDao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TransactionSQL extends BaseSQL {

    public TransactionDao getTransactionByAccountId(Integer accountId) {

        String sql = """
                SELECT id, amount, type, account_id, related_account_id
                FROM transactions
                WHERE account_id = ?
                ORDER BY id DESC
                LIMIT 1
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, accountId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    throw new RuntimeException("Transaction not found for account: " + accountId);
                }

                return TransactionDao.builder()
                        .id(resultSet.getInt("id"))
                        .amount(resultSet.getDouble("amount"))
                        .type(resultSet.getString("type"))
                        .accountId(resultSet.getInt("account_id"))
                        .relatedAccountId(resultSet.getInt("related_account_id"))
                        .build();
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to get transaction from DB", e);
        }
    }

    public List<TransactionDao> getTransactionsByAccountId(Integer accountId) {

        String sql = """
                SELECT id, amount, type, account_id, related_account_id
                FROM transactions
                WHERE account_id = ?
                ORDER BY id
                """;

        List<TransactionDao> transactions = new ArrayList<>();

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, accountId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    transactions.add(
                            TransactionDao.builder()
                                    .id(resultSet.getInt("id"))
                                    .amount(resultSet.getDouble("amount"))
                                    .type(resultSet.getString("type"))
                                    .accountId(resultSet.getInt("account_id"))
                                    .relatedAccountId(resultSet.getInt("related_account_id"))
                                    .build());
                }
            }
            return transactions;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to get transactions from DB", e);
        }
    }
}