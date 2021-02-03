package com.magmaguy.elitemobs.mobconstructor.custombosses;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.internal.NewMinidungeonRelativeBossLocationEvent;
import com.magmaguy.elitemobs.api.internal.RemovalReason;
import com.magmaguy.elitemobs.config.custombosses.CustomBossConfigFields;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.mobconstructor.SimplePersistentEntityInterface;
import com.magmaguy.elitemobs.powers.bosspowers.SpiritWalk;
import com.magmaguy.elitemobs.utils.WarningMessage;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashSet;

public class RegionalBossEntity implements SimplePersistentEntityInterface {

    private static final HashSet<RegionalBossEntity> regionalBossEntityList = new HashSet();

    public static HashSet<RegionalBossEntity> getRegionalBossEntitySet() {
        return regionalBossEntityList;
    }

    private final AbstractRegionalEntity abstractRegionalEntity;
    private final CustomBossConfigFields customBossConfigFields;
    private double leashRadius;
    private final int ticksBeforeUnixTime;
    private final int spawnCooldownInMinutes;
    public Location spawnLocation;
    private boolean isRespawning;
    public CustomBossEntity customBossEntity;

    public RegionalBossEntity(AbstractRegionalEntity abstractRegionalEntity) {
        this.abstractRegionalEntity = abstractRegionalEntity;
        this.customBossConfigFields = abstractRegionalEntity.customBossConfigFields;
        this.leashRadius = customBossConfigFields.getLeashRadius();
        this.ticksBeforeUnixTime = (int) abstractRegionalEntity.getTicksBeforeRespawn();
        this.spawnLocation = abstractRegionalEntity.getSpawnLocation();
        this.spawnCooldownInMinutes = customBossConfigFields.getSpawnCooldown();

        regionalBossEntityList.add(this);

        if (abstractRegionalEntity.isWorldIsLoaded())
            new BukkitRunnable() {
                @Override
                public void run() {
                    spawnRegionalBoss();
                }
            }.runTaskLater(MetadataHandler.PLUGIN, ticksBeforeUnixTime);
    }

    public void spawnRegionalBoss() {
        this.customBossEntity = CustomBossEntity.constructCustomBoss(
                customBossConfigFields,
                spawnLocation,
                customBossConfigFields.getLevel(),
                this,
                false);

        //todo: test this in intentionally protected areas
        if (this.customBossEntity == null || customBossEntity.getLivingEntity() == null) {
            new WarningMessage("Regional boss " + customBossConfigFields.getFile().getName() +
                    " failed to spawn in location " + spawnLocation.toString() + " ! Does the region prevent mobs" +
                    " from spawning?");
            return;
        }

        //Warn admins about bad spawn location
        if (!spawnLocation.getBlock().isPassable())
            new WarningMessage("Warning: Location " + abstractRegionalEntity.getRawString() + " for boss " +
                    customBossConfigFields.getFileName() + " seems to be inside of a solid block!");

        this.customBossEntity.regionalBossEntity = this;

        checkLeash();
        customBossEntity.getLivingEntity().addPotionEffect(new PotionEffect(PotionEffectType.SLOW, Integer.MAX_VALUE, 3));
        customBossEntity.setIsRegionalBoss(true);

    }

    private final RegionalBossEntity regionalBossEntity = this;

    public void respawnRegionalBoss() {
        if (isRespawning) return;
        isRespawning = true;

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!regionalBossEntityList.contains(regionalBossEntity)) {
                    cancel();
                    return;
                }
                spawnRegionalBoss();
                checkLeash();
                isRespawning = false;
            }

        }.runTaskLater(MetadataHandler.PLUGIN, spawnCooldownInMinutes * 20 * 60);
        //commit to the configs
        abstractRegionalEntity.updateTicksBeforeRespawn();
    }

    public boolean isRespawning() {
        return isRespawning;
    }

    public void setLeashRadius(double leashRadius) {
        this.leashRadius = leashRadius;
    }

    private BukkitTask leashTask;

    private void checkLeash() {

        if (leashRadius < 1)
            return;

        leashTask = new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    if (customBossEntity == null ||
                            customBossEntity.getLivingEntity() == null ||
                            !customBossEntity.getLivingEntity().isValid()) {
                        cancel();
                        return;
                    }
                    if (customBossEntity.getLivingEntity().getLocation().distanceSquared(spawnLocation) > Math.pow(leashRadius, 2))
                        SpiritWalk.spiritWalkRegionalBossAnimation(customBossEntity, customBossEntity.getLivingEntity().getLocation(), spawnLocation);
                } catch (Exception ex) {
                    new WarningMessage("Async leash task errored!");
                }
            }
        }.runTaskTimerAsynchronously(MetadataHandler.PLUGIN, 20 * 3, 20 * 3);

    }

    /**
     * Gets rid of repeating tasks that would cause issues if the custom boss ceases to be valid during runtime
     */
    public void removeTemporarily() {
        if (leashTask != null)
            leashTask.cancel();
    }

    /**
     * Permanently deletes this instance of the regional boss from the config files
     */
    public void removePermanently() {
        if (customBossEntity != null)
            customBossEntity.remove(true);
        regionalBossEntityList.remove(this);
        abstractRegionalEntity.remove();
    }

    /**
     * Returns the name of the world the boss is meant to spawn in. Used for checking if the boss should spawn when a world loads.
     *
     * @return The name of the world the boss is meant to spawn in.
     */
    public String getSpawnWorldName() {
        return abstractRegionalEntity.getWorldName();
    }

    public CustomBossConfigFields getCustomBossConfigFields() {
        return customBossConfigFields;
    }

    /**
     * Runs on chunk load. Should start repeating tasks that rely on the boss being loaded.
     */
    @Override
    public void chunkLoad() {
        checkLeash();
    }

    /**
     * Runs on chunk unload. Should stop repeating tasks that rely on the boss being loaded.
     */
    @Override
    public void chunkUnload() {
        removeTemporarily();
    }

    /**
     * Runs on world load. Starts a new instance of the boss in-game that was previously cached in an unloaded world.
     * This is the primary means of regional boss loading for world-based minidungeons in the dungeon packager.
     */
    public void worldLoad() {
        abstractRegionalEntity.setSpawnLocation();
        spawnRegionalBoss();
    }

    /**
     * Runs on world unload. Removes this instance of the boss in-game.
     * This is the primary means of removing regional bosses when a world-based minidungeons is uninstalled via the dungeon packager
     */
    public void worldUnload() {
        spawnLocation = null;
        abstractRegionalEntity.setWorldIsLoaded(false);
        abstractRegionalEntity.setSpawnLocation(null);
        removeTemporarily();
        EntityTracker.unregister(customBossEntity.uuid, RemovalReason.WORLD_UNLOAD);
        customBossEntity.remove(true);
    }

    public static class RegionalBossEntityEvents implements Listener {
        @EventHandler(ignoreCancelled = true)
        public void onNewMinidungeonRelativeBossLocationEvent(NewMinidungeonRelativeBossLocationEvent event) {
            new AbstractRegionalEntity(event.getRealLocation(), event.getCustomBossConfigFields());
        }
    }

}
