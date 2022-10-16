package com.magmaguy.elitemobs.mobconstructor;

import com.magmaguy.elitemobs.EliteMobs;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.adventurersguild.GuildRank;
import com.magmaguy.elitemobs.config.ValidWorldsConfig;
import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfig;
import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import com.magmaguy.elitemobs.config.customspawns.CustomSpawnConfig;
import com.magmaguy.elitemobs.config.customspawns.CustomSpawnConfigFields;
import com.magmaguy.elitemobs.events.MoonPhaseDetector;
import com.magmaguy.elitemobs.events.TimedEvent;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.elitemobs.thirdparty.worldguard.WorldGuardFlagChecker;
import com.magmaguy.elitemobs.utils.DebugMessage;
import com.magmaguy.elitemobs.utils.VersionChecker;
import com.magmaguy.elitemobs.utils.WarningMessage;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

public class CustomSpawn {

    @Getter
    private final CustomSpawnConfigFields customSpawnConfigFields;
    @Getter
    private final ArrayList<CustomBossEntity> customBossEntities = new ArrayList<>();
    @Getter
    @Setter
    private boolean isEvent = false;
    @Getter
    @Setter
    private World world;
    private TimedEvent timedEvent;
    private int allTries = 0;
    @Getter
    @Setter
    private Location spawnLocation;
    @Setter
    private boolean keepTrying = true;

    /**
     * Used by the TimedEvent system to find valid locations
     */
    public CustomSpawn(String customSpawnConfig, List<String> customBossesFilenames, TimedEvent timedEvent) {
        this.customSpawnConfigFields = CustomSpawnConfig.getCustomEvent(customSpawnConfig);
        this.timedEvent = timedEvent;

        if (customSpawnConfigFields == null) {
            new WarningMessage("Invalid custom spawn detected for file " + customSpawnConfig + " in event " + timedEvent.getCustomEventsConfigFields().getFilename());
            return;
        }

        customBossesFilenames.forEach(bossString -> {
            CustomBossesConfigFields customBossesConfigFields = CustomBossesConfig.getCustomBoss(bossString);
            if (customBossesConfigFields == null) {
                new WarningMessage("Attempted to pass invalid boss into CustomSpawn: " + bossString);
                return;
            }
            CustomBossEntity customBossEntity = new CustomBossEntity(customBossesConfigFields);
            customBossEntities.add(customBossEntity);
            customBossEntity.setCustomSpawn(this);
        });
    }

    public CustomSpawn(String customSpawnConfig, CustomBossEntity customBossEntity) {
        this.customSpawnConfigFields = CustomSpawnConfig.getCustomEvent(customSpawnConfig);
        if (customSpawnConfigFields == null) {
            new WarningMessage("Invalid custom spawn detected for file " + customSpawnConfig);
            return;
        }
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
            if (!tempLocation.getBlock().getType().isAir())
                continue;
            Location locationAbove = new Location(location.getWorld(), location.getX(), height + 1, location.getZ());
            if (!locationAbove.getBlock().getType().isAir())
                continue;
            return height;
        }
        return -100;
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
                if (spawnLocation == null) {
                    Bukkit.getScheduler().runTaskLater(MetadataHandler.PLUGIN, () -> generateCustomSpawn(), 1);
                    cancel();
                    return;
                }
                //One last check
                //Last line of defense - spawn a test mob. If some uknown protection system prevents spawning it should prevent this
                LivingEntity testEntity = spawnLocation.getWorld().spawn(spawnLocation, Zombie.class);
                if (!testEntity.isValid()) {
                    spawnLocation = null;
                    //Run 1 tick later to make sure it doesn't get stuck trying over and over again in the same tick
                    Bukkit.getScheduler().runTaskLater(MetadataHandler.PLUGIN, () -> generateCustomSpawn(), 1);
                    cancel();
                    return;
                }
                testEntity.remove();

                if (!keepTrying) cancel();

                if (Objects.requireNonNull(spawnLocation.getWorld()).getTime() < customSpawnConfigFields.getEarliestTime() ||
                        spawnLocation.getWorld().getTime() > customSpawnConfigFields.getLatestTime())
                    return;

                if (customSpawnConfigFields.getMoonPhase() != null)
                    if (!MoonPhaseDetector.detectMoonPhase(spawnLocation.getWorld()).equals(customSpawnConfigFields.getMoonPhase()))
                        return;

                for (CustomBossEntity customBossEntity : customBossEntities)
                    if (!customBossEntity.exists())
                        customBossEntity.spawn(spawnLocation, isEvent);

                cancel();

                if (timedEvent != null)
                    timedEvent.queueEvent();

            }
        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 1);
    }

    private void generateCustomSpawn() {
        //If the global cooldown if enforced and this is a timed event wait for the cd to be over
        /*
        if (timedEvent != null && System.currentTimeMillis() < TimedEvent.getNextEventTrigger()) {
            Bukkit.getScheduler().scheduleAsyncDelayedTask(MetadataHandler.PLUGIN, this::generateCustomSpawn, 20 * 60L);
            return;
        }

         */

        int maxTries = 100;
        int tries = 0;
        while (tries < maxTries && spawnLocation == null) {
            if (!keepTrying)
                return;
            tries++;
            allTries++;
            this.spawnLocation = generateRandomSpawnLocation();
            if (spawnLocation != null)
                break;
        }

        if (spawnLocation == null) {
            if (keepTrying) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        generateCustomSpawn();
                        if (timedEvent != null)
                            new DebugMessage("Failed to spawn " + timedEvent.getCustomEventsConfigFields().getFilename() + " after " + allTries + " tries. Will try again in 1 minute.");
                    }
                }.runTaskLaterAsynchronously(MetadataHandler.PLUGIN, 20 * 60);
            } else {
                customBossEntities.forEach((customBossEntity -> {
                    if (customBossEntity.summoningEntity != null)
                        customBossEntity.summoningEntity.removeReinforcement(customBossEntity);
                }));
            }
        } else {
            if (isEvent) new DebugMessage("Spawned bosses for event after " + allTries + " tries");
            spawn();
        }
    }

    public Location generateRandomSpawnLocation() {
        if (customSpawnConfigFields == null) {
            new WarningMessage("Something tried to spawn but has invalid custom spawn config fields! This isn't good.", true);
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
        if (Bukkit.getOnlinePlayers().isEmpty()) {
            keepTrying = false;
            if (timedEvent != null)
                timedEvent.end();
            return null;
        }

        //Go through online players and select the ones in valid worlds
        List<Player> validPlayers = new ArrayList<>();
        if (world == null)
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (!PlayerData.isInMemory(player.getUniqueId())) continue;
                Location playerLocation = player.getLocation();
                if (!ValidWorldsConfig.getValidWorlds().contains(playerLocation.getWorld().getName()))
                    continue;
                if (!playerLocation.getWorld().getGameRuleValue(GameRule.DO_MOB_SPAWNING)) continue;
                if (!customSpawnConfigFields.getValidWorlds().isEmpty())
                    if (!customSpawnConfigFields.getValidWorlds().contains(playerLocation.getWorld()))
                        continue;
                if (!customSpawnConfigFields.getValidWorldEnvironments().isEmpty())
                    if (!customSpawnConfigFields.getValidWorldEnvironments().contains(Objects.requireNonNull(playerLocation.getWorld()).getEnvironment()))
                        continue;
                if (GuildRank.getActiveGuildRank(player) == 0) continue;
                validPlayers.add(player);
            }
        else
            validPlayers.addAll(world.getPlayers());

        //If there are no players in valid worlds, skip
        if (validPlayers.isEmpty())
            return null;

        //Select a random player
        Player selectedPlayer = validPlayers.get(ThreadLocalRandom.current().nextInt(validPlayers.size()));

        //Randomize vector between 24 and 128 block away from a source location
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

        if (!customSpawnConfigFields.getValidBiomes().isEmpty() && !customSpawnConfigFields.getValidBiomes().contains(location.getBlock().getBiome()))
            return null;

        //Set Y level - Location isn't final yet
        if (customSpawnConfigFields.isSurfaceSpawn())
            //this won't work for Nether environments, but who wants surface spawns on the Nether?
            location = Objects.requireNonNull(location.getWorld()).getHighestBlockAt(location).getLocation().add(new Vector(0.5, 1, 0.5));
        else if (customSpawnConfigFields.isUndergroundSpawn()) {
            int highestBlockYAt = Objects.requireNonNull(location.getWorld()).getHighestBlockYAt(location);
            //Let's hope there's caves
            if (location.getY() > highestBlockYAt ||
                    location.getY() > highestBlockYAt / 2D) {
                for (int y = (int) location.getY(); y > -64; y--) {
                    Location tempLocation = location.clone();
                    tempLocation.setY(y);
                    if (location.getBlock().getType().equals(Material.VOID_AIR)) return null;
                    if (y < customSpawnConfigFields.getLowestYLevel()) return null;
                    Block groundBlock = location.clone().subtract(new Vector(0, 1, 0)).getBlock();
                    if (!groundBlock.getType().isSolid()) continue;
                    //Check temp location block
                    if (!tempLocation.getBlock().getType().isAir()) continue;
                    //Check block above temp location
                    if (!tempLocation.add(new Vector(0, 1, 0)).getBlock().getType().isAir()) continue;
                    location = tempLocation;
                    break;
                }
            } else {
                for (int y = (int) location.getY(); y < highestBlockYAt; y++) {
                    Location tempLocation = location.clone();
                    tempLocation.setY(y);
                    if (location.getBlock().getType().equals(Material.VOID_AIR)) return null;
                    if (y < customSpawnConfigFields.getLowestYLevel()) return null;
                    Block groundBlock = location.clone().subtract(new Vector(0, 1, 0)).getBlock();
                    if (!groundBlock.getType().isSolid()) continue;
                    //Check temp location block
                    if (!tempLocation.getBlock().getType().isAir()) continue;
                    //Check block above temp location
                    if (!tempLocation.add(new Vector(0, 1, 0)).getBlock().getType().isAir()) continue;
                    location = tempLocation;
                    break;
                }
            }
        } else
            //Straight upwards check
            location.setY(getHighestValidBlock(location, getHighestValidBlock(location, customSpawnConfigFields.getHighestYLevel())));

        //Prevent spawning right on top of players
        assert world != null;
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

            if (!WorldGuardFlagChecker.doMobSpawnFlag(location))
                return null;
        }

        //Light level check - following 1.18 rules
        if (!customSpawnConfigFields.isCanSpawnInLight())
            if (location.getBlock().getLightLevel() > 8)
                return null;

        return location;
    }

}
