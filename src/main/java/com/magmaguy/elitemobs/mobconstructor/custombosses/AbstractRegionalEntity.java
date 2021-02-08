package com.magmaguy.elitemobs.mobconstructor.custombosses;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.custombosses.CustomBossConfigFields;
import com.magmaguy.elitemobs.utils.InfoMessage;
import com.magmaguy.elitemobs.utils.WarningMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.*;

//yes, this isn't actually abstract. By abstract, read as a regional entity that may or may not currently exist in-game
public class AbstractRegionalEntity {

    /**
     * Initializes all the regional bosses, one Custom Boss File at a time.
     *
     * @param customBossConfigFields CustomBossConfigFields for the regional boss to be intialized.
     */
    public static void initialize(CustomBossConfigFields customBossConfigFields) {
        List<String> locations = customBossConfigFields.getFileConfiguration().getStringList("spawnLocations");
        if (locations.size() < 1) {
            new InfoMessage(customBossConfigFields.getFile().getName() + " does not have a set location yet! It will not spawn. Did you install its minidungeon?");
            return;
        }
        for (String string : locations)
            new AbstractRegionalEntity(string, customBossConfigFields);
    }

    public static HashMap<CustomBossConfigFields, ArrayList<AbstractRegionalEntity>> abstractRegionalEntityHashMap = new HashMap<>();

    /**
     * Adds a specific instance (or avoids the removal) of a Regional Entity to the config files. Instances of Regional
     * Entities not added here are not commited to configuration files and will not survive restarts.
     *
     * @param abstractRegionalEntity The object of the Regional Entity to be added to the config files, or not removed.
     */
    private static void addAbstractRegionalEntity(AbstractRegionalEntity abstractRegionalEntity) {
        ArrayList<AbstractRegionalEntity> abstractRegionalEntities = abstractRegionalEntityHashMap.get(abstractRegionalEntity.customBossConfigFields);
        if (abstractRegionalEntities != null) {
            abstractRegionalEntities.add(abstractRegionalEntity);
            abstractRegionalEntityHashMap.put(abstractRegionalEntity.customBossConfigFields, abstractRegionalEntities);
        } else
            abstractRegionalEntityHashMap.put(abstractRegionalEntity.customBossConfigFields, new ArrayList<>(Collections.singletonList(abstractRegionalEntity)));
    }

    /**
     * Removes a specific instance of a Regional Entity from the config files.
     *
     * @param abstractRegionalEntity The object of the Regional Entity that is mean to be removed from the config files.
     */
    private static void removeAbstractRegionalEntity(AbstractRegionalEntity abstractRegionalEntity) {
        ArrayList<AbstractRegionalEntity> abstractRegionalEntities = abstractRegionalEntityHashMap.get(abstractRegionalEntity.customBossConfigFields);
        for (Iterator<AbstractRegionalEntity> abstractRegionalEntityIterator = abstractRegionalEntities.iterator(); abstractRegionalEntityIterator.hasNext(); ) {
            AbstractRegionalEntity regionalEntity = abstractRegionalEntityIterator.next();
            if (regionalEntity.abstractRegionalBossUUID.equals(abstractRegionalEntity.abstractRegionalBossUUID)) {
                abstractRegionalEntityIterator.remove();
                break;
            }
        }
        abstractRegionalEntityHashMap.put(abstractRegionalEntity.customBossConfigFields, abstractRegionalEntities);
        abstractRegionalEntity.customBossConfigFields.filesOutOfSync = true;
    }

    /**
     * Gets a specific AbstractRegionalEntity based on the location provided. Returns null if none are found.
     * This is used by the Dungeon Packager in order to sync up packaged Custom Bosses with existing Custom Bosses.
     * NOTE: For reliability reasons, it uses the SPAWN LOCATIONS and not the current entity locations. This may cause issues
     * if spawn locations get stacked on top of each other.
     *
     * @param customBossConfigFields Custom Boss Config Fields of the regional entity to look up
     * @param location               Spawn location of the entity to look up
     * @return
     */
    public static AbstractRegionalEntity get(CustomBossConfigFields customBossConfigFields, Location location) {
        ArrayList<AbstractRegionalEntity> abstractRegionalEntities = abstractRegionalEntityHashMap.get(customBossConfigFields);
        if (abstractRegionalEntities == null) return null;
        for (AbstractRegionalEntity abstractRegionalEntity : abstractRegionalEntities)
            if (!abstractRegionalEntity.isSyncedWithDungeonPackage)
                if (abstractRegionalEntity.getSpawnLocation() != null)
                    if (abstractRegionalEntity.getSpawnLocation().equals(location))
                        return abstractRegionalEntity;
        return null;
    }

    /**
     * Repeating task that commits changes in unix time stamps for regional boss respawn times to config.
     * Changes are cached and only updated every so often to avoid Concurrent Modification Exceptions from multiple writes,
     * as well as performance issues from doing too many write operations.
     */
    public static void abstractRegionalEntityDataSaver() {
        new BukkitRunnable() {
            @Override
            public void run() {
                save();
            }
        }.runTaskTimerAsynchronously(MetadataHandler.PLUGIN, 20 * 5, 20 * 5);
    }

    public static void save() {
        for (CustomBossConfigFields customBossConfigFields : abstractRegionalEntityHashMap.keySet()) {
            if (!customBossConfigFields.filesOutOfSync) continue;
            customBossConfigFields.filesOutOfSync = false;
            List<String> spawnLocations = new ArrayList<>();
            for (AbstractRegionalEntity abstractRegionalEntity : abstractRegionalEntityHashMap.get(customBossConfigFields))
                spawnLocations.add(abstractRegionalEntity.rawString);
            customBossConfigFields.getFileConfiguration().set("spawnLocations", spawnLocations);
            try {
                customBossConfigFields.getFileConfiguration().save(customBossConfigFields.getFile());
            } catch (Exception ex) {
                new WarningMessage("Failed to save respawn timer for " + customBossConfigFields.getFileConfiguration().getName() + " !");
            }
        }
    }


    private String rawString;
    private double x, y, z;
    private float pitch, yaw;
    private String worldName;
    private long unixRespawnTime;
    private long ticksBeforeRespawn = 0;
    private final UUID abstractRegionalBossUUID = UUID.randomUUID();
    private Location spawnLocation;
    private final File file;
    private final FileConfiguration fileConfiguration;
    private final int respawnCoolDownInMinutes;
    private boolean worldIsLoaded;
    public CustomBossConfigFields customBossConfigFields;
    public boolean isSyncedWithDungeonPackage = false;
    public RegionalBossEntity regionalBossEntity;

    /**
     * Creates a regional boss based on the configuration settings that the custom boss has, using a location in the
     * spawnLocations field.
     * This is the main way regional bosses get loaded in-game.
     *
     * @param rawString              Raw string from spawnLocations string list. Format: worldName,x,y,z,pitch,yaw:unixTimeStampForRespawn
     * @param customBossConfigFields Custom Boss Config Fields that this boss will pull from.
     */
    public AbstractRegionalEntity(String rawString, CustomBossConfigFields customBossConfigFields) {
        this.customBossConfigFields = customBossConfigFields;
        this.rawString = rawString;
        this.file = customBossConfigFields.getFile();
        this.fileConfiguration = customBossConfigFields.getFileConfiguration();
        this.respawnCoolDownInMinutes = customBossConfigFields.getSpawnCooldown();
        parseRawString();
        regionalBossEntity = new RegionalBossEntity(this);
        addAbstractRegionalEntity(this);
    }

    /**
     * Creates a regional boss based on a location.
     * This is used by the Dungeon Packager to add regional bosses. This is also used by the command system to instantly
     * add spawn locations from in-game using /em customboss filename.yml addSpawnLocation
     *
     * @param location
     * @param customBossConfigFields
     */
    public AbstractRegionalEntity(Location location, CustomBossConfigFields customBossConfigFields) {
        this.customBossConfigFields = customBossConfigFields;
        this.file = customBossConfigFields.getFile();
        this.fileConfiguration = customBossConfigFields.getFileConfiguration();
        this.respawnCoolDownInMinutes = 0;
        this.spawnLocation = location;
        this.worldName = location.getWorld().getName();
        this.x = location.getX();
        this.y = location.getY();
        this.z = location.getZ();
        this.worldIsLoaded = true;
        this.unixRespawnTime = 0;
        constructRawString();
        customBossConfigFields.filesOutOfSync = true;
        regionalBossEntity = new RegionalBossEntity(this);
        addAbstractRegionalEntity(this);
    }

    //This is used to make sure that console doesn't get spammed a billion times with the same world isn't present notification
    public static ArrayList<String> worldNotifications = new ArrayList<>();

    /**
     * Parses the locations / respawn cooldown strings to be usable from in-game. The output does not always result in valid
     * locations, as this will store theoretical locations to be used later if the world loads.
     */
    private void parseRawString() {
        //Format: worldName,x,y,z,pitch,yaw:unixTimeStamp
        //Note: :unixTimeStamp is optional
        try {

            String[] strings = rawString.split(":")[0].split(",");
            this.worldName = strings[0];
            this.x = Double.parseDouble(strings[1]);
            this.y = Double.parseDouble(strings[2]);
            this.z = Double.parseDouble(strings[3]);
            this.pitch = Float.parseFloat(strings[4]);
            this.yaw = Float.parseFloat(strings[5]);
            if (rawString.contains(":")) {
                this.unixRespawnTime = Long.parseLong(rawString.split(":")[1]);
                this.ticksBeforeRespawn = parseTicksBeforeRespawnFromConfig();
            }

            if (Bukkit.getWorld(worldName) == null) {
                if (!worldNotifications.contains(worldName)) {
                    worldNotifications.add(worldName);
                    new InfoMessage("World " + worldName + " is not loaded, so a regional boss in " + fileConfiguration.getName() + " will not spawn!");
                }
            } else
                this.spawnLocation = new Location(Bukkit.getWorld(worldName), x, y, z, pitch, yaw);

        } catch (Exception ex) {
            new WarningMessage("Custom boss " + fileConfiguration.getName() + " has invalid location string: " + rawString);
            new WarningMessage("Expected format: worldName,x,y,z,pitch,yaw:unixTimeStamp . The best way of fixing this issue is deleting this entry.");
            ex.printStackTrace();
        }

        worldIsLoaded = spawnLocation != null;
    }

    /**
     * Creates a spawnLocations entry for this regional boss
     */
    private void constructRawString() {
        rawString = spawnLocation.getWorld().getName() + "," + spawnLocation.getX() + "," + spawnLocation.getY() + "," +
                spawnLocation.getZ() + "," + spawnLocation.getPitch() + "," + spawnLocation.getYaw() + ":" + unixRespawnTime;
    }

    /**
     * Converts the unix time stamp into how many ticks it would take for that timestamp to be reached, assuming 20tps.
     *
     * @return How many ticks are left before the unix timestamp is reached.
     */
    private long parseTicksBeforeRespawnFromConfig() {
        return (unixRespawnTime - System.currentTimeMillis()) / 1000 * 20 < 0 ?
                0 : (unixRespawnTime - System.currentTimeMillis()) / 1000 * 20;
    }

    /**
     * Queues an update to the config files regarding the unix timestamps for the boss respawns.
     */
    public void updateTicksBeforeRespawn() {
        unixRespawnTime = (respawnCoolDownInMinutes * 60L * 1000L) + System.currentTimeMillis();
        rawString = worldName + "," + x + "," + y + "," + z + "," + pitch + "," + yaw + ":" + unixRespawnTime;
        customBossConfigFields.filesOutOfSync = true;
    }

    /**
     * Fully removes this instance of the regional boss from the configuration files.
     */
    public void remove() {
        removeAbstractRegionalEntity(this);
    }

    /**
     * Returns how many ticks are left before the regional boss is meant to respawn
     *
     * @return How many ticks are left before the regional boss is meant to respawn
     */
    public long getTicksBeforeRespawn() {
        return ticksBeforeRespawn;
    }

    /**
     * Returns the real location at which the regional boss will spawn. Can be null if the world is not loaded.
     *
     * @return Real location the boss is mean to spawn in.
     */
    public Location getSpawnLocation() {
        return spawnLocation;
    }

    /**
     * Used to return the raw config string of the location.
     *
     * @return Raw config string associated to the location
     */
    public String getRawString() {
        return this.rawString;
    }

    /**
     * Returns whether the world the regional boss is supposed to spawn in is loaded.
     *
     * @return If the world the regional boss is supposed to spawn in is loaded.
     */
    public boolean isWorldIsLoaded() {
        return worldIsLoaded;
    }

    /**
     * Sets whether the world the regional boss is supposed to load in is loaded. This is for checks only, and does not
     * cause the boss to be loaded, that is handled in the RegionalBossEntity class.
     *
     * @param worldIsLoaded Sets if the world the regional boss is supposed to spawn in is loaded.
     */
    public void setWorldIsLoaded(boolean worldIsLoaded) {
        this.worldIsLoaded = worldIsLoaded;
    }

    /**
     * Sets the spawn location of the regional boss. This is necessary for when the world unloads during runtime.
     *
     * @param spawnLocation Sets the spawn location of the regional boss.
     */
    public void setSpawnLocation(Location spawnLocation) {
        this.spawnLocation = spawnLocation;
        regionalBossEntity.spawnLocation = spawnLocation;
    }

    /**
     * Sets the spawn location of the regional boss. This is necessary for when the world loads during runtime.
     */
    public void setSpawnLocation() {
        this.spawnLocation = new Location(Bukkit.getWorld(worldName), x, y, z, pitch, yaw);
        regionalBossEntity.spawnLocation = this.spawnLocation;
        this.worldIsLoaded = true;
    }

    /**
     * Returns the world name of the world the regional boss is meant to spawn in.
     *
     * @return The world name of the world the regional boss is meant to spawn in.
     */
    public String getWorldName() {
        return worldName;
    }
}
