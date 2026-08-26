package apiTests.iteration2_senior.dao.sql;

import java.sql.Connection;

public class BaseSQL {
    protected final Connection connection;

    public BaseSQL() {
        this.connection = DatabaseConnection.getConnection();
    }
}