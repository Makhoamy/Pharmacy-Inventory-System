package pharmacy;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static final String URL =
            "jdbc:mysql://localhost:3306/pharmacydb";

    private static final String USER = "root";
    private static final String PASSWORD = "12345";

    public static Connection getConnection() throws SQLException {
       try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("JDBC Driver not found: " + e.getMessage());
        }
        return DriverManager.getConnection(URL,USER,PASSWORD);
    }
}