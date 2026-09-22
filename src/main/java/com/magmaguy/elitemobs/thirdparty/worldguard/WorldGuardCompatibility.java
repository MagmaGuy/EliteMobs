package com.magmaguy.elitemobs.thirdparty.worldguard;

import com.magmaguy.magmacore.util.Logger;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flag;
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

        try {
            StateFlag spawn = register(registry, new StateFlag("elitemob-spawning", true), StateFlag.class);
            StateFlag onlySpawn = register(registry, new StateFlag("elitemob-only-spawning", false), StateFlag.class);
            StateFlag antiExploit = register(registry, new StateFlag("elitemobs-antiexploit", true), StateFlag.class);
            StateFlag dungeon = register(registry, new StateFlag("elitemobs-dungeon", false), StateFlag.class);
            StateFlag events = register(registry, new StateFlag("elitemobs-events", true), StateFlag.class);
            IntegerFlag minimum = register(registry, new IntegerFlag("elitemobs-minimum-level"), IntegerFlag.class);
            IntegerFlag maximum = register(registry, new IntegerFlag("elitemobs-maximum-level"), IntegerFlag.class);
            StateFlag regeneration = register(registry, new StateFlag("elitemobs-explosion-regen", true), StateFlag.class);
            StateFlag blockDamage = register(registry, new StateFlag("elitemobs-explosion-block-damage", true), StateFlag.class);

            ELITEMOBS_SPAWN_FLAG = spawn;
            ELITEMOBS_ONLY_SPAWN_FLAG = onlySpawn;
            ELITEMOBS_ANTIEXPLOIT = antiExploit;
            ELITEMOBS_DUNGEON = dungeon;
            ELITEMOBS_EVENTS = events;
            ELITEMOBS_MINIMUM_LEVEL = minimum;
            ELITEMOBS_MAXIMUM_LEVEL = maximum;
            ELITEMOBS_EXPLOSION_REGEN = regeneration;
            ELITEMOBS_EXPLOSION_BLOCK_DAMAGE = blockDamage;
            return true;
        } catch (IllegalStateException failure) {
            Logger.warn("Could not initialize EliteMobs WorldGuard flags: " + failure.getMessage());
            return false;
        }
    }

    private static <T extends Flag<?>> T register(FlagRegistry registry, T requested, Class<T> type) {
        try {
            registry.register(requested);
            return requested;
        } catch (FlagConflictException | IllegalStateException conflict) {
            Flag<?> existing = registry.get(requested.getName());
            if (!type.isInstance(existing))
                throw new IllegalStateException("Flag " + requested.getName() + " is missing or is not a " + type.getSimpleName(), conflict);
            return type.cast(existing);
        }
    }
}
