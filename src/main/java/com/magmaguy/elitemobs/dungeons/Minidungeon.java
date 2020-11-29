package com.magmaguy.elitemobs.dungeons;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.custombosses.CustomBossConfigFields;
import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfig;
import com.magmaguy.elitemobs.config.dungeonpackager.DungeonPackagerConfigFields;
import com.magmaguy.elitemobs.dungeons.worlds.MinidungeonWorldLoader;
import com.magmaguy.elitemobs.thirdparty.worldguard.WorldGuardCompatibility;
import com.magmaguy.elitemobs.utils.DebugMessage;
import com.magmaguy.elitemobs.utils.WarningMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Minidungeon {

    //Stores the filename and an instance of this object
    public static HashMap<String, Minidungeon> minidungeons = new HashMap<>();

    public boolean isDownloaded, isInstalled;
    public DungeonPackagerConfigFields dungeonPackagerConfigFields;
    public RelativeDungeonLocations relativeDungeonLocations;

    public Minidungeon(DungeonPackagerConfigFields dungeonPackagerConfigFields) {
        minidungeons.put(dungeonPackagerConfigFields.getFileName(), this);
        this.dungeonPackagerConfigFields = dungeonPackagerConfigFields;
        switch (dungeonPackagerConfigFields.getDungeonLocationType()) {
            case WORLD:
                setupWorldBasedMinidungeon();
                break;
            case SCHEMATIC:
                setupSchematicBasedMinidungeon();
                break;
            default:
                new WarningMessage("Mindungeon " + dungeonPackagerConfigFields.getFileName() + " does not have a valid dungeonLocationType! Valid types: schematic, world");
                this.isDownloaded = this.isInstalled = false;
                break;
        }
    }

    private void setupWorldBasedMinidungeon() {
        if (dungeonPackagerConfigFields.getWorldName() == null || dungeonPackagerConfigFields.getWorldName().isEmpty()) {
            this.isDownloaded = this.isInstalled = false;
            new WarningMessage("Minidungeon " + dungeonPackagerConfigFields.getFileName() + " does not have a valid world name in the dungeon packager!");
            return;
        }

        //Check if the world's been loaded
        if (Bukkit.getWorld(dungeonPackagerConfigFields.getWorldName()) != null) {
            this.isDownloaded = this.isInstalled = true;
            return;
        }

        //Since the world isn't loaded, there is no chance that the world is installed
        this.isInstalled = false;

        //Check if the world's been downloaded
        isDownloaded = Files.exists(Bukkit.getWorldContainer().toPath());

        if (isDownloaded && !isInstalled)
            if (dungeonPackagerConfigFields.isEnabled())
                MinidungeonWorldLoader.loadWorld(this);

    }

    private void setupSchematicBasedMinidungeon() {
        this.isInstalled = false;

        //If the configuration of the package is wrong
        if (this.dungeonPackagerConfigFields.getSchematicName() == null || this.dungeonPackagerConfigFields.getSchematicName().isEmpty()) {
            this.isDownloaded = false;
            new WarningMessage("The minidungeon package " + this.dungeonPackagerConfigFields.getFileName() + " does not have a valid schematic file name!");
        }

        //If worldedit isn't installed, the checks can't be continued
        if (!Bukkit.getPluginManager().isPluginEnabled("WorldEdit")) {
            this.isDownloaded = false;
            return;
        }

        try {

            this.isDownloaded = Files.exists(Paths.get(MetadataHandler.PLUGIN.getDataFolder().getParentFile().getAbsolutePath()
                    + "/WorldEdit/schematics/" + dungeonPackagerConfigFields.getSchematicName()));
        } catch (Exception ex) {
            this.isDownloaded = false;
        }

        this.relativeDungeonLocations = new RelativeDungeonLocations(dungeonPackagerConfigFields.getRelativeBossLocations());

    }

    public void commitLocation(Player player) {
        this.relativeDungeonLocations.commitLocations(player);
    }

    public class RelativeDungeonLocations {
        ArrayList<RelativeDungeonLocation> relativeDungeonLocations = new ArrayList<>();
        public int bossCount = 0;

        public RelativeDungeonLocations(List<String> rawStrings) {
            for (String rawString : rawStrings) {
                relativeDungeonLocations.add(new RelativeDungeonLocation(rawString));
                bossCount++;
            }
        }

        /**
         * This runs when an admit tries to install a dungeon
         *
         * @param player
         */
        public void commitLocations(Player player) {
            for (RelativeDungeonLocation relativeDungeonLocation : relativeDungeonLocations)
                relativeDungeonLocation.customBossConfigFields.addSpawnLocation(player.getLocation().clone().add(relativeDungeonLocation.location));
        }

        public class RelativeDungeonLocation {
            CustomBossConfigFields customBossConfigFields;
            Location location;

            public RelativeDungeonLocation(String rawLocationString) {
                try {
                    customBossConfigFields = CustomBossesConfig.getCustomBoss(rawLocationString.split(":")[0]);
                    this.location = new Location(null,
                            vectorGetter(rawLocationString, 0),
                            vectorGetter(rawLocationString, 1),
                            vectorGetter(rawLocationString, 2),
                            (float) vectorGetter(rawLocationString, 3),
                            (float) vectorGetter(rawLocationString, 4));
                } catch (Exception ex) {
                    new DebugMessage("Failed to generate dungeon from raw " + rawLocationString);
                    ex.printStackTrace();
                }
            }

            private double vectorGetter(String rawLocationString, int position) {
                return Double.parseDouble(rawLocationString.split(":")[1].split(",")[position]);
            }
        }
    }

    public void buttonToggleBehavior(Player player) {
        switch (this.dungeonPackagerConfigFields.getDungeonLocationType()) {
            case WORLD:
                worldButtonToggleBehavior(player);
                break;
            case SCHEMATIC:
                schematicButtonToggleBehavior(player);
                break;
        }
    }

    private void worldButtonToggleBehavior(Player player) {
        World world = Bukkit.getWorld(dungeonPackagerConfigFields.getWorldName());
        if (world == null) {
            dungeonPackagerConfigFields.setEnabled(true);
            loadWorld(player);
        } else {
            dungeonPackagerConfigFields.setEnabled(false);
            unloadWorld(player);
        }
    }

    private void loadWorld(Player player) {
        try {
            World world = MinidungeonWorldLoader.runtimeLoadWorld(this);
            WorldGuardCompatibility.protectWorldMinidugeonArea(world.getSpawnLocation());
            player.teleport(world.getSpawnLocation());
            player.sendMessage("Minidungeon " + dungeonPackagerConfigFields.getWorldName() +
                    " has been loaded! The world is now loaded and the regional bosses are up.");
            isInstalled = true;
        } catch (Exception exception) {
            player.sendMessage("Warning: Failed to load the " + dungeonPackagerConfigFields.getWorldName() + " world!");
        }
    }

    private void unloadWorld(Player player) {
        try {
            for (Player iteratedPlayer : Bukkit.getOnlinePlayers())
                if (iteratedPlayer.getWorld().getName().equals(dungeonPackagerConfigFields.getWorldName())) {
                    player.sendMessage("[EliteMobs] Failed to unload Minidungeon because at least one player is in the world "
                            + dungeonPackagerConfigFields.getWorldName());
                    return;
                }
            MinidungeonWorldLoader.unloadWorld(this);
            player.sendMessage("Minidugeon " + dungeonPackagerConfigFields.getWorldName() +
                    " has been unloaded! The world is now unloaded. The world is now unloaded and the regional bosses are down.");
            isInstalled = false;
        } catch (Exception exception) {
            player.sendMessage("Warning: Failed to unload the " + dungeonPackagerConfigFields.getWorldName() + " world!");
        }
    }

    private void schematicButtonToggleBehavior(Player player) {
        player.sendMessage("This feature is harder to implement, it will be ready in a few days!");
    }

}
