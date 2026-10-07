package com.magmaguy.elitemobs.dungeons;

import com.magmaguy.elitemobs.config.contentpackages.ContentPackagesConfigFields;
import com.magmaguy.elitemobs.dungeons.utility.DungeonUtils;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.mobconstructor.custombosses.RegionalBossEntity;
import com.magmaguy.elitemobs.npcs.NPCEntity;
import com.magmaguy.elitemobs.treasurechest.TreasureChest;
import com.magmaguy.magmacore.util.Logger;
import com.magmaguy.magmacore.util.WorldFolderResolver;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is specifically for world-based dungeons
 */
public class WorldDungeonPackage extends WorldPackage implements Dungeon {
    private int lowestLevel;
    private int highestLevel;
    // No initializers on these two: the EMPackage constructor runs baseInitialization before this class's
    // initializers, and an initializer would then discard the worlds it loaded.
    @Getter
    private World wormholeWorld;
    @Getter
    private List<World> retiredWorlds;

    public WorldDungeonPackage(ContentPackagesConfigFields contentPackagesConfigFields) {
        super(contentPackagesConfigFields);
    }

    @Override
    public void baseInitialization() {
        super.baseInitialization();
        initializeWormholeWorld();
        initializeRetiredWorlds();
    }

    private void initializeRetiredWorlds() {
        retiredWorlds = new ArrayList<>();
        List<String> retiredWorldNames = contentPackagesConfigFields.getRetiredWorldNames();
        // Without an installed package there is nowhere to send players, so the server's own fallback places them.
        if (retiredWorldNames == null || !isInstalled) return;
        for (String retiredWorldName : retiredWorldNames) {
            // A blank name resolves to the server folder itself, which folderExists reports as present.
            if (retiredWorldName == null || retiredWorldName.isBlank()) continue;
            if (Bukkit.getWorld(retiredWorldName) == null && !WorldFolderResolver.folderExists(retiredWorldName)) continue;
            if (RetiredWorldRedirect.isInUse(retiredWorldName)) {
                Logger.warn("Retired world " + retiredWorldName + " of package " + contentPackagesConfigFields.getFilename()
                        + " was not loaded: it is the default world or still used by a package.");
                continue;
            }
            Location teleportLocation = contentPackagesConfigFields.getTeleportLocation();
            if (teleportLocation == null || !teleportLocation.isWorldLoaded()) {
                Logger.warn("Retired world " + retiredWorldName + " was not loaded: package "
                        + contentPackagesConfigFields.getFilename() + " has no loaded teleportLocation to send players to.");
                continue;
            }
            World retiredWorld = DungeonUtils.loadWorld(retiredWorldName, contentPackagesConfigFields.getEnvironment(),
                    contentPackagesConfigFields);
            // Names differing only in case resolve to the same world.
            if (retiredWorld == null || retiredWorlds.contains(retiredWorld)) continue;
            retiredWorlds.add(retiredWorld);
            RetiredWorldRedirect.register(retiredWorld, contentPackagesConfigFields);
        }
    }

    private void initializeWormholeWorld() {
        // A blank name would resolve to the server folder itself.
        if (contentPackagesConfigFields.getWormholeWorldName() != null &&
                !contentPackagesConfigFields.getWormholeWorldName().isBlank()) {
            // Check if the wormhole world file exists before attempting to load it
            String wormholeWorldName = contentPackagesConfigFields.getWormholeWorldName();
            // Picks up the wormhole world at either legacy or Paper-26.1+ modern layout.
            boolean wormholeWorldExists = Bukkit.getWorld(wormholeWorldName) != null ||
                    WorldFolderResolver.folderExists(wormholeWorldName);

            if (wormholeWorldExists) {
                wormholeWorld = DungeonUtils.loadWorld(this.getContentPackagesConfigFields().getWormholeWorldName(), this.getContentPackagesConfigFields().getEnvironment(), contentPackagesConfigFields);
            }
            // If the world doesn't exist, silently skip loading (no warning needed as it's optional)
        }
    }

    @Override
    public void doInstall(Player player) {
        super.doInstall(player);
        if (!isInstalled) return;
        // WorldPackage.doInstall loads these worlds, but only baseInitialization records them,
        // and an earlier uninstall in this session cleared both.
        initializeWormholeWorld();
        initializeRetiredWorlds();
    }

    @Override
    public void initializeContent() {
        if (isInstalled) {
            getEntities();
            qualifyEntities();
            getChests();
            getNPCs();
        }
    }

    @Override
    public void doUninstall(Player player) {
        // Call parent to unload worlds (handles wormholeWorld too) and persist isEnabled = false
        super.doUninstall(player);
        if (isInstalled) return;

        customBossEntityList.clear();
        treasureChestList.clear();
        npcEntities.clear();
        wormholeWorld = null;
        for (World retiredWorld : retiredWorlds) RetiredWorldRedirect.unregister(retiredWorld.getName());
        retiredWorlds.clear();
    }

    private void getEntities() {
        for (RegionalBossEntity regionalBossEntity : RegionalBossEntity.getRegionalBossEntitySet())
            if (regionalBossEntity != null && matchesPackageWorld(regionalBossEntity.getWorldName()))
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
            if (treasureChest != null && matchesPackageWorld(treasureChest.getWorldName()))
                treasureChestList.add(treasureChest);
    }

    private void getNPCs() {
        for (NPCEntity npcEntity : EntityTracker.getNpcEntities().values())
            if (npcEntity != null && matchesPackageWorld(npcEntity.getWorldName()))
                npcEntities.add(npcEntity);
    }

    private boolean matchesPackageWorld(String worldName) {
        if (worldName == null) return false;
        return world != null && worldName.equals(world.getName()) ||
                wormholeWorld != null && worldName.equals(wormholeWorld.getName());
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
