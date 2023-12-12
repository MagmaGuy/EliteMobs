package com.magmaguy.elitemobs.instanced.dungeons;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.DungeonCompleteEvent;
import com.magmaguy.elitemobs.api.DungeonStartEvent;
import com.magmaguy.elitemobs.api.InstancedDungeonRemoveEvent;
import com.magmaguy.elitemobs.api.WorldInstanceEvent;
import com.magmaguy.elitemobs.api.internal.RemovalReason;
import com.magmaguy.elitemobs.config.dungeonpackager.DungeonPackagerConfig;
import com.magmaguy.elitemobs.config.dungeonpackager.DungeonPackagerConfigFields;
import com.magmaguy.elitemobs.dungeons.utility.DungeonUtils;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.instanced.MatchInstance;
import com.magmaguy.elitemobs.mobconstructor.custombosses.InstancedBossEntity;
import com.magmaguy.elitemobs.npcs.NPCEntity;
import com.magmaguy.elitemobs.thirdparty.worldguard.WorldGuardCompatibility;
import com.magmaguy.elitemobs.utils.*;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class DungeonInstance extends MatchInstance {
    @Getter
    private static final Set<DungeonInstance> dungeonInstances = new HashSet<>();
    private final List<DungeonObjective> dungeonObjectives = new ArrayList<>();
    @Getter
    private final World world;
    @Getter
    private final String instancedWorldName;
    private final File instancedWorldFile;
    @Getter
    private final DungeonPackagerConfigFields dungeonPackagerConfigFields;
    private List<InstancedBossEntity> instancedBossEntities = new ArrayList<>();
    @Getter
    private int levelSync = -1;
    private String difficultyName = null;
    @Getter
    private String difficultyID = null;

    public DungeonInstance(DungeonPackagerConfigFields dungeonPackagerConfigFields,
                           Location lobbyLocation,
                           Location startLocation,
                           World world,
                           File instancedWorldFile,
                           Player player,
                           String difficultyName) {
        super(startLocation,
                null, //todo: the end location is currently not definable
                dungeonPackagerConfigFields.getMinPlayerCount(),
                dungeonPackagerConfigFields.getMaxPlayerCount());
        super.lobbyLocation = lobbyLocation;
        this.dungeonPackagerConfigFields = dungeonPackagerConfigFields;
        for (String rawObjective : dungeonPackagerConfigFields.getRawDungeonObjectives())
            this.dungeonObjectives.add(DungeonObjective.registerObjective(this, rawObjective));
        this.world = world;
        this.instancedWorldName = world.getName();
        this.instancedWorldFile = instancedWorldFile;
        this.difficultyName = difficultyName;
        setDifficulty(difficultyName);
        addNewPlayer(player);
        instancedBossEntities = InstancedBossEntity.initializeInstancedBosses(dungeonPackagerConfigFields.getWorldName(), world, players.size(), this);
        NPCEntity.initializeInstancedNPCs(dungeonPackagerConfigFields.getWorldName(), world, players.size(), this);
        dungeonInstances.add(this);
        super.permission = dungeonPackagerConfigFields.getPermission();
    }

    public static void setupInstancedDungeon(Player player, String instancedDungeonConfigFieldsString, String difficultyName) {
        DungeonPackagerConfigFields instancedDungeonsConfigFields = DungeonPackagerConfig.getDungeonPackages().get(instancedDungeonConfigFieldsString);
        if (instancedDungeonsConfigFields == null) {
            player.sendMessage("[EliteMobs] Failed to get data for dungeon " + instancedDungeonConfigFieldsString + "! The dungeon will not start.");
            return;
        }

        if (instancedDungeonsConfigFields.getPermission() != null && !instancedDungeonsConfigFields.getPermission().isEmpty())
            if (!player.hasPermission(instancedDungeonsConfigFields.getPermission())) {
                player.sendMessage("[EliteMobs] You don't have the permission to go to this dungeon!");
                return;
            }

        String instancedWordName = WorldInstantiator.getNewWorldName(instancedDungeonsConfigFields.getWorldName());

        if (!launchEvent(instancedDungeonsConfigFields, instancedWordName, player)) return;

        CompletableFuture<File> future = CompletableFuture.supplyAsync(() ->
                cloneWorldFiles(instancedDungeonsConfigFields, instancedWordName, player));
        future.thenAccept(file -> {
            if (file == null) return;
            new BukkitRunnable() {
                @Override
                public void run() {
                    initializeInstancedWorld(instancedDungeonsConfigFields, instancedWordName, player, file, difficultyName);
                }
            }.runTask(MetadataHandler.PLUGIN);
        });
    }

    protected static boolean launchEvent(DungeonPackagerConfigFields instancedDungeonsConfigFields, String instancedWordName, Player player) {
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

    protected static File cloneWorldFiles(DungeonPackagerConfigFields instancedDungeonsConfigFields, String instancedWordName, Player player) {
        File targetFile = WorldInstantiator.cloneWorld(instancedDungeonsConfigFields.getWorldName(), instancedWordName, instancedDungeonsConfigFields.getDungeonConfigFolderName());
        if (targetFile == null) {
            player.sendMessage("[EliteMobs] Failed to copy the world! Report this to the dev. The dungeon will not start.");
            return null;
        }
        return targetFile;
    }

    protected static DungeonInstance initializeInstancedWorld(DungeonPackagerConfigFields instancedDungeonsConfigFields,
                                                              String instancedWordName,
                                                              Player player,
                                                              File targetFile,
                                                              String difficultyName) {
        World world = DungeonUtils.loadWorld(instancedWordName, instancedDungeonsConfigFields.getEnvironment());
        if (world == null) {
            player.sendMessage("[EliteMobs] Failed to load the world! Report this to the dev. The dungeon will not start.");
            return null;
        }
        if (Bukkit.getPluginManager().getPlugin("WorldGuard") != null)
            WorldGuardCompatibility.protectWorldMinidugeonArea(world);

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
            return new DungeonInstance(instancedDungeonsConfigFields, lobbyLocation, startLocation, world, targetFile, player, difficultyName);
        else
            return new EnchantmentDungeonInstance(instancedDungeonsConfigFields, lobbyLocation, startLocation, world, targetFile, player, difficultyName);
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
        announce("[EliteMobs] Completed! Arena will self-destruct in 2 minutes!");
        announce("MagmaGuy's note: This is still a work in progress, please be patient! Hope you enjoyed your run.");
        new BukkitRunnable() {

            @Override
            public void run() {
                removeInstance();
            }
        }.runTaskLater(MetadataHandler.PLUGIN, 2 * 60 * 20L);
    }

    public void removeInstance() {
        participants.forEach(player -> player.sendMessage("[EliteMobs] Closing instance!"));
        HashSet<Player> participants = new HashSet<>(this.participants);
        participants.forEach(this::removeAnyKind);
        instances.remove(this);
        DungeonInstance dungeonInstance = this;
        if (world == null) {
            new WarningMessage("Instanced dungeon's world was already unloaded before removing the entities in it! This shouldn't happen, but doesn't break anything.");
            return;
        }
        world.getEntities().forEach(entity -> EntityTracker.unregister(entity, RemovalReason.WORLD_UNLOAD));
        new BukkitRunnable() {
            @Override
            public void run() {
                //The world might get removed before this timer
                //if (world != null) {
                //Arrays.stream(world.getLoadedChunks()).forEach(chunk -> chunk.unload(false));
                if (!Bukkit.unloadWorld(world, false)) {
                    new WarningMessage("Failed to unload world " + instancedWorldName + " ! This is bad, report this to the developer!");
                }
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        WorldInstantiator.recursivelyDelete(instancedWorldFile);
                    }
                }.runTaskAsynchronously(MetadataHandler.PLUGIN);
                //}
                new EventCaller(new InstancedDungeonRemoveEvent(dungeonInstance));
                dungeonInstances.remove(dungeonInstance);
            }
        }.runTaskLater(MetadataHandler.PLUGIN, 120);

    }

    private void setDifficulty(String difficultyName) {
        if (difficultyName == null) return;
        if (dungeonPackagerConfigFields.getDifficulties() == null ||
                dungeonPackagerConfigFields.getDifficulties().isEmpty())
            return;
        Map difficulty = null;
        for (Map difficultyMap : dungeonPackagerConfigFields.getDifficulties())
            if (difficultyMap.get("name") != null && difficultyMap.get("name").equals(difficultyName)) {
                difficulty = difficultyMap;
                break;
            }
        if (difficulty == null) {
            new WarningMessage("Failed to set difficulty " + difficulty + " for instanced dungeon " + dungeonPackagerConfigFields.getFilename());
            return;
        }

        if (difficulty.get("levelSync") != null) {
            try {
                this.levelSync = MapListInterpreter.parseInteger("levelSync", difficulty.get("levelSync"), dungeonPackagerConfigFields.getFilename());
            } catch (Exception exception) {
                new WarningMessage("Incorrect level sync entry for dungeon " + dungeonPackagerConfigFields.getFilename() + " ! Value: " + levelSync + " . No level sync will be applied!");
                this.levelSync = 0;
            }
        } else
            this.levelSync = 0;

        //Used for loot
        if (difficulty.get("id") != null) {
            this.difficultyID = MapListInterpreter.parseString("id", difficulty.get("id"), dungeonPackagerConfigFields.getFilename());
        }
    }

    @Override
    protected boolean isInRegion(Location location) {
        return location.getWorld().equals(startLocation.getWorld());
    }

}
