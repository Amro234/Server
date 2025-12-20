package com.mycompany.server.service;

import com.mycompany.server.db.DatabaseManager;
import java.sql.*;
import java.util.UUID;
import org.json.JSONObject;

public class AuthService {

    public static JSONObject register(String username, String email, String password) {
        JSONObject response = new JSONObject();
        response.put("success", false);

        String sql = "INSERT INTO users (username, email, password, score) VALUES (?, ?, ?, 0)";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, username);
            pstmt.setString(2, email);
            pstmt.setString(3, hashPassword(password));

            if (pstmt.executeUpdate() > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        int userId = rs.getInt(1);

                        response.put("success", true);

                        JSONObject userJson = new JSONObject();
                        userJson.put("id", userId);
                        userJson.put("username", username);
                        userJson.put("email", email);
                        userJson.put("score", 0);
                        response.put("user", userJson);
                        System.out.println("[AUTH] Registered new user: " + username);
                    }
                }
            }
        } catch (SQLException e) {
            String state = e.getSQLState();
            String msg = e.getMessage().toLowerCase();
            System.err.println("[AUTH] Registration SQL Error [" + state + "]: " + e.getMessage());

            if (state.equals("23505")) {
                if (msg.contains("username")) {
                    response.put("message", "This username is already taken.");
                } else if (msg.contains("email")) {
                    response.put("message", "This email address is already registered.");
                } else {
                    response.put("message", "User already exists with this username or email.");
                }
            } else {
                response.put("message", "Database error: " + e.getMessage());
            }
        }
        return response;
    }

    public static JSONObject login(String username, String password) {
        JSONObject response = new JSONObject();
        response.put("success", false);

        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, hashPassword(password));

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int userId = rs.getInt("id");

                    response.put("success", true);

                    JSONObject userJson = new JSONObject();
                    userJson.put("id", userId);
                    userJson.put("username", rs.getString("username"));
                    userJson.put("email", rs.getString("email"));
                    userJson.put("score", rs.getInt("score"));
                    response.put("user", userJson);
                    System.out.println("[AUTH] User logged in: " + username);
                } else {
                    response.put("message", "Invalid username or password.");
                }
            }
        } catch (SQLException e) {
            response.put("message", "Database error: " + e.getMessage());
        }
        return response;
    }

    private static String hashPassword(String password) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashedBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }

    public static JSONObject logout(String token) {
        JSONObject response = new JSONObject();
        response.put("success", false);

        String sql = "DELETE FROM user_sessions WHERE token = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, token);
            response.put("success", pstmt.executeUpdate() > 0);
        } catch (SQLException e) {
            response.put("message", "Logout error: " + e.getMessage());
        }
        return response;
    }

}
