package com.magmaguy.elitemobs.utils;

import com.magmaguy.easyminecraftgoals.internal.FakeText;
import com.magmaguy.elitemobs.MetadataHandler;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class DialogArmorStand {

    public static FakeText createDialogArmorStand(Entity sourceEntity, String dialog, Vector offset) {

        offset.add(getDisplacementVector(sourceEntity).subtract(new Vector(0, 1, 0)));
        Vector finalOffset = offset;
        FakeText fakeText = VisualDisplay.generateFakeText(sourceEntity.getLocation().clone().add(finalOffset), dialog, 30);
        if (fakeText == null) return null;

        new BukkitRunnable() {
            int taskTimer = 0;

            @Override
            public void run() {
                taskTimer++;

                if (taskTimer > 15 || !sourceEntity.isValid()) {
                    fakeText.remove();
                    cancel();
                    return;
                }
                Location newLoc = sourceEntity.getLocation().clone().add(finalOffset).add(new Vector(0, taskTimer * 0.05, 0));
                fakeText.teleport(newLoc);
            }

        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 2);

        return fakeText;
    }

    private static Vector getDisplacementVector(Entity sourceEntity) {
        double height = 2.3;
        if (sourceEntity instanceof LivingEntity)
            height = ((LivingEntity) sourceEntity).getEyeHeight();
        return new Vector(0, height, 0);
    }

    public static FakeText createDialogArmorStand(LivingEntity sourceEntity, String dialog) {

        if (sourceEntity == null) return null;

        FakeText fakeText = VisualDisplay.generateFakeText(sourceEntity.getLocation().clone().add(getDisplacementVector(sourceEntity)), dialog, 30);
        if (fakeText == null) return null;

        new BukkitRunnable() {
            int taskTimer = 0;

            @Override
            public void run() {
                if (taskTimer > 15 || !sourceEntity.isValid()) {
                    fakeText.remove();
                    cancel();
                    return;
                }
                fakeText.teleport(sourceEntity.getLocation().clone().add(getDisplacementVector(sourceEntity)));
                taskTimer++;
            }
        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 1);
        return fakeText;
    }

}
