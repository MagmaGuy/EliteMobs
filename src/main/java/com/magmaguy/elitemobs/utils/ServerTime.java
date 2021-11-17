package com.magmaguy.elitemobs.utils;

import com.magmaguy.elitemobs.MetadataHandler;
import lombok.Getter;
import org.bukkit.Bukkit;

public class ServerTime {
    @Getter
    private static double time = 0;

    private ServerTime() {
    }

    public static void startTickCounter() {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(MetadataHandler.PLUGIN, () -> time++, 0, 1);
    }
}
