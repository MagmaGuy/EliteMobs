package com.magmaguy.elitemobs.utils;

import com.magmaguy.elitemobs.MetadataHandler;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public class CooldownHandler {
    private CooldownHandler() {
    }

    public static void initialize(List list, Object object, int cooldownInTicks) {
        list.add(object);

        new BukkitRunnable() {
            @Override
            public void run() {
                list.remove(object);
            }
        }.runTaskLater(MetadataHandler.PLUGIN, cooldownInTicks);

    }

}
