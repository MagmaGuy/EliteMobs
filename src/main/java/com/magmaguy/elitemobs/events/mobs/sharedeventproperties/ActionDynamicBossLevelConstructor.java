package com.magmaguy.elitemobs.events.mobs.sharedeventproperties;

import com.magmaguy.elitemobs.playerdata.ElitePlayerInventory;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class ActionDynamicBossLevelConstructor {

    public static int determineDynamicBossLevel(Location bossLocation) {
        int bossLevel = 1;
        for (Entity entity : bossLocation.getWorld().getNearbyEntities(bossLocation, Bukkit.getViewDistance() * 16, 256, Bukkit.getViewDistance() * 16))
            if (entity instanceof Player)
                if (ElitePlayerInventory.playerInventories.get(entity.getUniqueId()) != null)
                    if (ElitePlayerInventory.playerInventories.get(entity.getUniqueId()).getNaturalMobSpawnLevel(true) > bossLevel)
                        bossLevel = ElitePlayerInventory.playerInventories.get(entity.getUniqueId()).getNaturalMobSpawnLevel(false);
        return bossLevel;
    }

}
