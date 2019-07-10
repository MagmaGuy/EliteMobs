package com.magmaguy.elitemobs;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import org.bukkit.Bukkit;

public class WorldGuardCompatibility {

    public static final Flag ELITEMOBS_SPAWN_FLAG = new StateFlag("elitemob-spawning", true);

    public static boolean initialize() {

        //Enable WorldGuard
        if (Bukkit.getPluginManager().getPlugin("WorldGuard") != null) {
            Bukkit.getLogger().info("[EliteMobs] WorldGuard detected.");
            FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();
            Bukkit.getLogger().info("[EliteMobs] Enabling flags:");
            try {
                registry.register(ELITEMOBS_SPAWN_FLAG);
                Bukkit.getLogger().info("[EliteMobs] - elitemob-spawning");
                return true;
            } catch (FlagConflictException | IllegalStateException e) {
                Bukkit.getLogger().info("[EliteMobs] Warning: flag elitemob-spawning already exists! This is normal if you've just now reloaded EliteMobs.");
                return false;
            }

        }

        return false;

    }

    public static final Flag getEliteMobsSpawnFlag() {
        return ELITEMOBS_SPAWN_FLAG;
    }

}
