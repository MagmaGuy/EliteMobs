package com.magmaguy.elitemobs.dungeons;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.config.dungeonpackager.DungeonPackagerConfigFields;
import com.magmaguy.elitemobs.dungeons.utility.DungeonUtils;
import com.magmaguy.elitemobs.thirdparty.worldguard.WorldGuardCompatibility;
import com.magmaguy.elitemobs.utils.WarningMessage;
import com.magmaguy.elitemobs.wormhole.Wormhole;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

public class WorldPackage extends EMPackage {

    @Getter
    protected World world;

    public WorldPackage(DungeonPackagerConfigFields dungeonPackagerConfigFields) {
        super(dungeonPackagerConfigFields);
    }

    @Override
    public void baseInitialization() {
        super.baseInitialization();
        if (dungeonPackagerConfigFields.getWorldName() == null || dungeonPackagerConfigFields.getWorldName().isEmpty()) {
            this.isDownloaded = this.isInstalled = false;
            new WarningMessage("Packaged content " + dungeonPackagerConfigFields.getFilename() + " does not have a valid world name in the dungeon packager!");
            return;
        }

        content.put(dungeonPackagerConfigFields.getWorldName(), this);
        if (dungeonPackagerConfigFields.getWormholeWorldName() != null &&
                !dungeonPackagerConfigFields.getWormholeWorldName().isEmpty())
            content.put(dungeonPackagerConfigFields.getWormholeWorldName(), this);

        //Check if the world's been loaded
        if (Bukkit.getWorld(dungeonPackagerConfigFields.getWorldName()) != null) {
            this.isDownloaded = this.isInstalled = true;
            world = Bukkit.getWorld(dungeonPackagerConfigFields.getWorldName());
            dungeonPackagerConfigFields.initializeWorld();
            return;
        }

        //Since the world isn't loaded, check if it should be
        this.isInstalled = dungeonPackagerConfigFields.isEnabled();

        //Check if the world's been downloaded
        isDownloaded = Files.exists(Paths.get(Bukkit.getWorldContainer() + File.separator + dungeonPackagerConfigFields.getWorldName()));

        if (isDownloaded && isInstalled) {
            world = DungeonUtils.loadWorld(this);
            dungeonPackagerConfigFields.initializeWorld();
        }
    }


    @Override
    public boolean install(Player player) {
        if (!super.install(player)) return false;
        DungeonUtils.loadWorld(this);
        dungeonPackagerConfigFields.installWorld();
        player.teleport(dungeonPackagerConfigFields.getTeleportLocation());
        world = dungeonPackagerConfigFields.getTeleportLocation().getWorld();
        WorldGuardCompatibility.protectWorldMinidugeonArea(dungeonPackagerConfigFields.getTeleportLocation());
        for (Wormhole wormhole : Wormhole.getWormholes())
            wormhole.onDungeonInstall(dungeonPackagerConfigFields.getFilename());
        player.sendMessage(ChatColorConverter.convert("[EliteMobs] Successfully installed " + dungeonPackagerConfigFields.getName() + "! To uninstall, do /em setup again and click on this content again."));
        return true;
    }

    @Override
    public boolean uninstall(Player player) {
        if (!super.uninstall(player)) return false;
        if (!DungeonUtils.unloadWorld(this)) {
            isInstalled = true;
            return false;
        }
        for (Wormhole wormhole : Wormhole.getWormholes())
            wormhole.onDungeonUninstall(dungeonPackagerConfigFields.getFilename());
        dungeonPackagerConfigFields.uninstallWorld();
        world = null;
        return true;
    }
}
