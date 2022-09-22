package com.magmaguy.elitemobs.dungeons;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.dungeonpackager.DungeonPackagerConfigFields;
import org.bukkit.entity.Player;

import java.io.File;

public class WorldInstancedDungeonPackage extends EMPackage implements CombatContent {
    private int level;

    public WorldInstancedDungeonPackage(DungeonPackagerConfigFields dungeonPackagerConfigFields) {
        super(dungeonPackagerConfigFields);
    }

    @Override
    public void baseInitialization() {
        super.baseInitialization();
        this.level = dungeonPackagerConfigFields.getContentLevel();
        File file = new File(MetadataHandler.PLUGIN.getDataFolder().getAbsolutePath() +
                File.separatorChar + "world_blueprints" + File.separatorChar + dungeonPackagerConfigFields.getDungeonConfigFolderName());
        if (!file.exists()) {
            this.isDownloaded = false;
            this.isInstalled = false;
            return;
        }
        this.isDownloaded = true;
        this.isInstalled = dungeonPackagerConfigFields.isEnabled();
    }

    @Override
    public boolean install(Player player) {
        dungeonPackagerConfigFields.simpleInstall();
        this.isInstalled = true;
        return true;
    }

    @Override
    public boolean uninstall(Player player) {
        dungeonPackagerConfigFields.simpleUninstall();
        this.isInstalled = false;
        return true;
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
