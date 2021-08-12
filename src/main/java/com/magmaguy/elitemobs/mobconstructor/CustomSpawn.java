package com.magmaguy.elitemobs.mobconstructor;

import com.magmaguy.elitemobs.EliteMobs;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfig;
import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import com.magmaguy.elitemobs.config.customspawns.CustomSpawnConfig;
import com.magmaguy.elitemobs.config.customspawns.CustomSpawnConfigFields;
import com.magmaguy.elitemobs.events.MoonPhaseDetector;
import com.magmaguy.elitemobs.events.TimedEvent;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.thirdparty.worldguard.WorldGuardFlagChecker;
import com.magmaguy.elitemobs.utils.InfoMessage;
import com.magmaguy.elitemobs.utils.VersionChecker;
import com.magmaguy.elitemobs.utils.WarningMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class CustomSpawn {

    private final CustomSpawnConfigFields customSpawnConfigFields;
    private final ArrayList<CustomBossEntity> customBossEntities = new ArrayList<>();
    public boolean isEvent = false;
    public World world;
    TimedEvent timedEvent;
    private int allTries = 0;
    private Location spawnLocation;
    private boolean keepTrying = true;

    /**
     * Used by the TimedEvent system to find valid locations
     */
    public CustomSpawn(String customSpawnConfig, List<String> customBossesFilenames, TimedEvent timedEvent) {
        this.customSpawnConfigFields = CustomSpawnConfig.customSpawnConfig.getCustomEvent(customSpawnConfig);
        this.timedEvent = timedEvent;
        customBossesFilenames.stream().forEach(bossString -> {
            CustomBossesConfigFields customBossesConfigFields = CustomBossesConfig.getCustomBoss(bossString);
            if (customBossesConfigFields == null) {
                new WarningMessage("Attempted to pass invalid boss into CustomSpawn: " + bossString);
                return;
            }
            customBossEntities.add(new CustomBossEntity(customBossesConfigFields));
        });
    }

    public CustomSpawn(String customSpawnConfig, CustomBossEntity customBossEntity) {
        this.customSpawnConfigFields = CustomSpawnConfig.customSpawnConfig.getCustomEvent(customSpawnConfig);
        customBossEntities.add(customBossEntity);
    }

    public static int getHighestValidBlock(Location location, int highestYLevel) {
        int height = location.getBlockY() - 1;
        //todo: when the Minecraft max height goes over 256 (and under 0) this will need to get reviewed
        while (height < 256) {
            height++;
            if (height > highestYLevel)
                return -1;
            Location floorLocation = new Location(location.getWorld(), location.getX(), height - 1, location.getZ());
            if (floorLocation.getBlock().isPassable())
                continue;
            Location tempLocation = new Location(location.getWorld(), location.getX(), height, location.getZ());
            if (!tempLocation.getBlock().getType().equals(Material.AIR) && !tempLocation.getBlock().getType().equals(Material.CAVE_AIR))
                continue;
            Location locationAbove = new Location(location.getWorld(), location.getX(), height + 1, location.getZ());
            if (!locationAbove.getBlock().getType().equals(Material.AIR) && !locationAbove.getBlock().getType().equals(Material.CAVE_AIR))
                continue;
            return height;
        }
        return -100;
    }

    public World getWorld() {
        return world;
    }

    public void setWorld(World world) {
        this.world = world;
    }

    public boolean isKeepTrying() {
        return keepTrying;
    }

    public void setKeepTrying(boolean keepTrying) {
        this.keepTrying = keepTrying;
    }

    public CustomSpawnConfigFields getCustomSpawnConfigFields() {
        return customSpawnConfigFields;
    }

    public ArrayList<CustomBossEntity> getCustomBossEntities() {
        return customBossEntities;
    }

    public void queueSpawn() {
        //Make sure a location exists
        if (spawnLocation == null)
            new BukkitRunnable() {
                @Override
                public void run() {
                    generateCustomSpawn();
                }
            }.runTaskAsynchronously(MetadataHandler.PLUGIN);
        else
            spawn();

    }

    private void spawn() {
        //Pass back to sync if it's in async
        new BukkitRunnable() {
            @Override
            public void run() {

                if (!keepTrying) cancel();

                if (spawnLocation.getWorld().getTime() < customSpawnConfigFields.getEarliestTime() ||
                        spawnLocation.getWorld().getTime() > customSpawnConfigFields.getLatestTime())
                    return;

                if (customSpawnConfigFields.getMoonPhase() != null)
                    if (!MoonPhaseDetector.detectMoonPhase(spawnLocation.getWorld()).equals(customSpawnConfigFields.getMoonPhase()))
                        return;

                for (CustomBossEntity customBossEntity : customBossEntities)
                    customBossEntity.spawn(spawnLocation, false);

                if (timedEvent != null)
                    timedEvent.queueEvent();

                cancel();

            }
        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 1);
    }

    private void generateCustomSpawn() {
        int maxTries = 100;
        int tries = 0;
        while (tries < maxTries && spawnLocation == null) {
            tries++;
            allTries++;
            this.spawnLocation = generateRandomSpawnLocation();
            if (spawnLocation != null) {
                break;
            }
        }

        if (spawnLocation == null) {
            if (keepTrying) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        generateCustomSpawn();
                        new InfoMessage("Failed to spawn " + timedEvent.getCustomEventsConfigFields().getFilename() + " after " + allTries + " tries. Will try again in 1 minute.");
                    }
                }.runTaskLaterAsynchronously(MetadataHandler.PLUGIN, 20 * 60);
            } else {
                customBossEntities.forEach((customBossEntity -> {
                    if (customBossEntity.summoningEntity != null)
                        customBossEntity.summoningEntity.removeReinforcement(customBossEntity);
                }));
            }
        } else {
            if (isEvent)
                new InfoMessage("Spawned bosses for event after " + allTries + " tries");
            spawn();
        }

    }

    public Location getSpawnLocation() {
        return spawnLocation;
    }

    public CustomSpawn setSpawnLocation(Location spawnLocation) {
        this.spawnLocation = spawnLocation;
        return this;
    }

    public boolean isEvent() {
        return isEvent;
    }

    public void setEvent(boolean event) {
        isEvent = event;
    }

    public Location generateRandomSpawnLocation() {

        if (customSpawnConfigFields == null) {
            new WarningMessage("Something tried to spawn but has invalid custom spawn config fields! This isn't good.");
            new WarningMessage("Bosses: ");
            getCustomBossEntities().forEach((customBossEntity) -> {
                if (customBossEntity != null)
                    if (customBossEntity.getName() != null)
                        new WarningMessage(customBossEntity.getCustomBossesConfigFields().getName());
            });
            if (timedEvent != null) {
                new WarningMessage("Event: " + timedEvent.getCustomEventsConfigFields().getFilename());
                timedEvent.end();
            }
            return null;
        }

        //If there are no players online, don't spawn anything - this condition shouldn't be reachable in the first place
        if (Bukkit.getOnlinePlayers().size() == 0) {
            keepTrying = false;
            if (timedEvent != null)
                timedEvent.end();
            return null;
        }

        //Go through online players and select the ones in valid worlds
        List<Player> validPlayers = new ArrayList<>();
        if (world == null)
            for (Player player : Bukkit.getOnlinePlayers()) {
                Location playerLocation = player.getLocation();
                if (!customSpawnConfigFields.getValidWorlds().isEmpty())
                    if (!customSpawnConfigFields.getValidWorlds().contains(playerLocation.getWorld()))
                        continue;
                if (!customSpawnConfigFields.getValidWorldTypes().isEmpty())
                    if (!customSpawnConfigFields.getValidWorldTypes().contains(playerLocation.getWorld().getEnvironment()))
                        continue;
                validPlayers.add(player);
            }
        else
            validPlayers.addAll(world.getPlayers());

        //If there are no players in valid worlds, skip
        if (validPlayers.isEmpty())
            return null;

        //Select a random player
        Player selectedPlayer = validPlayers.get(ThreadLocalRandom.current().nextInt(validPlayers.size()));

        //Randomize vector between 24 to 128 block away from a source location
        Vector randomizedVector = new Vector(ThreadLocalRandom.current().nextInt(24, 128),
                0, //this doesn't really matter, it likely gets modified later
                ThreadLocalRandom.current().nextInt(24, 128));

        //Allow negative X and Z values
        if (ThreadLocalRandom.current().nextBoolean())
            randomizedVector.setX(randomizedVector.getX() * -1);
        if (ThreadLocalRandom.current().nextBoolean())
            randomizedVector.setY(randomizedVector.getY() * -1);

        //Temp location - do not run checks on it yet
        Location location = selectedPlayer.getLocation().clone().add(randomizedVector);
        //This doesn't matter too much since it will be parsed later, also these values are already tweaked for 1.18.
        if (VersionChecker.serverVersionOlderThan(18, 1))
            location.setY(ThreadLocalRandom.current().nextInt(-0, 256));
        else
            location.setY(ThreadLocalRandom.current().nextInt(-64, 256));
        World world = location.getWorld();

        //Set Y level - Location isn't final yet
        if (customSpawnConfigFields.isSurfaceSpawn())
            //this won't work for Nether environments, but who wants surface spawns on the Nether?
            location = location.getWorld().getHighestBlockAt(location).getLocation().add(new Vector(0.5, 1, 0.5));
        else if (customSpawnConfigFields.isUndergroundSpawn()) {
            //Let's hope there's caves
            if (location.getY() > location.getWorld().getHighestBlockAt(location).getY()) {
                for (int y = (int) location.getY(); y > -64; y--) {
                    Location tempLocation = location.clone();
                    tempLocation.setY(y);
                    if (location.getBlock().getType().equals(Material.VOID_AIR)) return null;
                    if (y < customSpawnConfigFields.getLowestYLevel()) return null;
                    Block groundBlock = location.clone().subtract(new Vector(0, 1, 0)).getBlock();
                    if (!groundBlock.getType().isSolid()) continue;
                    //Check templocation block
                    if (!tempLocation.getBlock().getType().isAir()) continue;
                    //Check block above templocation
                    if (!tempLocation.add(new Vector(0, 1, 0)).getBlock().getType().isAir()) continue;
                    location = tempLocation;
                    break;
                }
            }
        } else
            //Straight upwards check
            location.setY(getHighestValidBlock(location, getHighestValidBlock(location, customSpawnConfigFields.getHighestYLevel())));

        //Prevent spawning right on top of players
        for (Player player : world.getPlayers())
            if (player.getLocation().distanceSquared(location) < Math.pow(24, 2))
                return null;

        //Nether ceiling check
        if (location.getY() > 127 && world.getEnvironment().equals(World.Environment.NETHER))
            return null;

        //Custom height check
        if (location.getY() == -100 || location.getY() > customSpawnConfigFields.getHighestYLevel() || location.getY() < customSpawnConfigFields.getLowestYLevel())
            return null;

        //Check WorldGuard flags
        if (EliteMobs.worldGuardIsEnabled) {
            if (!WorldGuardFlagChecker.doEventFlag(location))
                return null;

            if (!WorldGuardFlagChecker.doEliteMobsSpawnFlag(location))
                return null;
        }

        //Light level check - following 1.18 rules
        if (!customSpawnConfigFields.isCanSpawnInLight())
            if (location.getBlock().getLightLevel() > 8)
                return null;

        return location;

    }

}
