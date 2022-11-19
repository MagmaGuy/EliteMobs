package com.magmaguy.elitemobs.mobconstructor.custombosses;

import com.google.common.collect.ArrayListMultimap;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.internal.NewSchematicPackageRelativeBossLocationEvent;
import com.magmaguy.elitemobs.api.internal.RemovalReason;
import com.magmaguy.elitemobs.config.ItemSettingsConfig;
import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfig;
import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.mobconstructor.PersistentMovingEntity;
import com.magmaguy.elitemobs.mobconstructor.PersistentObject;
import com.magmaguy.elitemobs.mobconstructor.custombosses.transitiveblocks.TransitiveBlock;
import com.magmaguy.elitemobs.powers.SpiritWalk;
import com.magmaguy.elitemobs.utils.ConfigurationLocation;
import com.magmaguy.elitemobs.utils.WarningMessage;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class RegionalBossEntity extends CustomBossEntity implements PersistentObject, PersistentMovingEntity {

    private static final ArrayListMultimap<CustomBossesConfigFields, RegionalBossEntity> regionalBossesFromConfigFields = ArrayListMultimap.create();
    private final double leashRadius;
    //Format: worldName,x,y,z,pitch,yaw:unixTimeForRespawn
    private String rawString;
    private String rawLocationString;
    private long ticksBeforeRespawn = 0;
    private long unixRespawnTime;
    private int respawnCoolDownInMinutes = -1;
    private boolean isRespawning = false;
    private BukkitTask leashTask;
    @Getter
    @Setter
    private List<TransitiveBlock> onSpawnTransitiveBlocks;
    @Getter
    @Setter
    private List<TransitiveBlock> onRemoveTransitiveBlocks;
    private boolean removed = false;


    public RegionalBossEntity(CustomBossesConfigFields customBossesConfigFields, String rawString) {
        super(customBossesConfigFields);
        this.rawString = rawString;
        this.rawLocationString = rawString.split(":")[0];
        this.respawnCoolDownInMinutes = customBossesConfigFields.getSpawnCooldown();
        this.leashRadius = customBossesConfigFields.getLeashRadius();
        this.onSpawnTransitiveBlocks = TransitiveBlock.serializeTransitiveBlocks(customBossesConfigFields.getOnSpawnBlockStates(), customBossesConfigFields.getFilename());
        this.onRemoveTransitiveBlocks = TransitiveBlock.serializeTransitiveBlocks(customBossesConfigFields.getOnRemoveBlockStates(), customBossesConfigFields.getFilename());

        regionalBossesFromConfigFields.put(customBossesConfigFields, this);

        unixRespawnTime = 0;
        if (rawString.contains(":"))
            unixRespawnTime = Long.parseLong(rawString.split(":")[1]);
        ticksBeforeRespawn = 0;
        if (unixRespawnTime > 0)
            ticksBeforeRespawn = (unixRespawnTime - System.currentTimeMillis()) / 1000 * 20 < 0 ?
                    0 : (unixRespawnTime - System.currentTimeMillis()) / 1000 * 20;
        super.worldName = rawLocationString.split(",")[0];
        super.spawnLocation = ConfigurationLocation.serialize(rawLocationString);

        super.setPersistent(true);
    }

    public RegionalBossEntity(CustomBossesConfigFields customBossesConfigFields, Location location, boolean permanent, boolean persistent) {
        super(customBossesConfigFields);
        this.onSpawnTransitiveBlocks = TransitiveBlock.serializeTransitiveBlocks(customBossesConfigFields.getOnSpawnBlockStates(), customBossesConfigFields.getFilename());
        this.onRemoveTransitiveBlocks = TransitiveBlock.serializeTransitiveBlocks(customBossesConfigFields.getOnRemoveBlockStates(), customBossesConfigFields.getFilename());
        this.onSpawnTransitiveBlocks = TransitiveBlock.serializeTransitiveBlocks(customBossesConfigFields.getOnSpawnBlockStates(), customBossesConfigFields.getFilename());
        this.onRemoveTransitiveBlocks = TransitiveBlock.serializeTransitiveBlocks(customBossesConfigFields.getOnRemoveBlockStates(), customBossesConfigFields.getFilename());
        super.setPersistent(persistent);
        super.spawnLocation = location;
        this.leashRadius = customBossesConfigFields.getLeashRadius();
        if (permanent) {
            this.rawString = ConfigurationLocation.deserialize(location);
            this.rawLocationString = rawString.split(":")[0];
            this.unixRespawnTime = 0;
            this.respawnCoolDownInMinutes = customBossesConfigFields.getSpawnCooldown();
            regionalBossesFromConfigFields.put(customBossesConfigFields, this);
            saveNewLocation();
        }
    }

    public static void regionalBossesShutdown() {
        regionalBossesFromConfigFields.clear();
    }

    public static List<RegionalBossEntity> getRegionalBossEntities(CustomBossesConfigFields customBossesConfigFields) {
        return regionalBossesFromConfigFields.get(customBossesConfigFields);
    }

    public static Collection<RegionalBossEntity> getRegionalBossEntities() {
        return regionalBossesFromConfigFields.values();
    }

    public static void regionalDataSaver() {
        new BukkitRunnable() {
            @Override
            public void run() {
                save();
            }
        }.runTaskTimerAsynchronously(MetadataHandler.PLUGIN, 20L * 5, 20L * 5);
    }

    public static void save() {
        for (CustomBossesConfigFields customBossesConfigFields : regionalBossesFromConfigFields.keySet()) {
            if (!customBossesConfigFields.isFilesOutOfSync()) continue;
            customBossesConfigFields.setFilesOutOfSync(false);
            List<String> spawnLocations = new ArrayList<>();
            for (RegionalBossEntity regionalBossEntity : regionalBossesFromConfigFields.get(customBossesConfigFields))
                if (!regionalBossEntity.removed)
                    spawnLocations.add(regionalBossEntity.rawString);
            customBossesConfigFields.getFileConfiguration().set("spawnLocations", spawnLocations);
            try {
                customBossesConfigFields.getFileConfiguration().save(customBossesConfigFields.getFile());
            } catch (Exception ex) {
                new WarningMessage("Failed to save respawn timer for " + customBossesConfigFields.getFileConfiguration().getName() + " !");
            }
        }
    }

    public static RegionalBossEntity getRegionalBoss(CustomBossesConfigFields customBossesConfigFields, Location spawnLocation) {
        for (RegionalBossEntity regionalBossEntity : regionalBossesFromConfigFields.get(customBossesConfigFields))
            if (Objects.equals(regionalBossEntity.getSpawnLocation(), spawnLocation))
                return regionalBossEntity;
        return null;
    }

    public static List<RegionalBossEntity> getRegionalBossEntitySet() {
        return new ArrayList<>(regionalBossesFromConfigFields.values());
    }

    @Nullable
    public static RegionalBossEntity createTemporaryRegionalBossEntity(String filename, Location spawnLocation) {
        CustomBossesConfigFields customBossesConfigFields = CustomBossesConfig.getCustomBoss(filename);
        if (customBossesConfigFields == null)
            return null;
        return new RegionalBossEntity(customBossesConfigFields, spawnLocation, false, false);
    }

    public static RegionalBossEntity createPermanentRegionalBossEntity(CustomBossesConfigFields customBossesConfigFields, Location spawnLocation) {
        RegionalBossEntity regionalBossEntity = new RegionalBossEntity(customBossesConfigFields, spawnLocation, true, true);
        regionalBossEntity.initialize();
        return regionalBossEntity;
    }

    public boolean isRespawning() {
        return isRespawning;
    }

    public void saveNewLocation() {
        if (spawnLocation == null) {
            new WarningMessage("Failed to save regional boss because it failed to spawn correctly!");
        }
        customBossesConfigFields.setFilesOutOfSync(true);
        rawString = ConfigurationLocation.deserialize(spawnLocation);
    }

    public void initialize() {
        queueSpawn(false);
    }

    private BukkitTask respawnTask = null;

    public void queueSpawn(boolean silent) {
        RegionalBossEntity regionalBossEntity = this;
        this.isRespawning = true;
        respawnTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (phaseBossEntity != null) phaseBossEntity.silentReset();
                ticksBeforeRespawn = 0;
                //Reminder: this might not spawn a living entity as it gets queued for when the chunk loads
                regionalBossEntity.spawn(silent);
                regionalBossEntity.getDamagers().clear();
            }
        }.runTaskLater(MetadataHandler.PLUGIN, ticksBeforeRespawn);
    }

    public void forceRespawn() {
        if (respawnTask == null) return;
        respawnTask.cancel();
        ticksBeforeRespawn = 0;
        spawn(false);
        getDamagers().clear();
    }

    public void respawn() {
        if (respawnCoolDownInMinutes < 0) return;
        this.isRespawning = true;
        unixRespawnTime = (respawnCoolDownInMinutes * 60L * 1000L) + System.currentTimeMillis();
        ticksBeforeRespawn = respawnCoolDownInMinutes * 60L * 20L;
        rawString = rawLocationString + ":" + unixRespawnTime;
        if (phaseBossEntity != null)
            phaseBossEntity.getPhase1Config().setFilesOutOfSync(true);
        else
            customBossesConfigFields.setFilesOutOfSync(true);
        queueSpawn(false);
    }

    public void checkLeash() {
        if (leashRadius < 1)
            return;
        RegionalBossEntity regionalBossEntity = this;
        if (leashTask != null) leashTask.cancel();
        leashTask = Bukkit.getScheduler().runTaskTimerAsynchronously(MetadataHandler.PLUGIN, () -> {
            try {
                if (!isValid()) {
                    cancelLeash();
                    return;
                }
                if (getLivingEntity().getLocation().distanceSquared(spawnLocation) > Math.pow(leashRadius, 2))
                    SpiritWalk.spiritWalkRegionalBossAnimation(regionalBossEntity, getLivingEntity().getLocation(), spawnLocation);

            } catch (Exception ex) {
                ex.printStackTrace();
                new WarningMessage("Async leash task errored!");
            }
        }, 20L * 3, 20L * 3);
    }

    private void cancelLeash() {
        if (leashTask == null) return;
        leashTask.cancel();
        leashTask = null;
    }

    public void setLeashRadius(int leashRadius) {
        getCustomBossesConfigFields().setLeashRadius(leashRadius);
        getCustomBossesConfigFields().setFilesOutOfSync(true);
    }

    @Override
    public void spawn(boolean silent) {
        super.spawn(silent);
        this.isRespawning = false;
        if (!ItemSettingsConfig.isRegionalBossesDropVanillaLoot())
            super.vanillaLoot = false;
        if (livingEntity != null)
            checkLeash();
    }

    @Override
    public void remove(RemovalReason removalReason) {
        cancelLeash();
        super.remove(removalReason);

        switch (removalReason) {
            case REMOVE_COMMAND:
                if (phaseBossEntity != null)
                    phaseBossEntity.silentReset();
                EntityTracker.getEliteMobEntities().remove(super.eliteUUID);
                //regionalBossesFromConfigFields.remove(customBossesConfigFields, this);
                removed = true;
                getCustomBossesConfigFields().setFilesOutOfSync(true);
                break;
            case DEATH:
            case BOSS_TIMEOUT:
                if (!(this instanceof InstancedBossEntity))
                    respawn();
                break;
            default:
                break;
        }
    }

    /**
     * Runs on chunk load. Should start repeating tasks that rely on the boss being loaded.
     */
    @Override
    public void chunkLoad() {
        super.chunkLoad();
        checkLeash();
    }

    public static class RegionalBossEntityEvents implements Listener {
        @EventHandler(ignoreCancelled = true)
        public void onNewMinidungeonRelativeBossLocationEvent(NewSchematicPackageRelativeBossLocationEvent event) {
            new RegionalBossEntity(event.getCustomBossesConfigFields(), ConfigurationLocation.deserialize(event.getRealLocation()));
            event.getCustomBossesConfigFields().setFilesOutOfSync(true);
        }
    }

}
