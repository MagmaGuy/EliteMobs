package com.magmaguy.elitemobs.events.mobs.sharedeventproperties;

import com.magmaguy.elitemobs.mobspawning.MobLevelCalculator;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class DynamicBossLevel {

    public static int determineDynamicBossLevel(LivingEntity boss) {

        int bossLevel = 1;

        for (Entity entity : boss.getNearbyEntities(50, 50, 50)) {

            if (entity instanceof Player) {

                bossLevel += MobLevelCalculator.determineMobLevel((Player) entity);

            }

        }

        return bossLevel;

    }

}
