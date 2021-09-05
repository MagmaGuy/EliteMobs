package com.magmaguy.elitemobs.utils;

import org.bukkit.Bukkit;

public class DebugMessage {
    private static boolean debugMode = false;

    public DebugMessage(Object message) {
        if (debugMode) {
            Bukkit.getLogger().warning("[EliteMobs] Debug message: " + message);
            for (StackTraceElement element : Thread.currentThread().getStackTrace())
                Bukkit.getLogger().info(element.toString());
        }
    }

    public static void toggleDebugMode() {
        debugMode = !debugMode;
    }

    public static boolean isDebugMode() {
        return debugMode;
    }
}
