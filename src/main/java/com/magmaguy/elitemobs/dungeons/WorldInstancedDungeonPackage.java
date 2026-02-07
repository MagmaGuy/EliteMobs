package com.magmaguy.elitemobs.dungeons;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.DungeonInstallEvent;
import com.magmaguy.elitemobs.api.DungeonUninstallEvent;
import com.magmaguy.elitemobs.config.contentpackages.ContentPackagesConfig;
import com.magmaguy.elitemobs.config.contentpackages.ContentPackagesConfigFields;
import com.magmaguy.elitemobs.utils.EventCaller;
import com.magmaguy.magmacore.util.ChatColorConverter;
import com.magmaguy.magmacore.util.Logger;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.File;

public class WorldInstancedDungeonPackage extends EMPackage implements CombatContent {
    private int level;

    public WorldInstancedDungeonPackage(ContentPackagesConfigFields contentPackagesConfigFields) {
        super(contentPackagesConfigFields);
    }

    @Override
    public void doInstall(Player player) {
        DungeonInstallEvent event = new DungeonInstallEvent(contentPackagesConfigFields);
        new EventCaller(event);
        contentPackagesConfigFields.simpleInstall();
        player.sendMessage(ChatColorConverter.convert("&2Dungeon " + contentPackagesConfigFields.getFilename() + " installed!"));
        if (!contentPackagesConfigFields.isEnchantmentChallenge()) {
            player.sendMessage(ChatColorConverter.convert("&6Instanced dungeons must be accessed either through the &a/em &6menu or an NPC! NPCs for premade EliteMobs content can be found at the Adventurer's Guild Hub map."));
            player.sendMessage("Remember that instanced dungeons create a world when you join them and remove that world when you are done playing in them!");
        } else {
            player.sendMessage(ChatColorConverter.convert("&8[EliteMobs] &2Enchantment instanced dungeon installed! This dungeon can only be accessed when attempting to enchant items and getting a challenge as a result!"));
            ContentPackagesConfig.getEnchantedChallengeDungeonPackages().put(contentPackagesConfigFields.getFilename(), contentPackagesConfigFields);
        }
        this.isInstalled = true;
    }

    @Override
    public void doUninstall(Player player) {
        DungeonUninstallEvent event = new DungeonUninstallEvent(contentPackagesConfigFields);
        new EventCaller(event);
        contentPackagesConfigFields.simpleUninstall();
        isInstalled = false;
        player.sendMessage(ChatColorConverter.convert("&a[EliteMobs] Successfully uninstalled " + contentPackagesConfigFields.getName() + "!"));
    }

    @Override
    public void baseInitialization() {
        this.level = contentPackagesConfigFields.getContentLevel();
        File file = new File(MetadataHandler.PLUGIN.getDataFolder().getAbsolutePath() +
                File.separatorChar + "world_blueprints" + File.separatorChar + contentPackagesConfigFields.getDungeonConfigFolderName());
        if (!file.exists()) {
            this.isDownloaded = false;
            this.isInstalled = false;
            return;
        } else {
            //This removes all instanced worlds not previously correctly removed
            for (File worldFile : Bukkit.getWorldContainer().listFiles()) {
                if (worldFile.getName().contains(file.getName()) && worldFile.getName().matches(".*_\\d{1,2}$")) {
                    try {
                        FileUtils.deleteDirectory(worldFile);
                        Logger.info("Removing previously instanced world " + worldFile.getName());
                    } catch (Exception e) {
                        Logger.warn("Failed to remove previously instanced world " + worldFile.getName());
                    }
                }
            }
        }
        this.isDownloaded = true;
        this.isInstalled = contentPackagesConfigFields.isEnabled();
    }

    @Override
    public void initializeContent() {
    }

    @Override
    public int getLowestLevel() {
        return level;
    }

    @Override
    public int getHighestLevel() {
        return level;
    }
}
