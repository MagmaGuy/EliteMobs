package com.magmaguy.elitemobs.dungeons;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.dungeonpackager.DungeonPackagerConfig;
import com.magmaguy.elitemobs.config.dungeonpackager.DungeonPackagerConfigFields;
import com.magmaguy.magmacore.util.ChatColorConverter;
import com.magmaguy.magmacore.util.Logger;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
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
        } else {
            //This removes all instanced worlds not previously correctly removed
            for (File worldFile : Bukkit.getWorldContainer().listFiles()){
                Logger.debug("world name: " + worldFile.getName());
                Logger.debug("current name: " + file.getName());
                if (worldFile.getName().contains(file.getName()) && worldFile.getName().matches(".*_\\d{1,2}$")) {
                    try{
                    FileUtils.deleteDirectory(worldFile);
                    Logger.info("Removing previously instanced world " + worldFile.getName());}catch (Exception e){
                        Logger.warn("Failed to remove previously instanced world " + worldFile.getName());
                    }
                }
            }
        }
        this.isDownloaded = true;
        this.isInstalled = dungeonPackagerConfigFields.isEnabled();
    }

    @Override
    public boolean install(Player player) {
        dungeonPackagerConfigFields.simpleInstall();
        this.isInstalled = true;
        player.sendMessage(ChatColorConverter.convert("&2Dungeon " + dungeonPackagerConfigFields.getFilename() + " installed!"));
        if (!dungeonPackagerConfigFields.isEnchantmentChallenge()) {
            player.sendMessage(ChatColorConverter.convert("&6Instanced dungeons must be accessed either through the &a/em &6menu or an NPC! NPCs for premade EliteMobs content can be found at the Adventurer's Guild Hub map."));
            player.sendMessage("Remember that instanced dungeons create a world when you join them and remove that world when you are done playing in them!");
        } else {
            player.sendMessage(ChatColorConverter.convert("&8[EliteMobs] &2Enchantment instanced dungeon installed! This dungeon can only be accessed when attempting to enchant items and getting a challenge as a result!"));
            DungeonPackagerConfig.getEnchantedChallengeDungeonPackages().put(dungeonPackagerConfigFields.getFilename(), dungeonPackagerConfigFields);
        }
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
