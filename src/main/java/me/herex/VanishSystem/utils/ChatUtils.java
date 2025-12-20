package me.herex.VanishSystem.utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class ChatUtils {

    public static String color(String message) {
        if (message == null) return "";
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public static void sendMessage(Player player, String message) {
        if (message != null && !message.isEmpty()) {
            player.sendMessage(color(message));
        }
    }

    public static void sendActionBar(Player player, String message) {
        try {
            // For 1.8.8, we need to use reflection
            Object packet = createActionBarPacket(color(message));
            sendPacket(player, packet);
        } catch (Exception e) {
            // Fallback to chat message
            sendMessage(player, message);
        }
    }

    private static Object createActionBarPacket(String message) throws Exception {
        try {
            // Get NMS classes
            Class<?> packetClass = getNMSClass("PacketPlayOutChat");
            Class<?> componentClass = getNMSClass("IChatBaseComponent");
            Class<?> serializerClass = getNMSClass("IChatBaseComponent$ChatSerializer");

            // Create IChatBaseComponent
            Method serializerMethod = serializerClass.getMethod("a", String.class);
            Object component = serializerMethod.invoke(null, "{\"text\":\"" + message + "\"}");

            // Create packet with action bar type (2)
            Constructor<?> constructor = packetClass.getConstructor(componentClass, byte.class);
            return constructor.newInstance(component, (byte) 2);
        } catch (Exception e) {
            throw new Exception("Failed to create action bar packet", e);
        }
    }

    private static void sendPacket(Player player, Object packet) throws Exception {
        try {
            Object handle = player.getClass().getMethod("getHandle").invoke(player);
            Object playerConnection = handle.getClass().getField("playerConnection").get(handle);
            Method sendPacketMethod = playerConnection.getClass().getMethod("sendPacket", getNMSClass("Packet"));
            sendPacketMethod.invoke(playerConnection, packet);
        } catch (Exception e) {
            throw new Exception("Failed to send packet", e);
        }
    }

    private static Class<?> getNMSClass(String name) throws Exception {
        String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        return Class.forName("net.minecraft.server." + version + "." + name);
    }

    // Helper method to get Bukkit class
    private static Class<?> getCraftBukkitClass(String name) throws Exception {
        String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        return Class.forName("org.bukkit.craftbukkit." + version + "." + name);
    }
}