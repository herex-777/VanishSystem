package me.herex.VanishSystem.data;

import java.sql.Connection;
import java.util.UUID;

public interface Database {
    Connection getConnection();
    void setupDatabase();
    void saveVanishState(UUID uuid, boolean vanished);
    boolean isVanished(UUID uuid);
    void closeConnection();
}