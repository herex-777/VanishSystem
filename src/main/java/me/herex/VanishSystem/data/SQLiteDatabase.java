package me.herex.VanishSystem.data;

import me.herex.VanishSystem.VanishSystem;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class SQLiteDatabase implements Database {

    private final VanishSystem plugin;
    private Connection connection;
    private final String path;

    public SQLiteDatabase(VanishSystem plugin, String path) {
        this.plugin = plugin;
        this.path = path;
    }

    @Override
    public Connection getConnection() {
        return connection;
    }

    @Override
    public void setupDatabase() {
        try {
            // Ensure the directory exists
            File dbFile = new File(path);
            File parentDir = dbFile.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
                plugin.getLogger().info("Created directory: " + parentDir.getAbsolutePath());
            }

            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + path);

            String createTable = "CREATE TABLE IF NOT EXISTS vanish_states (" +
                    "uuid VARCHAR(36) PRIMARY KEY," +
                    "vanished BOOLEAN NOT NULL," +
                    "last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";

            try (PreparedStatement stmt = connection.prepareStatement(createTable)) {
                stmt.execute();
            }

            plugin.getLogger().info("SQLite database connected at: " + path);
        } catch (ClassNotFoundException e) {
            plugin.getLogger().severe("SQLite JDBC driver not found!");
            e.printStackTrace();
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to connect to SQLite database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void saveVanishState(UUID uuid, boolean vanished) {
        if (connection == null) {
            plugin.getLogger().warning("Database connection is null!");
            return;
        }

        String sql = "INSERT OR REPLACE INTO vanish_states (uuid, vanished) VALUES (?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, uuid.toString());
            stmt.setBoolean(2, vanished);
            stmt.executeUpdate();
            plugin.getLogger().info("Saved vanish state for " + uuid + ": " + vanished);
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to save vanish state: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public boolean isVanished(UUID uuid) {
        if (connection == null) {
            plugin.getLogger().warning("Database connection is null!");
            return false;
        }

        String sql = "SELECT vanished FROM vanish_states WHERE uuid = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, uuid.toString());
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                boolean vanished = rs.getBoolean("vanished");
                plugin.getLogger().info("Loaded vanish state for " + uuid + ": " + vanished);
                return vanished;
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to check vanish state: " + e.getMessage());
            e.printStackTrace();
        }

        plugin.getLogger().info("No vanish state found for " + uuid);
        return false;
    }

    @Override
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                plugin.getLogger().info("Database connection closed");
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to close database connection: " + e.getMessage());
            e.printStackTrace();
        }
    }
}