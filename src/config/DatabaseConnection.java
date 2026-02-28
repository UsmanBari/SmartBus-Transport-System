package config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Singleton DB connection manager.
 * Provides a single shared Connection to transport_db.
 */
public class DatabaseConnection {

    private static final String URL      = "jdbc:mysql://localhost:3306/transport_db"
                                         + "?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private static final String USER     = "root";
    private static final String PASSWORD = "";

    private static Connection instance;

    private DatabaseConnection() {}

    /**
     * Returns the singleton Connection, creating it if necessary.
     */
    public static Connection getInstance() throws SQLException {
        if (instance == null || instance.isClosed()) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
            } catch (ClassNotFoundException e) {
                throw new SQLException("MySQL JDBC Driver not found. Add mysql-connector-j JAR to classpath.", e);
            }
            instance = DriverManager.getConnection(URL, USER, PASSWORD);
        }
        return instance;
    }

    /**
     * Closes the connection (call on shutdown).
     */
    public static void close() {
        if (instance != null) {
            try { instance.close(); } catch (SQLException ignored) {}
            instance = null;
        }
    }
}
