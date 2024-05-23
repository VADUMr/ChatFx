package com.example.chatfx;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Database {
    private static final String DB_URL = "jdbc:mysql://localhost/testt";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";

    private static Connection conn;

    static {
        try {
            conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            System.out.println("З'єднання з базою даних встановлено");
        } catch (SQLException e) {
            System.err.println("Не вдалося встановити з'єднання з базою даних");
            e.printStackTrace();
        }
    }

    public static void logMessage(String nickname, String message, String ipAddress) {
        Thread dataThread = new Thread(() -> {
            try {
                String sql = "INSERT INTO messages (nickname, message, ip_address) VALUES (?, ?, ?)";
                PreparedStatement statement = conn.prepareStatement(sql);
                statement.setString(1, nickname);
                statement.setString(2, message);
                statement.setString(3, ipAddress);
                int rowsInserted = statement.executeUpdate();
                System.out.println(rowsInserted + " рядків вставлено");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
        dataThread.start();
    }
    public static List<Message> getLast50Messages() {
        List<Message> messages = new ArrayList<>();
        try {
            String sql = "SELECT id, nickname, message, ip_address FROM messages ORDER BY id DESC LIMIT 50";
            Statement statement = conn.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String nickname = resultSet.getString("nickname");
                String message = resultSet.getString("message");
                String ipAddress = resultSet.getString("ip_address");
                Message msg = new Message(id, nickname, message, ipAddress);
                messages.add(0, msg);
            }

            resultSet.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return messages;
    }
    public static boolean deleteMessageById(int messageId) {
        boolean deleted = false;
        try {
            String sql = "DELETE FROM messages WHERE id = ?";
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setInt(1, messageId);
            int rowsDeleted = statement.executeUpdate();
            deleted = rowsDeleted > 0;
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return deleted;
    }
    public static List<Message> searchMessages(String keyword) {
        List<Message> searchResults = new ArrayList<>();
        try {
            String sql = "SELECT id, nickname, message, ip_address FROM messages WHERE message LIKE ?";
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setString(1, "%" + keyword + "%");
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String nickname = resultSet.getString("nickname");
                String message = resultSet.getString("message");
                String ipAddress = resultSet.getString("ip_address");
                Message msg = new Message(id, nickname, message, ipAddress);
                searchResults.add(msg);
            }
            resultSet.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return searchResults;
    }

    public static void main(String[] args) {
        while (true) {
        }
    }
}