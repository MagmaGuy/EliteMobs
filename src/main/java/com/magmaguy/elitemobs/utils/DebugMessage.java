package com.magmaguy.elitemobs.utils;

import org.bukkit.Bukkit;

public class DebugMessage {
    public static boolean debugMode = false;

    public DebugMessage(Object message) {
        if (debugMode)
            Bukkit.getLogger().warning("[EliteMobs] Debug message: " + message);
    }
}
