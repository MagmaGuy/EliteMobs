package com.magmaguy.elitemobs.utils;

import com.magmaguy.elitemobs.MetadataHandler;
import lombok.Getter;
import org.bukkit.Bukkit;

public class ServerTime {
    @Getter
    private static double serverTime = 0;

    public static void startTickCounter() {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(MetadataHandler.PLUGIN, () -> serverTime++, 0, 1);
    }
}
