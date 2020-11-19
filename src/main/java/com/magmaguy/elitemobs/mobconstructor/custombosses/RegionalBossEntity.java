package com.magmaguy.elitemobs.mobconstructor.custombosses;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.EliteMobDeathEvent;
import com.magmaguy.elitemobs.config.custombosses.CustomBossConfigFields;
import com.magmaguy.elitemobs.events.mobs.sharedeventproperties.DynamicBossLevelConstructor;
import com.magmaguy.elitemobs.powers.bosspowers.SpiritWalk;
import com.magmaguy.elitemobs.thirdparty.discordsrv.DiscordSRVAnnouncement;
import com.magmaguy.elitemobs.utils.ChunkLocationChecker;
import com.magmaguy.elitemobs.utils.WarningMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;

public class RegionalBossEntity implements Listener {

    private static final HashSet<RegionalBossEntity> regionalBossEntityList = new HashSet();

    public static HashSet<RegionalBossEntity> getRegionalBossEntityList() {
        return regionalBossEntityList;
    }

    public CustomBossEntity customBossEntity;
    private final CustomBossConfigFields.ConfigRegionalEntity configRegionalEntity;
    public boolean isAlive;
    private final Location spawnLocation;
    private double leashRadius;
    private final int respawnCooldown;
    public boolean inCooldown = false;
    private final CustomBossConfigFields customBossConfigFields;

    public RegionalBossEntity(CustomBossConfigFields customBossConfigFields, CustomBossConfigFields.ConfigRegionalEntity configRegionalEntity) {
        this.configRegionalEntity = configRegionalEntity;
        this.spawnLocation = configRegionalEntity.spawnLocation;
        this.respawnCooldown = customBossConfigFields.getSpawnCooldown();
        this.customBossConfigFields = customBossConfigFields;
        this.leashRadius = customBossConfigFields.getLeashRadius();
        if (spawnLocation == null || spawnLocation.getWorld() == null) {
            new WarningMessage("Spawn location for regional boss " + customBossConfigFields.getFileName() + " is not valid. Incorrect location: " + spawnLocation);
            new WarningMessage("EliteMobs will skip this entity's spawn.");
            return;
        }
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

        if (spawnLocation == null)
            return;

        try {
            spawnLocation.getChunk().load();
        } catch (Exception ex) {
            new WarningMessage("Failed to load location " + spawnLocation.toString() + " - this location can not be loaded");
            new WarningMessage("Does the world " + spawnLocation.getWorld() + " exist? Did the world name change or has the world been removed?");
            return;
        }
        customBossEntity = CustomBossEntity.constructCustomBoss(customBossConfigFields.getFileName(), spawnLocation, mobLevel, this, false);
        isAlive = true;

        try {
            //todo: clean this up
            customBossEntity.advancedGetEntity();
        } catch (Exception ex) { //This is thrown if the entity failed to spawn if, for example, the entity type is not valid
            new WarningMessage("Regional boss from config file " + customBossConfigFields.getFileName() + " failed to spawn." +
                    " This is either because another plugin prevented it from spawning (check region management plugins like WorldGuard)" +
                    " or because something in the config (probably entity type) was not valid.");
            return;
        }
        checkLeash();
        regionalBossWatchdog();
        customBossEntity.getLivingEntity().addPotionEffect(new PotionEffect(PotionEffectType.SLOW, Integer.MAX_VALUE, 3));
        customBossEntity.setIsRegionalBoss(true);
        if (customBossConfigFields.getTimeout() > 0)
            startEscapeMechanismDelay(customBossConfigFields.getTimeout());

    }

    private void respawnRegionalBoss() {

        isAlive = false;
        inCooldown = true;

        new BukkitRunnable() {
            @Override
            public void run() {
                customBossConfigFields.saveTicksBeforeRespawn();
            }
        }.runTaskAsynchronously(MetadataHandler.PLUGIN);

        new BukkitRunnable() {
            @Override
            public void run() {
                inCooldown = false;
                spawnRegionalBoss();
                checkLeash();
            }

        }.runTaskLater(MetadataHandler.PLUGIN, respawnCooldown * 20 * 60);

        customBossConfigFields.updateTicksBeforeRespawn(configRegionalEntity.uuid, respawnCooldown);

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

                Entity livingEntity = customBossEntity.advancedGetEntity();

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
                    SpiritWalk.spiritWalkRegionalBossAnimation(customBossEntity, livingEntity.getLocation(), spawnLocation);

            }
        }.runTaskTimer(MetadataHandler.PLUGIN, 20, 20 * 3);

    }

    /**
     * Starts the escape mechanic for bosses that have this feature. After a set time, in minutes, the boss will escape,
     * potentially broadcasting an escape message.
     */
    private void startEscapeMechanismDelay(int timeout) {

        if (timeout < 1) return;

        new BukkitRunnable() {

            @Override
            public void run() {
                LivingEntity livingEntity = customBossEntity.advancedGetEntity();
                if (livingEntity == null || livingEntity.isDead()) {
                    customBossEntity.remove();
                    return;
                }
                customBossEntity.remove();
                if (customBossConfigFields.getAnnouncementPriority() < 1) return;
                if (customBossConfigFields.getEscapeMessage() != null)
                    for (Player player : Bukkit.getOnlinePlayers())
                        if (player.getWorld().equals(livingEntity.getWorld()))
                            player.sendMessage(ChatColorConverter.convert(customBossConfigFields.getEscapeMessage()));
                if (customBossConfigFields.getAnnouncementPriority() < 3) return;
                new DiscordSRVAnnouncement(ChatColorConverter.convert(customBossConfigFields.getEscapeMessage()));

            }

        }.runTaskLater(MetadataHandler.PLUGIN, 20 * 60 * timeout);

    }

    /**
     * This may cause issues when bosses wander really far from the spawn chunk
     */
    private void regionalBossWatchdog() {

        new BukkitRunnable() {

            @Override
            public void run() {
                if (inCooldown)
                    return;
                if (!ChunkLocationChecker.locationIsLoaded(spawnLocation)) return;
                if (customBossEntity == null ||
                        customBossEntity.advancedGetEntity() == null ||
                        customBossEntity.advancedGetEntity().isDead()) {
                    respawnRegionalBoss();
                    cancel();
                }

            }

        }.runTaskTimerAsynchronously(MetadataHandler.PLUGIN, 20, 20 * 60);
    }

    public CustomBossConfigFields getCustomBossConfigFields() {
        return customBossConfigFields;
    }

    public static class RegionalBossEntityEvents implements Listener {
        @EventHandler
        public void onRegionalBossDeath(EliteMobDeathEvent event) {
            for (RegionalBossEntity regionalBossEntity : getRegionalBossEntityList()) {
                if (!regionalBossEntity.isAlive) return;
                if (regionalBossEntity.customBossEntity.advancedGetEntity() == null) return;
                if (!event.getEliteMobEntity().getLivingEntity().getUniqueId().equals(regionalBossEntity.customBossEntity.uuid))
                    return;
                regionalBossEntity.respawnRegionalBoss();
            }
        }
    }

}
