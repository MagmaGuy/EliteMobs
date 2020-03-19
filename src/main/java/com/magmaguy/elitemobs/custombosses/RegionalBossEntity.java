package com.magmaguy.elitemobs.custombosses;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.EliteMobDeathEvent;
import com.magmaguy.elitemobs.config.custombosses.CustomBossConfigFields;
import com.magmaguy.elitemobs.events.mobs.sharedeventproperties.DynamicBossLevelConstructor;
import com.magmaguy.elitemobs.powers.ElitePower;
import com.magmaguy.elitemobs.powers.bosspowers.SpiritWalk;
import com.magmaguy.elitemobs.utils.WarningMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.UUID;

public class RegionalBossEntity implements Listener {

    private static HashSet<RegionalBossEntity> regionalBossEntityList = new HashSet();

    public static HashSet<RegionalBossEntity> getRegionalBossEntityList() {
        return regionalBossEntityList;
    }

    private boolean isAlive;
    private Location spawnLocation;
    private double leashRadius;
    private int respawnCooldown;
    private boolean inCooldown = false;
    private UUID uuid;
    private CustomBossConfigFields customBossConfigFields;

    public RegionalBossEntity(CustomBossConfigFields customBossConfigFields) {
        this.spawnLocation = customBossConfigFields.getSpawnLocation();
        this.respawnCooldown = customBossConfigFields.getSpawnCooldown();
        this.customBossConfigFields = customBossConfigFields;
        this.leashRadius = customBossConfigFields.getLeashRadius();
        spawnRegionalBoss();
        regionalBossEntityList.add(this);
    }

    private void spawnRegionalBoss() {

        EntityType entityType;

        try {
            entityType = EntityType.valueOf(customBossConfigFields.getEntityType());
        } catch (Exception ex) {
            new WarningMessage("Invalid entity type for " + customBossConfigFields.getFileName() + " ! Is " +
                    customBossConfigFields.getEntityType() + " a valid entity type in the Spigot API?");
            return;
        }

        int mobLevel;

        if (customBossConfigFields.getLevel().equalsIgnoreCase("dynamic")) {
            mobLevel = DynamicBossLevelConstructor.findDynamicBossLevel();
        } else {
            try {
                mobLevel = Integer.valueOf(customBossConfigFields.getLevel());
            } catch (Exception ex) {
                new WarningMessage("Regional Elite Mob level for " + customBossConfigFields.getFileName() + " is neither numeric nor dynamic. Fix the configuration for it.");
                return;
            }
        }

        HashSet<ElitePower> elitePowers = new HashSet<>();
        for (String powerName : customBossConfigFields.getPowers())
            if (ElitePower.getElitePower(powerName) != null)
                elitePowers.add(ElitePower.getElitePower(powerName));
            else
                new WarningMessage("Warning: power name " + powerName + " is not registered! Skipping it for custom mob construction...");

        spawnLocation.getChunk().load();
        CustomBossEntity customBossEntity = new CustomBossEntity(customBossConfigFields, entityType, spawnLocation, mobLevel, elitePowers);
        isAlive = true;
        uuid = customBossEntity.getLivingEntity().getUniqueId();
        checkLeash();
        regionalBossWatchdog();

    }

    private void respawnRegionalBoss() {

        isAlive = false;
        inCooldown = true;

        new BukkitRunnable() {
            @Override
            public void run() {
                inCooldown = false;
                spawnRegionalBoss();
                checkLeash();
            }

        }.runTaskLater(MetadataHandler.PLUGIN, respawnCooldown * 20 * 60);

    }

    public void setLeashRadius(double radius) {
        this.leashRadius = radius;
    }

    private void checkLeash() {

        if (leashRadius < 1)
            return;

        new BukkitRunnable() {
            @Override
            public void run() {

                Entity livingEntity = Bukkit.getEntity(uuid);

                if (livingEntity == null && isAlive)
                    return;

                if (!isAlive || livingEntity.isDead()) {
                    cancel();
                    return;
                }

                if (!livingEntity.isValid()) {
                    return;
                }

                if (livingEntity.getLocation().distance(spawnLocation) > leashRadius)
                    SpiritWalk.spiritWalkAnimation((LivingEntity) livingEntity, livingEntity.getLocation(), spawnLocation);

            }
        }.runTaskTimer(MetadataHandler.PLUGIN, 20, 20 * 3);

    }

    private void regionalBossWatchdog() {

        new BukkitRunnable() {

            @Override
            public void run() {
                Entity entity = Bukkit.getEntity(uuid);

                if (entity == null || !isAlive) {
                    cancel();
                    return;
                }

                if (entity.isDead()) {
                    respawnRegionalBoss();
                    cancel();
                }

            }

        }.runTaskTimer(MetadataHandler.PLUGIN, 20, 20);
    }

    public void setSpawnLocation(Location spawnLocation) {
        this.spawnLocation = spawnLocation;
    }

    public CustomBossConfigFields getCustomBossConfigFields() {
        return customBossConfigFields;
    }

    public static class RegionalBossEntityEvents implements Listener {
        @EventHandler
        public void onRegionalBossDeath(EliteMobDeathEvent event) {
            for (RegionalBossEntity regionalBossEntity : getRegionalBossEntityList()) {
                if (!regionalBossEntity.isAlive) return;
                if (regionalBossEntity.uuid == null) return;
                if (!event.getEliteMobEntity().getLivingEntity().getUniqueId().equals(regionalBossEntity.uuid)) return;
                regionalBossEntity.respawnRegionalBoss();
            }
        }
    }

}
