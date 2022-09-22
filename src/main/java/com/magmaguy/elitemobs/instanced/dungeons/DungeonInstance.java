package com.magmaguy.elitemobs.instanced.dungeons;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.DungeonCompleteEvent;
import com.magmaguy.elitemobs.api.DungeonStartEvent;
import com.magmaguy.elitemobs.api.WorldInstanceEvent;
import com.magmaguy.elitemobs.config.dungeonpackager.DungeonPackagerConfig;
import com.magmaguy.elitemobs.config.dungeonpackager.DungeonPackagerConfigFields;
import com.magmaguy.elitemobs.dungeons.utility.DungeonUtils;
import com.magmaguy.elitemobs.instanced.MatchInstance;
import com.magmaguy.elitemobs.mobconstructor.custombosses.InstancedBossEntity;
import com.magmaguy.elitemobs.thirdparty.worldguard.WorldGuardCompatibility;
import com.magmaguy.elitemobs.utils.ConfigurationLocation;
import com.magmaguy.elitemobs.utils.EventCaller;
import com.magmaguy.elitemobs.utils.WorldInstantiator;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DungeonInstance extends MatchInstance {
    @Getter
    private static final Set<DungeonInstance> dungeonInstances = new HashSet<>();
    private final List<DungeonObjective> dungeonObjectives = new ArrayList<>();
    private final World world;
    private final File instancedWorldFile;
    @Getter
    private final DungeonPackagerConfigFields dungeonPackagerConfigFields;
    List<InstancedBossEntity> instancedBossEntities = new ArrayList<>();

    public DungeonInstance(DungeonPackagerConfigFields dungeonPackagerConfigFields,
                           Location lobbyLocation,
                           Location startLocation,
                           World world,
                           File instancedWorldFile,
                           Player player) {
        super(startLocation,
                null, //todo: the end location is currently not definable
                dungeonPackagerConfigFields.getMinPlayerCount(),
                dungeonPackagerConfigFields.getMaxPlayerCount());
        super.lobbyLocation = lobbyLocation;
        this.dungeonPackagerConfigFields = dungeonPackagerConfigFields;
        for (String rawObjective : dungeonPackagerConfigFields.getRawDungeonObjectives())
            this.dungeonObjectives.add(DungeonObjective.registerObjective(this, rawObjective));
        this.world = world;
        this.instancedWorldFile = instancedWorldFile;
        addNewPlayer(player);
        instancedBossEntities = InstancedBossEntity.initializeInstancedBosses(dungeonPackagerConfigFields.getWorldName(), world, players.size());
        dungeonInstances.add(this);
    }

    public static boolean setupInstancedDungeon(Player player, String instancedDungeonConfigFieldsString) {
        DungeonPackagerConfigFields instancedDungeonsConfigFields = DungeonPackagerConfig.getDungeonPackages().get(instancedDungeonConfigFieldsString);
        if (instancedDungeonsConfigFields == null) {
            player.sendMessage("[EliteMobs] Failed to get data for dungeon " + instancedDungeonConfigFieldsString + "! The dungeon will not start.");
            return false;
        }

        if (instancedDungeonsConfigFields.getPermission() != null && !instancedDungeonsConfigFields.getPermission().isEmpty())
            if (!player.hasPermission(instancedDungeonsConfigFields.getPermission())) {
                player.sendMessage("[EliteMobs] You don't have the permission to go to this dungeon!");
                return false;
            }

        String instancedWordName = WorldInstantiator.getNewWorldName(instancedDungeonsConfigFields.getWorldName());

        WorldInstanceEvent worldInstanceEvent = new WorldInstanceEvent(
                instancedDungeonsConfigFields.getWorldName(),
                instancedWordName,
                instancedDungeonsConfigFields);
        new EventCaller(worldInstanceEvent);
        if (worldInstanceEvent.isCancelled()) {
            player.sendMessage("[EliteMobs] Something cancelled the instancing event! The dungeon will not start.");
            return false;
        }
        new BukkitRunnable() {
            @Override
            public void run() {
                File targetFile = WorldInstantiator.cloneWorld(instancedDungeonsConfigFields.getWorldName(), instancedWordName, instancedDungeonsConfigFields.getDungeonConfigFolderName());
                if (targetFile == null) {
                    player.sendMessage("[EliteMobs] Failed to copy the world! Report this to the dev. The dungeon will not start.");
                    return;
                }

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        World world = DungeonUtils.loadWorld(instancedWordName, instancedDungeonsConfigFields.getEnvironment());
                        if (world == null) {
                            player.sendMessage("[EliteMobs] Failed to load the world! Report this to the dev. The dungeon will not start.");
                            return;
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
                        DungeonInstance dungeonInstance = new DungeonInstance(instancedDungeonsConfigFields, lobbyLocation, startLocation, world, targetFile, player);
                    }
                }.runTask(MetadataHandler.PLUGIN);
            }
        }.runTaskAsynchronously(MetadataHandler.PLUGIN);

        return true;
    }

    @Override
    protected void startMatch() {
        super.startMatch();
        new EventCaller(new DungeonStartEvent(this));
    }

    public boolean checkCompletionStatus() {
        //if (!super.state.equals(InstancedRegionState.ONGOING)) return;
        for (DungeonObjective dungeonObjective : dungeonObjectives)
            if (!dungeonObjective.isCompleted())
                return false;
        new EventCaller(new DungeonCompleteEvent(this));
        //This means the dungeon just completed
        endMatch();
        return true;
    }

    @Override
    public void endMatch() {
        super.endMatch();
        announce("[EliteMobs] Completed! Arena will self-destruct in 5 minutes!");
        announce("MagmaGuy's note: This is still a work in progress, please be patient! Hope you enjoyed your run.");
        DungeonInstance dungeonInstance = this;
        new BukkitRunnable() {
            final DungeonInstance laterInstance = dungeonInstance;

            @Override
            public void run() {
                HashSet<Player> participants = new HashSet<>(laterInstance.participants);
                participants.forEach(laterInstance::removeAnyKind);
                instances.remove(laterInstance);
                Bukkit.unloadWorld(world, false);
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        WorldInstantiator.recursivelyDelete(instancedWorldFile);
                    }
                }.runTaskAsynchronously(MetadataHandler.PLUGIN);
                dungeonInstances.remove(laterInstance);
            }
        }.runTaskLater(MetadataHandler.PLUGIN, 5 * 60 * 20L);


    }

    @Override
    protected boolean isInRegion(Location location) {
        return location.getWorld().equals(startLocation.getWorld());
    }

}
