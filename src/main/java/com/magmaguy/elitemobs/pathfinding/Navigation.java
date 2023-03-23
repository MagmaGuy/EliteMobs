package com.magmaguy.elitemobs.pathfinding;

import com.magmaguy.easyminecraftpathfinding.NMSManager;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.mobconstructor.custombosses.RegionalBossEntity;
import org.bukkit.Location;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class Navigation {
    public static void navigate(Mob mob, Location destination, int speed) {
        NMSManager.getAdapter().move(mob, speed, destination);
    }

    public static BukkitTask backToSpawn(RegionalBossEntity regionalBossEntity) {
        return getThere((Mob) regionalBossEntity.getLivingEntity(), regionalBossEntity.getSpawnLocation().clone(), false);
    }

    private static BukkitTask getThere(Mob livingEntity, Location location, boolean force) {
        if (livingEntity == null || location == null) return null;
        return new BukkitRunnable() {
            @Override
            public void run() {
                if (!livingEntity.isValid() ||
                        !force && livingEntity.getTarget() instanceof Player ||
                        livingEntity.getLocation().distanceSquared(location) < Math.pow(2, 2)) {
                    cancel();
                    return;
                }
                NMSManager.getAdapter().move(livingEntity, 1, location);
            }
        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 1);
    }
}
