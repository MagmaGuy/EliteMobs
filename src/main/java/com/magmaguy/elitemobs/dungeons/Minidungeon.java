package com.magmaguy.elitemobs.dungeons;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.internal.NewMinidungeonRelativeBossLocationEvent;
import com.magmaguy.elitemobs.api.internal.RemovalReason;
import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfig;
import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import com.magmaguy.elitemobs.config.customtreasurechests.CustomTreasureChestConfigFields;
import com.magmaguy.elitemobs.config.customtreasurechests.CustomTreasureChestsConfig;
import com.magmaguy.elitemobs.config.dungeonpackager.DungeonPackagerConfigFields;
import com.magmaguy.elitemobs.dungeons.worlds.MinidungeonWorldLoader;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.RegionalBossEntity;
import com.magmaguy.elitemobs.powerstances.GenericRotationMatrixMath;
import com.magmaguy.elitemobs.thirdparty.worldguard.WorldGuardCompatibility;
import com.magmaguy.elitemobs.treasurechest.TreasureChest;
import com.magmaguy.elitemobs.utils.EventCaller;
import com.magmaguy.elitemobs.utils.InfoMessage;
import com.magmaguy.elitemobs.utils.WarningMessage;
import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class Minidungeon {

    //Stores the filename and an instance of this object
    public static HashMap<String, Minidungeon> minidungeons = new HashMap<>();

    @Getter
    @Setter
    private boolean isDownloaded, isInstalled;
    @Getter
    private boolean bossesDownloaded = true;
    @Getter
    private DungeonPackagerConfigFields dungeonPackagerConfigFields;
    @Getter
    private RelativeDungeonLocations relativeBossLocations, relativeTreasureChestLocations;
    @Getter
    private RealDungeonLocations realDungeonLocations;
    @Getter
    private World world;
    @Getter
    private Location teleportLocation;
    @Getter
    private Integer lowestTier, highestTier, regionalBossCount = 0;

    public Minidungeon(DungeonPackagerConfigFields dungeonPackagerConfigFields) {
        minidungeons.put(dungeonPackagerConfigFields.getFilename(), this);
        this.dungeonPackagerConfigFields = dungeonPackagerConfigFields;
        regionalBossCount = 0;
        if (dungeonPackagerConfigFields.getDungeonLocationType() == null) {
            new WarningMessage("Dungeon " + dungeonPackagerConfigFields.getFilename() + " has an invalid location type! It will not boot.");
            return;
        }
        switch (dungeonPackagerConfigFields.getDungeonLocationType()) {
            case WORLD:
                initializeWorldBasedMinidungeon();
                break;
            case SCHEMATIC:
                initializeSchematicBasedMinidungeon();
                break;
            default:
                new WarningMessage("Minidungeon " + dungeonPackagerConfigFields.getFilename() + " does not have a valid dungeonLocationType! Valid types: schematic, world");
                this.isDownloaded = this.isInstalled = false;
                break;
        }

    }

    private void initializeWorldBasedMinidungeon() {
        if (dungeonPackagerConfigFields.getWorldName() == null || dungeonPackagerConfigFields.getWorldName().isEmpty()) {
            this.isDownloaded = this.isInstalled = false;
            new WarningMessage("Minidungeon " + dungeonPackagerConfigFields.getFilename() + " does not have a valid world name in the dungeon packager!");
            return;
        }

        //Check if the world's been loaded
        if (Bukkit.getWorld(dungeonPackagerConfigFields.getWorldName()) != null) {
            this.isDownloaded = this.isInstalled = true;
            world = Bukkit.getWorld(dungeonPackagerConfigFields.getWorldName());
            teleportLocation = world.getSpawnLocation().clone().add(dungeonPackagerConfigFields.getTeleportPoint());
            quantifyWorldBosses();
            return;
        }

        //Since the world isn't loaded, there is no chance that the world is installed
        this.isInstalled = false;

        //Check if the world's been downloaded
        isDownloaded = Files.exists(Paths.get(Bukkit.getWorldContainer() + File.separator + dungeonPackagerConfigFields.getWorldName()));

        if (isDownloaded && dungeonPackagerConfigFields.isEnabled())
            if (dungeonPackagerConfigFields.isEnabled())
                world = MinidungeonWorldLoader.loadWorld(this);

        if (world != null)
            teleportLocation = world.getSpawnLocation().clone().add(dungeonPackagerConfigFields.getTeleportPoint());
    }

    private void initializeSchematicBasedMinidungeon() {
        //If the configuration of the package is wrong
        if (this.dungeonPackagerConfigFields.getSchematicName() == null || this.dungeonPackagerConfigFields.getSchematicName().isEmpty()) {
            this.isDownloaded = false;
            new WarningMessage("The minidungeon package " + this.dungeonPackagerConfigFields.getFilename() + " does not have a valid schematic file name!");
        }

        //If worldedit isn't installed, the checks can't be continued
        if (!Bukkit.getPluginManager().isPluginEnabled("WorldEdit")) {
            this.isDownloaded = false;
            return;
        }

        try {
            if (Bukkit.getPluginManager().isPluginEnabled("FastAsyncWorldEdit"))
                this.isDownloaded = Files.exists(Paths.get(MetadataHandler.PLUGIN.getDataFolder().getParentFile().getAbsolutePath()
                        + File.separator + "FastAsyncWorldEdit" + File.separator + "schematics" + File.separator + dungeonPackagerConfigFields.getSchematicName()));
            else
                this.isDownloaded = Files.exists(Paths.get(MetadataHandler.PLUGIN.getDataFolder().getParentFile().getAbsolutePath()
                        + File.separator + "WorldEdit" + File.separator + "schematics" + File.separator + dungeonPackagerConfigFields.getSchematicName()));

        } catch (Exception ex) {
            this.isDownloaded = false;
        }

        if (dungeonPackagerConfigFields.getAnchorPoint() != null)
            this.isInstalled = true;

    }

    /**
     * Used to rotate any relative coordinates for a dungeon - transitive blocks / custom reinforcements
     *
     * @param localAnchorPoint
     * @param relativeCoordinate
     * @return
     */
    public Location getRotatedFinalLocation(Location localAnchorPoint, Vector relativeCoordinate) {
        return GenericRotationMatrixMath.rotateVectorYAxis(
                dungeonPackagerConfigFields.getRotation(),
                localAnchorPoint,
                relativeCoordinate).toLocation(localAnchorPoint.getWorld());
    }

    /**
     * This is only relevant for the schematic dungeons as the world dungeons do not store data about boss locations
     */
    private void checkIfBossesInstalled() {
        for (RelativeDungeonLocations.RelativeDungeonLocation relativeDungeonLocation : relativeBossLocations.relativeDungeonLocations) {
            if (relativeDungeonLocation.customBossesConfigFields == null) {
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
        this.realDungeonLocations.uncommitLocations(this);
    }

    public boolean initializeRelativeLocationAddition(CustomBossesConfigFields customBossesConfigFields, Location location) {
        Location relativeLocation = addRelativeLocation(customBossesConfigFields, location);
        NewMinidungeonRelativeBossLocationEvent event = new NewMinidungeonRelativeBossLocationEvent(
                this,
                relativeLocation,
                dungeonPackagerConfigFields.getAnchorPoint().clone().add(relativeLocation),
                customBossesConfigFields);
        new EventCaller(event);
        if (event.isCancelled()) return false;


        RegionalBossEntity.createPermanentRegionalBossEntity(customBossesConfigFields, location);

        return relativeLocation != null;
    }

    private Location addRelativeLocation(CustomBossesConfigFields customBossesConfigFields, Location location) {
        if (dungeonPackagerConfigFields.getAnchorPoint() == null)
            return null;
        Location relativeLocation = location.clone().subtract(dungeonPackagerConfigFields.getAnchorPoint());
        if (dungeonPackagerConfigFields.addRelativeBossLocation(customBossesConfigFields, relativeLocation))
            return relativeLocation;
        else
            return null;
    }

    public void removeRelativeLocation(RegionalBossEntity regionalBossEntity) {
        RealDungeonLocations.RealDungeonLocation realDungeonLocation = null;
        for (RealDungeonLocations.RealDungeonLocation iterated : realDungeonLocations.locations) {
            if (iterated.regionalBossEntity != null)
                if (iterated.regionalBossEntity.equals(regionalBossEntity)) {
                    realDungeonLocation = iterated;
                    break;
                }
        }

        if (realDungeonLocation == null) {
            new WarningMessage("Failed to unregister Regional Boss location from the dungeon packager!");
            return;
        }

        dungeonPackagerConfigFields.removeRelativeBossLocation(regionalBossEntity.getCustomBossesConfigFields(), realDungeonLocation.relativeLocation);
        RealDungeonLocations.RealDungeonLocation finalRealDungeonLocation = realDungeonLocation;
        relativeBossLocations.relativeDungeonLocations.removeIf(relativeDungeonLocation -> relativeDungeonLocation.location.equals(finalRealDungeonLocation.relativeLocation) &&
                relativeDungeonLocation.customBossesConfigFields.equals(regionalBossEntity.getCustomBossesConfigFields()));
        realDungeonLocations.locations.remove(realDungeonLocation);
        new InfoMessage("Correctly unregistered entry from dungeon packager!");

    }

    public Location addRelativeTreasureChest(String customTreasureChestConfigString, Location location, Player player) {
        if (dungeonPackagerConfigFields.getAnchorPoint() == null)
            return null;
        CustomTreasureChestConfigFields customTreasureChestConfigFields = CustomTreasureChestsConfig.getCustomTreasureChestConfigFields().get(customTreasureChestConfigString);

        if (customTreasureChestConfigFields == null) return null;

        Location relativeLocation = location.clone().subtract(dungeonPackagerConfigFields.getAnchorPoint());
        CustomTreasureChestsConfig.addTreasureChestEntry(player, customTreasureChestConfigString);

        if (dungeonPackagerConfigFields.addRelativeTreasureChests(customTreasureChestConfigFields, relativeLocation))
            return relativeLocation;
        else
            return null;
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
            default:
                new WarningMessage("Invalid dungeon location type for Minidungeon " + dungeonPackagerConfigFields.getFilename() + " !");
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
            world = MinidungeonWorldLoader.runtimeLoadWorld(this);
            WorldGuardCompatibility.protectWorldMinidugeonArea(world.getSpawnLocation(), this);
            player.teleport(world.getSpawnLocation());
            player.sendMessage("Minidungeon " + dungeonPackagerConfigFields.getWorldName() +
                    " has been loaded! The world is now loaded and the regional bosses are up.");
            player.sendMessage(ChatColorConverter.convert("&cYou might want to do &a/em reload &cto fix the initial spawns and skins!"));
            isInstalled = true;
            teleportLocation = world.getSpawnLocation().clone().add(dungeonPackagerConfigFields.getTeleportPoint());
        } catch (Exception exception) {
            new WarningMessage("Warning: Failed to load the " + dungeonPackagerConfigFields.getWorldName() + " world!");
            player.sendMessage("Warning: Failed to load the " + dungeonPackagerConfigFields.getWorldName() + " world!");
            exception.printStackTrace();
        }
        if (isInstalled)
            quantifyWorldBosses();
    }

    private void unloadWorld(Player player) {
        try {
            for (Player iteratedPlayer : Bukkit.getOnlinePlayers())
                if (iteratedPlayer.getWorld().getName().equals(dungeonPackagerConfigFields.getWorldName())) {
                    player.sendMessage("[EliteMobs] Failed to unload Minidungeon because at least one player is in the world "
                            + dungeonPackagerConfigFields.getWorldName());
                    return;
                }
            for (RegionalBossEntity regionalBossEntity : RegionalBossEntity.getRegionalBossEntitySet())
                if (regionalBossEntity.getSpawnLocation().getWorld() == world)
                    regionalBossEntity.worldUnload();
            MinidungeonWorldLoader.unloadWorld(this);
            player.sendMessage("Minidugeon " + dungeonPackagerConfigFields.getWorldName() +
                    " has been unloaded! The world is now unloaded. The world is now unloaded and the regional bosses are down.");
            isInstalled = false;
        } catch (Exception exception) {
            player.sendMessage("Warning: Failed to unload the " + dungeonPackagerConfigFields.getWorldName() + " world!");
        }
        regionalBossCount = 0;
    }

    private void schematicButtonToggleBehavior(Player player) {
        if (!isInstalled) {
            installSchematicMinidungeon(player);
        } else {
            uninstallSchematicMinidungeon(player);
        }
    }

    private void installSchematicMinidungeon(Player player) {
        player.performCommand("schematic load " + dungeonPackagerConfigFields.getSchematicName());
        new BukkitRunnable() {
            @Override
            public void run() {
                for (int i = 0; i < 20; i++)
                    player.sendMessage("");
                player.sendMessage(ChatColorConverter.convert("&2-------------------------------------------------"));
                player.sendMessage(ChatColorConverter.convert("&7[EliteMobs] &2Ready to install " + dungeonPackagerConfigFields.getDungeonSizeCategory().toString().toLowerCase() + "!"));
                TextComponent pasteString = new TextComponent(ChatColorConverter.convert("&aClick here to place the &lbuilding and bosses &awhere you're standing!"));
                pasteString.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/em setup minidungeon " + dungeonPackagerConfigFields.getFilename()));
                pasteString.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(ChatColorConverter.convert("&2Click me!")).create()));
                TextComponent noPasteString = new TextComponent(ChatColorConverter.convert("&4&lOr &4click here to &lonly set the bosses with no building&4!"));
                noPasteString.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/em setup minidungeonNoPaste " + dungeonPackagerConfigFields.getFilename()));
                noPasteString.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(ChatColorConverter.convert("&cOnly click if you're already standing at the bulding's anchor point!")).create()));
                player.spigot().sendMessage(pasteString);
                player.spigot().sendMessage(noPasteString);
                player.sendMessage(ChatColorConverter.convert("&2-------------------------------------------------"));
            }
        }.runTaskLater(MetadataHandler.PLUGIN, 20);
    }

    public void finalizeMinidungeonInstallation(Player player, boolean pastedSchematic) {
        dungeonPackagerConfigFields.setEnabled(true, player.getLocation());
        this.relativeBossLocations = new RelativeDungeonLocations(dungeonPackagerConfigFields.getRelativeBossLocations(), true);
        this.relativeTreasureChestLocations = new RelativeDungeonLocations(dungeonPackagerConfigFields.getRelativeTreasureChestLocations(), false);
        this.realDungeonLocations = new RealDungeonLocations();
        commitLocations();
        this.isInstalled = true;

        if (dungeonPackagerConfigFields.isProtect()) {
            Location realCorner1 = getRotatedFinalLocation(dungeonPackagerConfigFields.getAnchorPoint(), dungeonPackagerConfigFields.getCorner1());
            Location realCorner2 = getRotatedFinalLocation(dungeonPackagerConfigFields.getAnchorPoint(), dungeonPackagerConfigFields.getCorner2());
            WorldGuardCompatibility.defineMinidungeon(realCorner1, realCorner2, dungeonPackagerConfigFields.getAnchorPoint(), dungeonPackagerConfigFields.getSchematicName(), this);
        }

        teleportLocation = getRotatedFinalLocation(dungeonPackagerConfigFields.getAnchorPoint(), dungeonPackagerConfigFields.getTeleportPoint());

        for (int i = 0; i < 20; i++)
            player.sendMessage("");

        player.sendMessage(ChatColorConverter.convert("&2" + dungeonPackagerConfigFields.getName() + " installed!"));
        TextComponent setupOptions = new TextComponent(ChatColorConverter.convert("&4Click here to uninstall!"));
        if (pastedSchematic)
            setupOptions.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/em setup unminidungeon " + dungeonPackagerConfigFields.getFilename()));
        else
            setupOptions.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/em setup unminidungeonNoPaste " + dungeonPackagerConfigFields.getFilename()));
        player.spigot().sendMessage(setupOptions);
        quantifySchematicBosses(true);
    }

    public void uninstallSchematicMinidungeon(Player player) {
        if (dungeonPackagerConfigFields.isProtect())
            if (Bukkit.getPluginManager().isPluginEnabled("WorldGuard"))
                WorldGuardCompatibility.removeMinidungeon(
                        dungeonPackagerConfigFields.getSchematicName(),
                        dungeonPackagerConfigFields.getAnchorPoint());
        uncommitLocations();
        dungeonPackagerConfigFields.setEnabled(false, player.getLocation());
        this.isInstalled = false;
        this.regionalBossCount = 0;
        player.sendMessage("[EliteMobs] EliteMobs attempted to uninstall a minidungeon. Further WorldEdit commands might be required to remove the physical structure of the minidungeon.");
    }

    public void quantifySchematicBosses(boolean freshInstall) {
        if (!isInstalled) return;
        if (!freshInstall) {
            this.relativeBossLocations = new RelativeDungeonLocations(dungeonPackagerConfigFields.getRelativeBossLocations(), true);
            this.relativeTreasureChestLocations = new RelativeDungeonLocations(dungeonPackagerConfigFields.getRelativeTreasureChestLocations(), false);
            this.realDungeonLocations = new RealDungeonLocations();
        }
        this.teleportLocation = getRotatedFinalLocation(dungeonPackagerConfigFields.getAnchorPoint(), dungeonPackagerConfigFields.getTeleportPoint());
        for (String regionalBossLocations : dungeonPackagerConfigFields.getRelativeBossLocations()) {
            String bossFileName = regionalBossLocations.split(":")[0];
            CustomBossesConfigFields customBossesConfigFields = CustomBossesConfig.getCustomBoss(bossFileName);
            quantificationFilter(customBossesConfigFields);
            RegionalBossEntity.getRegionalBossEntities(customBossesConfigFields).forEach((regionalBossEntity -> regionalBossEntity.setMinidungeon(this)));
            if (customBossesConfigFields != null && customBossesConfigFields.isRegionalBoss())
                regionalBossCount++;
        }
    }

    public void quantifyWorldBosses() {
        if (!isInstalled) return;
        for (RegionalBossEntity regionalBossEntity : RegionalBossEntity.getRegionalBossEntitySet())
            if (Objects.equals(regionalBossEntity.getWorldName(), world.getName())) {
                regionalBossEntity.setMinidungeon(this);
                regionalBossCount++;
                quantificationFilter(regionalBossEntity.getCustomBossesConfigFields());
            }
    }

    private void quantificationFilter(CustomBossesConfigFields customBossesConfigFields) {
        try {
            int level = customBossesConfigFields.getLevel();
            lowestTier = lowestTier == null ? level : lowestTier < level ? lowestTier : level;
            highestTier = highestTier == null ? level : highestTier > level ? highestTier : level;
        } catch (Exception ex) {
        }
    }

    /**
     * This can only exist if the anchor point actually exists
     */
    public class RealDungeonLocations {
        ArrayList<RealDungeonLocation> locations = new ArrayList<>();

        public RealDungeonLocations() {
            for (RelativeDungeonLocations.RelativeDungeonLocation relativeDungeonLocation : relativeBossLocations.relativeDungeonLocations)
                if (dungeonPackagerConfigFields.getRotation() == 0F)
                    locations.add(
                            new RealDungeonLocation(relativeDungeonLocation.location,
                                    dungeonPackagerConfigFields.getAnchorPoint().clone().add(relativeDungeonLocation.location),
                                    relativeDungeonLocation.customBossesConfigFields));
                else {
                    locations.add(
                            new RealDungeonLocation(relativeDungeonLocation.location,
                                    getRotatedFinalLocation(dungeonPackagerConfigFields.getAnchorPoint(), relativeDungeonLocation.location),
                                    relativeDungeonLocation.customBossesConfigFields));
                }

            for (RelativeDungeonLocations.RelativeDungeonLocation relativeDungeonLocation : relativeTreasureChestLocations.relativeDungeonLocations)
                if (dungeonPackagerConfigFields.getRotation() == 0F)
                    locations.add(
                            new RealDungeonLocation(relativeDungeonLocation.location,
                                    dungeonPackagerConfigFields.getAnchorPoint().clone().add(relativeDungeonLocation.location),
                                    relativeDungeonLocation.customTreasureChestConfigFields));
                else
                    locations.add(
                            new RealDungeonLocation(relativeDungeonLocation.location,
                                    getRotatedFinalLocation(dungeonPackagerConfigFields.getAnchorPoint(), relativeDungeonLocation.location.add(new Vector(0.5, 0.5, 0.5))),
                                    relativeDungeonLocation.customTreasureChestConfigFields));
        }

        /**
         * This runs when an admin tries to install a dungeon
         */
        public void commitLocations() {
            for (RealDungeonLocation realDungeonLocation : locations)
                if (realDungeonLocation.customBossesConfigFields != null)
                    realDungeonLocation.regionalBossEntity = RegionalBossEntity.createPermanentRegionalBossEntity(realDungeonLocation.customBossesConfigFields, realDungeonLocation.location);
                else if (realDungeonLocation.customTreasureChestConfigFields != null)
                    realDungeonLocation.treasureChest = CustomTreasureChestsConfig.addTreasureChestEntry(realDungeonLocation.location, realDungeonLocation.customTreasureChestConfigFields.getFilename());
        }

        public void uncommitLocations(Minidungeon minidungeon) {
            Collection<EliteEntity> eliteEntities = new ArrayList<>(EntityTracker.getEliteMobs().values());
            for (EliteEntity eliteEntity : eliteEntities)
                if (eliteEntity instanceof CustomBossEntity)
                    if (((CustomBossEntity) eliteEntity).getMinidungeon() == minidungeon) {
                        new InfoMessage("Removed " + eliteEntity.getName());
                        eliteEntity.remove(RemovalReason.REMOVE_COMMAND);
                    }
            Collection<TreasureChest> treasureChests = new ArrayList<>(TreasureChest.getTreasureChestHashMap().values());
            for (TreasureChest treasureChest : treasureChests) {
                treasureChest.removeTreasureChest();
            }
        }

        public class RealDungeonLocation {
            private final Location location;
            private final Vector relativeLocation;
            private CustomBossesConfigFields customBossesConfigFields;
            private CustomTreasureChestConfigFields customTreasureChestConfigFields;
            private RegionalBossEntity regionalBossEntity;
            private TreasureChest treasureChest;

            public RealDungeonLocation(Vector relativeLocation, Location location, CustomBossesConfigFields customBossesConfigFields) {
                this.relativeLocation = relativeLocation;
                this.location = location;
                this.customBossesConfigFields = customBossesConfigFields;
                if (isInstalled)
                    this.regionalBossEntity = RegionalBossEntity.getRegionalBoss(customBossesConfigFields, location);
            }

            public RealDungeonLocation(Vector relativeLocation, Location location, CustomTreasureChestConfigFields customTreasureChestConfigFields) {
                this.relativeLocation = relativeLocation;
                this.location = location;
                this.customTreasureChestConfigFields = customTreasureChestConfigFields;
                if (isInstalled)
                    this.treasureChest = TreasureChest.getTreasureChest(location);
            }
        }
    }

    /**
     * This stores the relative locations of the dungeon, meaning the locations relative to an anchor point. The locations
     * do not take the rotation into account, that is done when converting for the real locations
     */
    public class RelativeDungeonLocations {
        private ArrayList<RelativeDungeonLocation> relativeDungeonLocations = new ArrayList<>();
        @Getter
        private int bossCount = 0;

        public RelativeDungeonLocations(List<String> rawStrings, boolean boss) {
            for (String rawString : rawStrings) {
                if (rawString.isEmpty()) continue;
                relativeDungeonLocations.add(new RelativeDungeonLocation(rawString, boss));
                bossCount++;
            }
        }

        public class RelativeDungeonLocation {
            CustomBossesConfigFields customBossesConfigFields;
            CustomTreasureChestConfigFields customTreasureChestConfigFields;
            Vector location;

            public RelativeDungeonLocation(String rawLocationString, boolean boss) {
                try {
                    if (boss)
                        customBossesConfigFields = CustomBossesConfig.getCustomBoss(rawLocationString.split(":")[0]);
                    else
                        customTreasureChestConfigFields = CustomTreasureChestsConfig.getCustomTreasureChestConfigFields().get(rawLocationString.split(":")[0]);
                    this.location = new Vector(
                            //no world location for relative positioning
                            //x
                            vectorGetter(rawLocationString, 0),
                            //y
                            vectorGetter(rawLocationString, 1),
                            //z
                            vectorGetter(rawLocationString, 2));
                    if (customBossesConfigFields == null && customTreasureChestConfigFields == null)
                        new WarningMessage("Failed to correctly parse line " + rawLocationString + " for minidungeon " + dungeonPackagerConfigFields.getFilename());
                    //unfortunately pitch and yaw won't work here, not that it really matters
                    //(float) vectorGetter(rawLocationString, 3),
                    //(float) vectorGetter(rawLocationString, 4));
                } catch (Exception ex) {
                    new WarningMessage("Failed to generate dungeon from raw " + rawLocationString);
                    ex.printStackTrace();
                }
            }

            private double vectorGetter(String rawLocationString, int position) {
                try {
                    return Double.parseDouble(rawLocationString.split(":")[1].split(",")[position]);
                } catch (Exception e) {
                    new WarningMessage("Failed to parse relative location " + rawLocationString + " for minidungeon " + dungeonPackagerConfigFields.getFilename());
                    return 0;
                }
            }
        }
    }

}
