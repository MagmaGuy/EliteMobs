package com.magmaguy.elitemobs.instanced.dungeons;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.DungeonCompleteEvent;
import com.magmaguy.elitemobs.api.DungeonStartEvent;
import com.magmaguy.elitemobs.api.InstancedDungeonRemoveEvent;
import com.magmaguy.elitemobs.api.WorldInstanceEvent;
import com.magmaguy.elitemobs.api.internal.RemovalReason;
import com.magmaguy.elitemobs.config.DungeonsConfig;
import com.magmaguy.elitemobs.config.contentpackages.ContentPackagesConfig;
import com.magmaguy.elitemobs.config.contentpackages.ContentPackagesConfigFields;
import com.magmaguy.elitemobs.dungeons.utility.DungeonUtils;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.instanced.MatchInstance;
import com.magmaguy.elitemobs.instanced.WorldOperationQueue;
import com.magmaguy.elitemobs.mobconstructor.custombosses.InstancedBossEntity;
import com.magmaguy.elitemobs.npcs.NPCEntity;
import com.magmaguy.elitemobs.treasurechest.TreasureChest;
import com.magmaguy.elitemobs.utils.ConfigurationLocation;
import com.magmaguy.elitemobs.utils.EventCaller;
import com.magmaguy.elitemobs.utils.MapListInterpreter;
import com.magmaguy.elitemobs.utils.WorldInstantiator;
import com.magmaguy.magmacore.util.Logger;
import com.magmaguy.magmacore.util.TemporaryWorldManager;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.*;

public class DungeonInstance extends MatchInstance {
    @Getter
    private static final Set<DungeonInstance> dungeonInstances = new HashSet<>();

    public static void shutdown() {
        dungeonInstances.clear();
    }

    private final List<DungeonObjective> dungeonObjectives = new ArrayList<>();
    @Getter
    private World world;
    @Getter
    private String instancedWorldName;
    @Getter
    private ContentPackagesConfigFields contentPackagesConfigFields;
    private List<InstancedBossEntity> instancedBossEntities = new ArrayList<>();
    @Getter
    private int levelSync = -1;
    private String rawLevelSync = null; // Stores the original config value (e.g., "+5", "-3", or "70")
    private String difficultyName = null;
    @Getter
    private String difficultyID = null;

    public DungeonInstance(ContentPackagesConfigFields contentPackagesConfigFields,
                           Location lobbyLocation,
                           Location startLocation,
                           World world,
                           Player player,
                           String difficultyName) {
        super(startLocation,
                null, //todo: the end location is currently not definable
                contentPackagesConfigFields.getMinPlayerCount(),
                contentPackagesConfigFields.getMaxPlayerCount());
        if (cancelled) return;
        super.lobbyLocation = lobbyLocation;
        this.contentPackagesConfigFields = contentPackagesConfigFields;
        for (String rawObjective : contentPackagesConfigFields.getRawDungeonObjectives())
            this.dungeonObjectives.add(DungeonObjective.registerObjective(this, rawObjective));
        this.world = world;
        this.instancedWorldName = world.getName();
        this.difficultyName = difficultyName;
        setDifficulty(difficultyName);
        addNewPlayer(player);
        new InitializeEntitiesTask(this, contentPackagesConfigFields, world).runTaskLater(MetadataHandler.PLUGIN, 20 * 3L);
        dungeonInstances.add(this);
        super.permission = contentPackagesConfigFields.getPermission();
    }

    public static void setupInstancedDungeon(Player player, String instancedDungeonConfigFieldsString, String difficultyName) {
        ContentPackagesConfigFields instancedDungeonsConfigFields = ContentPackagesConfig.getDungeonPackages().get(instancedDungeonConfigFieldsString);
        if (instancedDungeonsConfigFields == null) {
            player.sendMessage("[EliteMobs] Failed to get data for dungeon " + instancedDungeonConfigFieldsString + "! The dungeon will not start.");
            return;
        }

        if (instancedDungeonsConfigFields.getPermission() != null && !instancedDungeonsConfigFields.getPermission().isEmpty())
            if (!player.hasPermission(instancedDungeonsConfigFields.getPermission())) {
                player.sendMessage("[EliteMobs] You don't have the permission to go to this dungeon!");
                return;
            }

        String instancedWorldName = WorldInstantiator.getNewWorldName(instancedDungeonsConfigFields.getWorldName());

        if (!launchEvent(instancedDungeonsConfigFields, instancedWorldName, player)) return;

        WorldOperationQueue.queueOperation(
                player,
                () -> cloneWorldFiles(instancedDungeonsConfigFields, instancedWorldName, player) != null,
                () -> initializeInstancedWorld(instancedDungeonsConfigFields, instancedWorldName, player, difficultyName),
                instancedDungeonsConfigFields.getName()
        );
    }

    protected static boolean launchEvent(ContentPackagesConfigFields instancedDungeonsConfigFields, String instancedWordName, Player player) {
        WorldInstanceEvent worldInstanceEvent = new WorldInstanceEvent(
                instancedDungeonsConfigFields.getWorldName(),
                instancedWordName,
                instancedDungeonsConfigFields);
        new EventCaller(worldInstanceEvent);
        if (worldInstanceEvent.isCancelled()) {
            player.sendMessage("[EliteMobs] Something cancelled the instancing event! The dungeon will not start.");
            return false;
        }
        return true;
    }

    protected static File cloneWorldFiles(ContentPackagesConfigFields instancedDungeonsConfigFields, String instancedWordName, Player player) {
        File targetFile = WorldInstantiator.cloneWorld(instancedDungeonsConfigFields.getWorldName(), instancedWordName, instancedDungeonsConfigFields.getDungeonConfigFolderName());
        if (targetFile == null) {
            player.sendMessage("[EliteMobs] Failed to copy the world! Report this to the dev. The dungeon will not start.");
            return null;
        }
        return targetFile;
    }

    protected static DungeonInstance initializeInstancedWorld(ContentPackagesConfigFields instancedDungeonsConfigFields,
                                                              String instancedWordName,
                                                              Player player,
                                                              String difficultyName) {
        World world = DungeonUtils.loadWorld(instancedWordName, instancedDungeonsConfigFields.getEnvironment(), instancedDungeonsConfigFields);
        if (world == null) {
            player.sendMessage("[EliteMobs] Failed to load the world! Report this to the dev. The dungeon will not start.");
            return null;
        }

        //Location where players are teleported to start completing the dungeon
        Location startLocation = ConfigurationLocation.serialize(instancedDungeonsConfigFields.getStartLocationString());
        startLocation.setWorld(world);
        //Lobby location is optional, if null it should be the same as the start location
        Location lobbyLocation = ConfigurationLocation.serialize(instancedDungeonsConfigFields.getTeleportLocationString());
        if (lobbyLocation != null) lobbyLocation.setWorld(world);
        else lobbyLocation = startLocation;
        //Location where players are teleported to upon completion, this usually gets overriden with the previous location players were at
        //todo: will probably want to define this at some point Location endLocation = ConfigurationLocation.serialize(instancedDungeonsConfigFields.getEndLocation());
        //endLocation.setWorld(world);
        if (!instancedDungeonsConfigFields.isEnchantmentChallenge())
            return new DungeonInstance(instancedDungeonsConfigFields, lobbyLocation, startLocation, world, player, difficultyName);
        else
            return new EnchantmentDungeonInstance(instancedDungeonsConfigFields, lobbyLocation, startLocation, world, player, difficultyName);
    }

    @Override
    public boolean addNewPlayer(Player player) {
        if (!super.addNewPlayer(player)) return false;
        if (levelSync > 0)
            player.sendMessage("[EliteMobs] Dungeon difficulty is set to " + difficultyName + " ! Level sync caps your item level to " + levelSync + ".");
        return true;
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
        //if (!super.state.equals(InstancedRegionState.ONGOING)) return;
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
            removeInstance();
            return;
        }
        announce(DungeonsConfig.getInstancedDungeonCompleteMessage());
        new DestroyMatchTask().runTaskLater(MetadataHandler.PLUGIN, 2 * 60 * 20L);
    }

    @Override
    public void destroyMatch() {
        super.destroyMatch();
        removeInstance();
    }

    public void removeInstance() {
        participants.forEach(player -> player.sendMessage(DungeonsConfig.getInstancedDungeonClosingInstanceMessage()));
        HashSet<Player> participants = new HashSet<>(this.participants);
        participants.forEach(this::removeAnyKind);
        instances.remove(this);
        DungeonInstance dungeonInstance = this;
        if (world == null) {
            Logger.warn("Instanced dungeon's world was already unloaded before removing the entities in it! This shouldn't happen, but doesn't break anything.");
            return;
        }
        world.getEntities().forEach(entity -> EntityTracker.unregister(entity, RemovalReason.WORLD_UNLOAD));
        new RemoveInstanceTask(dungeonInstance).runTaskLater(MetadataHandler.PLUGIN, 20 * 30L);
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
            Logger.warn("Failed to set difficulty " + difficulty + " for instanced dungeon " + contentPackagesConfigFields.getFilename());
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
            instancedBossEntities = InstancedBossEntity.initializeInstancedBosses(contentPackagesConfigFields.getWorldName(), world, players.size(), dungeonInstance);
            NPCEntity.initializeInstancedNPCs(contentPackagesConfigFields.getWorldName(), world, players.size(), dungeonInstance);
            TreasureChest.initializeInstancedTreasureChests(contentPackagesConfigFields.getWorldName(), world);
        }
    }

    private class DestroyMatchTask extends BukkitRunnable {
        @Override
        public void run() {
            destroyMatch();
        }
    }

    private class RemoveInstanceTask extends BukkitRunnable {
        private final DungeonInstance dungeonInstance;

        public RemoveInstanceTask(DungeonInstance dungeonInstance) {
            this.dungeonInstance = dungeonInstance;
        }

        @Override
        public void run() {
            new EventCaller(new InstancedDungeonRemoveEvent(dungeonInstance));
            dungeonInstances.remove(dungeonInstance);
            TemporaryWorldManager.permanentlyDeleteWorld(world);
        }
    }
}
