package com.magmaguy.elitemobs.powerstances;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.internal.RemovalReason;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import org.bukkit.entity.Item;
import org.bukkit.scheduler.BukkitRunnable;

public class VisualItemRemover {

    private VisualItemRemover() {
    }

    public static void removeItems(Object[][] multiDimensionalTrailTracker) {
        for (Object[] objects : multiDimensionalTrailTracker)
            for (Object object : objects) {
                if (!(object instanceof Item)) continue;
                Item item = (Item) object;
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        item.remove();
                        EntityTracker.unregister(item, RemovalReason.EFFECT_TIMEOUT);
                    }
                }.runTask(MetadataHandler.PLUGIN);
            }
    }

}
