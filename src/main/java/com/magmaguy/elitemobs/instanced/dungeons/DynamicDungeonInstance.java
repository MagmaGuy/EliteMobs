package com.magmaguy.elitemobs.instanced.dungeons;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.contentpackages.ContentPackagesConfig;
import com.magmaguy.elitemobs.config.contentpackages.ContentPackagesConfigFields;
import com.magmaguy.elitemobs.dungeons.utility.DungeonUtils;
import com.magmaguy.elitemobs.mobconstructor.custombosses.InstancedBossEntity;
import com.magmaguy.elitemobs.utils.ConfigurationLocation;
import com.magmaguy.elitemobs.utils.WorldInstantiator;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.concurrent.CompletableFuture;

public class DynamicDungeonInstance extends DungeonInstance {
    @Getter
    private final int selectedLevel;

    public DynamicDungeonInstance(ContentPackagesConfigFields contentPackagesConfigFields,
                                  Location lobbyLocation,
                                  Location startLocation,
                                  World world,
                                  File instancedWorldFile,
                                  Player player,
                                  String difficultyName,
                                  int selectedLevel) {
        super(contentPackagesConfigFields, lobbyLocation, startLocation, world, instancedWorldFile, player, difficultyName);
        this.selectedLevel = selectedLevel;

        // Recalculate level sync for dynamic dungeons based on the player-selected level
        recalculateLevelSyncForDynamicLevel(selectedLevel);

        new SetBossLevelsTask(this, selectedLevel).runTaskLater(MetadataHandler.PLUGIN, 20 * 4L);
    }

    public static void setupDynamicDungeon(Player player, String dungeonConfigFieldsString, String difficultyName, int selectedLevel) {
        ContentPackagesConfigFields dynamicDungeonConfigFields = ContentPackagesConfig.getDungeonPackages().get(dungeonConfigFieldsString);
        if (dynamicDungeonConfigFields == null) {
            player.sendMessage("[EliteMobs] Failed to get data for dynamic dungeon " + dungeonConfigFieldsString + "! The dungeon will not start.");
            return;
        }

        if (dynamicDungeonConfigFields.getPermission() != null && !dynamicDungeonConfigFields.getPermission().isEmpty())
            if (!player.hasPermission(dynamicDungeonConfigFields.getPermission())) {
                player.sendMessage("[EliteMobs] You don't have the permission to go to this dungeon!");
                return;
            }

        String instancedWorldName = WorldInstantiator.getNewWorldName(dynamicDungeonConfigFields.getWorldName());

        if (!launchEvent(dynamicDungeonConfigFields, instancedWorldName, player)) return;

        CompletableFuture<File> future = CompletableFuture.supplyAsync(() ->
                cloneWorldFiles(dynamicDungeonConfigFields, instancedWorldName, player));
        future.thenAccept(file -> {
            if (file == null) return;
            new InitializeDynamicWorldTask(dynamicDungeonConfigFields, instancedWorldName, player, file, difficultyName, selectedLevel).runTask(MetadataHandler.PLUGIN);
        });
    }

    protected static DynamicDungeonInstance initializeDynamicWorld(ContentPackagesConfigFields dynamicDungeonConfigFields,
                                                                   String instancedWorldName,
                                                                   Player player,
                                                                   File targetFile,
                                                                   String difficultyName,
                                                                   int selectedLevel) {
        World world = DungeonUtils.loadWorld(instancedWorldName, dynamicDungeonConfigFields.getEnvironment(), dynamicDungeonConfigFields);
        if (world == null) {
            player.sendMessage("[EliteMobs] Failed to load the world! Report this to the dev. The dungeon will not start.");
            return null;
        }

        Location startLocation = ConfigurationLocation.serialize(dynamicDungeonConfigFields.getStartLocationString());
        startLocation.setWorld(world);
        Location lobbyLocation = ConfigurationLocation.serialize(dynamicDungeonConfigFields.getTeleportLocationString());
        if (lobbyLocation != null) lobbyLocation.setWorld(world);
        else lobbyLocation = startLocation;

        return new DynamicDungeonInstance(dynamicDungeonConfigFields, lobbyLocation, startLocation, world, targetFile, player, difficultyName, selectedLevel);
    }

    @Override
    public boolean addNewPlayer(Player player) {
        if (!super.addNewPlayer(player)) return false;
        // Show additional info about the selected level for dynamic dungeons
        player.sendMessage("[EliteMobs] Dynamic dungeon level set to " + selectedLevel + "!");
        return true;
    }

    private static class InitializeDynamicWorldTask extends BukkitRunnable {
        private final ContentPackagesConfigFields dynamicDungeonConfigFields;
        private final String instancedWorldName;
        private final Player player;
        private final File file;
        private final String difficultyName;
        private final int selectedLevel;

        public InitializeDynamicWorldTask(ContentPackagesConfigFields dynamicDungeonConfigFields,
                                          String instancedWorldName,
                                          Player player,
                                          File file,
                                          String difficultyName,
                                          int selectedLevel) {
            this.dynamicDungeonConfigFields = dynamicDungeonConfigFields;
            this.instancedWorldName = instancedWorldName;
            this.player = player;
            this.file = file;
            this.difficultyName = difficultyName;
            this.selectedLevel = selectedLevel;
        }

        @Override
        public void run() {
            initializeDynamicWorld(dynamicDungeonConfigFields, instancedWorldName, player, file, difficultyName, selectedLevel);
        }
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
