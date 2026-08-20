package apiTests.iteration1_senior.database;

import apiTests.iteration1_senior.configs.Config;
import apiTests.iteration1_senior.dao.AccountDao;
import apiTests.iteration1_senior.dao.UserDao;
import lombok.Builder;
import lombok.Data;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class DBRequest {
    // 3. Содержит конфигурации
    private RequestType requestType;
    private String table;
    private List<Condition> conditions;
    private Class<?> extractAsClass;

    public enum RequestType {
        SELECT, INSERT, UPDATE, DELETE
    }

    public <T> T extractAs(Class<T> clazz) {
        this.extractAsClass = clazz;
        return executeQuery(clazz);  // 4. Происходит исполнение sql запроса
    }

    //0. Каждый из шагов написан в цепочке

    private <T> T executeQuery(Class<T> clazz) {
        String sql = buildSQL();

        try (Connection connection = getConnection(); // 5. Соединение с базой данных - чтобы соединение установилось,
             // нам должен быть известен jdbc address (это просто API запросов к бд), то есть, где находится база данных
             // - ее урл -> передаем наш запрос базе данных и получаем ответ
             PreparedStatement statement = connection.prepareStatement(sql)) {

            // Set parameters for conditions
            if (conditions != null) {
                // 5.1 Несколько попыток соединения
                for (int i = 0; i < conditions.size(); i++) {
                    statement.setObject(i + 1, conditions.get(i).getValue());
                }
            }
            // 5.2 В зависимости от результата у нас происходит маппинг в соответствующий класс
            // Как сделать более масштабируемый маппинг?
            try (ResultSet resultSet = statement.executeQuery()) {
                if (clazz == UserDao.class) {
                    return (T) mapToUserDao(resultSet);
                }
                if (clazz == AccountDao.class) {
                    return (T) mapToAccountDao(resultSet);
                }
                // Add more mappings as needed
                throw new UnsupportedOperationException("Mapping for " + clazz.getSimpleName() + " not implemented");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database query failed", e);
        }
    }
    //5.2.2 Маппинг колонки к соответствующему значению
    // С помощью рефлексии это можно сделать автоматически)))
    private UserDao mapToUserDao(ResultSet resultSet) throws SQLException {
        if (resultSet.next()) {
            return UserDao.builder()
                    .id(resultSet.getLong("id"))  // колонку id кладем в id и так далее
                    .username(resultSet.getString("username"))
                    .password(resultSet.getString("password"))
                    .role(resultSet.getString("role"))
                    .name(resultSet.getString("name"))
                    .build();
        }
        return null;
    }

    private AccountDao mapToAccountDao(ResultSet resultSet) throws SQLException {
        if (resultSet.next()) {
            return AccountDao.builder()
                    .id(resultSet.getLong("id"))
                    .accountNumber(resultSet.getString("account_number"))
                    .balance(resultSet.getDouble("balance"))
                    .customerId(resultSet.getLong("customer_id"))
                    .build();
        }
        return null;
    }

    private String buildSQL() {
        StringBuilder sql = new StringBuilder();

        switch (requestType) {
            case SELECT:
                sql.append("SELECT * FROM ").append(table);
                if (conditions != null && !conditions.isEmpty()) {
                    sql.append(" WHERE ");
                    for (int i = 0; i < conditions.size(); i++) {
                        if (i > 0) sql.append(" AND ");
                        sql.append(conditions.get(i).getColumn()).append(" ").append(conditions.get(i).getOperator()).append(" ?");
                    }
                }
                break;
            default:
                throw new UnsupportedOperationException("Request type " + requestType + " not implemented");
        }

        return sql.toString();
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                Config.getProperty("db.url"),
                Config.getProperty("db.username"),
                Config.getProperty("db.password")
        );
    }

    public static DBRequestBuilder builder() {
        return new DBRequestBuilder();
    }
    //1. Отдельный класс для билдера - обертки, который хранит те же значения, что и сам класс
    public static class DBRequestBuilder {
        private RequestType requestType;
        private String table;
        private List<Condition> conditions = new ArrayList<>();
        private Class<?> extractAsClass;

        public DBRequestBuilder requestType(RequestType requestType) {
            this.requestType = requestType;
            return this;
        }

        public DBRequestBuilder where(Condition condition) {
            this.conditions.add(condition);
            return this;
        }

        public DBRequestBuilder table(String table) {
            this.table = table;
            return this;
        }

        public <T> T extractAs(Class<T> clazz) { //аналогичен методу в билдере .build()
            this.extractAsClass = clazz;
            DBRequest request = DBRequest.builder()
                    .requestType(requestType)
                    .table(table)
                    .conditions(conditions)
                    .extractAsClass(extractAsClass)
                    .build(); //2. Какой-то sql запрос как результат
            return request.extractAs(clazz);
        }
    }
}