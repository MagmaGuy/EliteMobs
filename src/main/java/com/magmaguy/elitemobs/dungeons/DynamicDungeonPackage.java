package com.magmaguy.elitemobs.dungeons;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.DungeonInstallEvent;
import com.magmaguy.elitemobs.api.DungeonUninstallEvent;
import com.magmaguy.elitemobs.config.DungeonsConfig;
import com.magmaguy.elitemobs.config.contentpackages.ContentPackagesConfigFields;
import com.magmaguy.elitemobs.utils.EventCaller;
import com.magmaguy.magmacore.util.Logger;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.File;

public class DynamicDungeonPackage extends EMPackage implements CombatContent {
    private static final int MIN_LEVEL = 5;
    private static final int MAX_LEVEL = 200;

    public DynamicDungeonPackage(ContentPackagesConfigFields contentPackagesConfigFields) {
        super(contentPackagesConfigFields);
    }

    @Override
    public void doInstall(Player player) {
        DungeonInstallEvent event = new DungeonInstallEvent(contentPackagesConfigFields);
        new EventCaller(event);
        contentPackagesConfigFields.simpleInstall();
        player.sendMessage(DungeonsConfig.getDynamicDungeonInstalledMessage().replace("$name", contentPackagesConfigFields.getFilename()));
        player.sendMessage(DungeonsConfig.getDynamicDungeonAccessMessage1());
        player.sendMessage(DungeonsConfig.getDynamicDungeonAccessMessage2());
        player.sendMessage(DungeonsConfig.getDynamicDungeonAccessMessage3());
        this.isInstalled = true;
    }

    @Override
    public void doUninstall(Player player) {
        DungeonUninstallEvent event = new DungeonUninstallEvent(contentPackagesConfigFields);
        new EventCaller(event);
        contentPackagesConfigFields.simpleUninstall();
        isInstalled = false;
        player.sendMessage(DungeonsConfig.getContentUninstalledMessage().replace("$name", contentPackagesConfigFields.getName()));
    }

    @Override
    public void baseInitialization() {
        File file = new File(MetadataHandler.PLUGIN.getDataFolder().getAbsolutePath() +
                File.separatorChar + "world_blueprints" + File.separatorChar + contentPackagesConfigFields.getDungeonConfigFolderName());
        if (!file.exists()) {
            this.isDownloaded = false;
            this.isInstalled = false;
            return;
        } else {
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
        return MIN_LEVEL;
    }

    @Override
    public int getHighestLevel() {
        return MAX_LEVEL;
    }
}
