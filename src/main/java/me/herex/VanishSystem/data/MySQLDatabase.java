package me.herex.VanishSystem.data;

import me.herex.VanishSystem.VanishSystem;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class MySQLDatabase implements Database {

    private final VanishSystem plugin;
    private Connection connection;
    private final String host;
    private final int port;
    private final String database;
    private final String username;
    private final String password;

    public MySQLDatabase(VanishSystem plugin, String host, int port, String database, String username, String password) {
        this.plugin = plugin;
        this.host = host;
        this.port = port;
        this.database = database;
        this.username = username;
        this.password = password;
    }

    @Override
    public Connection getConnection() {
        return connection;
    }

    @Override
    public void setupDatabase() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            String url = "jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false";
            connection = DriverManager.getConnection(url, username, password);

            String createTable = "CREATE TABLE IF NOT EXISTS vanish_states (" +
                    "uuid VARCHAR(36) PRIMARY KEY," +
                    "vanished BOOLEAN NOT NULL," +
                    "last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";

            try (PreparedStatement stmt = connection.prepareStatement(createTable)) {
                stmt.execute();
            }

            plugin.getLogger().info("MySQL database connected!");
        } catch (ClassNotFoundException | SQLException e) {
            plugin.getLogger().severe("Failed to connect to MySQL database: " + e.getMessage());
        }
    }

    @Override
    public void saveVanishState(UUID uuid, boolean vanished) {
        String sql = "INSERT INTO vanish_states (uuid, vanished) VALUES (?, ?) " +
                "ON DUPLICATE KEY UPDATE vanished = ?, last_updated = CURRENT_TIMESTAMP";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, uuid.toString());
            stmt.setBoolean(2, vanished);
            stmt.setBoolean(3, vanished);
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to save vanish state: " + e.getMessage());
        }
    }

    @Override
    public boolean isVanished(UUID uuid) {
        String sql = "SELECT vanished FROM vanish_states WHERE uuid = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, uuid.toString());
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getBoolean("vanished");
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to check vanish state: " + e.getMessage());
        }

        return false;
    }

    @Override
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to close database connection: " + e.getMessage());
        }
    }
}