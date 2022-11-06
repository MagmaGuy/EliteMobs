package com.magmaguy.elitemobs.mobconstructor.custombosses;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.combatsystem.antiexploit.PreventMountExploit;
import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfig;
import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import com.magmaguy.elitemobs.utils.WarningMessage;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;

public class CustomBossMount {
    private CustomBossMount() {
    }

    public static CustomBossEntity generateMount(CustomBossEntity customBossEntity) {
        if (customBossEntity.customBossesConfigFields.getMountedEntity() == null) return null;
        try {
            EntityType entityType = EntityType.valueOf(customBossEntity.customBossesConfigFields.getMountedEntity());
            LivingEntity livingEntity = (LivingEntity) customBossEntity.getLivingEntity().getWorld()
                    .spawnEntity(customBossEntity.getLivingEntity().getLocation(), entityType);
            PreventMountExploit.bypass = true;
            livingEntity.addPassenger(customBossEntity.getLivingEntity());
            livingEntity.setRemoveWhenFarAway(false);
            customBossEntity.livingEntityMount = livingEntity;
        } catch (Exception ex) {
            //This runs when it's not an API entity
            CustomBossesConfigFields customBossesConfigFields = CustomBossesConfig.getCustomBoss(customBossEntity.customBossesConfigFields.getMountedEntity());
            if (customBossesConfigFields != null) {
                CustomBossEntity mountEntity = CustomBossEntity.createCustomBossEntity(customBossEntity.customBossesConfigFields.getMountedEntity());
                if (mountEntity == null) {
                    new WarningMessage("Mount for boss " + customBossEntity.getCustomBossesConfigFields().getFilename() + " is not valid!");
                    return null;
                }
                mountEntity.setSpawnLocation(customBossEntity.getLivingEntity().getLocation());
                mountEntity.setBypassesProtections(customBossEntity.getBypassesProtections());
                mountEntity.setPersistent(false);
                mountEntity.setMount(true);
                mountEntity.spawn(false);

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (!mountEntity.isValid())
                            return;
                        if (mountEntity.getCustomModel() != null)
                            mountEntity.getCustomModel().addPassenger(customBossEntity);
                        else {
                            PreventMountExploit.bypass = true;
                            mountEntity.getLivingEntity().addPassenger(customBossEntity.getLivingEntity());
                        }
                        customBossEntity.customBossMount = mountEntity;
                    }
                }.runTaskLater(MetadataHandler.PLUGIN, 5);
                return mountEntity;
            }

            new WarningMessage("Attempted to make Custom Boss " + customBossEntity.customBossesConfigFields.getFilename() + " mount invalid" +
                    " entity or boss " + customBossEntity.customBossesConfigFields.getMountedEntity() + " . Fix this in the configuration file.");
        }
        return null;
    }
}
