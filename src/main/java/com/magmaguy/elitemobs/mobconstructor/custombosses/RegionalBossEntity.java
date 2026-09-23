package com.magmaguy.elitemobs.mobconstructor.custombosses;

import com.google.common.collect.ArrayListMultimap;
import com.magmaguy.easyminecraftgoals.NMSManager;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.internal.RemovalReason;
import com.magmaguy.elitemobs.config.ItemSettingsConfig;
import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfig;
import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.mobconstructor.PersistentMovingEntity;
import com.magmaguy.elitemobs.mobconstructor.PersistentObject;
import com.magmaguy.elitemobs.mobconstructor.custombosses.transitiveblocks.TransitiveBlock;
import com.magmaguy.elitemobs.pathfinding.Navigation;
import com.magmaguy.elitemobs.pathfinding.patrol.PatrolService;
import com.magmaguy.elitemobs.powers.specialpowers.SpiritWalkSupport;
import com.magmaguy.elitemobs.utils.ConfigurationLocation;
import com.magmaguy.magmacore.util.AttributeManager;
import com.magmaguy.magmacore.util.ChunkLocationChecker;
import com.magmaguy.magmacore.util.Logger;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class RegionalBossEntity extends CustomBossEntity implements PersistentObject, PersistentMovingEntity {

    private static final ArrayListMultimap<CustomBossesConfigFields, RegionalBossEntity> regionalBossesFromConfigFields = ArrayListMultimap.create();
    @Getter
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
    @Getter
    private boolean removed = false;
    private BukkitTask respawnTask = null;
    private long respawnGeneration;
    private boolean freshEncounterPending;

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
        super.spawnLocation = ConfigurationLocation.serialize(rawLocationString);

        super.setPersistent(true);
    }

    public RegionalBossEntity(CustomBossesConfigFields customBossesConfigFields, Location location, boolean permanent, boolean persistent) {
        super(customBossesConfigFields);
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

    /**
     * Used by third party plugins to spawn regional bosses (BetterStructures uses this)
     *
     * @param configurationFilename Filename of the configuration file as set in the custombosses folder of EliteMobs
     * @param spawnLocation         Spawn location of the regional boss
     */
    @Nullable
    public static RegionalBossEntity SpawnRegionalBoss(String configurationFilename, Location spawnLocation) {
        CustomBossesConfigFields thisCustomBossConfigurationField = CustomBossesConfig.getCustomBoss(configurationFilename);
        if (thisCustomBossConfigurationField == null) return null;
        if (spawnLocation == null) return null;
        return new RegionalBossEntity(thisCustomBossConfigurationField, spawnLocation, true, true);
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
        }.runTaskTimer(MetadataHandler.PLUGIN, 20L * 5, 20L * 5);
    }

    public static void save() {
        save(false);
    }

    /** Flushes inline after Bukkit stops accepting this plugin's scheduled work. */
    public static void flushOnShutdown() {
        save(true);
    }

    private static void save(boolean flush) {
        for (CustomBossesConfigFields fields : regionalBossesFromConfigFields.keySet()) {
            if (!fields.isFilesOutOfSync()) continue;
            List<String> locations = new ArrayList<>();
            for (RegionalBossEntity boss : regionalBossesFromConfigFields.get(fields))
                if (!boss.removed) locations.add(boss.rawString);
            try {
                fields.saveRegionalSpawnLocations(locations, flush);
            } catch (RuntimeException failure) {
                Logger.warn("Failed to save regional state for " + fields.getFilename() + ": " + failure.getMessage());
                // A failed file must not prevent other saves or unrelated shutdown cleanup.
                if (flush) Logger.warn("Unsaved spawnLocations for " + fields.getFilename() + ": " + locations);
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

    public String getConfigurationLocationString() {
        return rawString;
    }

    /** Moves this live actor between config ownership buckets so the periodic saver cannot undo a fork. */
    public void rebindPatrolConfig(CustomBossesConfigFields fields) {
        CustomBossesConfigFields previous = getCustomBossesConfigFields();
        regionalBossesFromConfigFields.remove(previous, this);
        // A patrol fork inherits the same boss definition. Do not rebuild powers or combat state.
        customBossesConfigFields = Objects.requireNonNull(fields, "fields");
        regionalBossesFromConfigFields.put(fields, this);
        previous.setFilesOutOfSync(true);
        fields.setFilesOutOfSync(true);
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
        regionalBossEntity.queueSpawn(false);
        return regionalBossEntity;
    }

    public boolean isRespawning() {
        return isRespawning;
    }

    public void saveNewLocation() {
        if (spawnLocation == null) {
            Logger.warn("Failed to save regional boss because it failed to spawn correctly!");
        }
        customBossesConfigFields.setFilesOutOfSync(true);
        rawString = ConfigurationLocation.deserialize(spawnLocation);
        rawLocationString = rawString;
        unixRespawnTime = 0;
    }

    public void initialize() {
        queueSpawn(RegionalBossSpawnPolicy.forPersistedState(unixRespawnTime));
    }

    public void queueSpawn(boolean silent) {
        queueSpawn(SpawnLifecycle.fromSilentFlag(silent));
    }

    private void cancelQueuedRespawn() {
        ++respawnGeneration;
        if (respawnTask != null) respawnTask.cancel();
        respawnTask = null;
    }

    private void queueSpawn(SpawnLifecycle.Context spawnContext) {
        cancelQueuedRespawn();
        long generation = respawnGeneration;
        this.isRespawning = true;
        respawnTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (generation != respawnGeneration || removed) return;
                respawnTask = null;
                ticksBeforeRespawn = 0;
                clearPersistedRespawnTime();
                if (livingEntity != null && livingEntity.isValid()) {
                    isRespawning = false;
                    return;
                }
                freshEncounterPending = true;
                spawn(spawnContext);
            }
        }.runTaskLater(MetadataHandler.PLUGIN, ticksBeforeRespawn);
    }

    private void clearPersistedRespawnTime() {
        if (unixRespawnTime <= 0) return;
        unixRespawnTime = 0;
        rawString = rawLocationString;
        if (phaseBossEntity != null)
            phaseBossEntity.getPhase1Config().setFilesOutOfSync(true);
        else
            customBossesConfigFields.setFilesOutOfSync(true);
    }

    public void forceRespawn() {
        if (respawnTask == null) return;
        cancelQueuedRespawn();
        ticksBeforeRespawn = 0;
        clearPersistedRespawnTime();
        if (livingEntity != null && livingEntity.isValid()) {
            isRespawning = false;
            return;
        }
        freshEncounterPending = true;
        spawn(false);
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
        leashTask = Bukkit.getScheduler().runTaskTimer(MetadataHandler.PLUGIN, () -> {
            try {
                if (!isValid()) {
                    cancelLeash();
                    return;
                }
                if (getLivingEntity().getLocation().distanceSquared(spawnLocation) > Math.pow(leashRadius, 2))
                    SpiritWalkSupport.spiritWalkRegionalBossAnimation(regionalBossEntity, getLivingEntity().getLocation(), getSpawnLocation());
            } catch (Exception ex) {
                ex.printStackTrace();
                Logger.warn("Leash task errored!");
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
        spawn(SpawnLifecycle.fromSilentFlag(silent));
    }

    @Override
    protected void spawn(SpawnLifecycle.Context spawnContext) {
        // Persistence restoration resumes this encounter and retires its obsolete respawn timer.
        if (respawnTask != null) {
            cancelQueuedRespawn();
            ticksBeforeRespawn = 0;
            clearPersistedRespawnTime();
        }
        if (livingEntity != null && livingEntity.isValid()) return;
        if (freshEncounterPending) {
            if (phaseBossEntity != null) phaseBossEntity.silentReset();
            health = null;
        }
        super.spawn(spawnContext);
        if (livingEntity == null || !livingEntity.isValid()) return;
        this.isRespawning = false;
        if (!ItemSettingsConfig.isRegionalBossesDropVanillaLoot())
            super.vanillaLoot = false;
        if (NMSManager.isEnabled()) {
            if (livingEntity != null && leashRadius > 0) {
                Navigation.addSoftLeashAI(this);
                Navigation.addHardLeashAI(this);
            }
        } else if (livingEntity != null)
            checkLeash();
    }

    @Override
    protected void onBodyMaterialized() {
        if (freshEncounterPending) {
            clearDamagers();
            freshEncounterPending = false;
        }
        super.onBodyMaterialized();
    }

    @Override
    public void remove(RemovalReason removalReason) {
        beginRemovalCall();
        try {
            cancelLeash();
            super.remove(removalReason);

            switch (removalReason) {
                case REMOVE_COMMAND:
                    permanentlyRemove();
                    break;
                case DEATH:
                case BOSS_TIMEOUT:
                    //this is used for 1-time regional bosses, such as the ones spawned by BetterStructures
                    //Temporary regionals (reinforcement summons) can never respawn - their cooldown
                    //is unset, so respawn() no-ops - and without a full removal each one stayed in
                    //the elite tracking map forever, pinning its world in memory. Heavy instanced
                    //dungeon usage accumulated thousands of them.
                    if (customBossesConfigFields.isRemoveAfterDeath() || isTemporary()) {
                        permanentlyRemove();
                        break;
                    }
                    if (!(this instanceof InstancedBossEntity))
                        respawn();
                    break;
                case CHUNK_UNLOAD:
                    if (isTemporary()) {
                        //A chunk-unloaded temporary regional is gone for good (nothing respawns
                        //it), so its tracking must go too.
                        permanentlyRemove();
                        break;
                    }
                    if (!PatrolService.hasConfiguredPatrol(this)
                            && ChunkLocationChecker.chunkAtLocationIsLoaded(spawnLocation))
                        respawn();
                    break;
                case PHASE_BOSS_RESET:
                case PHASE_BOSS_PHASE_END:
                case EFFECT_TIMEOUT:
                case ENTITY_REPLACEMENT:
                    //The elite continues to exist through these - never a terminal removal.
                    break;
                default:
                    //WORLD_UNLOAD, SHUTDOWN, KILL_COMMAND, REINFORCEMENT_CULL, ARENA_RESET and the
                    //remaining terminal reasons: temporary regionals must be fully removed.
                    if (isTemporary()) permanentlyRemove();
                    break;
            }
        } finally {
            finishRemovalCall();
        }
    }

    /**
     * Whether this regional boss exists outside the configuration files: reinforcement summons and
     * other runtime-only regionals have no serialized location, are never in
     * regionalBossesFromConfigFields, and can never respawn once removed.
     */
    private boolean isTemporary() {
        return rawString == null;
    }

    private void permanentlyRemove() {
        // Keep the terminal phase until the deferred removal event has applied its block cleanup.
        EntityTracker.getEliteMobEntities().remove(super.eliteUUID);
        removed = true;
        cancelQueuedRespawn();
        freshEncounterPending = false;
        //Temporary regionals were never written to the configuration, so there is nothing to sync.
        if (!isTemporary()) {
            var persistedConfig = phaseBossEntity == null ? getCustomBossesConfigFields() : phaseBossEntity.getPhase1Config();
            persistedConfig.setFilesOutOfSync(true);
        }
    }

    /**
     * Runs on chunk load. Should start repeating tasks that rely on the boss being loaded.
     */
    @Override
    public void chunkLoad() {
        restorePersistedSpawn(SpawnLifecycle.Context.RESTORED);
    }

    public void removeSlow() {
        if (getCustomBossesConfigFields().isAlert() || getLivingEntity() == null) return;
        AttributeManager.setAttribute(getLivingEntity(), "generic_follow_range", getFollowDistance());
        AttributeManager.setAttribute(getLivingEntity(), "generic_movement_speed", getMovementSpeedAttribute());
    }

}
