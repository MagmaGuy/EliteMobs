package com.magmaguy.elitemobs.mobconstructor;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.EliteMobHealEvent;
import com.magmaguy.elitemobs.api.EliteMobRemoveEvent;
import com.magmaguy.elitemobs.api.internal.RemovalReason;
import com.magmaguy.elitemobs.collateralminecraftchanges.KeepNeutralsAngry;
import com.magmaguy.elitemobs.combatsystem.LevelScaling;
import com.magmaguy.elitemobs.combatsystem.NaturalEliteCombatTweak;
import com.magmaguy.elitemobs.combatsystem.antiexploit.AntiExploitMessage;
import com.magmaguy.elitemobs.config.AntiExploitConfig;
import com.magmaguy.elitemobs.config.DefaultConfig;
import com.magmaguy.elitemobs.config.MobCombatSettingsConfig;
import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.events.CustomEvent;
import com.magmaguy.elitemobs.mobconstructor.custombosses.AdvancedAggroManager;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.RegionalBossEntity;
import com.magmaguy.elitemobs.mobconstructor.mobdata.aggressivemobs.EliteMobProperties;
import com.magmaguy.elitemobs.playerdata.ElitePlayerInventory;
import com.magmaguy.elitemobs.powers.PowerExecutionOrder;
import com.magmaguy.elitemobs.powers.lua.LuaElitePower;
import com.magmaguy.elitemobs.powers.meta.ElitePower;
import com.magmaguy.elitemobs.powers.scripts.EliteScript;
import com.magmaguy.elitemobs.powerstances.MajorPowerPowerStance;
import com.magmaguy.elitemobs.powerstances.MinorPowerPowerStance;
import com.magmaguy.elitemobs.tagger.PersistentTagger;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.utils.EventCaller;
import com.magmaguy.magmacore.util.AttributeManager;
import com.magmaguy.magmacore.util.ChatColorConverter;
import com.magmaguy.magmacore.util.Logger;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.*;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Consumer;
import java.util.concurrent.ThreadLocalRandom;

public class EliteEntity {

    protected final HashMap<Player, Double> damagers = new HashMap<>();
    private final Map<UUID, EnumMap<SkillType, Double>> skillDamageContributions = new HashMap<>();
    protected final UUID eliteUUID = UUID.randomUUID();
    private boolean removalEventCalled = false;
    private int removalCallDepth = 0;
    private RemovalReason pendingRemovalEventReason;
    //Used for custom arbitrary tags from elite scripts
    private final HashSet<String> customMetadata = new HashSet<>();
    /*
    Store all powers in one set, makes no sense to access it in individual sets.
    The reason they are split up in the first place is to add them in a certain ratio
    Once added, they can just be stored in a pool
     */
    @Getter
    protected HashSet<ElitePower> elitePowers = new TrackedElitePowerSet();
    private transient List<ElitePower> orderedElitePowers = null;
    /**
     * Threat is deliberately tracked separately from damage contribution. Damage remains the
     * source of loot and participation credit, while threat is what target selection consumes and
     * can therefore be amplified by tanking mechanics such as Loud Strikes.
     */
    protected final HashMap<Player, Double> aggro = new HashMap<>();
    private UUID forcedTargetPlayerId;
    private UUID forcedTargetLeaseId;
    private long forcedTargetExpiresAtNanos;
    /*
    Note that a lot of values here are defined by EliteMobProperties.java
     */
    protected LivingEntity livingEntity;
    //LivingEntity gets removed as soon as it dies, unsyncedLivingEntity only ever overwrites when a new living entity is created.
    protected LivingEntity unsyncedLivingEntity;
    @Getter
    @Setter
    protected int level = -1;
    @Getter
    protected double maxHealth;
    @Getter
    protected String name;
    @Getter
    protected int minorPowerCount = 0;
    @Getter
    protected int majorPowerCount = 0;
    @Getter
    @Setter
    protected boolean minorVisualEffect = false;
    @Getter
    @Setter
    protected boolean majorVisualEffect = false;
    @Getter
    @Setter
    protected boolean visualEffectObfuscated = true;
    @Getter
    @Setter
    protected boolean isNaturalEntity;

    /**
     * Returns whether this entity uses scaled combat.
     * Natural elites use scaled combat when the global config is enabled.
     * Distance-based natural elite levels intentionally disable scaled combat.
     * Custom bosses override this with their per-boss config.
     */
    public boolean isScaledCombat() {
        return isNaturalEntity &&
                !isDistanceBasedNaturalEliteLevel() &&
                MobCombatSettingsConfig.isUseScaledCombatForNaturalElites();
    }

    private boolean isDistanceBasedNaturalEliteLevel() {
        Location location = spawnLocation != null ? spawnLocation : getLocation();
        return location != null &&
                location.getWorld() != null &&
                MobCombatSettingsConfig.isUseDistanceBasedNaturalEliteLevelsForWorld(location.getWorld().getName());
    }
    protected EntityType entityType;
    @Getter
    protected Boolean isPersistent = false;
    @Getter
    @Setter
    protected boolean vanillaLoot = true;
    @Getter
    @Setter
    protected boolean eliteLoot = true;
    @Getter
    @Setter
    protected boolean randomLoot = true;
    @Getter
    protected CreatureSpawnEvent.SpawnReason spawnReason;
    @Getter
    @Setter
    protected double healthMultiplier = 1.0;
    @Getter
    @Setter
    protected double damageMultiplier = 1.0;
    protected double defaultMaxHealth;
    @Getter
    @Setter
    protected boolean inCooldown = false;
    @Getter
    protected boolean triggeredAntiExploit = false;
    protected int antiExploitPoints = 0;
    @Getter
    protected boolean inAntiExploitCooldown = false;
    @Getter
    @Setter
    protected boolean inCombat = false;
    @Getter
    protected boolean inCombatGracePeriod = false;
    @Getter
    @Setter
    protected EliteEntity summoningEntity;
    protected List<CustomBossEntity> globalReinforcementEntities = new ArrayList<>();
    protected List<CustomBossEntity> eliteReinforcementEntities = new ArrayList<>();
    //currently used to store ender crystals for the dragon boss fight
    protected List<Entity> nonEliteReinforcementEntities = new ArrayList<>();
    protected boolean bypassesProtections = false;
    protected Double health = null;
    @Setter
    protected Location spawnLocation;
    @Getter
    @Setter
    private boolean dying = false;
    @Getter
    @Setter
    private boolean healing = false;
    //Used by other plugins to tag bosses with custom data
    private final HashMap<NamespacedKey, Object> customData = new HashMap<>();
    private final HashMap<String, Long> sharedCooldowns = new HashMap<>();
    //Owned by EliteMobs. Kept on the actor so mind lifecycle cannot drift into a parallel UUID map.
    private transient EliteMindBinding eliteMindBinding;
    //Same ownership rule for API-registered Lua powers: the actor is the lifecycle source of truth.
    private transient EliteLuaPowerBinding eliteLuaPowerBinding;
    @Getter
    private final ElitePowerSuppression powerSuppression =
            new ElitePowerSuppression(this::onPowerPauseReasonChanged);
    private transient List<Runnable> powerStanceCleanup = new ArrayList<>();
    private transient BukkitTask pendingPowerStanceRefresh;
    private transient long powerStanceGeneration;
    private transient boolean serviceManagedPowerMutation;
    //Native Mind actors are normalized before they become observable through EliteMobSpawnEvent.
    //The service attaches the native Mind between preparation and this one-shot commit.
    private transient boolean preparedMindSpawn;
    private transient boolean preparedMindSpawnAttempted;
    private transient List<Runnable> preparedMindSpawnCleanup;

    /**
     * Functions as a placeholder for {@link CustomBossEntity} that haven't been initialized yet. Uses the builder pattern
     * in order to further initialize values at an arbitrary point in the future.
     * <p>
     * Uses:
     * - {@link CustomEvent} queueing through the {@link CustomSpawn} system
     * <p>
     * {@link EliteEntity} constructed this way must at some point correctly invoke {@link CustomBossEntity#setSpawnLocation(Location)}
     * for the spawn method.
     */
    public EliteEntity() {
    }

    /**
     * This is the generic constructor used in most instances of natural elite mob generation
     */
    public EliteEntity(LivingEntity livingEntity,
                       int level,
                       CreatureSpawnEvent.SpawnReason spawnReason) {
        setLevel(level);
        // Ordinary actors retain the historical event boundary in setLivingEntity. Native Mind
        // actors use the explicit prepare/commit transaction below.
        setLivingEntity(livingEntity, spawnReason);
        if (spawnReason == CreatureSpawnEvent.SpawnReason.NATURAL) {
            isNaturalEntity = true;
        }
        EliteMobProperties eliteMobProperties = EliteMobProperties.getPluginData(livingEntity);
        setDefaultName(eliteMobProperties);
        setArmor();
        setMaxHealth();
        randomizePowers(eliteMobProperties);
    }

    private void prepareNormalizedActor(LivingEntity livingEntity,
                                        int level,
                                        CreatureSpawnEvent.SpawnReason spawnReason,
                                        boolean randomizePowers) {
        setLevel(level);
        normalizeLivingEntity(livingEntity, spawnReason);
        if (spawnReason == CreatureSpawnEvent.SpawnReason.NATURAL) {
            isNaturalEntity = true;
        }
        //Get correct instance of plugin data, necessary for settings names and health among other things
        EliteMobProperties eliteMobProperties = EliteMobProperties.getPluginData(livingEntity);
        NativeMindActorDefaults.requireRandomizedPowerSupport(
                livingEntity.getType(), eliteMobProperties, randomizePowers);
        setDefaultName(eliteMobProperties);
        setArmor();
        setMaxHealth();
        if (randomizePowers) randomizePowers(eliteMobProperties);
    }

    /**
     * Prepares a native Mind actor without publishing {@link com.magmaguy.elitemobs.api.EliteMobSpawnEvent}.
     * The Mind service attaches its native program and then calls {@link #commitPreparedMindSpawn()}.
     */
    void prepareMindActor(LivingEntity livingEntity,
                          int level,
                          CreatureSpawnEvent.SpawnReason spawnReason,
                          boolean randomizePowers) {
        Objects.requireNonNull(livingEntity, "livingEntity");
        Objects.requireNonNull(spawnReason, "spawnReason");
        if (preparedMindSpawn || preparedMindSpawnAttempted || this.livingEntity != null) {
            throw new IllegalStateException("Elite actor was already prepared");
        }
        preparedMindSpawn = true;
        preparedMindSpawnCleanup = new ArrayList<>();
        prepareNormalizedActor(livingEntity, level, spawnReason, randomizePowers);
    }

    /**
     * Publishes a prepared native Mind actor exactly once after its binding is attached.
     *
     * @return true when listeners accepted the actor; false when the spawn event was cancelled
     */
    boolean commitPreparedMindSpawn() {
        if (!preparedMindSpawn) {
            throw new IllegalStateException("Elite actor does not have a prepared native Mind spawn");
        }
        if (preparedMindSpawnAttempted) {
            throw new IllegalStateException("Prepared native Mind spawn was already committed");
        }
        if (eliteMindBinding == null) {
            throw new IllegalStateException("Prepared native Mind spawn has no attached Mind binding");
        }
        preparedMindSpawnAttempted = true;
        boolean accepted = commitNormalizedSpawn();
        if (accepted) {
            preparedMindSpawn = false;
            preparedMindSpawnCleanup = null;
        }
        return accepted;
    }

    /** Cleans a prepared actor that never became an accepted EliteMobs spawn. */
    void rollbackPreparedMindSpawn() {
        EntityTracker.getEliteMobEntities().remove(eliteUUID, this);
        cleanupPreparedMindSpawnEffects();
        preparedMindSpawn = false;
        preparedMindSpawnAttempted = true;
        closeAllPowerRuntimes();
        closePowerSuppression();
        elitePowers.clear();
        clearDamagers();
        if (livingEntity != null && livingEntity.isValid()) livingEntity.remove();
        livingEntity = null;
        unsyncedLivingEntity = null;
        spawnLocation = null;
    }

    EliteMindBinding getEliteMindBinding() {
        return eliteMindBinding;
    }

    void setEliteMindBinding(EliteMindBinding eliteMindBinding) {
        this.eliteMindBinding = eliteMindBinding;
    }

    EliteLuaPowerBinding getEliteLuaPowerBinding() {
        return eliteLuaPowerBinding;
    }

    void setEliteLuaPowerBinding(EliteLuaPowerBinding eliteLuaPowerBinding) {
        this.eliteLuaPowerBinding = eliteLuaPowerBinding;
        if (eliteLuaPowerBinding == null) return;
        for (ElitePowerPauseReason reason : ElitePowerPauseReason.values()) {
            if (powerSuppression.isSuppressed(reason)) {
                eliteLuaPowerBinding.setPauseReason(reason, true);
            }
        }
    }

    boolean isPreparedMindSpawn() {
        return preparedMindSpawn && !preparedMindSpawnAttempted;
    }

    void addPreparedMindSpawnCleanup(Runnable cleanup) {
        Objects.requireNonNull(cleanup, "cleanup");
        if (!isPreparedMindSpawn() || preparedMindSpawnCleanup == null) {
            throw new IllegalStateException("Elite actor is not accepting prepared-spawn cleanup");
        }
        preparedMindSpawnCleanup.add(cleanup);
    }

    /** Reconnects a native body restored within the same server session without replaying spawn. */
    void reattachMindBody(LivingEntity replacement) {
        this.livingEntity = Objects.requireNonNull(replacement, "replacement");
        this.unsyncedLivingEntity = replacement;
        this.entityType = replacement.getType();
        if (!(this instanceof CustomBossEntity)) this.spawnLocation = replacement.getLocation().clone();
        PersistentTagger.tagElite(replacement, eliteUUID);
    }

    boolean suspendMindBodyForChunkUnload(LivingEntity removedBody) {
        if (eliteMindBinding == null) return false;
        if (livingEntity != null
                && removedBody != null
                && !livingEntity.getUniqueId().equals(removedBody.getUniqueId())) return false;
        livingEntity = null;
        unsyncedLivingEntity = null;
        return true;
    }

    /** Terminal removal for a native Mind actor, including one whose body is chunk-unloaded. */
    void terminateMindActor(RemovalReason removalReason) {
        cleanupPreparedMindSpawnEffects();
        EntityTracker.getEliteMobEntities().remove(eliteUUID);
        remove(removalReason);
    }

    private void cleanupPreparedMindSpawnEffects() {
        if (preparedMindSpawnCleanup == null) return;
        List<Runnable> cleanup = preparedMindSpawnCleanup;
        preparedMindSpawnCleanup = null;
        for (int index = cleanup.size() - 1; index >= 0; index--) {
            try {
                cleanup.get(index).run();
            } catch (RuntimeException exception) {
                Logger.warn("Failed to roll back a prepared EliteMobs spawn effect: "
                        + exception.getMessage());
            }
        }
    }

    /**
     * This is used for other plugins to register custom data into a boss for their own tracking
     * @param namespacedKey The key other authors want to use for their data,
     * @param object The object to store. Can be null if you just want to "tag" the entity and check if it has the data later.
     */
    public void addCustomData(@NotNull NamespacedKey namespacedKey, Object object) {
        customData.put(namespacedKey, object);
    }

    /**
     * Removes custom data stored by plugins
     * @param namespacedKey
     */
    public void removeCustomData(NamespacedKey namespacedKey) {
        customData.remove(namespacedKey);
    }

    /**
     * Checks if the specified namespacedKey is present for the boss
     * @param namespacedKey
     * @return
     */
    public boolean hasCustomData(NamespacedKey namespacedKey) {
        return customData.containsKey(namespacedKey);
    }

    /**
     * Returns the custom data the entity has
     * @param namespacedKey
     * @return
     */
    public Object getCustomData(NamespacedKey namespacedKey){
        return customData.get(namespacedKey);
    }

    public Location getSpawnLocation() {
        return spawnLocation.clone();
    }

    public boolean getBypassesProtections() {
        return bypassesProtections;
    }

    public void setBypassesProtections(boolean bypassesProtections) {
        this.bypassesProtections = bypassesProtections;
    }

    /**
     * Elite UUID. This UUID is guaranteed to be final for the Elite Entity, and is not in sync with the {@link LivingEntity} {@link UUID} .
     * <p>
     * Noteworthy uses: {@link EliteEntity} will survive chunk reloads, and in the case of {@link RegionalBossEntity} will survive deaths for the entirety of runtime.
     *
     * @return The final UUID value of this {@link EliteEntity}
     */
    public UUID getEliteUUID() {
        return eliteUUID;
    }

    public void addDamager(Player player, double damage) {
        addDamager(player, damage, null);
    }

    /** Records reward damage and, when known, the weapon skill that actually produced it. */
    public void addDamager(Player player, double damage, SkillType progressionSkill) {
        if (player == null || !Double.isFinite(damage) || damage <= 0) return;

        Player trackedPlayer = findTrackedPlayer(player);
        damagers.merge(trackedPlayer, damage, Double::sum);
        if (progressionSkill != null && progressionSkill.isWeaponSkill())
            skillDamageContributions
                    .computeIfAbsent(player.getUniqueId(), ignored -> new EnumMap<>(SkillType.class))
                    .merge(progressionSkill, damage, Double::sum);

        ElitePlayerInventory inventory = ElitePlayerInventory.getPlayer(player);
        double loudStrikesBonus = inventory == null ? 0D : inventory.getLoudStrikesBonusMultiplier(false);
        if (!Double.isFinite(loudStrikesBonus) || loudStrikesBonus < 0D) loudStrikesBonus = 0D;
        double classThreat = com.magmaguy.elitemobs.experimentalcombat.ExperimentalCombatRuntime.isActive(player)
                && com.magmaguy.elitemobs.experimentalcombat.ExperimentalCombatModule
                .activeClassLineageSnapshot(player.getUniqueId())
                .map(lineage -> lineage.root().id().equals("spellcaster")).orElse(false) ? .8D : 1D;
        aggro.merge(trackedPlayer, damage * (1D + loudStrikesBonus) * classThreat, Double::sum);

        AdvancedAggroManager.updateTarget(this);
    }

    /**
     * Adds combat threat without inventing damage or reward contribution. Class taunts and other
     * semantic threat sources must use this path so targeting and kill-credit accounting cannot
     * silently collapse back into the same number.
     */
    public void addThreat(Player player, double threat) {
        if (player == null || !Double.isFinite(threat) || threat <= 0D) return;
        Player trackedPlayer = findTrackedPlayer(player);
        aggro.merge(trackedPlayer, threat, Double::sum);
        AdvancedAggroManager.updateTarget(this);
    }

    /** Temporarily pins target selection without conflating taunt threat with reward damage. */
    public void forceTarget(Player player, int durationTicks) {
        if (player == null || durationTicks <= 0) return;
        long now = System.nanoTime();
        long duration = durationTicks > Long.MAX_VALUE / 50_000_000L
                ? Long.MAX_VALUE
                : durationTicks * 50_000_000L;
        long expiresAt = duration == Long.MAX_VALUE || Long.MAX_VALUE - now < duration
                ? Long.MAX_VALUE
                : now + duration;
        forcedTargetPlayerId = player.getUniqueId();
        UUID leaseId = UUID.randomUUID();
        forcedTargetLeaseId = leaseId;
        forcedTargetExpiresAtNanos = expiresAt;
        AdvancedAggroManager.updateTarget(this);
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!leaseId.equals(forcedTargetLeaseId)
                        || !player.getUniqueId().equals(forcedTargetPlayerId)) return;
                clearForcedTarget();
                if (isValid()) AdvancedAggroManager.updateTarget(EliteEntity.this);
            }
        }.runTaskLater(MetadataHandler.PLUGIN, durationTicks);
    }

    /** Returns the live lease owner; spatial eligibility remains the aggro manager's concern. */
    public UUID getForcedTargetPlayerId() {
        if (forcedTargetPlayerId != null && forcedTargetExpiresAtNanos <= System.nanoTime())
            clearForcedTarget();
        return forcedTargetPlayerId;
    }

    public void clearForcedTarget(UUID playerId) {
        if (playerId != null && playerId.equals(forcedTargetPlayerId)) clearForcedTarget();
    }

    public void clearForcedTarget() {
        forcedTargetPlayerId = null;
        forcedTargetLeaseId = null;
        forcedTargetExpiresAtNanos = 0L;
    }

    private Player findTrackedPlayer(Player player) {
        for (Player trackedPlayer : damagers.keySet())
            if (trackedPlayer.getUniqueId().equals(player.getUniqueId())) return trackedPlayer;
        for (Player trackedPlayer : aggro.keySet())
            if (trackedPlayer.getUniqueId().equals(player.getUniqueId())) return trackedPlayer;
        return player;
    }

    public boolean hasDamagers() {
        return !damagers.isEmpty();
    }

    public Map<Player, Double> getDamagers() {
        return damagers;
    }

    /** Immutable per-skill damage for one player in this encounter. */
    public Map<SkillType, Double> getSkillDamageContributions(UUID playerId) {
        Map<SkillType, Double> contributions = skillDamageContributions.get(playerId);
        return contributions == null ? Map.of() : Collections.unmodifiableMap(new EnumMap<>(contributions));
    }

    /**
     * Returns the accumulated threat used by elite target selection. The returned map is read-only;
     * callers that need to reset combat state must use {@link #clearDamagers()} so damage and threat
     * cannot drift apart.
     */
    public Map<Player, Double> getAggro() {
        return Collections.unmodifiableMap(aggro);
    }

    /**
     * Copies threat, but not damage/reward contribution, from a summoning elite.
     */
    public void inheritAggroFrom(EliteEntity summoningEntity) {
        if (summoningEntity == null || summoningEntity == this) return;
        clearForcedTarget();
        aggro.clear();
        summoningEntity.aggro.forEach((player, threat) -> {
            if (player != null && threat != null && Double.isFinite(threat) && threat > 0D)
                aggro.put(player, threat);
        });
        AdvancedAggroManager.updateTarget(this);
    }

    /**
     * Clears both reward contribution and combat threat for a fresh encounter.
     */
    public void clearDamagers() {
        damagers.clear();
        aggro.clear();
        skillDamageContributions.clear();
        clearForcedTarget();
    }

    public boolean isCustomBossEntity() {
        return this instanceof CustomBossEntity;
    }

    public boolean isEnderDragon() {
        return EntityType.ENDER_DRAGON.equals(entityType) ||
                livingEntity != null && livingEntity.getType().equals(EntityType.ENDER_DRAGON) ||
                unsyncedLivingEntity != null && unsyncedLivingEntity.getType().equals(EntityType.ENDER_DRAGON);
    }

    /**
     * Returns the {@link LivingEntity} currently being used by the {@link EliteEntity}. Returns null if none is currently active, even
     * if one was active previously.
     *
     * @return Currently active {@link LivingEntity}
     * @see EliteEntity#getUnsyncedLivingEntity() if you want a version that does get nulled once the {@link LivingEntity} stops being valid
     */
    public LivingEntity getLivingEntity() {
        return livingEntity;
    }

    /**
     * This {@link LivingEntity} represents the previous entity spawned by EliteMobs. It is only overwritten when a new one
     * is generated, whereas {@link EliteEntity#getLivingEntity()} returns null as soon as the {@link LivingEntity} stops being alive
     * for safety reasons.
     * <p>
     * This is the method used when operations need to be run on dead instances, such as in {@link com.magmaguy.elitemobs.api.EliteMobDeathEvent}.
     *
     * @return The latest {@link LivingEntity} generated for this {@link EliteEntity}
     */
    public LivingEntity getUnsyncedLivingEntity() {
        return unsyncedLivingEntity;
    }

    public void setUnsyncedLivingEntity(LivingEntity newEntity) {
        unsyncedLivingEntity = newEntity;
    }

    public void setLivingEntity(LivingEntity livingEntity, CreatureSpawnEvent.SpawnReason spawnReason) {
        if (livingEntity == null) return;
        normalizeLivingEntity(livingEntity, spawnReason);
        commitNormalizedSpawn();
    }

    private void normalizeLivingEntity(LivingEntity livingEntity, CreatureSpawnEvent.SpawnReason spawnReason) {
        if (livingEntity == null) return;
        this.removalEventCalled = false;
        this.pendingRemovalEventReason = null;
        this.removalCallDepth = 0;
        if (!(this instanceof CustomBossEntity))
            this.spawnLocation = livingEntity.getLocation().clone();
        this.livingEntity = livingEntity;
        this.unsyncedLivingEntity = livingEntity;
        this.entityType = livingEntity.getType();

        this.livingEntity.setCanPickupItems(false);
        if (livingEntity.getEquipment() != null) {
            livingEntity.getEquipment().setItemInMainHandDropChance(0);
            livingEntity.getEquipment().setItemInOffHandDropChance(0);
            livingEntity.getEquipment().setHelmetDropChance(0);
            livingEntity.getEquipment().setChestplateDropChance(0);
            livingEntity.getEquipment().setLeggingsDropChance(0);
            livingEntity.getEquipment().setBootsDropChance(0);
        }

        if (livingEntity.getType().equals(EntityType.RABBIT)) {
            ((Rabbit) livingEntity).setRabbitType(Rabbit.Type.THE_KILLER_BUNNY);
        }

        if (entityType.equals(EntityType.WOLF)) {
            Wolf wolf = (Wolf) livingEntity;
            wolf.setAngry(true);
            wolf.setBreed(false);
            KeepNeutralsAngry.showMeYouWarFace(this);
        }

        if (entityType.equals(EntityType.POLAR_BEAR)) {
            KeepNeutralsAngry.showMeYouWarFace(this);
        }

        if (entityType.equals(EntityType.ENDER_DRAGON))
            if (((EnderDragon) livingEntity).getBossBar() != null)
                ((EnderDragon) livingEntity).getBossBar().setTitle(getName());

        if (entityType.equals(EntityType.LLAMA)) {
            KeepNeutralsAngry.showMeYouWarFace(this);
        }

        if (entityType.equals(EntityType.IRON_GOLEM) && this instanceof CustomBossEntity)
            KeepNeutralsAngry.showMeYouWarFace(this);

        if (entityType.equals(EntityType.GOAT)) {
            ((Goat) livingEntity).setScreaming(true);
        }

        if (livingEntity instanceof Bee) {
            KeepNeutralsAngry.showMeYouWarFace(this);
            ((Bee) livingEntity).setCannotEnterHiveTicks(Integer.MAX_VALUE);
        }

        if (livingEntity instanceof Wither wither)
            wither.getBossBar().setVisible(false);

        this.spawnReason = spawnReason;

        //This sets whether the entity gets despawned when beyond a certain distance from the player, should only happen
        //for entities which aren't pseudo-persistent
        this.getLivingEntity().setRemoveWhenFarAway(!isPersistent);
        this.getLivingEntity().setPersistent(false);

        setMaxHealth();

        if (getName() == null)
            setDefaultName(EliteMobProperties.getPluginData(entityType));

        this.name = livingEntity.getCustomName();
    }

    private boolean commitNormalizedSpawn() {
        if (livingEntity == null) {
            throw new IllegalStateException("Cannot commit an EliteEntity without a living entity");
        }
        // Preserve the historical event-time marker contract. EntityTracker writes the same tag
        // again after listeners accept the spawn and only then publishes the actor in its map.
        PersistentTagger.tagElite(livingEntity, eliteUUID);
        EntityTracker.registerEliteMob(this);
        if (EntityTracker.getEliteMobEntities().get(eliteUUID) != this) return false;
        if (preparedMindSpawn
                && (eliteMindBinding == null || livingEntity == null || !livingEntity.isValid())) {
            EntityTracker.getEliteMobEntities().remove(eliteUUID, this);
            throw new IllegalStateException("Native Mind actor was removed during EliteMobSpawnEvent");
        }
        AdvancedAggroManager.updateTarget(this);
        return true;
    }

    public void setNameVisible(boolean isVisible) {
        //Check if the boss is already dead
        if (livingEntity == null) return;
        livingEntity.setCustomNameVisible(isVisible);
    }

    public void setMaxHealth() {
        EliteMobProperties properties = EliteMobProperties.getPluginData(entityType);
        double nativeBaseHealth = livingEntity == null
                ? Double.NaN
                : AttributeManager.getAttributeBaseValue(livingEntity, "generic_max_health");
        this.defaultMaxHealth = NativeMindActorDefaults.baseHealth(properties, nativeBaseHealth);
        // Use exponential HP scaling: +5 levels = 2x HP, -5 levels = 0.5x HP
        // This replaces the old damage modifier system for a better player experience
        double calculatedHealth = LevelScaling.calculateMobHealth(level, this.defaultMaxHealth);
        calculatedHealth = NaturalEliteCombatTweak.getTweakedMobHealth(this, level, calculatedHealth);
        this.maxHealth = calculateSafeMaxHealth(calculatedHealth);
        if (livingEntity != null) AttributeManager.setAttribute(livingEntity, "generic_max_health", maxHealth);
        if (health == null) {
            if (livingEntity != null) livingEntity.setHealth(maxHealth);
            this.health = maxHealth;
        }
        //This is useful for phase boss entities that spawn in unloaded chunks and shouldn't full heal between phases, like in dungeons
        else if (livingEntity != null)
            livingEntity.setHealth(Math.min(health, AttributeManager.getAttributeBaseValue(livingEntity, "generic_max_health")));
    }

    public void setNormalizedMaxHealth() {
        this.defaultMaxHealth = MobCombatSettingsConfig.getNormalizedBaselineHealth();
        // Use exponential HP scaling for normalized combat too
        this.maxHealth = calculateSafeMaxHealth(LevelScaling.calculateMobHealth(level, this.defaultMaxHealth));
        if (livingEntity != null) {
            AttributeManager.setAttribute(livingEntity, "generic_max_health", maxHealth);
            livingEntity.setHealth(maxHealth);
        }
        this.health = maxHealth;
    }

    private double calculateSafeMaxHealth(double calculatedHealth) {
        double safeCalculatedHealth = calculatedHealth;
        if (!Double.isFinite(safeCalculatedHealth) || safeCalculatedHealth <= 0D) {
            Logger.warn("EliteMobs calculated invalid base health " + calculatedHealth + " for " + entityType + " level " + level + ". Falling back to level 1 health.");
            safeCalculatedHealth = LevelScaling.calculateMobHealth(1, this.defaultMaxHealth);
        }

        double safeHealthMultiplier = healthMultiplier;
        if (!Double.isFinite(safeHealthMultiplier) || safeHealthMultiplier <= 0D) {
            Logger.warn("EliteMobs found invalid health multiplier " + healthMultiplier + " for " + entityType + " level " + level + ". Falling back to 1.0.");
            safeHealthMultiplier = 1D;
        }

        double minecraftMaxHealth = LevelScaling.getMinecraftMaxHealth();
        double multipliedHealth = safeCalculatedHealth * safeHealthMultiplier;
        if (!Double.isFinite(multipliedHealth) || multipliedHealth > minecraftMaxHealth) {
            Logger.warn("EliteMobs calculated max health " + multipliedHealth + " for " + entityType + " level " + level + " with health multiplier " + safeHealthMultiplier + ". Capping it to " + minecraftMaxHealth + " to avoid invalid health values.");
            return minecraftMaxHealth;
        }

        return Math.max(1D, multipliedHealth);
    }

    public void resetMaxHealth() {
        AttributeManager.setAttribute(livingEntity, "generic_max_health", maxHealth);
        livingEntity.setHealth(maxHealth);
        this.health = maxHealth;
    }

    /**
     * Health is cached by EliteMobs for when health needs to be displayed when the {@link LivingEntity} isn't valid, but
     * return the field from the {@link LivingEntity} when available
     *
     * @return Boss health
     */
    public double getHealth() {
        if (livingEntity != null)
            return livingEntity.getHealth();
        else if (this.health != null)
            return this.health;
        else {
            setMaxHealth();
            return this.health;
        }
    }

    public void setHealth(double health) {
        if (livingEntity == null) return;
        this.health = Math.min(health, Math.min(this.maxHealth, AttributeManager.getAttributeBaseValue(livingEntity, "generic_max_health")));
        livingEntity.setHealth(this.health);
    }

    public void syncPluginHealth(double health) {
        this.health = health;
    }

    public void heal(double healAmount) {
        EliteMobHealEvent eliteMobHealEvent = new EliteMobHealEvent(this, healAmount);
        new EventCaller(eliteMobHealEvent);
        if (eliteMobHealEvent.isCancelled()) return;
        // The cached value can still be the pre-hit health until the next sync.
        setHealth(getHealth() + healAmount);
    }

    public void fullHeal() {
        EliteMobHealEvent eliteMobHealEvent = new EliteMobHealEvent(this, true);
        new EventCaller(eliteMobHealEvent);
        if (eliteMobHealEvent.isCancelled()) return;
        setHealth(this.maxHealth);
        this.health = maxHealth;
        clearDamagers();
    }

    private void setArmor() {
        if (!MobCombatSettingsConfig.isDoEliteArmor()) return;
        if (livingEntity.getEquipment() == null) return;

        if (!(livingEntity instanceof Zombie || livingEntity instanceof PigZombie ||
                livingEntity instanceof Skeleton || livingEntity instanceof WitherSkeleton)) return;

        livingEntity.getEquipment().setBoots(new ItemStack(Material.AIR));
        livingEntity.getEquipment().setLeggings(new ItemStack(Material.AIR));
        livingEntity.getEquipment().setChestplate(new ItemStack(Material.AIR));
        livingEntity.getEquipment().setHelmet(new ItemStack(Material.AIR));

        if (level >= 5 && MobCombatSettingsConfig.isDoEliteHelmets())
            livingEntity.getEquipment().setHelmet(new ItemStack(Material.LEATHER_HELMET));

        if (level >= 10) livingEntity.getEquipment().setBoots(new ItemStack(Material.LEATHER_BOOTS));
        if (level >= 15) livingEntity.getEquipment().setLeggings(new ItemStack(Material.LEATHER_LEGGINGS));
        if (level >= 20) livingEntity.getEquipment().setChestplate(new ItemStack(Material.LEATHER_CHESTPLATE));
        if (level >= 25 && MobCombatSettingsConfig.isDoEliteHelmets())
            livingEntity.getEquipment().setHelmet(new ItemStack(Material.CHAINMAIL_HELMET));
        if (level >= 30) livingEntity.getEquipment().setBoots(new ItemStack(Material.CHAINMAIL_BOOTS));
        if (level >= 35) livingEntity.getEquipment().setLeggings(new ItemStack(Material.CHAINMAIL_LEGGINGS));
        if (level >= 40) livingEntity.getEquipment().setChestplate(new ItemStack(Material.CHAINMAIL_CHESTPLATE));
        if (level >= 45 && MobCombatSettingsConfig.isDoEliteHelmets())
            livingEntity.getEquipment().setHelmet(new ItemStack(Material.IRON_HELMET));
        if (level >= 50) livingEntity.getEquipment().setBoots(new ItemStack(Material.IRON_BOOTS));
        if (level >= 55) livingEntity.getEquipment().setLeggings(new ItemStack(Material.IRON_LEGGINGS));
        if (level >= 60) livingEntity.getEquipment().setChestplate(new ItemStack(Material.IRON_CHESTPLATE));
        if (level >= 65) livingEntity.getEquipment().setBoots(new ItemStack(Material.DIAMOND_BOOTS));
        if (level >= 70 && MobCombatSettingsConfig.isDoEliteHelmets())
            livingEntity.getEquipment().setHelmet(new ItemStack(Material.DIAMOND_HELMET));
        if (level >= 75) livingEntity.getEquipment().setLeggings(new ItemStack(Material.DIAMOND_LEGGINGS));
        if (level >= 80) livingEntity.getEquipment().setChestplate(new ItemStack(Material.DIAMOND_CHESTPLATE));

        if (livingEntity.getEquipment().getHelmet() != null) {
            ItemMeta helmetMeta = livingEntity.getEquipment().getHelmet().getItemMeta();
            if (helmetMeta != null) {
                helmetMeta.setUnbreakable(true);
                livingEntity.getEquipment().getHelmet().setItemMeta(helmetMeta);
            }
        }
    }

    public void randomizePowers(EliteMobProperties eliteMobProperties) {

        if (level < 1) return;

        int availableDefensivePowers = 0;
        int availableOffensivePowers = 0;
        int availableMiscellaneousPowers = 0;
        int availableMajorPowers = 0;

        if (level >= 10) availableDefensivePowers = 1;
        if (level >= 20) availableOffensivePowers = 1;
        if (level >= 30) availableMiscellaneousPowers = 1;
        if (level >= 40) availableMajorPowers = 1;
        if (level >= 50) availableDefensivePowers = 2;
        if (level >= 60) availableOffensivePowers = 2;
        if (level >= 70) availableMiscellaneousPowers = 2;
        if (level >= 80) availableMajorPowers = 2;

        //apply defensive powers
        applyPowers((HashSet<PowersConfigFields>) eliteMobProperties.getValidDefensivePowers().clone(), availableDefensivePowers);

        //apply offensive powers
        applyPowers((HashSet<PowersConfigFields>) eliteMobProperties.getValidOffensivePowers().clone(), availableOffensivePowers);

        //apply miscellaneous powers
        applyPowers((HashSet<PowersConfigFields>) eliteMobProperties.getValidMiscellaneousPowers().clone(), availableMiscellaneousPowers);

        //apply major powers
        applyPowers((HashSet<PowersConfigFields>) eliteMobProperties.getValidMajorPowers().clone(), availableMajorPowers);

        initializePowerStances();

    }

    public void applyPowers(HashSet<PowersConfigFields> configFields, int availablePowerAmount) {
        configFields.removeIf(iteratedField -> !iteratedField.isEnabled());

        if (availablePowerAmount < 1) return;

        ArrayList<PowersConfigFields> localFields = new ArrayList<>(configFields);

        for (ElitePower mobPower : this.elitePowers)
            localFields.remove(mobPower);

        for (int i = 0; i < availablePowerAmount; i++)
            if (localFields.size() < 1)
                break;
            else {
                PowersConfigFields selectedField = localFields.get(ThreadLocalRandom.current().nextInt(localFields.size()));
                try {
                    ElitePower.addPower(this, selectedField);
                    localFields.remove(selectedField);
                    countPower(selectedField);
                } catch (Exception ex) {
                    Logger.warn("Failed to instance new power!");
                }
            }

    }

    /**
     * Applies an explicit set of powers, as used by {@code /em spawn elite <type> <level> <power>}. The minor and major
     * power counters get incremented here just like they do in {@link #randomizePowers(EliteMobProperties)}, otherwise
     * the power stance rings would size themselves to zero powers and never render.
     *
     * @param powersConfigFields The powers to apply
     */
    public void applyPowers(HashSet<PowersConfigFields> powersConfigFields) {
        powersConfigFields.forEach(field -> {
            ElitePower.addPower(this, field);
            countPower(field);
        });

        initializePowerStances();
    }

    private void countPower(PowersConfigFields powersConfigFields) {
        if (PowersConfigFields.isMajorPowerType(powersConfigFields.getPowerType()))
            this.majorPowerCount++;
        else
            this.minorPowerCount++;
    }

    public void setElitePowers(Collection<ElitePower> elitePowers) {
        this.elitePowers.clear();
        this.elitePowers.addAll(elitePowers);
    }

    List<ElitePower> copyElitePowersInAttachmentOrder() {
        return List.copyOf(elitePowers);
    }

    void replaceElitePowersInOrder(Collection<? extends ElitePower> replacement) {
        Objects.requireNonNull(replacement, "replacement");
        boolean previousMutationState = serviceManagedPowerMutation;
        serviceManagedPowerMutation = true;
        try {
            elitePowers.clear();
            elitePowers.addAll(replacement);
        } finally {
            serviceManagedPowerMutation = previousMutationState;
        }
        minorPowerCount = 0;
        majorPowerCount = 0;
        for (ElitePower power : replacement) {
            if (PowersConfigFields.isMajorPowerType(power.getPowerType())) majorPowerCount++;
            else minorPowerCount++;
        }
    }

    void refreshPowerStances() {
        initializePowerStances();
    }

    /**
     * Moves the legacy obfuscation transition back through the actor-owned stance lifecycle.
     * A generation token prevents a next-tick callback from resurrecting visuals after unload,
     * removal, or a power-loadout replacement.
     */
    public void requestPowerStanceRefreshAfterObfuscation() {
        if (livingEntity == null || pendingPowerStanceRefresh != null) return;
        long scheduledGeneration = powerStanceGeneration;
        pendingPowerStanceRefresh = new BukkitRunnable() {
            @Override
            public void run() {
                if (scheduledGeneration != powerStanceGeneration) return;
                pendingPowerStanceRefresh = null;
                visualEffectObfuscated = false;
                refreshPowerStances();
            }
        }.runTask(MetadataHandler.PLUGIN);
    }

    void suspendPowerStances() {
        closePowerStances();
    }

    void suspendUnmanagedPowerRuntimes() {
        closeAllPowerRuntimes();
    }

    void resumeUnmanagedPowerRuntimes() {
        try {
            for (ElitePower elitePower : elitePowers) {
                if (elitePower instanceof LuaElitePower luaElitePower) {
                    luaElitePower.startRuntimeOrThrow();
                } else if (elitePower instanceof EliteScript eliteScript) {
                    eliteScript.initializeCustomEvents(this);
                }
            }
        } catch (RuntimeException exception) {
            closeAllPowerRuntimes();
            throw exception;
        }
    }

    private void initializePowerStances() {
        closePowerStances();
        ArrayList<Runnable> cleanup = new ArrayList<>();
        powerStanceCleanup = cleanup;
        Consumer<Runnable> cleanupRegistrar = cleanup::add;
        try {
            new MinorPowerPowerStance(this, cleanupRegistrar);
            new MajorPowerPowerStance(this, cleanupRegistrar);
        } catch (RuntimeException exception) {
            closePowerStancesIfCurrent(cleanup);
            throw exception;
        }
        if (isPreparedMindSpawn()) {
            addPreparedMindSpawnCleanup(() -> closePowerStancesIfCurrent(cleanup));
        }
    }

    private void closePowerStances() {
        closePowerStancesIfCurrent(powerStanceCleanup);
    }

    private void closePowerStancesIfCurrent(List<Runnable> expectedCleanup) {
        if (powerStanceCleanup != expectedCleanup) return;
        powerStanceGeneration++;
        if (pendingPowerStanceRefresh != null) {
            pendingPowerStanceRefresh.cancel();
            pendingPowerStanceRefresh = null;
        }
        List<Runnable> cleanup = powerStanceCleanup;
        powerStanceCleanup = new ArrayList<>();
        for (int index = cleanup.size() - 1; index >= 0; index--) {
            try {
                cleanup.get(index).run();
            } catch (RuntimeException exception) {
                Logger.warn("Failed to close an EliteMobs power stance: " + exception.getMessage());
            }
        }
        minorVisualEffect = false;
        majorVisualEffect = false;
    }

    public List<ElitePower> getElitePowersInExecutionOrder() {
        if (orderedElitePowers == null) {
            orderedElitePowers = PowerExecutionOrder.ordered(elitePowers);
        }
        return orderedElitePowers;
    }

    public boolean hasPower(ElitePower mobPower) {
        for (ElitePower elitePower : elitePowers)
            if (elitePower.getPowersConfigFields().equals(mobPower.getPowersConfigFields()))
                return true;
        return false;
    }

    public boolean hasPower(PowersConfigFields mobPower) {
        for (ElitePower elitePower : elitePowers)
            if (elitePower.getPowersConfigFields().equals(mobPower))
                return true;
        return false;
    }

    public ElitePower getPower(ElitePower elitePower) {
        for (ElitePower iteratedPower : getElitePowers())
            if (iteratedPower.getClass().equals(elitePower.getClass()))
                return iteratedPower;
        return null;
    }

    public ElitePower getPower(String elitePower) {
        for (ElitePower iteratedPower : getElitePowers())
            if (iteratedPower.getFileName().equals(elitePower))
                return iteratedPower;
        return null;
    }

    public void setName(EliteMobProperties eliteMobProperties) {
        this.name = ChatColorConverter.convert(
                MobLevelPlaceholderFormatter.replaceLevelPlaceholders(
                        eliteMobProperties.getName(), this, level));
        livingEntity.setCustomName(this.name);
        livingEntity.setCustomNameVisible(DefaultConfig.isAlwaysShowNametags());
    }

    private void setDefaultName(EliteMobProperties eliteMobProperties) {
        if (eliteMobProperties != null) {
            setName(eliteMobProperties);
            return;
        }
        this.name = ChatColorConverter.convert(
                MobLevelPlaceholderFormatter.replaceLevelPlaceholders(
                        NativeMindActorDefaults.nameTemplate(entityType), this, level));
        livingEntity.setCustomName(this.name);
        livingEntity.setCustomNameVisible(DefaultConfig.isAlwaysShowNametags());
    }

    public void setName(String name, boolean applyToLivingEntity) {
        this.name = name;
        //This is necessary for the custom boss mega consumer
        if (applyToLivingEntity)
            livingEntity.setCustomName(name);
    }

    public void setPersistent(boolean bool) {
        this.isPersistent = bool;
        if (livingEntity != null)
            livingEntity.setRemoveWhenFarAway(!isPersistent);
    }

    public void doCooldown() {
        setInCooldown(true);
        new BukkitRunnable() {
            @Override
            public void run() {
                setInCooldown(false);
            }
        }.runTaskLater(MetadataHandler.PLUGIN, 20 * 15);
    }

    public void doGlobalPowerCooldown(int ticks) {
        setInCooldown(true);
        new BukkitRunnable() {
            @Override
            public void run() {
                setInCooldown(false);
            }
        }.runTaskLater(MetadataHandler.PLUGIN, ticks);
    }

    public void setTriggeredAntiExploit(boolean triggeredAntiExploit) {
        if (triggeredAntiExploit && isEnderDragon()) return;
        this.triggeredAntiExploit = triggeredAntiExploit;
        if (triggeredAntiExploit) {
            this.eliteLoot = false;
            this.vanillaLoot = false;
        }
    }

    private void invalidateElitePowerOrder() {
        orderedElitePowers = null;
    }

    private final class TrackedElitePowerSet extends LinkedHashSet<ElitePower> {
        @Override
        public boolean add(ElitePower elitePower) {
            requirePowerMutationAllowed();
            boolean changed = super.add(elitePower);
            if (changed) {
                applyPowerPauseState(elitePower);
                invalidateElitePowerOrder();
            }
            return changed;
        }

        @Override
        public boolean addAll(Collection<? extends ElitePower> collection) {
            requirePowerMutationAllowed();
            boolean changed = super.addAll(collection);
            if (changed) {
                collection.forEach(EliteEntity.this::applyPowerPauseState);
                invalidateElitePowerOrder();
            }
            return changed;
        }

        @Override
        public boolean remove(Object object) {
            requirePowerMutationAllowed();
            boolean changed = super.remove(object);
            if (changed) invalidateElitePowerOrder();
            return changed;
        }

        @Override
        public boolean removeAll(Collection<?> collection) {
            requirePowerMutationAllowed();
            boolean changed = super.removeAll(collection);
            if (changed) invalidateElitePowerOrder();
            return changed;
        }

        @Override
        public boolean retainAll(Collection<?> collection) {
            requirePowerMutationAllowed();
            boolean changed = super.retainAll(collection);
            if (changed) invalidateElitePowerOrder();
            return changed;
        }

        @Override
        public void clear() {
            requirePowerMutationAllowed();
            if (isEmpty()) {
                return;
            }
            super.clear();
            invalidateElitePowerOrder();
        }

        @Override
        public Iterator<ElitePower> iterator() {
            Iterator<ElitePower> delegate = super.iterator();
            return new Iterator<>() {
                @Override
                public boolean hasNext() {
                    return delegate.hasNext();
                }

                @Override
                public ElitePower next() {
                    return delegate.next();
                }

                @Override
                public void remove() {
                    requirePowerMutationAllowed();
                    delegate.remove();
                    invalidateElitePowerOrder();
                }
            };
        }

        private void requirePowerMutationAllowed() {
            if (!serviceManagedPowerMutation && eliteLuaPowerBinding != null) {
                throw new IllegalStateException(
                        "Service-managed Lua powers must be replaced through EliteLuaPowerService");
            }
        }
    }

    public void incrementAntiExploit(int value, String cause) {
        if (isEnderDragon()) return;
        antiExploitPoints += value;
        if (antiExploitPoints > AntiExploitConfig.getAntiExploitThreshold()) {
            setTriggeredAntiExploit(true);
            AntiExploitMessage.sendWarning(livingEntity, cause);
        }
    }

    public void decrementAntiExploit(int value) {
        if (isEnderDragon()) return;
        antiExploitPoints -= value;
    }

    public void setInAntiExploitCooldown() {
        if (isEnderDragon()) return;
        this.inAntiExploitCooldown = true;
        new BukkitRunnable() {
            @Override
            public void run() {
                inAntiExploitCooldown = false;
            }
        }.runTaskLater(MetadataHandler.PLUGIN, 20);
    }

    public void setCombatGracePeriod(int delayInTicks) {
        this.inCombatGracePeriod = true;
        new BukkitRunnable() {
            @Override
            public void run() {
                inCombatGracePeriod = false;
            }
        }.runTaskLater(MetadataHandler.PLUGIN, delayInTicks);
    }

    public void addGlobalReinforcement(CustomBossEntity customBossEntity) {
        this.globalReinforcementEntities.add(customBossEntity);
        if (customBossEntity.summoningEntity == null) customBossEntity.setSummoningEntity(this);
    }

    public void addReinforcement(CustomBossEntity customBossEntity) {
        this.eliteReinforcementEntities.add(customBossEntity);
        if (customBossEntity.summoningEntity == null) customBossEntity.setSummoningEntity(this);
    }

    public void addReinforcement(Entity entity) {
        nonEliteReinforcementEntities.removeIf(existing -> !existing.isValid());
        this.nonEliteReinforcementEntities.add(entity);
    }

    /**
     * Whether this entity is a boss's reinforcement (summoned by another elite) or a boss's mount.
     * <p>
     * Such entities must never award skill XP or coins. Dungeon lockout only applies to the
     * {@link com.magmaguy.elitemobs.mobconstructor.custombosses.InstancedBossEntity} itself, so its
     * mount and summoned adds — which are plain {@link CustomBossEntity}s, not InstancedBossEntities —
     * would otherwise keep paying out on every kill regardless of the player's cooldown. GLOBAL
     * reinforcements additionally respawn on a timer, making them an uncapped farm. Reinforcements are
     * identified via {@link #summoningEntity} (set on every summon path, including the back-link added
     * in {@link #addReinforcement(CustomBossEntity)} and {@link #addGlobalReinforcement(CustomBossEntity)});
     * mounts via {@link CustomBossEntity#isMount()}.
     */
    public boolean isReinforcementOrMount() {
        return summoningEntity != null || (this instanceof CustomBossEntity customBossEntity && customBossEntity.isMount());
    }

    /**
     * Returns true if the {@link LivingEntity} is exists and loaded.
     *
     * @return Whether the EliteEntity is currently valid.
     */
    public boolean isValid() {
        if (livingEntity == null) return false;
        return livingEntity.isValid();
    }

    public boolean exists() {
        if (livingEntity == null) return false;
        return !livingEntity.isDead();
    }

    public Location getLocation() {
        if (livingEntity != null) return livingEntity.getLocation();
        if (unsyncedLivingEntity != null) return unsyncedLivingEntity.getLocation();
        return null;
    }

    public void remove(RemovalReason removalReason) {
        beginRemovalCall();
        try {
            closePowerStances();
            closeAllPowerRuntimes();
            closeEliteLuaPowerBinding();
            closePowerSuppression();
            //This prevents the entity tracker from running this code twice when removing due to specific reasons
            //Custom bosses have their own tracking removal rules
            if (livingEntity != null && (!(this instanceof CustomBossEntity)))
                EntityTracker.getEliteMobEntities().remove(eliteUUID);
            //LibsDisguises' registry holds a hard reference to disguised entities and does
            //not reliably release it when the entity or its world goes away, pinning
            //instanced-world ServerLevels in memory. On death the undisguise is delayed
            //so the death animation still plays on the disguised form.
            if (livingEntity != null && org.bukkit.Bukkit.getPluginManager().isPluginEnabled("LibsDisguises")) {
                if (removalReason.equals(RemovalReason.DEATH)) {
                    org.bukkit.entity.LivingEntity disguisedEntity = livingEntity;
                    org.bukkit.Bukkit.getScheduler().runTaskLater(MetadataHandler.PLUGIN,
                            () -> com.magmaguy.elitemobs.thirdparty.libsdisguises.DisguiseEntity.undisguise(disguisedEntity), 60L);
                } else {
                    com.magmaguy.elitemobs.thirdparty.libsdisguises.DisguiseEntity.undisguise(livingEntity);
                }
            }
            if (livingEntity != null && !removalReason.equals(RemovalReason.DEATH))
                livingEntity.remove();
            if (livingEntity instanceof EnderDragon enderDragon && removalReason.equals(RemovalReason.DEATH)) {
                enderDragon.setPhase(EnderDragon.Phase.DYING);
                if (enderDragon.getDragonBattle() != null)
                    enderDragon.getDragonBattle().generateEndPortal(false);
            }
            this.livingEntity = null;
            releaseWorldReferences(removalReason);
            //Custom bosses finish additional persistent/model/tracking cleanup in
            //their override before publishing the terminal removal event.
            if (!(this instanceof CustomBossEntity))
                callRemoveEventOnce(removalReason);
        } finally {
            finishRemovalCall();
        }
    }

    private void closeEliteLuaPowerBinding() {
        EliteLuaPowerBinding binding = eliteLuaPowerBinding;
        if (binding == null) return;
        eliteLuaPowerBinding = null;
        try {
            binding.close();
        } catch (RuntimeException exception) {
            Logger.warn("Failed to close an EliteMobs Lua power binding: "
                    + exception.getMessage());
        }
    }

    private void closeAllPowerRuntimes() {
        for (ElitePower elitePower : elitePowers) {
            try {
                elitePower.closeRuntime();
            } catch (RuntimeException exception) {
                Logger.warn("Failed to close an EliteMobs power runtime: "
                        + exception.getMessage());
            }
        }
    }

    private void onPowerPauseReasonChanged(ElitePowerPauseReason reason, boolean paused) {
        EliteLuaPowerBinding binding = eliteLuaPowerBinding;
        if (binding != null) {
            try {
                binding.setPauseReason(reason, paused);
            } catch (RuntimeException exception) {
                Logger.warn("Failed to update an EliteMobs Lua power binding pause state: "
                        + exception.getMessage());
            }
        }
        for (ElitePower elitePower : elitePowers) {
            if (elitePower instanceof LuaElitePower luaElitePower) {
                try {
                    luaElitePower.setRuntimePauseReason(reason, paused);
                } catch (RuntimeException exception) {
                    Logger.warn("Failed to update an EliteMobs Lua power pause state: "
                            + exception.getMessage());
                }
            }
        }
    }

    private void applyPowerPauseState(ElitePower elitePower) {
        if (!(elitePower instanceof LuaElitePower luaElitePower)) return;
        for (ElitePowerPauseReason reason : ElitePowerPauseReason.values()) {
            if (powerSuppression.isSuppressed(reason)) {
                luaElitePower.setRuntimePauseReason(reason, true);
            }
        }
    }

    private void closePowerSuppression() {
        try {
            powerSuppression.close();
        } catch (RuntimeException exception) {
            Logger.warn("Failed to close Elite power suppression: " + exception.getMessage());
        }
    }

    /**
     * Drops the references that keep a gone world reachable: the discarded NMS entity behind
     * unsyncedLivingEntity (whose .level pins an entire ServerLevel) and the tracked
     * damager/threat players. Elites stay in static tracking maps well past removal — regional
     * bosses intentionally forever — so without this every retained elite pinned the world its
     * last living entity lived in, which is how completed instanced dungeons accumulated in
     * memory. Deliberately limited to the reasons where the world itself is going away: a dead
     * entity in a still-loaded world pins nothing extra, and death/combat listeners (loot
     * location reads, exit-combat watchdogs, delayed undisguises) may still need the reference.
     */
    private void releaseWorldReferences(RemovalReason removalReason) {
        if (!removalReason.equals(RemovalReason.WORLD_UNLOAD)
                && !removalReason.equals(RemovalReason.SHUTDOWN)) return;
        unsyncedLivingEntity = null;
        clearDamagers();
    }

    /**
     * @return whether any reference this elite still holds (living entity, spawn location) points
     * at the given world — used to purge tracking when that world unloads
     */
    public boolean referencesWorld(org.bukkit.World world) {
        if (world == null) return false;
        if (unsyncedLivingEntity != null && world.equals(unsyncedLivingEntity.getWorld())) return true;
        return spawnLocation != null && world.equals(spawnLocation.getWorld());
    }

    /**
     * Publishes the terminal removal notification at most once. Removal can be
     * reached through overlapping Bukkit death, tracker, shutdown, and custom
     * boss cleanup paths, so the guard is set before dispatch to remain safe if
     * a listener attempts another removal.
     */
    protected final void callRemoveEventOnce(RemovalReason removalReason) {
        if (removalEventCalled) return;
        pendingRemovalEventReason = removalReason;
        if (removalCallDepth > 0) return;
        publishPendingRemovalEvent();
    }

    protected final void beginRemovalCall() {
        removalCallDepth++;
    }

    protected final void finishRemovalCall() {
        if (removalCallDepth <= 0) return;
        removalCallDepth--;
        if (removalCallDepth == 0) publishPendingRemovalEvent();
    }

    private void publishPendingRemovalEvent() {
        if (removalEventCalled || pendingRemovalEventReason == null) return;
        RemovalReason removalReason = pendingRemovalEventReason;
        pendingRemovalEventReason = null;
        removalEventCalled = true;
        new EventCaller(new EliteMobRemoveEvent(this, removalReason));
    }

    public void removeReinforcement(CustomBossEntity customBossEntity) {
        eliteReinforcementEntities.remove(customBossEntity);
        globalReinforcementEntities.remove(customBossEntity);
    }

    public int getGlobalReinforcementsCount() {
        return this.globalReinforcementEntities.size();
    }

    public HashSet<String> getTags() {
        return customMetadata;
    }

    public boolean hasTag(String tag) {
        return customMetadata.contains(tag);
    }

    public void addTag(String tag) {
        customMetadata.add(tag);
    }

    public void addTags(List<String> tags) {
        customMetadata.addAll(tags);
    }

    public void removeTag(String tag) {
        customMetadata.remove(tag);
    }

    public void removeTags(List<String> tags) {
        customMetadata.removeAll(tags);
    }

    public boolean isSharedCooldownReady(String key) {
        cleanupSharedCooldown(key);
        return !sharedCooldowns.containsKey(key);
    }

    public long getSharedCooldownRemainingTicks(String key) {
        cleanupSharedCooldown(key);
        Long expiresAt = sharedCooldowns.get(key);
        if (expiresAt == null) {
            return 0L;
        }
        long remainingNanos = expiresAt - System.nanoTime();
        if (remainingNanos <= 0) {
            sharedCooldowns.remove(key);
            return 0L;
        }
        return Math.max(1L, remainingNanos / 50_000_000L);
    }

    public void setSharedCooldown(String key, long ticks) {
        if (ticks <= 0) {
            sharedCooldowns.remove(key);
            return;
        }
        sharedCooldowns.put(key, System.nanoTime() + ticks * 50_000_000L);
    }

    private void cleanupSharedCooldown(String key) {
        Long expiresAt = sharedCooldowns.get(key);
        if (expiresAt != null && expiresAt <= System.nanoTime()) {
            sharedCooldowns.remove(key);
        }
    }

}
