package apiTests.iteration2_senior.dao.sql;

import apiTests.iteration2_senior.configs.Config;

import java.sql.*;

public class DatabaseConnection {
    private DatabaseConnection() {}

    public static Connection getConnection() {
        try {
            Class.forName(Config.getProperty("db.driver"));

            return DriverManager.getConnection(
                    Config.getProperty("db.url"),
                    Config.getProperty("db.username"),
                    Config.getProperty("db.password")
            );
        } catch (SQLException | ClassNotFoundException e) {
            throw new RuntimeException("Failed to connect to database", e);
        }
    }
}