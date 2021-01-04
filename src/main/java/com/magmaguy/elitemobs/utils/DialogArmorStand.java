package com.magmaguy.elitemobs.utils;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.internal.RemovalReason;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class DialogArmorStand {

    public static ArmorStand createDialogArmorStand(Entity sourceEntity, String dialog, Vector offset) {

        ArmorStand armorStand = VisualArmorStand.VisualArmorStand(sourceEntity.getLocation().clone().add(new Vector(0, -50, 0)), dialog);

        //This part is necessary because armorstands are visible on their first tick to players
        new BukkitRunnable() {

            int taskTimer = 0;
            Location tickLocation = sourceEntity.getLocation().clone();

            @Override
            public void run() {

                if (sourceEntity.isValid())
                    tickLocation = sourceEntity.getLocation().clone();

                if (taskTimer == 0) {
                    armorStand.teleport(tickLocation.clone().add(offset).add(new Vector(0, 1, 0)));
                } else
                    armorStand.teleport(tickLocation.clone().add(offset).add(new Vector(0, taskTimer * 0.05, 0)));

                if (taskTimer == 1)
                    armorStand.setCustomNameVisible(true);

                taskTimer++;

                if (taskTimer > 15) {
                    EntityTracker.unregister(armorStand, RemovalReason.EFFECT_TIMEOUT);
                    cancel();
                }

            }

        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 2);

        return armorStand;
    }

    public static ArmorStand createDialogArmorStand(LivingEntity sourceEntity, String dialog) {
        ArmorStand armorStand = VisualArmorStand.VisualArmorStand(sourceEntity.getLocation().clone().add(new Vector(0, -50, 0)), dialog);
        //This part is necessary because armorstands are visible on their first tick to players
        new BukkitRunnable() {
            int taskTimer = 0;
            Location tickLocation = sourceEntity.getEyeLocation().clone();

            @Override
            public void run() {
                if (sourceEntity.isValid())
                    tickLocation = sourceEntity.getLocation().clone();
                if (taskTimer > 0)
                    armorStand.teleport(tickLocation.clone().add(new Vector(0, 2.3, 0)));
                if (taskTimer == 1)
                    armorStand.setCustomNameVisible(true);
                if (taskTimer > 15) {
                    EntityTracker.unregister(armorStand, RemovalReason.EFFECT_TIMEOUT);
                    cancel();
                }
                taskTimer++;
            }
        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 1);
        return armorStand;
    }

}
