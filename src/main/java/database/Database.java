package database;

import models.User;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
public class Database {
    private final String URL = "jdbc:mysql://localhost:3306/DeliveryService";
    private final String USER = "root";
    private final String PASSWORD = "";
    public Connection connection;

    // Konstruktor som ansluter till databasen
    public Database() {
        try {
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Database connected successfully via XAMPP!");
        } catch (SQLException e) {
            System.out.println("Error connecting to database: " + e.getMessage());
        }
    }

    // Metod för att hasha lösenordet
    public static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashedBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }

    // Lägg till användare i databasen
    public void addUser(User user) {
        String sql = "INSERT INTO Users (username, email, password) VALUES (?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, user.getUsername());
            statement.setString(2, user.getEmail());
            statement.setString(3, hashPassword(user.getPassword())); // Hasha lösenordet
            statement.executeUpdate();
            System.out.println("User registered successfully!");
        } catch (SQLException e) {
            System.out.println("Error adding user: " + e.getMessage());
        }
    }

    // Hämta en användare baserat på användarnamnet
    public User getUser(String username) {
        String sql = "SELECT * FROM Users WHERE username = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return new User(
                        resultSet.getString("username"),
                        resultSet.getString("email"),
                        resultSet.getString("password")); // Hämta hashat lösenord
            }
        } catch (SQLException e) {
            System.out.println("Error fetching user: " + e.getMessage());
        }
        return null;
    }

    // Ta bort en användare baserat på användarnamnet och lösenordet
    public void deleteUser(String username, String password) {
        String sql = "SELECT * FROM Users WHERE username = ? AND password = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            statement.setString(2, hashPassword(password)); // Hasha lösenordet för verifiering
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                // Om användaren finns med rätt lösenord, ta bort användaren
                String deleteSql = "DELETE FROM Users WHERE username = ?";
                try (PreparedStatement deleteStatement = connection.prepareStatement(deleteSql)) {
                    deleteStatement.setString(1, username);
                    deleteStatement.executeUpdate();
                    System.out.println("User deleted successfully!");
                }
            } else {
                System.out.println("User not found or password incorrect!");
            }
        } catch (SQLException e) {
            System.out.println("Error deleting user: " + e.getMessage());
        }
    }

    // Visa alla användare i databasen
    public void displayUsers() {
        String sql = "SELECT * FROM Users";
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            if (!resultSet.isBeforeFirst()) {
                System.out.println("No users in the database.");
                return;
            }

            while (resultSet.next()) {
                System.out.println("Username: " + resultSet.getString("username"));
                System.out.println("Email: " + resultSet.getString("email"));
                System.out.println("--------------------------------------------------");
            }
        } catch (SQLException e) {
            System.out.println("Error displaying users: " + e.getMessage());
        }
    }

    // Stäng anslutningen till databasen
    public void closeConnection() {
        try {
            if (connection != null) {
                connection.close();
                System.out.println("Database connection closed.");
            }
        } catch (SQLException e) {
            System.out.println("Error closing the database connection: " + e.getMessage());
        }
    }
}