package com.magmaguy.elitemobs.worldguard;

import com.magmaguy.elitemobs.utils.WarningMessage;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import org.bukkit.Bukkit;

public class WorldGuardCompatibility {

    private static final Flag ELITEMOBS_SPAWN_FLAG = new StateFlag("elitemob-spawning", true);
    private static final Flag ELITEMOBS_ONLY_SPAWN_FLAG = new StateFlag("elitemob-only-spawning", true);
    private static final Flag ELITEMOBS_ANTIEXPLOIT = new StateFlag("elitemobs-antiexploit", true);

    public static boolean initialize() {

        //Enable WorldGuard
        if (Bukkit.getPluginManager().getPlugin("WorldGuard") == null)
            return false;

        Bukkit.getLogger().info("[EliteMobs] WorldGuard detected.");

        FlagRegistry registry = null;

        try {
            registry = WorldGuard.getInstance().getFlagRegistry();
        } catch (Exception ex) {
            new WarningMessage("Something went wrong while loading WorldGuard. Are you using the right WorldGuard version?");
            return false;
        }
        Bukkit.getLogger().info("[EliteMobs] Enabling flags:");
        try {
            registry.register(ELITEMOBS_SPAWN_FLAG);
            Bukkit.getLogger().info("[EliteMobs] - elitemob-spawning");
        } catch (FlagConflictException | IllegalStateException e) {
            Bukkit.getLogger().warning("[EliteMobs] Warning: flag elitemob-spawning already exists! This is normal if you've just now reloaded EliteMobs.");
        }
        try {
            registry.register(ELITEMOBS_ONLY_SPAWN_FLAG);
            Bukkit.getLogger().info("[EliteMobs] - elitemob-only-spawning");
        } catch (FlagConflictException | IllegalStateException e) {
            Bukkit.getLogger().warning("[EliteMobs] Warning: flag elitemob-only-spawning already exists! This is normal if you've just now reloaded EliteMobs.");
        }
        try {
            registry.register(ELITEMOBS_ANTIEXPLOIT);
            Bukkit.getLogger().info("[EliteMobs] - elitemob-antiexploit");
        } catch (FlagConflictException | IllegalStateException e) {
            Bukkit.getLogger().warning("[EliteMobs] Warning: flag elitemob-antiexploit already exists! This is normal if you've just now reloaded EliteMobs.");
        }

        return true;

    }

    public static final Flag getEliteMobsSpawnFlag() {
        return ELITEMOBS_SPAWN_FLAG;
    }

    public static final Flag getEliteMobsOnlySpawnFlag() {
        return ELITEMOBS_ONLY_SPAWN_FLAG;
    }

    public static final Flag getEliteMobsDungeonFlag() {
        return ELITEMOBS_ANTIEXPLOIT;
    }

}
