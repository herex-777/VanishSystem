package me.herex.VanishSystem.commands;

import me.herex.VanishSystem.VanishSystem;
import me.herex.VanishSystem.managers.VanishManager;
import me.herex.VanishSystem.utils.ChatUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class VanishCommand implements CommandExecutor {

    private final VanishSystem plugin;
    private final VanishManager vanishManager;

    public VanishCommand(VanishSystem plugin) {
        this.plugin = plugin;
        this.vanishManager = plugin.getVanishManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cThis command can only be executed by a player.");
            return true;
        }

        Player player = (Player) sender;

        // Check permission
        if (!player.hasPermission("vanish.use") && !player.isOp()) {
            ChatUtils.sendMessage(player, plugin.getConfigManager().getMessage("no-permission"));
            return true;
        }

        // Check if world is enabled
        if (!vanishManager.isWorldEnabled(player.getWorld().getName())) {
            ChatUtils.sendMessage(player, "§cVanish is not enabled in this world!");
            return true;
        }

        // Toggle vanish
        vanishManager.toggleVanish(player);

        return true;
    }
}