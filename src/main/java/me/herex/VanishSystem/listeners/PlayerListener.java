package me.herex.VanishSystem.listeners;

import me.herex.VanishSystem.VanishSystem;
import me.herex.VanishSystem.managers.VanishManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {

    private final VanishSystem plugin;
    private final VanishManager vanishManager;

    public PlayerListener(VanishSystem plugin) {
        this.plugin = plugin;
        this.vanishManager = plugin.getVanishManager();
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        vanishManager.handlePlayerJoin(player);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        vanishManager.handlePlayerQuit(player);
    }
}