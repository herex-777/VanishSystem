package me.herex.VanishSystem.managers;

import me.herex.VanishSystem.VanishSystem;
import me.herex.VanishSystem.utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class VanishManager {

    private final VanishSystem plugin;
    private final ConfigManager configManager;
    private final Set<UUID> vanishedPlayers = new HashSet<>();
    private final Map<UUID, Integer> actionBarTasks = new HashMap<>();

    public VanishManager(VanishSystem plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
    }

    public void loadVanishedPlayers() {
        plugin.getLogger().info("Loading vanished players from database...");

        // Load from database and apply vanish to online players
        for (Player player : Bukkit.getOnlinePlayers()) {
            String uuidStr = player.getUniqueId().toString();
            boolean isVanished = plugin.getDatabaseManager().isVanished(uuidStr);

            if (isVanished) {
                vanishedPlayers.add(player.getUniqueId());
                applyVanish(player);
                startActionBar(player);
                plugin.getLogger().info("Player " + player.getName() + " is vanished (loaded from DB)");
            }
        }

        plugin.getLogger().info("Loaded " + vanishedPlayers.size() + " vanished players");
    }

    public void saveAllVanishedPlayers() {
        for (UUID uuid : vanishedPlayers) {
            plugin.getDatabaseManager().saveVanishState(uuid.toString(), true);
        }
    }

    public boolean toggleVanish(Player player) {
        UUID uuid = player.getUniqueId();

        if (vanishedPlayers.contains(uuid)) {
            // Unvanish
            vanishedPlayers.remove(uuid);
            removeVanish(player);
            stopActionBar(player);
            plugin.getDatabaseManager().saveVanishState(uuid.toString(), false);
            ChatUtils.sendMessage(player, configManager.getMessage("vanish-off"));
            plugin.getLogger().info(player.getName() + " is no longer vanished");
            return false;
        } else {
            // Vanish
            vanishedPlayers.add(uuid);
            applyVanish(player);
            startActionBar(player);
            plugin.getDatabaseManager().saveVanishState(uuid.toString(), true);
            ChatUtils.sendMessage(player, configManager.getMessage("vanish-on"));
            plugin.getLogger().info(player.getName() + " is now vanished");
            return true;
        }
    }

    public boolean isVanished(Player player) {
        return vanishedPlayers.contains(player.getUniqueId());
    }

    public boolean canSeeVanished(Player player) {
        return player.hasPermission("vanish.see") || player.isOp();
    }

    public boolean isWorldEnabled(String worldName) {
        List<String> enabledWorlds = configManager.getEnabledWorlds();

        if (enabledWorlds == null || enabledWorlds.isEmpty()) {
            return true; // Default to all worlds
        }

        if (enabledWorlds.contains("*")) {
            return true;
        }

        return enabledWorlds.contains(worldName);
    }

    private void applyVanish(Player player) {
        plugin.getLogger().info("Applying vanish to " + player.getName());

        // Hide from all players who can't see vanished
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (!online.equals(player) && !canSeeVanished(online)) {
                online.hidePlayer(player);
                plugin.getLogger().info("Hidden " + player.getName() + " from " + online.getName());
            }
        }
    }

    private void removeVanish(Player player) {
        plugin.getLogger().info("Removing vanish from " + player.getName());

        // Show to all players
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (!online.canSee(player)) {
                online.showPlayer(player);
                plugin.getLogger().info("Showed " + player.getName() + " to " + online.getName());
            }
        }
    }

    private void startActionBar(Player player) {
        if (!isWorldEnabled(player.getWorld().getName())) {
            return;
        }

        stopActionBar(player);

        int taskId = new BukkitRunnable() {
            @Override
            public void run() {
                if (player.isOnline() && isVanished(player)) {
                    ChatUtils.sendActionBar(player, configManager.getMessage("action-bar"));
                } else {
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 20L).getTaskId();

        actionBarTasks.put(player.getUniqueId(), taskId);
    }

    private void stopActionBar(Player player) {
        UUID uuid = player.getUniqueId();
        if (actionBarTasks.containsKey(uuid)) {
            Bukkit.getScheduler().cancelTask(actionBarTasks.get(uuid));
            actionBarTasks.remove(uuid);
        }
    }

    public void handlePlayerJoin(Player player) {
        plugin.getLogger().info("Handling join for " + player.getName());

        // Check if player was vanished before
        String uuidStr = player.getUniqueId().toString();
        boolean wasVanished = plugin.getDatabaseManager().isVanished(uuidStr);

        if (wasVanished) {
            vanishedPlayers.add(player.getUniqueId());
            applyVanish(player);
            startActionBar(player);
            ChatUtils.sendMessage(player, configManager.getMessage("vanish-restored"));
            plugin.getLogger().info("Restored vanish for " + player.getName());
        }

        // Hide vanished players from this player if they can't see them
        for (UUID vanishedUUID : vanishedPlayers) {
            Player vanished = Bukkit.getPlayer(vanishedUUID);
            if (vanished != null && !canSeeVanished(player) && !player.equals(vanished)) {
                player.hidePlayer(vanished);
                plugin.getLogger().info("Hidden vanished player " + vanished.getName() + " from " + player.getName());
            }
        }
    }

    public void handlePlayerQuit(Player player) {
        stopActionBar(player);
        plugin.getLogger().info("Handled quit for " + player.getName());
    }
}