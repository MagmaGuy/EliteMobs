package com.magmaguy.elitemobs.events.mobs.sharedeventproperties;

import com.magmaguy.elitemobs.mobspawning.MobLevelCalculator;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class ActionDynamicBossLevelConstructor {

    public static int determineDynamicBossLevel(Location bossLocation) {

        int bossLevel = 1;

        for (Entity entity : bossLocation.getWorld().getNearbyEntities(bossLocation, 50, 50, 50))
            if (entity instanceof Player)
                if (MobLevelCalculator.determineMobLevel((Player) entity) > bossLevel)
                    bossLevel = MobLevelCalculator.determineMobLevel((Player) entity);

        return bossLevel;

    }

}
