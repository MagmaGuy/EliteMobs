package com.magmaguy.elitemobs.instanced.dungeons;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.DungeonsConfig;
import com.magmaguy.elitemobs.config.contentpackages.ContentPackagesConfig;
import com.magmaguy.elitemobs.config.contentpackages.ContentPackagesConfigFields;
import com.magmaguy.elitemobs.dungeons.utility.DungeonUtils;
import com.magmaguy.elitemobs.instanced.WorldOperationQueue;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomMusic;
import com.magmaguy.elitemobs.mobconstructor.custombosses.InstancedBossEntity;
import com.magmaguy.elitemobs.quests.DynamicQuest;
import com.magmaguy.elitemobs.utils.ConfigurationLocation;
import com.magmaguy.elitemobs.utils.WorldInstantiator;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

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
        super(contentPackagesConfigFields, lobbyLocation, startLocation, world, player, difficultyName);
        this.selectedLevel = selectedLevel;

        // Recalculate level sync for dynamic dungeons based on the player-selected level
        recalculateLevelSyncForDynamicLevel(selectedLevel);

        new SetBossLevelsTask(this, selectedLevel).runTaskLater(MetadataHandler.PLUGIN, 20 * 4L);
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

        String instancedWorldName = WorldInstantiator.getNewWorldName(dynamicDungeonConfigFields.getWorldName());

        if (!launchEvent(dynamicDungeonConfigFields, instancedWorldName, player)) return;

        WorldOperationQueue.queueOperation(
                player,
                () -> cloneWorldFiles(dynamicDungeonConfigFields, instancedWorldName, player) != null,
                () -> initializeDynamicWorld(dynamicDungeonConfigFields, instancedWorldName, player, difficultyName, selectedLevel),
                dynamicDungeonConfigFields.getName()
        );
    }

    protected static DynamicDungeonInstance initializeDynamicWorld(ContentPackagesConfigFields dynamicDungeonConfigFields,
                                                                   String instancedWorldName,
                                                                   Player player,
                                                                   String difficultyName,
                                                                   int selectedLevel) {
        World world = DungeonUtils.loadWorld(instancedWorldName, dynamicDungeonConfigFields.getEnvironment(), dynamicDungeonConfigFields);
        if (world == null) {
            player.sendMessage(DungeonsConfig.getDynamicDungeonWorldLoadFailedMessage());
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

        return new DynamicDungeonInstance(dynamicDungeonConfigFields, lobbyLocation, startLocation, world, player, difficultyName, selectedLevel);
    }

    @Override
    public boolean addNewPlayer(Player player) {
        if (!super.addNewPlayer(player)) return false;
        // Show additional info about the selected level for dynamic dungeons
        player.sendMessage(DungeonsConfig.getDynamicDungeonLevelSetMessage().replace("$level", String.valueOf(selectedLevel)));

        // Adapt player's active DynamicQuests to the dungeon's selected level
        DynamicQuest.adaptPlayerQuestsToLevel(player, selectedLevel);

        return true;
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
