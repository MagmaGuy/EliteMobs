package com.magmaguy.elitemobs.instanced.dungeons;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.EliteMobDeathEvent;
import com.magmaguy.elitemobs.api.WorldInstanceEvent;
import com.magmaguy.elitemobs.config.instanceddungeons.InstancedDungeonsConfig;
import com.magmaguy.elitemobs.config.instanceddungeons.InstancedDungeonsConfigFields;
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
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class DungeonInstance extends MatchInstance {
    @Getter
    private static final HashMap<String, DungeonInstance> dungeonInstances = new HashMap<>();
    private final List<DungeonObjective> dungeonObjectives = new ArrayList<>();
    private final World world;
    List<InstancedBossEntity> instancedBossEntities = new ArrayList<>();
    private final File instancedWorldFile;

    public DungeonInstance(InstancedDungeonsConfigFields instancedDungeonsConfigFields, Location startLocation, Location endLocation, World world, File instancedWorldFile) {
        super(startLocation, endLocation,
                instancedDungeonsConfigFields.getMinimumPlayerCount(),
                instancedDungeonsConfigFields.getMaximumPlayerCount());
        //todo: add dungeon objectives from the configuration
        for (String rawObjective : instancedDungeonsConfigFields.getRawDungeonObjectives())
            this.dungeonObjectives.add(DungeonObjective.registerObjective(this, rawObjective));
        //todo: check if this causes issues in async
        this.world = world;
        this.instancedWorldFile = instancedWorldFile;
        instancedBossEntities = InstancedBossEntity.initializeInstancedBosses(instancedDungeonsConfigFields.getWorldName(), world);
    }

    public static boolean setupInstancedDungeon(Player player, String instancedDungeonConfigFieldsString) {
        InstancedDungeonsConfigFields instancedDungeonsConfigFields = InstancedDungeonsConfig.getInstancedDungeonConfigFields(instancedDungeonConfigFieldsString);
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
                File targetFile = WorldInstantiator.cloneWorld(instancedDungeonsConfigFields.getWorldName(), instancedWordName);
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

                        Location startLocation = ConfigurationLocation.serialize(instancedDungeonsConfigFields.getStartLocation()).clone();
                        startLocation.setWorld(world);
                        Location endLocation = ConfigurationLocation.serialize(instancedDungeonsConfigFields.getEndLocation());
                        //endLocation.setWorld(world);
                        DungeonInstance dungeonInstance = new DungeonInstance(instancedDungeonsConfigFields, startLocation, endLocation, world, targetFile);
                        dungeonInstance.addPlayer(player);
                    }
                }.runTask(MetadataHandler.PLUGIN);
            }
        }.runTaskAsynchronously(MetadataHandler.PLUGIN);

        return true;
    }

    @Override
    protected void startMatch() {
        super.startMatch();
    }

    public void checkCompletionStatus() {
        //if (!super.state.equals(InstancedRegionState.ONGOING)) return;
        for (DungeonObjective dungeonObjective : dungeonObjectives)
            if (!dungeonObjective.isCompleted())
                return;
        //This means the dungeon just completed
        endMatch();
    }

    @Override
    public void endMatch() {
        super.endMatch();
        HashSet<Player> participants = new HashSet<>(super.participants);
        participants.forEach(this::removeAnyKind);
        instances.remove(this);
        Bukkit.unloadWorld(world, false);
        new BukkitRunnable() {
            @Override
            public void run() {
                WorldInstantiator.recursivelyDelete(instancedWorldFile);
            }
        }.runTaskAsynchronously(MetadataHandler.PLUGIN);
    }

    public static class DungeonInstanceEvents implements Listener {
        @EventHandler
        public void onEliteMobDeath(EliteMobDeathEvent event) {
            for (DungeonInstance dungeonInstance : dungeonInstances.values())
                dungeonInstance.checkCompletionStatus();
        }
    }
}
