package com.magmaguy.elitemobs.quests;

import lombok.Getter;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Tracks quest completion lockouts for players.
 * Quests are identified by their filename.
 * Lockouts expire after the configured duration.
 */
public class QuestLockout implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    // Key: questFilename, Value: lockout expiration timestamp (unix millis)
    @Getter
    private final Map<String, Long> lockouts = new HashMap<>();

    /**
     * Adds a lockout for a quest completion.
     *
     * @param questFilename  The quest config filename
     * @param lockoutMinutes The duration of the lockout in minutes
     */
    public void addLockout(String questFilename, int lockoutMinutes) {
        long expirationTime = System.currentTimeMillis() + (lockoutMinutes * 60L * 1000L);
        lockouts.put(questFilename, expirationTime);
    }

    /**
     * Checks if a quest is currently locked out.
     *
     * @param questFilename The quest config filename
     * @return true if locked out, false otherwise
     */
    public boolean isLockedOut(String questFilename) {
        Long expirationTime = lockouts.get(questFilename);
        if (expirationTime == null) return false;
        if (System.currentTimeMillis() >= expirationTime) {
            lockouts.remove(questFilename);
            return false;
        }
        return true;
    }

    /**
     * Gets the remaining lockout time in milliseconds.
     *
     * @param questFilename The quest config filename
     * @return Remaining time in milliseconds, or 0 if not locked out
     */
    public long getRemainingLockoutMillis(String questFilename) {
        Long expirationTime = lockouts.get(questFilename);
        if (expirationTime == null) return 0;
        long remaining = expirationTime - System.currentTimeMillis();
        if (remaining <= 0) {
            lockouts.remove(questFilename);
            return 0;
        }
        return remaining;
    }

    /**
     * Formats the remaining lockout time as a human-readable string.
     *
     * @param questFilename The quest config filename
     * @return Formatted time string (e.g., "2h 30m" or "45m")
     */
    public String getFormattedRemainingTime(String questFilename) {
        long remainingMillis = getRemainingLockoutMillis(questFilename);
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
