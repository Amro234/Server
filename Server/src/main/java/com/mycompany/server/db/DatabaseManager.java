package com.mycompany.server.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.mycompany.server.manager.OnlineUsersManager;

public class DatabaseManager {
    private static DatabaseManager instance;
    private static final String DB_URL = "jdbc:derby://localhost:1527/tic_tac_toe;create=false";
    private static final String USER = "root";
    private static final String PASS = "root";

    private DatabaseManager() {
        try {
            try {
                Class.forName("org.apache.derby.jdbc.ClientDriver");
            } catch (ClassNotFoundException e) {
                Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
            }
            createTables();
        } catch (Exception e) {
            // System.err.println("[DB] Database fatal error: " + e.getMessage());
        }
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
            System.out.println("[UI] Total: " + OnlineUsersManager.getInstance().getOnlineCount());
        }
        return instance;
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, USER, PASS);
    }

    private void createTables() {
        try (Connection conn = getConnection();
                Statement stmt = conn.createStatement()) {

            // Users table
            try {
                stmt.executeUpdate("CREATE TABLE users (" +
                        "id INT NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY, " +
                        "username VARCHAR(50) NOT NULL UNIQUE, " +
                        "email VARCHAR(100) NOT NULL, " +
                        "password VARCHAR(255) NOT NULL, " +
                        "score INT DEFAULT 0)");
                System.out.println("[DB] Updated users table verified.");
            } catch (SQLException e) {
                if (!e.getSQLState().equals("X0Y32"))
                    throw e;
            }

        } catch (SQLException e) {
            // System.err.println("[DB] Table check/creation failed: " + e.getMessage());
        }
    }

    public int getTotalUsers() {
        int total = 0;
        String query = "SELECT COUNT(*) AS total FROM users";
        try (Connection conn = getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) {
                total = rs.getInt("total");
            }
            System.err.println("total " + total);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return total;
    }

    public boolean updateUserScore(int userId, int newScore) {
        String query = "UPDATE users SET score = ? WHERE id = ?";
        try (Connection conn = getConnection();
                java.sql.PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, newScore);
            pstmt.setInt(2, userId);

            int affected = pstmt.executeUpdate();
            return affected > 0;
        } catch (SQLException e) {
            System.err.println("[DB] Failed to update score: " + e.getMessage());
            return false;
        }
    }
}
