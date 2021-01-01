package com.magmaguy.elitemobs.mobconstructor.custombosses;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.combatsystem.antiexploit.PreventMountExploit;
import com.magmaguy.elitemobs.config.custombosses.CustomBossConfigFields;
import com.magmaguy.elitemobs.utils.WarningMessage;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;

public class CustomBossMount {
    public static CustomBossEntity generateMount(CustomBossEntity customBossEntity) {
        if (customBossEntity.customBossConfigFields.getMountedEntity() == null) return null;
        try {
            EntityType entityType = EntityType.valueOf(customBossEntity.customBossConfigFields.getMountedEntity());
            LivingEntity livingEntity = (LivingEntity) customBossEntity.getLivingEntity().getWorld()
                    .spawnEntity(customBossEntity.getLivingEntity().getLocation(), entityType);
            PreventMountExploit.bypass = true;
            livingEntity.addPassenger(customBossEntity.getLivingEntity());

        } catch (Exception ex) {
            //This runs when it's not an API entity
            for (CustomBossConfigFields iteratedField : CustomBossConfigFields.customBossConfigFields) {
                if (iteratedField.getFileName().equalsIgnoreCase(customBossEntity.customBossConfigFields.getMountedEntity())) {
                    CustomBossEntity mount = CustomBossEntity.constructCustomBoss(customBossEntity.customBossConfigFields.getMountedEntity(),
                            customBossEntity.getLivingEntity().getLocation(), customBossEntity.getLevel());
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            if (mount == null || mount.getLivingEntity() == null)
                                return;
                            PreventMountExploit.bypass = true;
                            mount.getLivingEntity().addPassenger(customBossEntity.getLivingEntity());
                        }
                    }.runTaskLater(MetadataHandler.PLUGIN, 2);
                    return mount;
                }
            }
            new WarningMessage("Attempted to make Custom Boss " + customBossEntity.customBossConfigFields.getFileName() + " mount invalid" +
                    " entity or boss " + customBossEntity.customBossConfigFields.getMountedEntity() + " . Fix this in the configuration file.");
        }
        return null;
    }
}
