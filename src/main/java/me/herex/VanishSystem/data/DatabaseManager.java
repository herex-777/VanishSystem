package me.herex.VanishSystem.data;

import me.herex.VanishSystem.VanishSystem;
import me.herex.VanishSystem.managers.ConfigManager;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DatabaseManager {

    private final VanishSystem plugin;
    private final ConfigManager configManager;
    private Database database;
    private Connection connection;

    public DatabaseManager(VanishSystem plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
    }

    public void setupDatabase() {
        String type = configManager.getDatabaseType();

        if (type.equalsIgnoreCase("mysql")) {
            database = new MySQLDatabase(
                    plugin,
                    configManager.getMySQLHost(),
                    configManager.getMySQLPort(),
                    configManager.getMySQLDatabase(),
                    configManager.getMySQLUsername(),
                    configManager.getMySQLPassword()
            );
        } else {
            // SQLite - fix the path
            String sqlitePath = configManager.getSQLitePath();
            // Ensure the directory exists
            File dbFile = new File(sqlitePath);
            if (!dbFile.getParentFile().exists()) {
                dbFile.getParentFile().mkdirs();
            }
            database = new SQLiteDatabase(plugin, sqlitePath);
        }

        database.setupDatabase();
    }

    public Database getDatabase() {
        return database;
    }

    public void saveVanishState(String uuid, boolean vanished) {
        if (database != null) {
            database.saveVanishState(java.util.UUID.fromString(uuid), vanished);
        }
    }

    public boolean isVanished(String uuid) {
        if (database != null) {
            return database.isVanished(java.util.UUID.fromString(uuid));
        }
        return false;
    }

    public void closeConnection() {
        if (database != null) {
            database.closeConnection();
        }
    }

    // Helper method to check database connection
    public boolean isConnected() {
        return database != null && database.getConnection() != null;
    }
}