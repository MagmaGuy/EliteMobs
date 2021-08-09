package com.magmaguy.elitemobs.mobconstructor.custombosses;

import com.google.common.collect.ArrayListMultimap;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.internal.NewMinidungeonRelativeBossLocationEvent;
import com.magmaguy.elitemobs.api.internal.RemovalReason;
import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfig;
import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import com.magmaguy.elitemobs.mobconstructor.SimplePersistentEntityInterface;
import com.magmaguy.elitemobs.powers.bosspowers.SpiritWalk;
import com.magmaguy.elitemobs.utils.ConfigurationLocation;
import com.magmaguy.elitemobs.utils.WarningMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class RegionalBossEntity extends CustomBossEntity implements SimplePersistentEntityInterface {

    private static final HashSet<RegionalBossEntity> regionalBosses = new HashSet();
    private static final ArrayListMultimap<CustomBossesConfigFields, RegionalBossEntity> regionalBossesFromConfigFields = ArrayListMultimap.create();
    private final double leashRadius;
    //Format: worldName,x,y,z,pitch,yaw:unixTimeForRespawn
    private String rawString;
    private String rawLocationString;
    private long ticksBeforeRespawn = 0;
    private long unixRespawnTime;
    private int respawnCoolDownInMinutes = 0;
    private boolean isRespawning = false;
    private BukkitTask leashTask;

    public RegionalBossEntity(CustomBossesConfigFields customBossesConfigFields, String rawString) {
        super(customBossesConfigFields);
        this.rawString = rawString;
        this.rawLocationString = rawString.split(":")[0];
        this.respawnCoolDownInMinutes = customBossesConfigFields.getSpawnCooldown();
        this.leashRadius = customBossesConfigFields.getLeashRadius();

        regionalBossesFromConfigFields.put(customBossesConfigFields, this);

        unixRespawnTime = 0;
        if (rawString.contains(":"))
            unixRespawnTime = Long.parseLong(rawString.split(":")[1]);
        ticksBeforeRespawn = 0;
        if (unixRespawnTime > 0)
            ticksBeforeRespawn = (unixRespawnTime - System.currentTimeMillis()) / 1000 * 20 < 0 ?
                    0 : (unixRespawnTime - System.currentTimeMillis()) / 1000 * 20;
        super.worldName = rawLocationString.split(",")[0];
        super.spawnLocation = ConfigurationLocation.deserialize(rawLocationString);
    }

    private RegionalBossEntity(CustomBossesConfigFields customBossesConfigFields, Location location, boolean permanent) {
        super(customBossesConfigFields);
        super.spawnLocation = location;
        this.leashRadius = customBossesConfigFields.getLeashRadius();
        if (permanent) {
            this.rawString = ConfigurationLocation.serialize(location);
            this.rawLocationString = rawString.split(":")[0];
            this.unixRespawnTime = 0;
            this.respawnCoolDownInMinutes = customBossesConfigFields.getSpawnCooldown();
            regionalBossesFromConfigFields.put(customBossesConfigFields, this);
            saveNewLocation();
        }
    }

    public static void regionalDataSaver() {
        new BukkitRunnable() {
            @Override
            public void run() {
                save();
            }
        }.runTaskTimerAsynchronously(MetadataHandler.PLUGIN, 20 * 5, 20 * 5);
    }

    public static void save() {
        for (CustomBossesConfigFields customBossesConfigFields : regionalBossesFromConfigFields.keySet()) {
            if (!customBossesConfigFields.isFilesOutOfSync()) continue;
            customBossesConfigFields.setFilesOutOfSync(false);
            List<String> spawnLocations = new ArrayList<>();
            for (RegionalBossEntity regionalBossEntity : regionalBossesFromConfigFields.get(customBossesConfigFields))
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
            if (regionalBossEntity.getSpawnLocation() == spawnLocation)
                return regionalBossEntity;
        return null;
    }

    public static HashSet<RegionalBossEntity> getRegionalBossEntitySet() {
        return regionalBosses;
    }

    @Nullable
    public static RegionalBossEntity createTemporaryRegionalBossEntity(String filename, Location spawnLocation) {
        CustomBossesConfigFields customBossesConfigFields = CustomBossesConfig.getCustomBoss(filename);
        if (customBossesConfigFields == null)
            return null;
        return new RegionalBossEntity(customBossesConfigFields, spawnLocation, false);
    }

    public static RegionalBossEntity createPermanentRegionalBossEntity(CustomBossesConfigFields customBossesConfigFields, Location spawnLocation) {
        RegionalBossEntity regionalBossEntity = new RegionalBossEntity(customBossesConfigFields, spawnLocation, true);
        regionalBossEntity.initialize();
        return regionalBossEntity;
    }

    public boolean isRespawning() {
        return isRespawning;
    }

    public void saveNewLocation() {
        if (spawnLocation == null || getLocation() == null) {
            new WarningMessage("Failed to save regional boss because it failed to spawn correctly!");
        }
        customBossesConfigFields.setFilesOutOfSync(true);
        rawString = ConfigurationLocation.serialize(spawnLocation);
    }

    public void initialize() {
        queueSpawn(false);
    }

    public void queueSpawn(boolean silent) {
        RegionalBossEntity regionalBossEntity = this;
        this.isRespawning = true;
        Bukkit.getScheduler().scheduleSyncDelayedTask(MetadataHandler.PLUGIN, () -> {
            ticksBeforeRespawn = 0;
            //Reminder: this might not spawn a living entity as it gets queued for when the chunk loads
            regionalBossEntity.spawn(silent);
        }, ticksBeforeRespawn);
    }

    public void respawn() {
        this.isRespawning = true;
        if (super.phaseBossEntity != null)
            super.phaseBossEntity.deathReset();
        unixRespawnTime = (respawnCoolDownInMinutes * 60L * 1000L) + System.currentTimeMillis();
        ticksBeforeRespawn = respawnCoolDownInMinutes * 60L * 20L;
        rawString = rawLocationString + ":" + unixRespawnTime;
        customBossesConfigFields.setFilesOutOfSync(true);
        queueSpawn(false);
    }

    public void checkLeash() {
        if (leashRadius < 1)
            return;
        RegionalBossEntity regionalBossEntity = this;
        leashTask = Bukkit.getScheduler().runTaskTimerAsynchronously(MetadataHandler.PLUGIN, () -> {
            try {
                if (getLivingEntity() == null ||
                        !getLivingEntity().isValid()) {
                    leashTask.cancel();
                    return;
                }
                if (getLivingEntity().getLocation().distanceSquared(spawnLocation) > Math.pow(leashRadius, 2))
                    SpiritWalk.spiritWalkRegionalBossAnimation(regionalBossEntity, getLivingEntity().getLocation(), spawnLocation);

            } catch (Exception ex) {
                new WarningMessage("Async leash task errored!");
            }
        }, 20 * 3, 20 * 3);
    }

    public void setLeashRadius(int leashRadius) {
        getCustomBossesConfigFields().setLeashRadius(leashRadius);
        getCustomBossesConfigFields().setFilesOutOfSync(true);
    }

    @Override
    public void spawn(boolean silent) {
        super.spawn(silent);
        this.isRespawning = false;
        checkLeash();
    }

    @Override
    public void remove(RemovalReason removalReason) {
        if (leashTask != null)
            leashTask.cancel();

        super.remove(removalReason);
        if (customBossesConfigFields.isReinforcement()) {
            regionalBosses.remove(this);
        }
        switch (removalReason) {
            case REMOVE_COMMAND:
                regionalBosses.remove(this);
                regionalBossesFromConfigFields.remove(customBossesConfigFields, this);
                getCustomBossesConfigFields().setFilesOutOfSync(true);
                break;
            case DEATH:
            case BOSS_TIMEOUT:
                respawn();
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

    /**
     * Runs on chunk unload. Should stop repeating tasks that rely on the boss being loaded.
     */
    @Override
    public void chunkUnload() {
        super.chunkUnload();
        //leash is already handled in softRemove()
    }

    /**
     * Runs on world load. Starts a new instance of the boss in-game that was previously cached in an unloaded world.
     * This is the primary means of regional boss loading for world-based minidungeons in the dungeon packager.
     */
    @Override
    public void worldLoad() {
        super.worldLoad();
    }

    /**
     * Runs on world unload. Removes this instance of the boss in-game.
     * This is the primary means of removing regional bosses when a world-based minidungeons is uninstalled via the dungeon packager
     */
    @Override
    public void worldUnload() {
        super.worldUnload();
    }

    public static class RegionalBossEntityEvents implements Listener {
        @EventHandler(ignoreCancelled = true)
        public void onNewMinidungeonRelativeBossLocationEvent(NewMinidungeonRelativeBossLocationEvent event) {
            new RegionalBossEntity(event.getCustomBossConfigFields(), ConfigurationLocation.serialize(event.getRealLocation()));
            event.getCustomBossConfigFields().setFilesOutOfSync(true);
        }
    }

}
