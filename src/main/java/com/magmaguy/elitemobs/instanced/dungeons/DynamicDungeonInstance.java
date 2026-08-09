package com.magmaguy.elitemobs.instanced.dungeons;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.DungeonsConfig;
import com.magmaguy.elitemobs.config.PartyConfig;
import com.magmaguy.elitemobs.config.contentpackages.ContentPackagesConfig;
import com.magmaguy.elitemobs.config.contentpackages.ContentPackagesConfigFields;
import com.magmaguy.elitemobs.dungeons.utility.DungeonUtils;
import com.magmaguy.elitemobs.instanced.WorldOperationQueue;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomMusic;
import com.magmaguy.elitemobs.mobconstructor.custombosses.InstancedBossEntity;
import com.magmaguy.elitemobs.parties.PartyDungeonReadyCheckManager;
import com.magmaguy.elitemobs.quests.DynamicQuest;
import com.magmaguy.elitemobs.utils.ConfigurationLocation;
import com.magmaguy.elitemobs.utils.WorldInstantiator;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.BooleanSupplier;
import java.util.stream.Collectors;

public class DynamicDungeonInstance extends DungeonInstance {
    @Getter
    private final int selectedLevel;

    public DynamicDungeonInstance(ContentPackagesConfigFields contentPackagesConfigFields,
                                  Location lobbyLocation,
                                  Location startLocation,
                                  World world,
                                  Player player,
                                  String difficultyName,
                                  int selectedLevel) {
        this(contentPackagesConfigFields, lobbyLocation, startLocation, world,
                List.of(player), difficultyName, selectedLevel);
    }

    public DynamicDungeonInstance(ContentPackagesConfigFields contentPackagesConfigFields,
                                  Location lobbyLocation,
                                  Location startLocation,
                                  World world,
                                  Collection<Player> initialPlayers,
                                  String difficultyName,
                                  int selectedLevel) {
        this(contentPackagesConfigFields, lobbyLocation, startLocation, world,
                initialPlayers, difficultyName, selectedLevel, () -> true);
    }

    private DynamicDungeonInstance(ContentPackagesConfigFields contentPackagesConfigFields,
                                   Location lobbyLocation,
                                   Location startLocation,
                                   World world,
                                   Collection<Player> initialPlayers,
                                   String difficultyName,
                                   int selectedLevel,
                                   BooleanSupplier admissionAuthorization) {
        super(contentPackagesConfigFields, lobbyLocation, startLocation, world,
                initialPlayers, difficultyName, admissionAuthorization);
        this.selectedLevel = selectedLevel;

        //The super constructor can already have rolled this instance back (cancelled MatchInstantiateEvent or a
        //failed initial join). Don't schedule a fresh task on a doomed/torn-down instance.
        if (cancelled || isInstanceRemovalScheduled() || getWorld() == null) return;

        try {
            // Recalculate level sync for dynamic dungeons based on the player-selected level
            recalculateLevelSyncForDynamicLevel(selectedLevel);
            getPlayers().forEach(this::applyDynamicEntryState);

            new SetBossLevelsTask(this, selectedLevel).runTaskLater(MetadataHandler.PLUGIN, 20 * 4L);
        } catch (RuntimeException exception) {
            // The base constructor has already registered this instance and admitted its players.
            // Roll that state back if subclass initialization fails before construction completes.
            deregisterFailedConstruction();
            throw exception;
        }
    }

    public static void setupDynamicDungeon(Player player, String dungeonConfigFieldsString, String difficultyName, int selectedLevel) {
        ContentPackagesConfigFields dynamicDungeonConfigFields = ContentPackagesConfig.getDungeonPackages().get(dungeonConfigFieldsString);
        if (dynamicDungeonConfigFields == null) {
            player.sendMessage(DungeonsConfig.getDynamicDungeonDataFailedMessage().replace("$dungeon", dungeonConfigFieldsString));
            return;
        }

        if (dynamicDungeonConfigFields.getPermission() != null && !dynamicDungeonConfigFields.getPermission().isEmpty())
            if (!player.hasPermission(dynamicDungeonConfigFields.getPermission())) {
                player.sendMessage(DungeonsConfig.getDynamicDungeonNoPermissionMessage());
                return;
            }

        List<UUID> entryMemberIds = prepareDungeonEntryRoster(player, dynamicDungeonConfigFields);
        if (entryMemberIds.isEmpty()) return;

        PartyDungeonReadyCheckManager.request(
                player,
                entryMemberIds,
                readyCheckLevelDescription(
                        readyCheckDescription(dynamicDungeonConfigFields, difficultyName), selectedLevel),
                reservation -> launchDynamicDungeon(
                        player,
                        dynamicDungeonConfigFields,
                        entryMemberIds,
                        difficultyName,
                        selectedLevel,
                        reservation));
    }

    private static boolean launchDynamicDungeon(Player player,
                                                 ContentPackagesConfigFields dynamicDungeonConfigFields,
                                                 List<UUID> entryMemberIds,
                                                 String difficultyName,
                                                 int selectedLevel,
                                                 PartyDungeonReadyCheckManager.LaunchReservation reservation) {
        if (!reservation.isValid()) return false;
        if (resolveDungeonEntryRoster(
                player, entryMemberIds, dynamicDungeonConfigFields, null).isEmpty()) return false;
        String instancedWorldName = WorldInstantiator.getNewWorldName(dynamicDungeonConfigFields.getWorldName());

        if (!launchEvent(dynamicDungeonConfigFields, instancedWorldName, player)) return false;
        if (!reservation.isValid()) return false;

        WorldOperationQueue.queueOperation(
                player,
                () -> !reservation.isValid()
                        || cloneWorldFiles(dynamicDungeonConfigFields, instancedWorldName) != null,
                () -> {
                    if (!reservation.isValid()) {
                        cleanupUnloadedWorldFolder(instancedWorldName);
                        return;
                    }
                    initializeDynamicWorld(dynamicDungeonConfigFields, instancedWorldName, player,
                            entryMemberIds, difficultyName, selectedLevel, reservation);
                },
                dynamicDungeonConfigFields.getName(),
                reservation::release
        );
        return true;
    }

    protected static DynamicDungeonInstance initializeDynamicWorld(ContentPackagesConfigFields dynamicDungeonConfigFields,
                                                                   String instancedWorldName,
                                                                   Player player,
                                                                   String difficultyName,
                                                                   int selectedLevel) {
        return initializeDynamicWorld(dynamicDungeonConfigFields, instancedWorldName, player,
                List.of(player.getUniqueId()), difficultyName, selectedLevel);
    }

    protected static DynamicDungeonInstance initializeDynamicWorld(ContentPackagesConfigFields dynamicDungeonConfigFields,
                                                                   String instancedWorldName,
                                                                   Player player,
                                                                   List<UUID> entryMemberIds,
                                                                   String difficultyName,
                                                                   int selectedLevel) {
        return initializeDynamicWorld(dynamicDungeonConfigFields, instancedWorldName, player,
                entryMemberIds, difficultyName, selectedLevel, null);
    }

    protected static DynamicDungeonInstance initializeDynamicWorld(ContentPackagesConfigFields dynamicDungeonConfigFields,
                                                                   String instancedWorldName,
                                                                   Player player,
                                                                   List<UUID> entryMemberIds,
                                                                   String difficultyName,
                                                                   int selectedLevel,
                                                                   PartyDungeonReadyCheckManager.LaunchReservation reservation) {
        if (reservation != null && !reservation.isValid()) {
            cleanupUnloadedWorldFolder(instancedWorldName);
            return null;
        }
        World world = DungeonUtils.loadWorld(instancedWorldName, dynamicDungeonConfigFields.getEnvironment(), dynamicDungeonConfigFields);
        if (world == null) {
            player.sendMessage(DungeonsConfig.getDynamicDungeonWorldLoadFailedMessage());
            cleanupUnloadedWorldFolder(instancedWorldName);
            return null;
        }

        try {
            List<Player> entryPlayers = resolveDungeonEntryRoster(
                    player, entryMemberIds, dynamicDungeonConfigFields, null);
            if (entryPlayers.isEmpty() || (reservation != null && !reservation.isValid())) {
                cleanupLoadedWorld(world);
                return null;
            }

            // Initialize dungeon music for this dynamic instanced world
            if (dynamicDungeonConfigFields.getSong() != null)
                new CustomMusic(dynamicDungeonConfigFields.getSong(), dynamicDungeonConfigFields, world);

            Location startLocation = ConfigurationLocation.serialize(dynamicDungeonConfigFields.getStartLocationString());
            startLocation.setWorld(world);
            Location lobbyLocation = ConfigurationLocation.serialize(dynamicDungeonConfigFields.getTeleportLocationString());
            if (lobbyLocation != null) lobbyLocation.setWorld(world);
            else lobbyLocation = startLocation;

            return new DynamicDungeonInstance(dynamicDungeonConfigFields, lobbyLocation, startLocation, world,
                    entryPlayers, difficultyName, selectedLevel,
                    reservation == null ? () -> true : reservation::isValid);
        } catch (Exception exception) {
            com.magmaguy.magmacore.util.Logger.warn("Failed to initialize dynamic dungeon world " + instancedWorldName + ": " + exception.getMessage());
            cleanupLoadedWorld(world);
            throw new RuntimeException(exception);
        }
    }

    @Override
    public boolean addNewPlayer(Player player) {
        Set<UUID> existingPlayerIds = getPlayers().stream()
                .map(Player::getUniqueId)
                .collect(Collectors.toSet());
        if (!super.addNewPlayer(player)) return false;
        getPlayers().stream()
                .filter(joinedPlayer -> !existingPlayerIds.contains(joinedPlayer.getUniqueId()))
                .forEach(this::applyDynamicEntryState);
        return true;
    }

    @Override
    protected boolean addEntryRoster(Player player,
                                     List<UUID> entryMemberIds,
                                     BooleanSupplier admissionAuthorization) {
        Set<UUID> existingPlayerIds = getPlayers().stream()
                .map(Player::getUniqueId)
                .collect(Collectors.toSet());
        if (!super.addEntryRoster(player, entryMemberIds, admissionAuthorization)) return false;
        getPlayers().stream()
                .filter(joinedPlayer -> !existingPlayerIds.contains(joinedPlayer.getUniqueId()))
                .forEach(this::applyDynamicEntryState);
        return true;
    }

    private void applyDynamicEntryState(Player player) {
        player.sendMessage(DungeonsConfig.getDynamicDungeonLevelSetMessage().replace("$level", String.valueOf(selectedLevel)));
        DynamicQuest.adaptPlayerQuestsToLevel(player, selectedLevel);
    }

    @Override
    protected String readyCheckDescription() {
        return readyCheckLevelDescription(super.readyCheckDescription(), selectedLevel);
    }

    private static String readyCheckLevelDescription(String dungeonDescription, int level) {
        return PartyConfig.getDungeonReadyCheckLevelFormat()
                .replace("$dungeon", dungeonDescription)
                .replace("$level", String.valueOf(level));
    }

    private class SetBossLevelsTask extends BukkitRunnable {
        private final DynamicDungeonInstance dynamicDungeonInstance;
        private final int level;

        public SetBossLevelsTask(DynamicDungeonInstance dynamicDungeonInstance, int level) {
            this.dynamicDungeonInstance = dynamicDungeonInstance;
            this.level = level;
        }

        @Override
        public void run() {
            if (isInstanceRemovalScheduled() || getWorld() == null || !getDungeonInstances().contains(dynamicDungeonInstance))
                return;
            getWorld().getEntities().forEach(entity -> {
                if (entity instanceof org.bukkit.entity.LivingEntity) {
                    Object eliteEntity = com.magmaguy.elitemobs.entitytracker.EntityTracker.getEliteMobEntity(entity);
                    if (eliteEntity instanceof InstancedBossEntity) {
                        InstancedBossEntity boss = (InstancedBossEntity) eliteEntity;
                        if (boss.getDungeonInstance() == dynamicDungeonInstance) {
                            boss.setEntityLevel(level);
                        }
                    }
                }
            });
        }
    }
}
