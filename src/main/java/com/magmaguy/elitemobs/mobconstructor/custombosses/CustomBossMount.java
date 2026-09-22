package com.magmaguy.elitemobs.mobconstructor.custombosses;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.internal.RemovalReason;
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

    public static CustomBossEntity generateMount(CustomBossEntity rider) {
        String definition = rider.getCustomBossesConfigFields().getMountedEntity();
        if (definition == null) return null;
        LivingEntity riderBody = rider.getLivingEntity();
        if (riderBody == null || !riderBody.isValid()) return null;

        EntityType nativeType;
        try {
            nativeType = EntityType.valueOf(definition);
        } catch (IllegalArgumentException customDefinition) {
            return generateCustomMount(rider, riderBody, definition);
        }
        if (nativeType.getEntityClass() == null || !LivingEntity.class.isAssignableFrom(nativeType.getEntityClass())) {
            Logger.warn("Mount " + definition + " for " + rider.getCustomBossesConfigFields().getFilename() + " is not a living entity.");
            return null;
        }
        LivingEntity mount = (LivingEntity) riderBody.getWorld().spawnEntity(riderBody.getLocation(), nativeType);
        rider.livingEntityMount = mount;
        boolean attached = false;
        try {
            if (rider.getLivingEntity() != riderBody) return null;
            mount.setRemoveWhenFarAway(false);
            attached = PreventMountExploit.addPassenger(mount, riderBody);
        } finally {
            if (!attached) {
                if (rider.livingEntityMount == mount) rider.livingEntityMount = null;
                mount.remove();
            }
        }
        return null;
    }

    private static CustomBossEntity generateCustomMount(CustomBossEntity rider, LivingEntity riderBody, String definition) {
        CustomBossesConfigFields fields = CustomBossesConfig.getCustomBoss(definition);
        if (fields == null) {
            Logger.warn("Invalid mount " + definition + " for " + rider.getCustomBossesConfigFields().getFilename());
            return null;
        }
        CustomBossEntity mount = new CustomBossEntity(fields);
        mount.setSpawnLocation(riderBody.getLocation());
        mount.setBypassesProtections(rider.getBypassesProtections());
        mount.setPersistent(false);
        mount.setMount(true);
        rider.customBossMount = mount;
        boolean queued = false;
        try {
            mount.spawn(false);
            if (!mount.isValid() || rider.getLivingEntity() != riderBody || !riderBody.isValid()) return null;
            rider.mountAttachmentTask = new BukkitRunnable() {
                @Override
                public void run() {
                    if (rider.customBossMount != mount) {
                        mount.remove(RemovalReason.REINFORCEMENT_CULL);
                        return;
                    }
                    rider.mountAttachmentTask = null;
                    boolean attached = false;
                    try {
                        if (!mount.isValid() || rider.getLivingEntity() != riderBody || !riderBody.isValid()) return;
                        if (mount.getCustomModel() != null) {
                            mount.getCustomModel().addPassenger(rider);
                            attached = true;
                        } else attached = PreventMountExploit.addPassenger(mount.getLivingEntity(), riderBody);
                    } finally {
                        if (!attached) removeCustomMount(rider, mount);
                    }
                }
            }.runTaskLater(MetadataHandler.PLUGIN, 5L);
            queued = true;
            return mount;
        } finally {
            if (!queued) removeCustomMount(rider, mount);
        }
    }

    private static void removeCustomMount(CustomBossEntity rider, CustomBossEntity mount) {
        if (rider.customBossMount == mount) rider.customBossMount = null;
        mount.remove(RemovalReason.REINFORCEMENT_CULL);
    }
}
