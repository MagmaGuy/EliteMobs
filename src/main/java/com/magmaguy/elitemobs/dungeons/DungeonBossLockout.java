package com.magmaguy.elitemobs.dungeons;

import lombok.Getter;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Tracks boss kill lockouts for dungeon content.
 * Bosses are identified by filename + spawn location (world stripped).
 * Lockouts expire after the configured duration.
 */
public class DungeonBossLockout implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    // Key: bossIdentifier (filename:x,y,z), Value: lockout expiration timestamp (unix millis)
    @Getter
    private final Map<String, Long> lockouts = new HashMap<>();

    /**
     * Creates a unique identifier for a boss based on filename and spawn location.
     * World is stripped from the location to ensure it works across dungeon instances.
     *
     * @param filename      The boss config filename
     * @param spawnLocation The spawn location string (world,x,y,z,yaw,pitch)
     * @return A unique identifier string
     */
    public static String createBossIdentifier(String filename, String spawnLocation) {
        // Strip world from spawn location - format is "world,x,y,z,yaw,pitch"
        String[] parts = spawnLocation.split(",");
        if (parts.length >= 4) {
            // Return filename:x,y,z (ignoring world, yaw, pitch)
            return filename + ":" + parts[1] + "," + parts[2] + "," + parts[3];
        }
        // Fallback if format is unexpected
        return filename + ":" + spawnLocation;
    }

    /**
     * Adds a lockout for a boss kill.
     *
     * @param bossIdentifier The unique boss identifier
     * @param lockoutMinutes The duration of the lockout in minutes
     */
    public void addLockout(String bossIdentifier, int lockoutMinutes) {
        long expirationTime = System.currentTimeMillis() + (lockoutMinutes * 60L * 1000L);
        lockouts.put(bossIdentifier, expirationTime);
    }

    /**
     * Checks if a boss is currently locked out.
     *
     * @param bossIdentifier The unique boss identifier
     * @return true if locked out, false otherwise
     */
    public boolean isLockedOut(String bossIdentifier) {
        Long expirationTime = lockouts.get(bossIdentifier);
        if (expirationTime == null) return false;
        if (System.currentTimeMillis() >= expirationTime) {
            lockouts.remove(bossIdentifier);
            return false;
        }
        return true;
    }

    /**
     * Gets the remaining lockout time in milliseconds.
     *
     * @param bossIdentifier The unique boss identifier
     * @return Remaining time in milliseconds, or 0 if not locked out
     */
    public long getRemainingLockoutMillis(String bossIdentifier) {
        Long expirationTime = lockouts.get(bossIdentifier);
        if (expirationTime == null) return 0;
        long remaining = expirationTime - System.currentTimeMillis();
        if (remaining <= 0) {
            lockouts.remove(bossIdentifier);
            return 0;
        }
        return remaining;
    }

    /**
     * Formats the remaining lockout time as a human-readable string.
     *
     * @param bossIdentifier The unique boss identifier
     * @return Formatted time string (e.g., "2h 30m" or "45m")
     */
    public String getFormattedRemainingTime(String bossIdentifier) {
        long remainingMillis = getRemainingLockoutMillis(bossIdentifier);
        if (remainingMillis <= 0) return "0m";

        long totalMinutes = remainingMillis / (60 * 1000);
        long hours = totalMinutes / 60;
        long minutes = totalMinutes % 60;

        if (hours > 0) {
            return hours + "h " + minutes + "m";
        }
        return minutes + "m";
    }

    /**
     * Cleans up expired lockouts from the map.
     */
    public void cleanupExpiredLockouts() {
        long currentTime = System.currentTimeMillis();
        Iterator<Map.Entry<String, Long>> iterator = lockouts.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Long> entry = iterator.next();
            if (currentTime >= entry.getValue()) {
                iterator.remove();
            }
        }
    }
}
