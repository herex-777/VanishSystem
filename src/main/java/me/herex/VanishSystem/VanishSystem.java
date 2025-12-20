package me.herex.VanishSystem;

import me.herex.VanishSystem.commands.VanishCommand;
import me.herex.VanishSystem.data.DatabaseManager;
import me.herex.VanishSystem.listeners.PlayerListener;
import me.herex.VanishSystem.managers.ConfigManager;
import me.herex.VanishSystem.managers.VanishManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class VanishSystem extends JavaPlugin {

    private static VanishSystem instance;
    private ConfigManager configManager;
    private VanishManager vanishManager;
    private DatabaseManager databaseManager;

    @Override
    public void onEnable() {
        instance = this;

        // Save default config if it doesn't exist
        saveDefaultConfig();
        reloadConfig();

        // Initialize managers
        configManager = new ConfigManager(this);
        databaseManager = new DatabaseManager(this);
        vanishManager = new VanishManager(this);

        // Setup database
        databaseManager.setupDatabase();

        // Register command
        getCommand("vanish").setExecutor(new VanishCommand(this));

        // Register listeners
        Bukkit.getPluginManager().registerEvents(new PlayerListener(this), this);

        // Load vanished players from database
        getServer().getScheduler().runTaskLater(this, () -> {
            vanishManager.loadVanishedPlayers();
        }, 20L); // 1 second delay

        getLogger().info("VanishSystem has been enabled!");
    }

    @Override
    public void onDisable() {
        // Save all vanished players to database
        if (vanishManager != null) {
            vanishManager.saveAllVanishedPlayers();
        }

        // Close database connection
        if (databaseManager != null) {
            databaseManager.closeConnection();
        }

        getLogger().info("VanishSystem has been disabled!");
    }

    public static VanishSystem getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public VanishManager getVanishManager() {
        return vanishManager;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }
}