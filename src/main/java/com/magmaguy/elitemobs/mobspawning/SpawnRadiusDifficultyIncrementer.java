package com.magmaguy.elitemobs.mobspawning;

import com.magmaguy.elitemobs.config.MobCombatSettingsConfig;
import org.bukkit.Location;

public class SpawnRadiusDifficultyIncrementer {

    private static final double DEFAULT_BLOCK_DISTANCE_BETWEEN_LEVEL_INCREMENTS = 100D;

    public static int distanceFromSpawnLevel(Location location) {

        double blockDistanceBetweenLevelIncrements = MobCombatSettingsConfig.getBlockDistanceBetweenLevelIncrements();

        Location spawnLocation = location.getWorld().getSpawnLocation();
        Location entityLocation = location;

        double distanceFromSpawn = spawnLocation.distance(entityLocation);

        return calculateDistanceBasedLevel(distanceFromSpawn, blockDistanceBetweenLevelIncrements);

    }

    /**
     * @deprecated Distance-based natural elite levels now return a fixed level, not an additive increase.
     */
    @Deprecated
    public static int distanceFromSpawnLevelIncrease(Location location) {
        return distanceFromSpawnLevel(location);
    }

    public static int calculateDistanceBasedLevel(double distanceFromSpawn, double blockDistanceBetweenLevelIncrements) {
        double distanceBetweenIncrements = sanitizeBlockDistanceBetweenLevelIncrements(blockDistanceBetweenLevelIncrements);
        if (!Double.isFinite(distanceFromSpawn) || distanceFromSpawn <= 0D) return 1;
        return Math.max(1, (int) (distanceFromSpawn / distanceBetweenIncrements));
    }

    private static double sanitizeBlockDistanceBetweenLevelIncrements(double blockDistanceBetweenLevelIncrements) {
        if (!Double.isFinite(blockDistanceBetweenLevelIncrements) || blockDistanceBetweenLevelIncrements <= 0D)
            return DEFAULT_BLOCK_DISTANCE_BETWEEN_LEVEL_INCREMENTS;
        return blockDistanceBetweenLevelIncrements;

    }

}
