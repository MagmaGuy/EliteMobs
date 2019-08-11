package com.magmaguy.elitemobs.utils;

import com.magmaguy.elitemobs.EntityTracker;
import com.magmaguy.elitemobs.MetadataHandler;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class DialogArmorStand {

    public static ArmorStand createDialogArmorStand(Location location, String dialog) {

        ArmorStand armorStand = (ArmorStand) location.getWorld().spawnEntity(location.add(new Vector(0, -50, 0)), EntityType.ARMOR_STAND);

        armorStand.setVisible(false);
        armorStand.setMarker(true);
        armorStand.setCustomName(dialog);
        armorStand.setGravity(false);
        EntityTracker.registerArmorStands(armorStand);
        armorStand.setCustomNameVisible(false);


        //This part is necessary because armorstands are visible on their first tick to players
        new BukkitRunnable() {

            int taskTimer = 0;

            @Override
            public void run() {

                if (taskTimer == 0) {
                    armorStand.teleport(new Location(armorStand.getWorld(), armorStand.getLocation().getX(),
                            armorStand.getLocation().getY() + 50, armorStand.getLocation().getZ()));
                } else
                    armorStand.teleport(new Location(armorStand.getWorld(), armorStand.getLocation().getX(),
                            armorStand.getLocation().getY() + 0.05, armorStand.getLocation().getZ()));

                if (taskTimer == 1)
                    armorStand.setCustomNameVisible(true);

                taskTimer++;

                if (taskTimer > 15) {

                    EntityTracker.unregisterArmorStand(armorStand);
                    cancel();

                }

            }

        }.runTaskTimer(Bukkit.getPluginManager().getPlugin(MetadataHandler.ELITE_MOBS), 0, 2);

        return armorStand;
    }

}
