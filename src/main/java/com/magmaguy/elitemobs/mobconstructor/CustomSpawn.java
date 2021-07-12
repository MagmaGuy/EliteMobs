package com.magmaguy.elitemobs.mobconstructor;

import com.magmaguy.elitemobs.EliteMobs;
import com.magmaguy.elitemobs.config.customspawns.CustomSpawnConfigFields;
import com.magmaguy.elitemobs.thirdparty.worldguard.WorldGuardCompatibility;
import com.magmaguy.elitemobs.thirdparty.worldguard.WorldGuardFlagChecker;
import com.magmaguy.elitemobs.utils.InfoMessage;
import com.magmaguy.elitemobs.utils.WarningMessage;
import com.sk89q.worldguard.protection.flags.Flags;
import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class CustomSpawn {

    public CustomSpawnConfigFields getCustomSpawnConfigFields() {
        return customSpawnConfigFields;
    }

    private CustomSpawnConfigFields customSpawnConfigFields;

    /**
     * Used by the TimedEvent system to find valid locations
     */
    public CustomSpawn(CustomSpawnConfigFields customSpawnConfigFields) {
        this.customSpawnConfigFields = customSpawnConfigFields;
    }

    EntityType entityType;

    /**
     * Used by nothing
     *
     * @param entityType
     */
    public CustomSpawn(EntityType entityType) {
        this.entityType = entityType;
    }

    /**
     * Used by the custom boss system to spawn custom bosses
     *
     * @param entityType
     * @param location
     */
    public CustomSpawn(EntityType entityType, Location location) {
        this.entityType = entityType;
        this.spawnLocation = location;
    }

    public Location getSpawnLocation() {
        return spawnLocation;
    }

    public CustomSpawn setSpawnLocation(Location spawnLocation) {
        this.spawnLocation = spawnLocation;
        return this;
    }

    private Location spawnLocation;

    public boolean isDungeonBoss() {
        return isDungeonBoss;
    }

    public CustomSpawn setRegionalBoss(boolean dungeonBoss) {
        isDungeonBoss = dungeonBoss;
        return this;
    }

    //Not all regional bosses should be treated as dungeon bosses, some can be reinforcements, more subtypes coming soon
    private boolean isDungeonBoss = false;

    public boolean validityCheck() {

        if (spawnLocation == null)
            for (int i = 0; i < 50; i++) {
                this.spawnLocation = generateRandomSpawnLocation();
                if (spawnLocation != null) {
                    new InfoMessage("Successfully generated instant spawn location after " + i + " tries!");
                    break;
                }
            }

        if (spawnLocation == null) {
            new WarningMessage("Failed to spawn elite because instant spawn location was null! Report this to the developer!");
            return false;
        }

        if (!customSpawnConfigFields.isBypassWorldGuard())
            if (EliteMobs.worldGuardIsEnabled)
                if (!WorldGuardFlagChecker.checkFlag(spawnLocation, Flags.MOB_SPAWNING))
                    if (isDungeonBoss) {
                        if (!WorldGuardFlagChecker.checkFlag(spawnLocation, WorldGuardCompatibility.getEliteMobsDungeonFlag()))
                            return false;
                    } else
                        return false;

        if (!customSpawnConfigFields.getValidWorlds().isEmpty())
            if (!customSpawnConfigFields.getValidWorlds().contains(spawnLocation.getWorld()))
                return false;

        if (!customSpawnConfigFields.getValidBiomes().isEmpty())
            if (!customSpawnConfigFields.getValidBiomes().contains(spawnLocation.getBlock().getBiome()))
                return false;

        if (customSpawnConfigFields.getHighestYLevel() != 0 && customSpawnConfigFields.getHighestYLevel() < spawnLocation.getY())
            return false;

        if (customSpawnConfigFields.getLowestYLevel() != 0 && customSpawnConfigFields.getLowestYLevel() > spawnLocation.getY())
            return false;

        return true;

    }

    public Location generateRandomSpawnLocation() {

        World world;
        if (!customSpawnConfigFields.getValidWorlds().isEmpty())
            world = customSpawnConfigFields.getValidWorlds().get(ThreadLocalRandom.current().nextInt(0, customSpawnConfigFields.getValidWorlds().size() - 1));
        else if (!customSpawnConfigFields.getValidWorldTypes().isEmpty()) {
            List<World> worldsList = new ArrayList<>();
            for (World iteratedWorld : Bukkit.getWorlds())
                if (customSpawnConfigFields.getValidWorldTypes().contains(iteratedWorld.getEnvironment()))
                    worldsList.add(iteratedWorld);
            world = worldsList.get(ThreadLocalRandom.current().nextInt(0, worldsList.size() - 1));
        } else
            world = Bukkit.getWorlds().get(ThreadLocalRandom.current().nextInt(0, Bukkit.getWorlds().size() - 1));

        Location location = null;

        int localMaxY = 256;
        if (world.getEnvironment().equals(World.Environment.NETHER))
            localMaxY = 127;
        Chunk chunk = world.getLoadedChunks()[ThreadLocalRandom.current().nextInt(0, world.getLoadedChunks().length - 1)];
        location = new Location(world, chunk.getX() + ThreadLocalRandom.current().nextInt(0, 16) + 0.5,
                ThreadLocalRandom.current().nextInt(customSpawnConfigFields.getLowestYLevel(), localMaxY),
                chunk.getZ() + ThreadLocalRandom.current().nextInt(0, 16) + 0.5);

        location.setY(getHighestValidBlock(location));
        if (location.getY() == -1 || location.getY() > customSpawnConfigFields.getHighestYLevel())
            return null;

        for (Player player : world.getPlayers())
            if (player.getLocation().distanceSquared(location) < Math.pow(24, 2))
                return null;

        if (!customSpawnConfigFields.canSpawnInLight())
            if (location.getBlock().getLightLevel() - 8 <= 0)
                return null;

        return location;

        //todo: account for cases in which there are no loaded chunks - also what if there are no players online?
    }

    private int getHighestValidBlock(Location location) {
        int height = location.getBlockY() - 1;
        //todo: when the Minecraft max height goes over 256 (and under 0) this will need to get reviewed
        while (height < 256) {
            height++;
            if (height > customSpawnConfigFields.getHighestYLevel())
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
        return -1;
    }

    public LivingEntity getLivingEntity() {
        return livingEntity;
    }

    public CustomSpawn setLivingEntity(LivingEntity livingEntity) {
        this.livingEntity = livingEntity;
        return this;
    }

    private LivingEntity livingEntity;

    public LivingEntity spawn() {
        if (!validityCheck()) return null;
        return (LivingEntity) spawnLocation.getWorld().spawnEntity(spawnLocation, entityType);
    }

}
