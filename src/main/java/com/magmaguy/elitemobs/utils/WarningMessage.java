package com.magmaguy.elitemobs.utils;

import com.magmaguy.elitemobs.MetadataHandler;
import org.bukkit.Bukkit;

public class WarningMessage {

    public WarningMessage(String message) {
        Bukkit.getLogger().warning("[EliteMobs] " + message);
    }

    public WarningMessage(String message, boolean stackTrace) {
        Bukkit.getLogger().warning("[EliteMobs] " + message);
        if (stackTrace) {
            Bukkit.getLogger().warning("[EliteMobs] Report the following to the developer at " + DiscordLinks.mainLink);
            Bukkit.getLogger().info("EliteMobs version: " + MetadataHandler.PLUGIN.getDescription().getVersion() + " | Server version: " + Bukkit.getServer().getVersion());
            for (StackTraceElement element : Thread.currentThread().getStackTrace())
                Bukkit.getLogger().info(element.toString());
        }
    }
}
