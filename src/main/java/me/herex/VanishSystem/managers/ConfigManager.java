package me.herex.VanishSystem.managers;

import me.herex.VanishSystem.VanishSystem;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

public class ConfigManager {

    private final VanishSystem plugin;
    private FileConfiguration config;

    public ConfigManager(VanishSystem plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
    }

    public List<String> getEnabledWorlds() {
        return config.getStringList("enabled-worlds");
    }

    public String getDatabaseType() {
        return config.getString("database.type", "sqlite");
    }

    public String getSQLitePath() {
        return config.getString("database.sqlite.path", "plugins/VanishSystem/vanish.db");
    }

    public String getMySQLHost() {
        return config.getString("database.mysql.host", "localhost");
    }

    public int getMySQLPort() {
        return config.getInt("database.mysql.port", 3306);
    }

    public String getMySQLDatabase() {
        return config.getString("database.mysql.database", "vanish");
    }

    public String getMySQLUsername() {
        return config.getString("database.mysql.username", "root");
    }

    public String getMySQLPassword() {
        return config.getString("database.mysql.password", "");
    }

    public String getMessage(String path) {
        return config.getString("messages." + path, "");
    }

    public void reloadConfig() {
        plugin.reloadConfig();
        config = plugin.getConfig();
    }
}