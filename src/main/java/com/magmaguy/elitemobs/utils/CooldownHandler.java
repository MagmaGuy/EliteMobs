package com.magmaguy.elitemobs.utils;

import com.magmaguy.elitemobs.MetadataHandler;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collection;

public class CooldownHandler {
    private CooldownHandler() {
    }

    @SuppressWarnings("unchecked")
    public static <T> void initialize(Collection<T> collection, T object, int cooldownInTicks) {
        collection.add(object);

        new BukkitRunnable() {
            @Override
            public void run() {
                collection.remove(object);
            }
        }.runTaskLater(MetadataHandler.PLUGIN, cooldownInTicks);

    }

}
