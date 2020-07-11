package com.magmaguy.elitemobs.worldguard;

import com.magmaguy.elitemobs.utils.WarningMessage;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.IntegerFlag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import org.bukkit.Bukkit;

public class WorldGuardCompatibility {

    private static StateFlag ELITEMOBS_SPAWN_FLAG = new StateFlag("elitemob-spawning", true);
    private static StateFlag ELITEMOBS_ONLY_SPAWN_FLAG = new StateFlag("elitemob-only-spawning", false);
    private static StateFlag ELITEMOBS_ANTIEXPLOIT = new StateFlag("elitemobs-antiexploit", true);
    private static StateFlag ELITEMOBS_DUNGEON = new StateFlag("elitemobs-dungeon", false);
    private static StateFlag ELITEMOBS_EVENTS = new StateFlag("elitemobs-events", true);
    private static IntegerFlag ELITEMOBS_MINIMUM_LEVEL = new IntegerFlag("elitemobs-minimum-level");
    private static IntegerFlag ELITEMOBS_MAXIMUM_LEVEL = new IntegerFlag("elitemobs-maximum-level");

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
            ELITEMOBS_SPAWN_FLAG = (StateFlag) registry.get("elitemob-spawning");
        }
        try {
            registry.register(ELITEMOBS_ONLY_SPAWN_FLAG);
            Bukkit.getLogger().info("[EliteMobs] - elitemob-only-spawning");
        } catch (FlagConflictException | IllegalStateException e) {
            Bukkit.getLogger().warning("[EliteMobs] Warning: flag elitemob-only-spawning already exists! This is normal if you've just now reloaded EliteMobs.");
            ELITEMOBS_ONLY_SPAWN_FLAG = (StateFlag) registry.get("elitemob-only-spawning");
        }
        try {
            registry.register(ELITEMOBS_ANTIEXPLOIT);
            Bukkit.getLogger().info("[EliteMobs] - elitemobs-antiexploit");
        } catch (FlagConflictException | IllegalStateException e) {
            Bukkit.getLogger().warning("[EliteMobs] Warning: flag elitemob-antiexploit already exists! This is normal if you've just now reloaded EliteMobs.");
            ELITEMOBS_ANTIEXPLOIT = (StateFlag) registry.get("elitemobs-antiexploit");
        }
        try {
            registry.register(ELITEMOBS_DUNGEON);
            Bukkit.getLogger().info("[EliteMobs] - elitemobs-dungeon");
        } catch (FlagConflictException | IllegalStateException e) {
            Bukkit.getLogger().warning("[EliteMobs] Warning: flag elitemob-dungeon already exists! This is normal if you've just now reloaded EliteMobs.");
            ELITEMOBS_DUNGEON = (StateFlag) registry.get("elitemobs-dungeon");
        }
        try {
            registry.register(ELITEMOBS_EVENTS);
            Bukkit.getLogger().info("[EliteMobs] - elitemobs-events");
        } catch (FlagConflictException | IllegalStateException e) {
            Bukkit.getLogger().warning("[EliteMobs] Warning: flag elitemob-events already exists! This is normal if you've just now reloaded EliteMobs.");
            ELITEMOBS_EVENTS = (StateFlag) registry.get("elitemobs-events");
        }
        try {
            registry.register(ELITEMOBS_MINIMUM_LEVEL);
            Bukkit.getLogger().info("[EliteMobs] - elitemobs-minimum-level");
        } catch (FlagConflictException | IllegalStateException e) {
            Bukkit.getLogger().warning("[EliteMobs] Warning: flag elitemob-minimum-level already exists! This is normal if you've just now reloaded EliteMobs.");
            ELITEMOBS_MINIMUM_LEVEL = (IntegerFlag) registry.get("elitemobs-minimum-level");
        }
        try {
            registry.register(ELITEMOBS_MAXIMUM_LEVEL);
            Bukkit.getLogger().info("[EliteMobs] - elitemobs-maximum-level");
        } catch (FlagConflictException | IllegalStateException e) {
            Bukkit.getLogger().warning("[EliteMobs] Warning: flag elitemob-maximum-level already exists! This is normal if you've just now reloaded EliteMobs.");
            ELITEMOBS_MAXIMUM_LEVEL = (IntegerFlag) registry.get("elitemobs-maximum-level");
        }

        return true;

    }

    public static final StateFlag getEliteMobsSpawnFlag() {
        return ELITEMOBS_SPAWN_FLAG;
    }

    public static final StateFlag getEliteMobsOnlySpawnFlag() {
        return ELITEMOBS_ONLY_SPAWN_FLAG;
    }

    public static final StateFlag getEliteMobsAntiExploitFlag() {
        return ELITEMOBS_ANTIEXPLOIT;
    }

    public static final StateFlag getEliteMobsDungeonFlag() {
        return ELITEMOBS_DUNGEON;
    }

    public static final StateFlag getEliteMobsEventsFlag() {
        return ELITEMOBS_EVENTS;
    }

    public static final IntegerFlag getEliteMobsMinimumLevel() {
        return ELITEMOBS_MINIMUM_LEVEL;
    }

    public static final IntegerFlag getEliteMobsMaximumLevel() {
        return ELITEMOBS_MAXIMUM_LEVEL;
    }

}
