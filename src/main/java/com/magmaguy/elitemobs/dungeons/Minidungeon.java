package com.magmaguy.elitemobs.dungeons;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.custombosses.CustomBossConfigFields;
import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfig;
import com.magmaguy.elitemobs.config.dungeonpackager.DungeonPackagerConfigFields;
import com.magmaguy.elitemobs.dungeons.worlds.MinidungeonWorldLoader;
import com.magmaguy.elitemobs.powerstances.GenericRotationMatrixMath;
import com.magmaguy.elitemobs.thirdparty.worldguard.WorldGuardCompatibility;
import com.magmaguy.elitemobs.utils.DebugMessage;
import com.magmaguy.elitemobs.utils.WarningMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class Minidungeon {

    //Stores the filename and an instance of this object
    public static HashMap<String, Minidungeon> minidungeons = new HashMap<>();

    public boolean isDownloaded, isInstalled;
    public boolean bossesDownloaded = true;
    public DungeonPackagerConfigFields dungeonPackagerConfigFields;
    public RelativeDungeonLocations relativeDungeonLocations;
    public RealDungeonLocations realDungeonLocations;

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
        isDownloaded = Files.exists(Paths.get(Bukkit.getWorldContainer() + "/" + dungeonPackagerConfigFields.getWorldName()));

        if (isDownloaded && !isInstalled)
            if (dungeonPackagerConfigFields.isEnabled())
                MinidungeonWorldLoader.loadWorld(this);

    }

    private void setupSchematicBasedMinidungeon() {
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
            if (Bukkit.getPluginManager().isPluginEnabled("FastAsyncWorldEdit"))
                this.isDownloaded = Files.exists(Paths.get(MetadataHandler.PLUGIN.getDataFolder().getParentFile().getAbsolutePath()
                        + "/FastAsyncWorldEdit/schematics/" + dungeonPackagerConfigFields.getSchematicName()));
            else
                this.isDownloaded = Files.exists(Paths.get(MetadataHandler.PLUGIN.getDataFolder().getParentFile().getAbsolutePath()
                        + "/WorldEdit/schematics/" + dungeonPackagerConfigFields.getSchematicName()));

        } catch (Exception ex) {
            this.isDownloaded = false;
        }

        if (dungeonPackagerConfigFields.getAnchorPoint() != null)
            this.isInstalled = true;

        this.relativeDungeonLocations = new RelativeDungeonLocations(dungeonPackagerConfigFields.getRelativeBossLocations());

        if (isInstalled)
            this.realDungeonLocations = new RealDungeonLocations();

        checkIfBossesInstalled();

    }

    /**
     * This is only relevant for the schematic dungeons as the world dungeons do not store data about boss locations
     */
    private void checkIfBossesInstalled() {
        for (RelativeDungeonLocations.RelativeDungeonLocation relativeDungeonLocation : relativeDungeonLocations.relativeDungeonLocations) {
            if (relativeDungeonLocation.customBossConfigFields == null) {
                this.bossesDownloaded = false;
                return;
            }
        }
        this.bossesDownloaded = true;
    }

    public void commitLocations() {
        this.realDungeonLocations.commitLocations();
    }

    public void uncommitLocations() {
        this.realDungeonLocations.uncommitLocations();
    }

    /**
     * This can only exist if the anchor point actually exists
     */
    public class RealDungeonLocations {
        ArrayList<RealDungeonLocation> realDungeonLocations = new ArrayList<>();

        public RealDungeonLocations() {
            for (RelativeDungeonLocations.RelativeDungeonLocation relativeDungeonLocation : relativeDungeonLocations.relativeDungeonLocations) {
                if (dungeonPackagerConfigFields.getRotation() == 0)
                    realDungeonLocations.add(
                            new RealDungeonLocation(
                                    dungeonPackagerConfigFields.getAnchorPoint().clone().add(relativeDungeonLocation.location),
                                    relativeDungeonLocation.customBossConfigFields));
                else {
                    realDungeonLocations.add(
                            new RealDungeonLocation(GenericRotationMatrixMath.rotateLocationYAxis(
                                    dungeonPackagerConfigFields.getRotation(),
                                    dungeonPackagerConfigFields.getAnchorPoint(),
                                    relativeDungeonLocation.location),
                                    relativeDungeonLocation.customBossConfigFields));
                }
            }
        }

        private final HashSet<UUID> usedUUIDs = new HashSet<>();

        public class RealDungeonLocation {
            public Location location;
            public CustomBossConfigFields customBossConfigFields;
            public CustomBossConfigFields.ConfigRegionalEntity configRegionalEntity;

            public RealDungeonLocation(Location location, CustomBossConfigFields customBossConfigFields) {
                this.location = location;
                this.customBossConfigFields = customBossConfigFields;
                if (isInstalled)
                    this.configRegionalEntity = getConfigRegionalEntity(this.location, this.customBossConfigFields);
            }
        }

        private CustomBossConfigFields.ConfigRegionalEntity getConfigRegionalEntity(Location spawnLocation, CustomBossConfigFields customBossConfigFields) {
            for (CustomBossConfigFields.ConfigRegionalEntity configRegionalEntity : customBossConfigFields.getConfigRegionalEntities().values()) {
                if (configRegionalEntity.spawnLocation.equals(spawnLocation) && !usedUUIDs.contains(configRegionalEntity.uuid)) {
                    usedUUIDs.add(configRegionalEntity.uuid);
                    return configRegionalEntity;
                }
            }
            return null;
        }

        /**
         * This runs when an admit tries to install a dungeon
         */
        public void commitLocations() {
            for (RealDungeonLocation realDungeonLocation : realDungeonLocations)
                realDungeonLocation.configRegionalEntity = realDungeonLocation.customBossConfigFields.addSpawnLocation(realDungeonLocation.location);
        }

        public void uncommitLocations() {
            for (RealDungeonLocation realDungeonLocation : realDungeonLocations)
                realDungeonLocation.customBossConfigFields.removeSpawnLocation(realDungeonLocation.configRegionalEntity);
        }
    }

    /**
     * This stores the relative locations of the dungeon, meaning the locations relative to an anchor point. The locations
     * do not take the rotation into account, that is done when converting for the real locations
     */
    public class RelativeDungeonLocations {
        ArrayList<RelativeDungeonLocation> relativeDungeonLocations = new ArrayList<>();
        public int bossCount = 0;

        public RelativeDungeonLocations(List<String> rawStrings) {
            for (String rawString : rawStrings) {
                relativeDungeonLocations.add(new RelativeDungeonLocation(rawString));
                bossCount++;
            }
        }

        public class RelativeDungeonLocation {
            CustomBossConfigFields customBossConfigFields;
            Vector location;

            public RelativeDungeonLocation(String rawLocationString) {
                try {
                    customBossConfigFields = CustomBossesConfig.getCustomBoss(rawLocationString.split(":")[0]);
                    this.location = new Vector(
                            //no world location for relative positioning
                            //x
                            vectorGetter(rawLocationString, 0),
                            //y
                            vectorGetter(rawLocationString, 1),
                            //z
                            vectorGetter(rawLocationString, 2));
                    //unfortunately pitch and yaw won't work here, not that it really matters
                    //(float) vectorGetter(rawLocationString, 3),
                    //(float) vectorGetter(rawLocationString, 4));
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
        //Cases where the map was not downloaded is already handled in SetupMenu since all it needs to do is post a download link
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
        if (!isInstalled) {
            dungeonPackagerConfigFields.setEnabled(true, player.getLocation());
            this.realDungeonLocations = new RealDungeonLocations();
            commitLocations();
            this.isInstalled = true;
        } else {
            dungeonPackagerConfigFields.setEnabled(false, player.getLocation());
            uncommitLocations();
            this.isInstalled = false;
        }
    }

}
