package com.magmaguy.elitemobs.thirdparty.worldguard;

import com.magmaguy.magmacore.util.Logger;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.IntegerFlag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import lombok.Getter;
import org.bukkit.Bukkit;

public class WorldGuardCompatibility {

    @Getter
    private static final StateFlag.State allow = StateFlag.State.ALLOW;
    @Getter
    private static final StateFlag.State deny = StateFlag.State.DENY;
    @Getter
    private static StateFlag ELITEMOBS_SPAWN_FLAG;
    @Getter
    private static StateFlag ELITEMOBS_ONLY_SPAWN_FLAG;
    @Getter
    private static StateFlag ELITEMOBS_ANTIEXPLOIT;
    @Getter
    private static StateFlag ELITEMOBS_DUNGEON;
    @Getter
    private static StateFlag ELITEMOBS_EVENTS;
    @Getter
    private static IntegerFlag ELITEMOBS_MINIMUM_LEVEL;
    @Getter
    private static IntegerFlag ELITEMOBS_MAXIMUM_LEVEL;
    @Getter
    private static StateFlag ELITEMOBS_EXPLOSION_REGEN;
    @Getter
    private static StateFlag ELITEMOBS_EXPLOSION_BLOCK_DAMAGE;

    public static boolean initialize() {

        //Enable WorldGuard
        if (Bukkit.getPluginManager().getPlugin("WorldGuard") == null)
            return false;

        Logger.info(" WorldGuard detected.");

        FlagRegistry registry = null;

        try {
            registry = WorldGuard.getInstance().getFlagRegistry();
        } catch (Exception ex) {
            Logger.warn("Something went wrong while loading WorldGuard. Are you using the right WorldGuard version?");
            return false;
        }

//        Logger.info(" Enabling flags:");
        try {
            ELITEMOBS_SPAWN_FLAG = new StateFlag("elitemob-spawning", true);
            registry.register(ELITEMOBS_SPAWN_FLAG);
//            Logger.info(" - elitemob-spawning");
        } catch (FlagConflictException | IllegalStateException e) {
//            Logger.warn(" Warning: flag elitemob-spawning already exists! This is normal if you've just now reloaded EliteMobs.");
            ELITEMOBS_SPAWN_FLAG = (StateFlag) registry.get("elitemob-spawning");
        }
        try {
            ELITEMOBS_ONLY_SPAWN_FLAG = new StateFlag("elitemob-only-spawning", false);
            registry.register(ELITEMOBS_ONLY_SPAWN_FLAG);
//            Logger.info(" - elitemob-only-spawning");
        } catch (FlagConflictException | IllegalStateException e) {
//            Logger.warn(" Warning: flag elitemob-only-spawning already exists! This is normal if you've just now reloaded EliteMobs.");
            ELITEMOBS_ONLY_SPAWN_FLAG = (StateFlag) registry.get("elitemob-only-spawning");
        }
        try {
            ELITEMOBS_ANTIEXPLOIT = new StateFlag("elitemobs-antiexploit", true);
            registry.register(ELITEMOBS_ANTIEXPLOIT);
//            Logger.info(" - elitemobs-antiexploit");
        } catch (FlagConflictException | IllegalStateException e) {
//            Logger.warn(" Warning: flag elitemob-antiexploit already exists! This is normal if you've just now reloaded EliteMobs.");
            ELITEMOBS_ANTIEXPLOIT = (StateFlag) registry.get("elitemobs-antiexploit");
        }
        try {
            ELITEMOBS_DUNGEON = new StateFlag("elitemobs-dungeon", false);
            registry.register(ELITEMOBS_DUNGEON);
//            Logger.info(" - elitemobs-dungeon");
        } catch (FlagConflictException | IllegalStateException e) {
//            Logger.warn(" Warning: flag elitemob-dungeon already exists! This is normal if you've just now reloaded EliteMobs.");
            ELITEMOBS_DUNGEON = (StateFlag) registry.get("elitemobs-dungeon");
        }
        try {
            ELITEMOBS_EVENTS = new StateFlag("elitemobs-events", true);
            registry.register(ELITEMOBS_EVENTS);
//            Logger.info(" - elitemobs-events");
        } catch (FlagConflictException | IllegalStateException e) {
//            Logger.warn("Warning: flag elitemob-events already exists! This is normal if you've just now reloaded EliteMobs.");
            ELITEMOBS_EVENTS = (StateFlag) registry.get("elitemobs-events");
        }
        try {
            ELITEMOBS_MINIMUM_LEVEL = new IntegerFlag("elitemobs-minimum-level");
            registry.register(ELITEMOBS_MINIMUM_LEVEL);
//            Logger.info(" - elitemobs-minimum-level");
        } catch (FlagConflictException | IllegalStateException e) {
//            Logger.warn(" Warning: flag elitemob-minimum-level already exists! This is normal if you've just now reloaded EliteMobs.");
            ELITEMOBS_MINIMUM_LEVEL = (IntegerFlag) registry.get("elitemobs-minimum-level");
        }
        try {
            ELITEMOBS_MAXIMUM_LEVEL = new IntegerFlag("elitemobs-maximum-level");
            registry.register(ELITEMOBS_MAXIMUM_LEVEL);
//            Logger.info("- elitemobs-maximum-level");
        } catch (FlagConflictException | IllegalStateException e) {
//            Logger.warn("Warning: flag elitemob-maximum-level already exists! This is normal if you've just now reloaded EliteMobs.");
            ELITEMOBS_MAXIMUM_LEVEL = (IntegerFlag) registry.get("elitemobs-maximum-level");
        }

        try {
            ELITEMOBS_EXPLOSION_REGEN = new StateFlag("elitemobs-explosion-regen", true);
            registry.register(ELITEMOBS_EXPLOSION_REGEN);
//            Logger.info(" - elitemobs-explosion-regen");
        } catch (FlagConflictException | IllegalStateException e) {
//            Logger.warn("Warning: flag elitemob-explosion-regen already exists! This is normal if you've just now reloaded EliteMobs.");
            ELITEMOBS_EXPLOSION_REGEN = (StateFlag) registry.get("elitemobs-explosion-regen");
        }

        try {
            ELITEMOBS_EXPLOSION_BLOCK_DAMAGE = new StateFlag("elitemobs-explosion-block-damage", true);
            registry.register(ELITEMOBS_EXPLOSION_BLOCK_DAMAGE);
//            Logger.info(" - elitemobs-explosion-block-damage");
        } catch (FlagConflictException | IllegalStateException e) {
//            Logger.warn(" Warning: flag elitemobs-explosion-block-damage already exists! This is normal if you've just now reloaded EliteMobs.");
            ELITEMOBS_EXPLOSION_REGEN = (StateFlag) registry.get("elitemobs-explosion-block-damage");
        }

        return true;
    }
}
