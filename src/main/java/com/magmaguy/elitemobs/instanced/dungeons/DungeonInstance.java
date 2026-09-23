package com.magmaguy.elitemobs.instanced.dungeons;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.DungeonCompleteEvent;
import com.magmaguy.elitemobs.api.DungeonStartEvent;
import com.magmaguy.elitemobs.api.InstancedDungeonRemoveEvent;
import com.magmaguy.elitemobs.api.WorldInstanceEvent;
import com.magmaguy.elitemobs.api.WorldUninstanceEvent;
import com.magmaguy.elitemobs.api.internal.RemovalReason;
import com.magmaguy.elitemobs.config.DungeonsConfig;
import com.magmaguy.elitemobs.config.PartyConfig;
import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import com.magmaguy.elitemobs.config.contentpackages.ContentPackagesConfig;
import com.magmaguy.elitemobs.config.contentpackages.ContentPackagesConfigFields;
import com.magmaguy.elitemobs.dungeons.EliteMobsWorld;
import com.magmaguy.elitemobs.dungeons.utility.DungeonUtils;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.instanced.InstancePlayerManager;
import com.magmaguy.elitemobs.instanced.MatchInstance;
import com.magmaguy.elitemobs.instanced.WorldOperationQueue;
import com.magmaguy.elitemobs.mobconstructor.PersistentObjectHandler;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomMusic;
import com.magmaguy.elitemobs.mobconstructor.custombosses.InstancedBossEntity;
import com.magmaguy.elitemobs.npcs.NPCEntity;
import com.magmaguy.elitemobs.parties.PartyDungeonReadyCheckManager;
import com.magmaguy.elitemobs.parties.PartyManager;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.elitemobs.treasurechest.TreasureChest;
import com.magmaguy.elitemobs.utils.ConfigurationLocation;
import com.magmaguy.elitemobs.utils.EventCaller;
import com.magmaguy.elitemobs.utils.MapListInterpreter;
import com.magmaguy.elitemobs.utils.WorldInstantiator;
import com.magmaguy.magmacore.util.Logger;
import com.magmaguy.magmacore.util.TemporaryWorldManager;
import com.magmaguy.magmacore.util.WorldFolderResolver;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.util.*;
import java.util.function.BooleanSupplier;

public class DungeonInstance extends MatchInstance {
    @Override
    protected Location participantExitLocation(Player player) {
        return previousLocationOrExit(player);
    }

    @Getter
    private static final Set<DungeonInstance> dungeonInstances = new HashSet<>();
    private static final Map<UUID, DungeonInstance> worldInstances = new HashMap<>();
    private static final Map<UUID, WorldRetirement> worldRetirements = new HashMap<>();

    public static DungeonInstance forWorld(UUID worldId) { return worldInstances.get(worldId); }
    public static boolean isWorldRetiring(UUID worldId) { return worldRetirements.containsKey(worldId); }

    public static void shutdown() {
        HashSet<DungeonInstance> copy = new HashSet<>(dungeonInstances);
        copy.addAll(worldInstances.values());
        copy.forEach(DungeonInstance::removeInstance);
        for (WorldRetirement retirement : new ArrayList<>(worldRetirements.values())) retirement.runNow(true);
    }

    private final List<DungeonObjective> dungeonObjectives = new ArrayList<>();

    List<DungeonObjective> objectiveSnapshot() { return List.copyOf(dungeonObjectives); }
    private boolean instanceRemovalScheduled = false;
    private boolean removalEventCalled = false;
    @Getter
    private World world;
    @Getter
    private String instancedWorldName;
    @Getter
    private ContentPackagesConfigFields contentPackagesConfigFields;
    private List<InstancedBossEntity> instancedBossEntities = new ArrayList<>();
    private boolean instancedBossEntitiesRemoved = false;
    @Getter
    private int levelSync = -1;
    private String rawLevelSync = null; // Stores the original config value (e.g., "+5", "-3", or "70")
    private String difficultyName = null;
    @Getter
    private String difficultyID = null;
    private BukkitTask initializeEntitiesTask = null;
    private BukkitTask destroyMatchTask = null;

    /** Creates a one-life encounter mob owned and cleaned up by this dungeon. */
    public InstancedBossEntity createEncounterBoss(CustomBossesConfigFields fields, Location location) {
        if (instanceRemovalScheduled || instancedBossEntitiesRemoved || world == null
                || location == null || !world.equals(location.getWorld())) return null;
        InstancedBossEntity boss = new InstancedBossEntity(fields, location.clone(), this);
        instancedBossEntities.add(boss);
        return boss;
    }

    public DungeonInstance(ContentPackagesConfigFields contentPackagesConfigFields,
                           Location lobbyLocation,
                           Location startLocation,
                           World world,
                           Player player,
                           String difficultyName) {
        this(contentPackagesConfigFields, lobbyLocation, startLocation, world, List.of(player), difficultyName);
    }

    public DungeonInstance(ContentPackagesConfigFields contentPackagesConfigFields,
                           Location lobbyLocation,
                           Location startLocation,
                           World world,
                           Collection<Player> initialPlayers,
                           String difficultyName) {
        this(contentPackagesConfigFields, lobbyLocation, startLocation, world,
                initialPlayers, difficultyName, () -> true);
    }

    protected DungeonInstance(ContentPackagesConfigFields contentPackagesConfigFields,
                              Location lobbyLocation,
                              Location startLocation,
                              World world,
                              Collection<Player> initialPlayers,
                              String difficultyName,
                              BooleanSupplier admissionAuthorization) {
        super(startLocation,
                null, //todo: the end location is currently not definable
                contentPackagesConfigFields.getMinPlayerCount(),
                contentPackagesConfigFields.getMaxPlayerCount());
        if (cancelled) {
            cleanupLoadedWorld(world);
            return;
        }
        //super() already added this instance to MatchInstance.instances and started its watchdog/message tasks.
        //If anything below throws (objective parsing, setDifficulty, addNewPlayer calling Player APIs on a player
        //who may have gone offline during the queued world clone), the static initializeInstancedWorld catch only
        //frees the world -- it has no reference to this half-built instance, so the watchdog would keep running in
        //CraftScheduler.pending and pin the instance + its world's ServerLevel (the exact reported leak). Tear the
        //instance registration down here so that cannot happen; world deletion is handled by the caller's catch.
        try {
            super.lobbyLocation = lobbyLocation;
            this.contentPackagesConfigFields = contentPackagesConfigFields;
            this.world = world;
            super.world = world;
            DungeonInstance previous = worldInstances.putIfAbsent(world.getUID(), this);
            if (previous != null && previous != this)
                throw new IllegalStateException("Dungeon world already has an owner: " + world.getName());
            for (DungeonObjective.TargetDefinition objective : contentPackagesConfigFields.instanceObjectives())
                dungeonObjectives.add(new DungeonKillTargetObjective(this, objective));
            this.world = world;
            super.world = world;
            this.instancedWorldName = world.getName();
            this.difficultyName = difficultyName;
            setDifficulty(difficultyName);
            super.permission = contentPackagesConfigFields.getPermission();
            if (!addDungeonPlayers(initialPlayers, admissionAuthorization)) {
                if (initialPlayers.size() > 1)
                    PartyManager.sendConfiguredMessage(initialPlayers.iterator().next(),
                            PartyConfig.getDungeonPartyJoinFailedMessage());
                dungeonInstances.add(this);
                removeInstance();
                return;
            }
            initializeEntitiesTask = new InitializeEntitiesTask(this, contentPackagesConfigFields, world).runTaskLater(MetadataHandler.PLUGIN, 20 * 3L);
            dungeonInstances.add(this);
        } catch (RuntimeException exception) {
            deregisterFailedConstruction();
            throw exception;
        }
    }

    /**
     * De-registers a dungeon instance whose constructor body threw after super() had already registered it and
     * started its scheduler tasks. Cancels every task and removes the instance from both registries so no orphaned
     * WatchdogTask can keep it (and its world's ServerLevel) alive. The cloned world itself is deleted by the
     * static cleanupLoadedWorld(world) call in the initializeInstancedWorld/initializeDynamicWorld catch block.
     */
    protected final void deregisterFailedConstruction() {
        cancelScheduledTasks();
        cancelInitializeEntitiesTask();
        cancelDestroyMatchTask();
        cancelRemoveInstanceTask();
        InstancePlayerManager.rollbackAdmissions(new HashSet<>(participants), this);
        players.clear();
        spectators.clear();
        participants.clear();
        playerLives.clear();
        previousPlayerLocations.clear();
        for (DungeonObjective dungeonObjective : dungeonObjectives)
            if (dungeonObjective != null) dungeonObjective.unregister();
        instances.remove(this);
        dungeonInstances.remove(this);
    }

    public static void setupInstancedDungeon(Player player, String instancedDungeonConfigFieldsString, String difficultyName) {
        ContentPackagesConfigFields instancedDungeonsConfigFields = ContentPackagesConfig.getDungeonPackages().get(instancedDungeonConfigFieldsString);
        if (instancedDungeonsConfigFields == null) {
            player.sendMessage(DungeonsConfig.getDungeonDataFailedMessage().replace("$dungeon", instancedDungeonConfigFieldsString));
            return;
        }

        if (instancedDungeonsConfigFields.getPermission() != null && !instancedDungeonsConfigFields.getPermission().isEmpty())
            if (!player.hasPermission(instancedDungeonsConfigFields.getPermission())) {
                player.sendMessage(DungeonsConfig.getDungeonNoPermissionMessage());
                return;
            }

        List<UUID> entryMemberIds = instancedDungeonsConfigFields.isEnchantmentChallenge()
                ? List.of(player.getUniqueId())
                : prepareDungeonEntryRoster(player, instancedDungeonsConfigFields);
        if (entryMemberIds.isEmpty()) return;

        PartyDungeonReadyCheckManager.request(
                player,
                entryMemberIds,
                readyCheckDescription(instancedDungeonsConfigFields, difficultyName),
                reservation -> launchInstancedDungeon(
                        player,
                        instancedDungeonsConfigFields,
                        entryMemberIds,
                        difficultyName,
                        reservation));
    }

    private static boolean launchInstancedDungeon(Player player,
                                                   ContentPackagesConfigFields instancedDungeonsConfigFields,
                                                   List<UUID> entryMemberIds,
                                                   String difficultyName,
                                                   PartyDungeonReadyCheckManager.LaunchReservation reservation) {
        if (!reservation.isValid()) return false;
        if (resolveDungeonEntryRoster(
                player, entryMemberIds, instancedDungeonsConfigFields, null).isEmpty()) return false;
        String instancedWorldName = WorldInstantiator.getNewWorldName(instancedDungeonsConfigFields.getWorldName());

        if (!launchEvent(instancedDungeonsConfigFields, instancedWorldName, player)) return false;
        if (!reservation.isValid()) return false;

        PendingWorldCopy copy = new PendingWorldCopy();
        WorldOperationQueue.queueOperation(
                player,
                () -> {
                    if (!reservation.isValid()) return true;
                    return copy.copy(() -> cloneWorldFiles(instancedDungeonsConfigFields, instancedWorldName));
                },
                () -> {
                    if (!reservation.isValid() || !copy.transferToInitializer()) return;
                    initializeInstancedWorld(instancedDungeonsConfigFields, instancedWorldName, player,
                            entryMemberIds, difficultyName, reservation);
                },
                instancedDungeonsConfigFields.getName(),
                () -> {
                    try { copy.cancel(); }
                    finally { reservation.release(); }
                }
        );
        return true;
    }

    protected static boolean launchEvent(ContentPackagesConfigFields instancedDungeonsConfigFields, String instancedWordName, Player player) {
        try { instancedDungeonsConfigFields.prepareInstanceDefinition(); }
        catch (IllegalArgumentException failure) {
            Logger.warn("Cannot launch dungeon: " + failure.getMessage());
            player.sendMessage(DungeonsConfig.getDungeonCancelledEventMessage());
            return false;
        }
        WorldInstanceEvent worldInstanceEvent = new WorldInstanceEvent(
                instancedDungeonsConfigFields.getWorldName(),
                instancedWordName,
                instancedDungeonsConfigFields);
        new EventCaller(worldInstanceEvent);
        if (worldInstanceEvent.isCancelled()) {
            player.sendMessage(DungeonsConfig.getDungeonCancelledEventMessage());
            return false;
        }
        return true;
    }

    protected static File cloneWorldFiles(ContentPackagesConfigFields instancedDungeonsConfigFields, String instancedWordName) {
        instancedDungeonsConfigFields.prepareInstanceDefinition();
        return WorldInstantiator.cloneWorld(instancedDungeonsConfigFields.getWorldName(), instancedWordName, instancedDungeonsConfigFields.getDungeonConfigFolderName(), instancedDungeonsConfigFields.getEnvironment());
    }

    protected static DungeonInstance initializeInstancedWorld(ContentPackagesConfigFields instancedDungeonsConfigFields,
                                                              String instancedWordName,
                                                              Player player,
                                                              String difficultyName) {
        return initializeInstancedWorld(instancedDungeonsConfigFields, instancedWordName, player,
                List.of(player.getUniqueId()), difficultyName);
    }

    protected static DungeonInstance initializeInstancedWorld(ContentPackagesConfigFields instancedDungeonsConfigFields,
                                                              String instancedWordName,
                                                              Player player,
                                                              List<UUID> entryMemberIds,
                                                              String difficultyName) {
        return initializeInstancedWorld(instancedDungeonsConfigFields, instancedWordName, player,
                entryMemberIds, difficultyName, null);
    }

    protected static DungeonInstance initializeInstancedWorld(ContentPackagesConfigFields instancedDungeonsConfigFields,
                                                              String instancedWordName,
                                                              Player player,
                                                              List<UUID> entryMemberIds,
                                                              String difficultyName,
                                                              PartyDungeonReadyCheckManager.LaunchReservation reservation) {
        if (reservation != null && !reservation.isValid()) {
            cleanupUnloadedWorldFolder(instancedWordName);
            return null;
        }
        World world = null;
        try {
            instancedDungeonsConfigFields.prepareInstanceDefinition();
            world = DungeonUtils.loadWorld(instancedWordName, instancedDungeonsConfigFields.getEnvironment(), instancedDungeonsConfigFields);
            if (world == null) {
                player.sendMessage(DungeonsConfig.getDungeonWorldLoadFailedMessage());
                cleanupUnloadedWorldFolder(instancedWordName);
                return null;
            }

            List<Player> entryPlayers = resolveDungeonEntryRoster(
                    player, entryMemberIds, instancedDungeonsConfigFields, null);
            if (entryPlayers.isEmpty() || (reservation != null && !reservation.isValid())) {
                cleanupLoadedWorld(world);
                return null;
            }

            // Initialize dungeon music for this instanced world
            if (instancedDungeonsConfigFields.getSong() != null)
                new CustomMusic(instancedDungeonsConfigFields.getSong(), instancedDungeonsConfigFields, world);

            //Location where players are teleported to start completing the dungeon
            Location startLocation = instancedDungeonsConfigFields.instanceStartLocation(world);
            //Lobby location is optional, if null it should be the same as the start location
            Location lobbyLocation = ConfigurationLocation.serialize(instancedDungeonsConfigFields.getTeleportLocationString());
            if (lobbyLocation != null) lobbyLocation.setWorld(world);
            else lobbyLocation = startLocation;
            //Location where players are teleported to upon completion, this usually gets overriden with the previous location players were at
            //todo: will probably want to define this at some point Location endLocation = ConfigurationLocation.serialize(instancedDungeonsConfigFields.getEndLocation());
            //endLocation.setWorld(world);
            if (!instancedDungeonsConfigFields.isEnchantmentChallenge())
                return new DungeonInstance(instancedDungeonsConfigFields, lobbyLocation, startLocation, world,
                        entryPlayers, difficultyName,
                        reservation == null ? () -> true : reservation::isValid);
            else
                return new EnchantmentDungeonInstance(instancedDungeonsConfigFields, lobbyLocation, startLocation,
                        world, entryPlayers.get(0), difficultyName);
        } catch (Exception exception) {
            Logger.warn("Failed to initialize instanced dungeon world " + instancedWordName + ": " + exception.getMessage());
            World loaded = world != null ? world : Bukkit.getWorld(instancedWordName);
            if (loaded == null) cleanupUnloadedWorldFolder(instancedWordName);
            else cleanupLoadedWorld(loaded);
            throw new RuntimeException(exception);
        }
    }

    @Override
    public boolean addNewPlayer(Player player) {
        // Preserve the public MatchInstance API contract: direct callers admit only the
        // requested player. Player-facing browsers use requestPartyEntry() for expansion.
        return addDungeonPlayers(List.of(player));
    }

    /** Player-facing entry boundary. Low-level API admission remains available through addNewPlayer(). */
    public boolean requestPartyEntry(Player player) {
        List<UUID> entryMemberIds = PartyManager.getDungeonEntryMemberIds(player);
        if (resolveAdmissionRoster(player, entryMemberIds).isEmpty()) return false;
        return PartyDungeonReadyCheckManager.request(
                player,
                entryMemberIds,
                readyCheckDescription(),
                reservation -> {
                    try {
                        return addEntryRoster(player, entryMemberIds, reservation::isValid);
                    } finally {
                        reservation.release();
                    }
                });
    }

    protected boolean addEntryRoster(Player initiator,
                                     List<UUID> entryMemberIds,
                                     BooleanSupplier admissionAuthorization) {
        if (!admissionAuthorization.getAsBoolean()) return false;
        List<Player> entryPlayers = resolveAdmissionRoster(initiator, entryMemberIds);
        if (entryPlayers.isEmpty() || !admissionAuthorization.getAsBoolean()) return false;

        boolean added = addDungeonPlayers(entryPlayers, admissionAuthorization);
        if (!added && entryMemberIds.size() > 1)
            PartyManager.sendConfiguredMessage(initiator, PartyConfig.getDungeonPartyJoinFailedMessage());
        return added;
    }

    private List<Player> resolveAdmissionRoster(Player player, List<UUID> entryMemberIds) {
        List<Player> entryPlayers = resolveDungeonEntryRoster(
                player, entryMemberIds, contentPackagesConfigFields, this);
        if (entryPlayers.isEmpty()) return List.of();

        long playersNeedingAdmission = entryPlayers.stream().filter(candidate -> !players.contains(candidate)).count();
        int availableSlots = maxPlayers - players.size();
        if (playersNeedingAdmission <= availableSlots) return entryPlayers;
        PartyManager.sendConfiguredMessage(player, PartyConfig.getDungeonPartyTooLargeMessage()
                .replace("$count", String.valueOf(playersNeedingAdmission))
                .replace("$max", String.valueOf(Math.max(0, availableSlots))));
        return List.of();
    }

    protected static String readyCheckDescription(ContentPackagesConfigFields configFields, String difficultyName) {
        String dungeonName = configFields.getName();
        if (dungeonName == null || dungeonName.isBlank()) dungeonName = configFields.getFilename();
        if (difficultyName == null || difficultyName.isBlank()) return dungeonName;
        return dungeonName + " - " + difficultyName;
    }

    protected String readyCheckDescription() {
        return readyCheckDescription(contentPackagesConfigFields, difficultyName);
    }

    private boolean addDungeonPlayers(Collection<Player> entryPlayers) {
        return addDungeonPlayers(entryPlayers, () -> true);
    }

    private boolean addDungeonPlayers(Collection<Player> entryPlayers, BooleanSupplier admissionAuthorization) {
        Set<UUID> existingPlayerIds = players.stream().map(Player::getUniqueId).collect(java.util.stream.Collectors.toSet());
        if (!InstancePlayerManager.addNewPlayers(entryPlayers, this, admissionAuthorization)) return false;
        for (Player entryPlayer : entryPlayers) {
            if (existingPlayerIds.contains(entryPlayer.getUniqueId())) continue;
            if (levelSync > 0)
                entryPlayer.sendMessage(DungeonsConfig.getDungeonDifficultyMessage()
                        .replace("$difficulty", difficultyName)
                        .replace("$levelSync", String.valueOf(levelSync)));
        }
        return true;
    }

    protected static List<UUID> prepareDungeonEntryRoster(Player initiator,
                                                           ContentPackagesConfigFields configFields) {
        List<UUID> memberIds = PartyManager.getDungeonEntryMemberIds(initiator);
        if (memberIds.size() > configFields.getMaxPlayerCount()) {
            PartyManager.sendConfiguredMessage(initiator, PartyConfig.getDungeonPartyTooLargeMessage()
                    .replace("$count", String.valueOf(memberIds.size()))
                    .replace("$max", String.valueOf(configFields.getMaxPlayerCount())));
            return List.of();
        }
        return resolveDungeonEntryRoster(initiator, memberIds, configFields, null).isEmpty()
                ? List.of()
                : memberIds;
    }

    protected static List<Player> resolveDungeonEntryRoster(Player initiator,
                                                             Collection<UUID> memberIds,
                                                             ContentPackagesConfigFields configFields,
                                                             DungeonInstance allowedInstance) {
        if (initiator == null || !initiator.isOnline()) return List.of();
        boolean personalEnchantmentChallenge = configFields.isEnchantmentChallenge()
                && memberIds.size() == 1
                && memberIds.contains(initiator.getUniqueId());
        if (!personalEnchantmentChallenge && !PartyManager.isDungeonEntryRosterCurrent(initiator, memberIds)) {
            PartyManager.sendConfiguredMessage(initiator, PartyConfig.getDungeonPartyChangedMessage());
            return List.of();
        }

        List<Player> entryPlayers = new ArrayList<>();
        for (UUID memberId : memberIds) {
            Player member = Bukkit.getPlayer(memberId);
            String memberName = member == null ? Bukkit.getOfflinePlayer(memberId).getName() : member.getName();
            if (member == null || !member.isOnline() || !member.isValid() || !PlayerData.isInMemory(memberId)) {
                PartyManager.sendConfiguredMessage(initiator, PartyConfig.getDungeonPartyUnavailableMessage()
                        .replace("$player", memberName == null ? PartyConfig.getUnknownPlayerName() : memberName));
                return List.of();
            }
            MatchInstance currentInstance = PlayerData.getMatchInstance(member);
            MatchInstance indexedInstance = MatchInstance.getAnyPlayerInstance(member);
            if ((currentInstance != null && currentInstance != allowedInstance)
                    || (indexedInstance != null && indexedInstance != allowedInstance)) {
                PartyManager.sendConfiguredMessage(initiator, PartyConfig.getDungeonPartyInInstanceMessage()
                        .replace("$player", member.getName()));
                return List.of();
            }
            String permission = configFields.getPermission();
            if (permission != null && !permission.isEmpty() && !member.hasPermission(permission)) {
                PartyManager.sendConfiguredMessage(initiator, PartyConfig.getDungeonPartyNoPermissionMessage()
                        .replace("$player", member.getName()));
                return List.of();
            }
            entryPlayers.add(member);
        }
        return List.copyOf(entryPlayers);
    }

    @Override
    protected void startMatch() {
        updateBossHealth();
        super.startMatch();
        new EventCaller(new DungeonStartEvent(this));
    }

    //Runs when the instance starts, adjusting boss health to the amount of players in the instance
    private void updateBossHealth() {
        instancedBossEntities.forEach(instancedBossEntity -> {
            instancedBossEntity.setNormalizedMaxHealth(players.size());
        });
    }

    public boolean checkCompletionStatus() {
        if (!super.state.equals(InstancedRegionState.ONGOING)) return false;
        for (DungeonObjective dungeonObjective : dungeonObjectives)
            if (!dungeonObjective.isCompleted())
                return false;
        new EventCaller(new DungeonCompleteEvent(this));
        //This means the dungeon just completed
        victory();
        return true;
    }

    @Override
    public void endMatch() {
        super.endMatch();
        if (players.isEmpty()) {
            destroyMatch();
            return;
        }
        announce(DungeonsConfig.getInstancedDungeonCompleteMessage());
        scheduleDelayedDestroy(2 * 60 * 20L);
    }

    /**
     * Schedules a cancellable delayed teardown, reusing the destroyMatchTask slot so it is cancelled by
     * cancelDestroyMatchTask()/cleanupInstanceReferences() like every other instanced-dungeon task. Used by
     * subclasses (e.g. enchantment challenges) that want a custom teardown delay without leaking a
     * fire-and-forget BukkitRunnable that pins the instance in the scheduler.
     */
    protected void scheduleDelayedDestroy(long delayTicks) {
        cancelDestroyMatchTask();
        destroyMatchTask = new DestroyMatchTask().runTaskLater(MetadataHandler.PLUGIN, delayTicks);
    }

    @Override
    public void destroyMatch() {
        super.destroyMatch();
        removeInstance();
    }

    public void removeInstance() {
        if (!isDestroyingMatch() && !hasMatchDestroyEventFired()) super.destroyMatch();
        boolean immediateRemoval = MetadataHandler.shutdownRequested;
        cancelScheduledTasks();
        cancelInitializeEntitiesTask();
        cancelDestroyMatchTask();

        // Prevent multiple removal attempts for the same instance
        if (instanceRemovalScheduled) {
            if (!immediateRemoval) {
                Logger.warn("removeInstance() called but already scheduled for " + (world != null ? world.getName() : "null world"));
                return;
            }
            cancelRemoveInstanceTask();
        } else {
            instanceRemovalScheduled = true;
            for (DungeonObjective dungeonObjective : dungeonObjectives) {
                if (dungeonObjective != null) dungeonObjective.unregister();
            }

            participants.forEach(player -> player.sendMessage(DungeonsConfig.getInstancedDungeonClosingInstanceMessage()));
            HashSet<Player> participants = new HashSet<>(this.participants);
            participants.forEach(this::removeAnyKind);
            instances.remove(this);
        }

        DungeonInstance dungeonInstance = this;
        removeInstancedBossEntities(RemovalReason.WORLD_UNLOAD);
        if (world == null) {
            Logger.warn("Instanced dungeon's world was already unloaded before removing the entities in it! This shouldn't happen, but doesn't break anything.");
            cleanupInstanceReferences();
            return;
        }

        world.getEntities().forEach(entity -> EntityTracker.unregister(entity, RemovalReason.WORLD_UNLOAD));
        WorldRetirement retirement = worldRetirements.computeIfAbsent(world.getUID(),
                ignored -> new WorldRetirement(world, dungeonInstance));
        if (immediateRemoval) retirement.runNow(true);
        else retirement.schedule(20L * 30L);
    }

    private void cancelInitializeEntitiesTask() {
        if (initializeEntitiesTask == null) return;
        initializeEntitiesTask.cancel();
        initializeEntitiesTask = null;
    }

    private void cancelDestroyMatchTask() {
        if (destroyMatchTask == null) return;
        destroyMatchTask.cancel();
        destroyMatchTask = null;
    }

    private void cancelRemoveInstanceTask() {
        WorldRetirement retirement = world == null ? null : worldRetirements.get(world.getUID());
        if (retirement != null) retirement.cancelPending();
    }

    private void removeInstancedBossEntities(RemovalReason removalReason) {
        if (instancedBossEntitiesRemoved) return;
        instancedBossEntitiesRemoved = true;
        for (InstancedBossEntity instancedBossEntity : new ArrayList<>(instancedBossEntities)) {
            if (instancedBossEntity != null)
                instancedBossEntity.remove(removalReason);
        }
    }

    protected boolean isInstanceRemovalScheduled() {
        return instanceRemovalScheduled;
    }

    /**
     * A dungeon instance is also dead once its world removal is scheduled or its
     * world reference is gone; both can precede the registry evictions the base
     * check keys on, and the browser holds these objects for the whole 30–90
     * second deletion window.
     */
    @Override
    public boolean isDefunct() {
        return super.isDefunct() || instanceRemovalScheduled || world == null;
    }

    protected static void cleanupUnloadedWorldFolder(String instancedWorldName) {
        if (Bukkit.getWorld(instancedWorldName) != null) {
            Logger.warn("Cannot clean up instanced dungeon world folder " + instancedWorldName + " while the world is loaded.");
            return;
        }
        WorldFolderResolver.deleteAllLayouts(instancedWorldName);
        Logger.info("Cleaned up unloaded instanced dungeon world folder " + instancedWorldName);
    }

    protected static void cleanupLoadedWorld(World world) {
        if (world == null) return;
        WorldRetirement retirement = worldRetirements.computeIfAbsent(world.getUID(),
                ignored -> new WorldRetirement(world, worldInstances.get(world.getUID())));
        retirement.runNow(MetadataHandler.shutdownRequested);
    }

    private static void cleanupWorldScopedState(World worldToDelete) {
        com.magmaguy.elitemobs.explosionregen.Explosion.discardForWorld(worldToDelete.getUID());
        UUID worldUUID = worldToDelete.getUID();
        EliteMobsWorld.destroy(worldUUID);
        com.magmaguy.magmacore.instance.InstanceProtector.removeProtectedWorld(worldToDelete);
        CustomMusic.removeDungeonMusic(worldUUID);
        TreasureChest.removeInstancedTreasureChests(worldToDelete);
        PersistentObjectHandler.removeForWorld(worldUUID);
    }

    public String getResolvedDifficultyID() {
        return contentPackagesConfigFields.getDifficultyResolver().resolveSelected(difficultyID);
    }

    public boolean matchesDifficulty(List<String> filter, String source) {
        return contentPackagesConfigFields.getDifficultyResolver().matches(filter, difficultyID, source);
    }

    private void setDifficulty(String difficultyName) {
        if (difficultyName == null) return;
        if (contentPackagesConfigFields.getDifficulties() == null ||
                contentPackagesConfigFields.getDifficulties().isEmpty())
            return;
        Map difficulty = null;
        for (Map difficultyMap : contentPackagesConfigFields.getDifficulties())
            if (difficultyMap.get("name") != null && difficultyMap.get("name").equals(difficultyName)) {
                difficulty = difficultyMap;
                break;
            }
        if (difficulty == null) {
            Logger.warn("Failed to set difficulty " + difficultyName + " for instanced dungeon " + contentPackagesConfigFields.getFilename());
            return;
        }

        if (difficulty.get("levelSync") != null) {
            try {
                this.rawLevelSync = MapListInterpreter.parseString("levelSync", difficulty.get("levelSync"), contentPackagesConfigFields.getFilename());
                this.levelSync = parseLevelSync(rawLevelSync, contentPackagesConfigFields.getContentLevel());
            } catch (Exception exception) {
                Logger.warn("Incorrect level sync entry for dungeon " + contentPackagesConfigFields.getFilename() + " ! Value: " + rawLevelSync + " . No level sync will be applied!");
                this.levelSync = 0;
            }
        } else
            this.levelSync = 0;

        //Used for loot
        if (difficulty.get("id") != null) {
            this.difficultyID = MapListInterpreter.parseString("id", difficulty.get("id"), contentPackagesConfigFields.getFilename());
        }
    }

    /**
     * Parses the levelSync value from config. Supports:
     * - Absolute values: "70" means level sync at 70
     * - Relative values: "+5" means content level + 5, "-3" means content level - 3
     * @param rawValue The raw string value from config
     * @param contentLevel The content level to use for relative calculations (can be -1 for dynamic dungeons)
     * @return The calculated level sync value, or 0 if invalid/disabled
     */
    protected int parseLevelSync(String rawValue, int contentLevel) {
        if (rawValue == null || rawValue.isEmpty()) return 0;

        rawValue = rawValue.trim();

        // Check for relative values (+N or -N)
        if (rawValue.startsWith("+") || rawValue.startsWith("-")) {
            int offset;
            try {
                offset = Integer.parseInt(rawValue);
            } catch (NumberFormatException e) {
                Logger.warn("Invalid relative level sync value: " + rawValue);
                return 0;
            }
            // If content level is -1 (dynamic), return 0 for now - will be recalculated later
            if (contentLevel < 0) return 0;
            return Math.max(1, contentLevel + offset);
        }

        // Absolute value
        try {
            return Integer.parseInt(rawValue);
        } catch (NumberFormatException e) {
            Logger.warn("Invalid level sync value: " + rawValue);
            return 0;
        }
    }

    /**
     * Recalculates the level sync based on a dynamic level (used by DynamicDungeonInstance).
     * This should be called after the player selects their level in the menu.
     * @param dynamicLevel The player-selected level for the dungeon
     */
    protected void recalculateLevelSyncForDynamicLevel(int dynamicLevel) {
        if (rawLevelSync == null || rawLevelSync.isEmpty()) {
            this.levelSync = 0;
            return;
        }

        String trimmed = rawLevelSync.trim();

        // Check for relative values (+N or -N)
        if (trimmed.startsWith("+") || trimmed.startsWith("-")) {
            try {
                int offset = Integer.parseInt(trimmed);
                this.levelSync = Math.max(1, dynamicLevel + offset);
            } catch (NumberFormatException e) {
                this.levelSync = 0;
            }
        }
        // Absolute values stay as they are (already parsed in setDifficulty)
    }

    /**
     * Checks if the level sync is using a relative value (starts with + or -)
     * @return true if relative, false if absolute or not set
     */
    protected boolean isRelativeLevelSync() {
        if (rawLevelSync == null || rawLevelSync.isEmpty()) return false;
        String trimmed = rawLevelSync.trim();
        return trimmed.startsWith("+") || trimmed.startsWith("-");
    }

    @Override
    protected boolean isInRegion(Location location) {
        return location.getWorld().equals(startLocation.getWorld());
    }

    private class InitializeEntitiesTask extends BukkitRunnable {
        private final DungeonInstance dungeonInstance;
        private final ContentPackagesConfigFields contentPackagesConfigFields;
        private final World world;

        public InitializeEntitiesTask(DungeonInstance dungeonInstance, ContentPackagesConfigFields contentPackagesConfigFields, World world) {
            this.dungeonInstance = dungeonInstance;
            this.contentPackagesConfigFields = contentPackagesConfigFields;
            this.world = world;
        }

        @Override
        public void run() {
            initializeEntitiesTask = null;
            if (isInstanceRemovalScheduled() || world == null || !dungeonInstances.contains(dungeonInstance))
                return;
            instancedBossEntities = InstancedBossEntity.initializeInstancedBosses(contentPackagesConfigFields.getWorldName(), world, players.size(), dungeonInstance);
            NPCEntity.initializeInstancedNPCs(contentPackagesConfigFields.getWorldName(), world, players.size(), dungeonInstance);
            TreasureChest.initializeInstancedTreasureChests(contentPackagesConfigFields.getWorldName(), world);
        }
    }

    private class DestroyMatchTask extends BukkitRunnable {
        @Override
        public void run() {
            destroyMatchTask = null;
            destroyMatch();
        }
    }

    /** The same retirement owner handles both complete instances and failed construction. */
    private static final class WorldRetirement implements Runnable {
        private final World world;
        private final DungeonInstance owner;
        private BukkitTask pending;
        private int attempts;
        private boolean immediate;
        private boolean running;

        private WorldRetirement(World world, DungeonInstance owner) {
            this.world = world;
            this.owner = owner;
        }

        private void cancelPending() {
            if (pending != null) { pending.cancel(); pending = null; }
        }

        private void schedule(long delay) {
            if (pending == null)
                pending = Bukkit.getScheduler().runTaskLater(MetadataHandler.PLUGIN, this, delay);
        }

        private void runNow(boolean immediate) {
            cancelPending();
            this.immediate |= immediate;
            run();
        }

        @Override
        public void run() {
            pending = null;
            if (running || worldRetirements.get(world.getUID()) != this) return;
            running = true;
            attempts++;
            try {
                if (Bukkit.getWorld(world.getUID()) != null) {
                    evacuatePlayers();
                    if (!world.getPlayers().isEmpty()) { retry(); return; }
                    var protection = com.magmaguy.magmacore.instance.InstanceProtector.getRules(world);
                    boolean unloaded = immediate
                            ? TemporaryWorldManager.trySyncPermanentlyDeleteWorld(world)
                            : TemporaryWorldManager.tryPermanentlyDeleteWorld(world);
                    if (!unloaded) {
                        if (protection != null)
                            com.magmaguy.magmacore.instance.InstanceProtector.addProtectedWorld(world, protection);
                        PersistentObjectHandler.loadWorld(world);
                        retry();
                        return;
                    }
                }
                // The protection/index remains authoritative until native unload actually succeeds.
                cleanupWorldScopedState(world);
                if (owner != null) {
                    if (!owner.removalEventCalled) {
                        owner.removalEventCalled = true;
                        new EventCaller(new InstancedDungeonRemoveEvent(owner));
                    }
                    if (owner.contentPackagesConfigFields != null)
                        new EventCaller(new WorldUninstanceEvent(owner.contentPackagesConfigFields, world.getName()));
                    owner.cleanupInstanceReferences();
                }
                worldRetirements.remove(world.getUID(), this);
            } catch (RuntimeException failure) {
                Logger.warn("Could not retire dungeon world " + world.getName() + ": " + failure.getMessage());
                retry();
            } finally {
                running = false;
            }
        }

        private void retry() {
            if (immediate || MetadataHandler.shutdownRequested || !MetadataHandler.PLUGIN.isEnabled()) {
                Logger.warn("Dungeon world " + world.getName()
                        + " could not be unloaded during shutdown. Its ownership was retained; no files were deleted.");
                return;
            }
            if (attempts == 1 || attempts == 12)
                Logger.warn("Dungeon world " + world.getName()
                        + " is still loaded. Protection and cleanup ownership were retained; cleanup will retry.");
            schedule(attempts < 12 ? 20L * 5L : 20L * 60L);
        }

        private void evacuatePlayers() {
            Location fallback = null;
            for (World other : Bukkit.getWorlds())
                if (!other.equals(world) && !isWorldRetiring(other.getUID())) {
                    fallback = other.getSpawnLocation();
                    break;
                }
            for (Player player : new ArrayList<>(world.getPlayers())) {
                Location destination = owner == null ? null : owner.previousPlayerLocations.get(player);
                if (!safeExit(destination)) destination = owner == null ? null : owner.exitLocation;
                if (!safeExit(destination)) destination = fallback;
                if (destination == null) continue;
                try {
                    if (player.getGameMode() == org.bukkit.GameMode.SPECTATOR) player.setSpectatorTarget(null);
                    if (owner == null) player.teleport(destination);
                    else com.magmaguy.elitemobs.instanced.InstancePlayerMovement.teleportForMatch(player, destination, owner, false);
                } catch (RuntimeException failure) {
                    Logger.warn("Could not evacuate " + player.getName() + " from " + world.getName() + ": " + failure.getMessage());
                }
            }
        }

        private boolean safeExit(Location location) {
            return location != null && location.getWorld() != null && !world.equals(location.getWorld())
                    && Bukkit.getWorld(location.getWorld().getUID()) != null
                    && !isWorldRetiring(location.getWorld().getUID());
        }
    }

    private void cleanupInstanceReferences() {
        removeInstancedBossEntities(RemovalReason.WORLD_UNLOAD);
        cancelScheduledTasks();
        cancelInitializeEntitiesTask();
        cancelDestroyMatchTask();
        cancelRemoveInstanceTask();
        instances.remove(this);
        dungeonInstances.remove(this);
        if (world != null) worldInstances.remove(world.getUID(), this);
        players.clear();
        spectators.clear();
        participants.clear();
        playerLives.clear();
        previousPlayerLocations.clear();
        new ArrayList<>(deathBanners.values()).forEach(deathLocation -> {
            try {
                deathLocation.clear(false);
            } catch (Exception exception) {
                Logger.warn("Failed to clear an instanced dungeon death banner during cleanup: " + exception.getMessage());
            }
        });
        deathBanners.clear();
        dungeonObjectives.clear();
        instancedBossEntities.clear();
        super.world = null;
        world = null;
        instancedWorldName = null;
        startLocation = null;
        lobbyLocation = null;
        exitLocation = null;
        contentPackagesConfigFields = null;
        difficultyName = null;
        rawLevelSync = null;
    }
}
