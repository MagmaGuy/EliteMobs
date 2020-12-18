package com.magmaguy.elitemobs.thirdparty.worldguard;

import com.magmaguy.elitemobs.utils.WarningMessage;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.IntegerFlag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.GlobalProtectedRegion;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.util.Vector;

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

    private static final StateFlag.State allow = StateFlag.State.ALLOW;
    private static final StateFlag.State deny = StateFlag.State.DENY;

    public static void protectWorldMinidugeonArea(Location location) {
        try {
            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionManager regions = container.get(BukkitAdapter.adapt(location.getWorld()));
            ProtectedRegion global = regions.getRegion("__global__");

            if (global == null) {
                // But we want a __global__, so let's create one
                global = new GlobalProtectedRegion("__global__");
                regions.addRegion(global);
            }

            protectMinidungeonArea(global);
        } catch (Exception ex) {
            new WarningMessage("Failed to protect minidungeon world area!");
        }
    }

    public static boolean protectMinidungeonArea(String regionName, Location location) {
        try {
            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionManager regions = container.get(BukkitAdapter.adapt(location.getWorld()));
            ProtectedRegion protectedRegion = regions.getRegion(regionName);
            if (protectedRegion == null) {
                new WarningMessage("The region name picked did not exist!");
                return false;
            }
            protectMinidungeonArea(protectedRegion);
            return true;
        } catch (Exception ex) {
            new WarningMessage("Failed to protect region " + regionName + " !");
            return false;
        }
    }

    public static void protectMinidungeonArea(ProtectedRegion protectedRegion) {
        protectedRegion.setFlag(ELITEMOBS_DUNGEON, allow);
        protectedRegion.setFlag(ELITEMOBS_ANTIEXPLOIT, deny);
        protectedRegion.setFlag(Flags.INTERACT, deny);
        protectedRegion.setFlag(Flags.CREEPER_EXPLOSION, deny);
        protectedRegion.setFlag(Flags.FIRE_SPREAD, deny);
        protectedRegion.setFlag(Flags.LAVA_FIRE, deny);
        protectedRegion.setFlag(Flags.LAVA_FLOW, deny);
        protectedRegion.setFlag(Flags.SNOW_FALL, deny);
        protectedRegion.setFlag(Flags.SNOW_MELT, deny);
        protectedRegion.setFlag(Flags.ICE_FORM, deny);
        protectedRegion.setFlag(Flags.ICE_MELT, deny);
        protectedRegion.setFlag(Flags.FROSTED_ICE_FORM, deny);
        protectedRegion.setFlag(Flags.FROSTED_ICE_MELT, deny);
        protectedRegion.setFlag(Flags.LEAF_DECAY, deny);
        protectedRegion.setFlag(Flags.GRASS_SPREAD, deny);
        protectedRegion.setFlag(Flags.MYCELIUM_SPREAD, deny);
        protectedRegion.setFlag(Flags.CROP_GROWTH, deny);
        protectedRegion.setFlag(Flags.SOIL_DRY, deny);
        //missing coral-fade
        //missing ravager-grief
        protectedRegion.setFlag(Flags.GHAST_FIREBALL, deny);
        protectedRegion.setFlag(Flags.WITHER_DAMAGE, deny);
        protectedRegion.setFlag(Flags.ENDER_BUILD, deny);
        protectedRegion.setFlag(Flags.ITEM_FRAME_ROTATE, deny);
        protectedRegion.setFlag(Flags.PLACE_VEHICLE, deny);
        protectedRegion.setFlag(Flags.DESTROY_VEHICLE, deny);
        protectedRegion.setFlag(Flags.PVP, deny);
        protectedRegion.setFlag(Flags.OTHER_EXPLOSION, deny);
        protectedRegion.setFlag(Flags.TRAMPLE_BLOCKS, deny);
        protectedRegion.setFlag(Flags.VINE_GROWTH, deny);
        protectedRegion.setFlag(Flags.MUSHROOMS, deny);
        protectedRegion.setFlag(Flags.DAMAGE_ANIMALS, allow);
        protectedRegion.setFlag(Flags.SLEEP, deny);
        protectedRegion.setFlag(Flags.CHEST_ACCESS, allow);
        protectedRegion.setFlag(Flags.ENTITY_ITEM_FRAME_DESTROY, deny);
        protectedRegion.setFlag(Flags.ENTITY_PAINTING_DESTROY, deny);
        protectedRegion.setFlag(Flags.MOB_SPAWNING, allow);
        protectedRegion.setFlag(Flags.TNT, deny);
        protectedRegion.setFlag(Flags.ENDERDRAGON_BLOCK_DAMAGE, deny);
        protectedRegion.setFlag(Flags.LIGHTER, deny);
        protectedRegion.setFlag(Flags.ENDERPEARL, deny);
        protectedRegion.setFlag(Flags.GREET_MESSAGE, "Now entering");
        protectedRegion.setFlag(Flags.FAREWELL_MESSAGE, "Now leaving");
        protectedRegion.setFlag(Flags.BLOCK_PLACE, deny);
        protectedRegion.setFlag(Flags.BLOCK_BREAK, deny);
    }

    /**
     * Automatically creates a worldguard region protected as an EliteMobs minidungeon using two x y z vectors for the
     * locations of the diagonally opposed locations
     *
     * @param corner1
     * @param corner2
     */
    public static void defineMinidungeon(Vector corner1, Vector corner2, Location anchorLocation, String schematicName) {
        try {
            RegionContainer regionContainer = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionManager regionManager = regionContainer.get(BukkitAdapter.adapt(anchorLocation.getWorld()));
            BlockVector3 min = BlockVector3.at(corner1.getBlockX(), corner1.getBlockY(), corner1.getBlockZ());
            BlockVector3 max = BlockVector3.at(corner2.getBlockX(), corner2.getBlockY(), corner2.getBlockZ());
            ProtectedRegion region = new ProtectedCuboidRegion(schematicName.replace(".schem", ""), min, max);
            protectMinidungeonArea(region);
            regionManager.addRegion(region);
        } catch (Exception ex) {
            new WarningMessage("Failed to add Minidungeon WorldGuard zone!");
        }
    }

    public static void removeMinidungeon(String schematicName, Location anchorLocation) {
        try {
            RegionContainer regionContainer = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionManager regionManager = regionContainer.get(BukkitAdapter.adapt(anchorLocation.getWorld()));
            regionManager.removeRegion(schematicName.replace(".schem", ""));
        } catch (Exception ex) {
            new WarningMessage("Failed to remove Minidungeon WorldGuard zone!");
        }
    }

}
