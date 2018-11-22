package com.magmaguy.elitemobs.utils;

import org.bukkit.Bukkit;

public class VersionChecker {

    public static boolean currentVersionIsUnder(int version, int subVersion) {

        if (Bukkit.getBukkitVersion().split("[.]").length < 4) {

            try {
                return Integer.parseInt(Bukkit.getBukkitVersion().split("[.]")[1].substring(0, 2)) < version;
            } catch (Exception ex) {
                return Integer.parseInt(Bukkit.getBukkitVersion().split("[.]")[1].substring(0, 1)) < version;
            }

        }

        if (Integer.parseInt(Bukkit.getBukkitVersion().split("[.]")[1]) < version) return true;

        if (Integer.parseInt(Bukkit.getBukkitVersion().split("[.]")[1]) == version) {
            try {

                byte actualSubversion = Byte.parseByte(Bukkit.getBukkitVersion().split("[.]")[2].substring(0, 1));
                return actualSubversion < subVersion;

            } catch (Exception e) {

                try {

                    byte actualSubversion = Byte.parseByte(Bukkit.getBukkitVersion().split("[.]")[2].substring(0, 0));
                    return actualSubversion < subVersion;

                } catch (Exception ex) {

                    Bukkit.getLogger().warning("[EliteMobs] Couldn't read version properly, report this to the developer!");
                    return false;

                }
            }

        }

        return false;

    }

}
