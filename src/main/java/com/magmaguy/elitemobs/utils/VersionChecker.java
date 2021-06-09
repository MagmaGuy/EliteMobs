package com.magmaguy.elitemobs.utils;

import org.bukkit.Bukkit;

public class VersionChecker {

    /**
     * Compares a Minecraft version with the current version on the server. Returns true if the version on the server is older.
     *
     * @param majorVersion Target major version to compare (i.e. 1.>>>17<<<.0)
     * @param minorVersion Target minor version to compare (i.e. 1.17.>>>0<<<)
     * @return Whether the version is under the value to be compared
     */
    public static boolean serverVersionOlderThan(int majorVersion, int minorVersion) {

        int actualMajorVersion = Integer.parseInt(Bukkit.getBukkitVersion().split("[.]")[1]);
        int actualMinorVersion = Integer.parseInt(Bukkit.getBukkitVersion().split("[.]")[2].split("-")[0]);

        if (actualMajorVersion < majorVersion)
            return true;

        return actualMajorVersion == majorVersion && actualMinorVersion < minorVersion;

    }

}
