package com.magmaguy.elitemobs.dungeons;

import com.magmaguy.elitemobs.api.DungeonUninstallEvent;
import com.magmaguy.elitemobs.config.DungeonsConfig;
import com.magmaguy.elitemobs.config.contentpackages.ContentPackagesConfigFields;
import com.magmaguy.elitemobs.dungeons.utility.DungeonUtils;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomMusic;
import com.magmaguy.elitemobs.utils.EventCaller;
import com.magmaguy.elitemobs.wormhole.Wormhole;
import com.magmaguy.magmacore.util.Logger;
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

    public WorldPackage(ContentPackagesConfigFields contentPackagesConfigFields) {
        super(contentPackagesConfigFields);
    }

    @Override
    public void doInstall(Player player) {
        DungeonUtils.loadWorld(this);
        contentPackagesConfigFields.installWorld();
        player.teleport(contentPackagesConfigFields.getTeleportLocation());
        world = contentPackagesConfigFields.getTeleportLocation().getWorld();
        if (contentPackagesConfigFields.getSong() != null)
            new CustomMusic(contentPackagesConfigFields.getSong(), contentPackagesConfigFields, world);
        for (Wormhole wormhole : Wormhole.getWormholes())
            wormhole.onDungeonInstall(contentPackagesConfigFields.getFilename());
        player.sendMessage(DungeonsConfig.getContentInstalledMessage().replace("$name", contentPackagesConfigFields.getName()));
    }

    @Override
    public void doUninstall(Player player) {
        DungeonUninstallEvent event = new DungeonUninstallEvent(contentPackagesConfigFields);
        new EventCaller(event);
        isInstalled = false;
        if (!DungeonUtils.unloadWorld(this)) {
            isInstalled = true;
            player.sendMessage(DungeonsConfig.getContentUninstallFailedMessage().replace("$name", contentPackagesConfigFields.getName()));
            return;
        }
        for (Wormhole wormhole : Wormhole.getWormholes())
            wormhole.onDungeonUninstall(contentPackagesConfigFields.getFilename());
        contentPackagesConfigFields.uninstallWorld();
        world = null;
        player.sendMessage(DungeonsConfig.getContentUninstalledMessage().replace("$name", contentPackagesConfigFields.getName()));
    }

    @Override
    public void baseInitialization() {
        if (contentPackagesConfigFields.getWorldName() == null || contentPackagesConfigFields.getWorldName().isEmpty()) {
            this.isDownloaded = this.isInstalled = false;
            Logger.warn("Packaged content " + contentPackagesConfigFields.getFilename() + " does not have a valid world name in the dungeon packager!");
            return;
        }

        content.put(contentPackagesConfigFields.getWorldName(), this);
        if (contentPackagesConfigFields.getWormholeWorldName() != null &&
                !contentPackagesConfigFields.getWormholeWorldName().isEmpty())
            content.put(contentPackagesConfigFields.getWormholeWorldName(), this);

        //Check if the world's been loaded
        if (Bukkit.getWorld(contentPackagesConfigFields.getWorldName()) != null) {
            this.isDownloaded = this.isInstalled = true;
            world = Bukkit.getWorld(contentPackagesConfigFields.getWorldName());
            EliteMobsWorld.create(world.getUID(), contentPackagesConfigFields);
            contentPackagesConfigFields.initializeWorld();
            return;
        }

        //Since the world isn't loaded, check if it should be
        this.isInstalled = contentPackagesConfigFields.isEnabled();

        //Check if the world's been downloaded
        isDownloaded = Files.exists(Paths.get(Bukkit.getWorldContainer() + File.separator + contentPackagesConfigFields.getWorldName()));

        if (isDownloaded && isInstalled) {
            world = DungeonUtils.loadWorld(this);
            if (contentPackagesConfigFields.getSong() != null)
                new CustomMusic(contentPackagesConfigFields.getSong(), contentPackagesConfigFields, world);
            contentPackagesConfigFields.initializeWorld();
        } else isInstalled = false;
    }

    @Override
    public void initializeContent() {
    }

}
