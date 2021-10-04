package com.magmaguy.elitemobs.utils;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

public class DebugMessage {
    private static boolean debugMode = false;

    public DebugMessage(Object message) {
        if (debugMode) {
            Bukkit.getLogger().warning("[EliteMobs] Debug message: " + message);
            for (StackTraceElement element : Thread.currentThread().getStackTrace())
                Bukkit.getLogger().info(element.toString());
        }
    }

    public static void toggleDebugMode(CommandSender commandSender) {
        debugMode = !debugMode;
        if (debugMode)
            commandSender.sendMessage("[EliteMobs] Debug mode on!");
        else
            commandSender.sendMessage("[EliteMobs] Debug mode off!");
    }

    public static boolean isDebugMode() {
        return debugMode;
    }
}
