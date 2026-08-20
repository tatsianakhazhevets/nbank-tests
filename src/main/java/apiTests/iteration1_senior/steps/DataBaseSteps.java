package apiTests.iteration1_senior.steps;

import apiTests.iteration1_senior.configs.Config;
import apiTests.iteration1_senior.dao.AccountDao;
import apiTests.iteration1_senior.dao.UserDao;
import apiTests.iteration1_senior.database.Condition;
import apiTests.iteration1_senior.database.DBRequest;
import common.helpers.StepLogger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DataBaseSteps {

    public enum Table{
        CUSTOMERS("customers"),
        ACCOUNTS("accounts");

        Table(String name) {
            this.name = name;
        }

        private String name;

        public String getName() {
            return name;
        }
    }

    public static UserDao getUserByUsername(String username) {
        return StepLogger.log("Get user from database by username: " + username, () -> {
            //Builder формирует sql request на основании переданных значений
            return DBRequest.builder()
                    .requestType(DBRequest.RequestType.SELECT) //нам нужен запрос SELECT
                    .table(Table.CUSTOMERS.getName())  //табличка customers - можно добавить в Enum, как улучшение -> вместо "customers" : Table.CUSTOMERS.getName()
                    .where(Condition.equalTo("username", username)) //username должны быть равны
                    .extractAs(UserDao.class); //сериализуем запись
        });
    }

    public static UserDao getUserById(Long id) {
        return StepLogger.log("Get user from database by ID: " + id, () -> {
            return DBRequest.builder()
                    .requestType(DBRequest.RequestType.SELECT)
                    .table(Table.CUSTOMERS.getName())
                    .where(Condition.equalTo("id", id))
                    .extractAs(UserDao.class);
        });
    }

    public static UserDao getUserByRole(String role) {
        return StepLogger.log("Get user from database by role: " + role, () -> {
            return DBRequest.builder()
                    .requestType(DBRequest.RequestType.SELECT)
                    .table(Table.CUSTOMERS.getName())
                    .where(Condition.equalTo("role", role))
                    .extractAs(UserDao.class);
        });
    }

    public static AccountDao getAccountByAccountNumber(String accountNumber) {
        return StepLogger.log("Get account from database by account number: " + accountNumber, () -> {
            return DBRequest.builder()
                    .requestType(DBRequest.RequestType.SELECT)
                    .table(Table.ACCOUNTS.getName())
                    .where(Condition.equalTo("account_number", accountNumber))
                    .extractAs(AccountDao.class);
        });
    }

    public static AccountDao getAccountById(Long id) {
        return StepLogger.log("Get account from database by ID: " + id, () -> {
            return DBRequest.builder()
                    .requestType(DBRequest.RequestType.SELECT)
                    .table(Table.ACCOUNTS.getName())
                    .where(Condition.equalTo("id", id))
                    .extractAs(AccountDao.class);
        });
    }

    public static AccountDao getAccountByCustomerId(Long customerId) {
        return StepLogger.log("Get account from database by customer ID: " + customerId, () -> {
            return DBRequest.builder()
                    .requestType(DBRequest.RequestType.SELECT)
                    .table(Table.CUSTOMERS.getName())
                    .where(Condition.equalTo("customer_id", customerId))
                    .extractAs(AccountDao.class);
        });
    }

    public static void updateAccountBalance(Long accountId, Double newBalance) {
        StepLogger.log("Update account balance in database for account ID: " + accountId + " to: " + newBalance, () -> {
            try (Connection connection = DriverManager.getConnection(
                    Config.getProperty("db.url"),
                    Config.getProperty("db.username"),
                    Config.getProperty("db.password"))) {

                String sql = "UPDATE accounts SET balance = ? WHERE id = ?";
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.setDouble(1, newBalance);
                    statement.setLong(2, accountId);
                    int rowsAffected = statement.executeUpdate();

                    if (rowsAffected == 0) {
                        throw new RuntimeException("No account found with ID: " + accountId);
                    }

                    return rowsAffected;
                }
            } catch (SQLException e) {
                throw new RuntimeException("Failed to update account balance", e);
            }
        });
    }
}