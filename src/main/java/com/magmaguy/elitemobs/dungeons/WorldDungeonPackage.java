package com.magmaguy.elitemobs.dungeons;

import com.magmaguy.elitemobs.EliteMobs;
import com.magmaguy.elitemobs.config.dungeonpackager.DungeonPackagerConfigFields;
import com.magmaguy.elitemobs.dungeons.utility.DungeonUtils;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.mobconstructor.custombosses.RegionalBossEntity;
import com.magmaguy.elitemobs.npcs.NPCEntity;
import com.magmaguy.elitemobs.thirdparty.worldguard.WorldGuardCompatibility;
import com.magmaguy.elitemobs.treasurechest.TreasureChest;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.Objects;

/**
 * This class is specifically for world-based dungeons
 */
public class WorldDungeonPackage extends WorldPackage implements Dungeon {
    private int lowestLevel;
    private int highestLevel;
    @Getter
    private World wormholeWorld = null;

    public WorldDungeonPackage(DungeonPackagerConfigFields dungeonPackagerConfigFields) {
        super(dungeonPackagerConfigFields);
    }

    @Override
    public void baseInitialization() {
        super.baseInitialization();
        initializeWormholeWorld();
    }

    private void initializeWormholeWorld() {
        if (dungeonPackagerConfigFields.getWormholeWorldName() != null &&
                !dungeonPackagerConfigFields.getWormholeWorldName().isEmpty() &&
                Bukkit.getWorld(dungeonPackagerConfigFields.getWormholeWorldName()) == null) {
            wormholeWorld = DungeonUtils.loadWorld(this.getDungeonPackagerConfigFields().getWormholeWorldName(), this.getDungeonPackagerConfigFields().getEnvironment());
            if (wormholeWorld != null && EliteMobs.worldGuardIsEnabled)
                WorldGuardCompatibility.protectWorldMinidugeonArea(Objects.requireNonNull(wormholeWorld).getSpawnLocation(), this);
        }
    }

    @Override
    public void initializeContent() {
        super.initializeContent();
        if (isInstalled) {
            getEntities();
            qualifyEntities();
            getChests();
            getNPCs();
        }
    }

    @Override
    public boolean uninstall(Player player) {
        if (!super.uninstall(player)) return false;
        customBossEntityList.clear();
        treasureChestList.clear();
        return true;
    }

    private void getEntities() {
        for (RegionalBossEntity regionalBossEntity : RegionalBossEntity.getRegionalBossEntitySet())
            if (regionalBossEntity != null &&
                    regionalBossEntity.getWorldName() != null &&
                    world != null &&
                    regionalBossEntity.getWorldName().equals(world.getName()) ||
                    wormholeWorld != null && regionalBossEntity.getWorldName().equals(wormholeWorld.getName()))
                customBossEntityList.add(regionalBossEntity);
    }

    private void qualifyEntities() {
        //Initialize data related to the highest and lowest levels for informational purposes
        DungeonUtils.Pair lowestAndHighestValues = DungeonUtils.getLowestAndHighestLevels(customBossEntityList);
        this.lowestLevel = lowestAndHighestValues.getLowestValue();
        this.highestLevel = lowestAndHighestValues.getHighestValue();
    }

    private void getChests() {
        for (TreasureChest treasureChest : TreasureChest.getTreasureChestHashMap().values())
            if (treasureChest.getWorldName() != null && world != null && treasureChest.getWorldName().equals(world.getName()) ||
                    wormholeWorld != null &&
                            treasureChest.getWorldName().equals(wormholeWorld.getName()))
                treasureChestList.add(treasureChest);
    }

    private void getNPCs() {
        if (world == null) return;
        for (NPCEntity npcEntity : EntityTracker.getNpcEntities().values())
            if (npcEntity.getWorldName().equals(world.getName()) ||
                    wormholeWorld != null &&
                            npcEntity.getWorldName().equals(wormholeWorld.getName()))
                npcEntities.add(npcEntity);
    }

    @Override
    public int getLowestLevel() {
        return lowestLevel;
    }

    @Override
    public int getHighestLevel() {
        return highestLevel;
    }
}
