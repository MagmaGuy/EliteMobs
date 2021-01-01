package com.magmaguy.elitemobs.powerstances;

import com.magmaguy.elitemobs.api.internal.RemovalReason;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import org.bukkit.entity.Item;

public class VisualItemRemover {

    public static void removeItems(Object[][] multiDimensionalTrailTracker) {
        for (int i = 0; i < multiDimensionalTrailTracker.length; i++)
            for (int j = 0; j < multiDimensionalTrailTracker[i].length; j++) {
                if (!(multiDimensionalTrailTracker[i][j] instanceof Item)) continue;
                Item item = (Item) multiDimensionalTrailTracker[i][j];
                item.remove();
                EntityTracker.unregister(item, RemovalReason.EFFECT_TIMEOUT);
            }
    }

}
