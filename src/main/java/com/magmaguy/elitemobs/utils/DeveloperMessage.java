package com.magmaguy.elitemobs.utils;

import org.bukkit.Bukkit;

public class DeveloperMessage {
    public DeveloperMessage(String message) {
        Bukkit.getLogger().warning("[EliteMobs] Developer message: " + message);
    }
}
