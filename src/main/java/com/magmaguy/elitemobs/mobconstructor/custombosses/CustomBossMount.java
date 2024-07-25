package com.magmaguy.elitemobs.mobconstructor.custombosses;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.combatsystem.antiexploit.PreventMountExploit;
import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfig;
import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import com.magmaguy.magmacore.util.Logger;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;

public class CustomBossMount {
    private CustomBossMount() {
    }

    //todo: Now that the boss megaconsumer exists, it's possible that delaying the mount by 5 ticks is no longer necessary and that we can just put it in the consumer... maybe
    public static CustomBossEntity generateMount(CustomBossEntity customBossEntity) {
        if (customBossEntity.customBossesConfigFields.getMountedEntity() == null) return null;
        if (customBossEntity.getLivingEntity() == null) {
            Logger.warn("Could not spawn mount for boss " + customBossEntity.customBossesConfigFields.getFilename() + " because the boss has no living entity! This probably means some other plugin is preventing this boss from spawning.");
            return null;
        }
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
                    Logger.warn("Mount for boss " + customBossEntity.getCustomBossesConfigFields().getFilename() + " is not valid!");
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
                        if (!mountEntity.isValid()) return;
                        if (customBossEntity.getLivingEntity() == null) return;
                        if (mountEntity.getCustomModel() != null)
                            mountEntity.getCustomModel().addPassenger(customBossEntity);
                        else {
                            PreventMountExploit.bypass = true;
                            if (mountEntity.getLivingEntity() != null)
                                mountEntity.getLivingEntity().addPassenger(customBossEntity.getLivingEntity());
                        }
                        customBossEntity.customBossMount = mountEntity;
                    }
                }.runTaskLater(MetadataHandler.PLUGIN, 5);
                return mountEntity;
            }

            Logger.warn("Attempted to make Custom Boss " + customBossEntity.customBossesConfigFields.getFilename() + " mount invalid" +
                    " entity or boss " + customBossEntity.customBossesConfigFields.getMountedEntity() + " . Fix this in the configuration file.");
        }
        return null;
    }
}
