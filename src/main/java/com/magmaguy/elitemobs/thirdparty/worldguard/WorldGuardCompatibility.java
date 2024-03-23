package com.magmaguy.elitemobs.thirdparty.worldguard;

import com.magmaguy.elitemobs.dungeons.EMPackage;
import com.magmaguy.elitemobs.dungeons.SchematicPackage;
import com.magmaguy.elitemobs.dungeons.WorldDungeonPackage;
import com.magmaguy.elitemobs.utils.WarningMessage;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.domains.DefaultDomain;
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
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.UUID;

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
            ELITEMOBS_SPAWN_FLAG = new StateFlag("elitemob-spawning", true);
            registry.register(ELITEMOBS_SPAWN_FLAG);
            Bukkit.getLogger().info("[EliteMobs] - elitemob-spawning");
        } catch (FlagConflictException | IllegalStateException e) {
            Bukkit.getLogger().warning("[EliteMobs] Warning: flag elitemob-spawning already exists! This is normal if you've just now reloaded EliteMobs.");
            ELITEMOBS_SPAWN_FLAG = (StateFlag) registry.get("elitemob-spawning");
        }
        try {
            ELITEMOBS_ONLY_SPAWN_FLAG = new StateFlag("elitemob-only-spawning", false);
            registry.register(ELITEMOBS_ONLY_SPAWN_FLAG);
            Bukkit.getLogger().info("[EliteMobs] - elitemob-only-spawning");
        } catch (FlagConflictException | IllegalStateException e) {
            Bukkit.getLogger().warning("[EliteMobs] Warning: flag elitemob-only-spawning already exists! This is normal if you've just now reloaded EliteMobs.");
            ELITEMOBS_ONLY_SPAWN_FLAG = (StateFlag) registry.get("elitemob-only-spawning");
        }
        try {
            ELITEMOBS_ANTIEXPLOIT = new StateFlag("elitemobs-antiexploit", true);
            registry.register(ELITEMOBS_ANTIEXPLOIT);
            Bukkit.getLogger().info("[EliteMobs] - elitemobs-antiexploit");
        } catch (FlagConflictException | IllegalStateException e) {
            Bukkit.getLogger().warning("[EliteMobs] Warning: flag elitemob-antiexploit already exists! This is normal if you've just now reloaded EliteMobs.");
            ELITEMOBS_ANTIEXPLOIT = (StateFlag) registry.get("elitemobs-antiexploit");
        }
        try {
            ELITEMOBS_DUNGEON = new StateFlag("elitemobs-dungeon", false);
            registry.register(ELITEMOBS_DUNGEON);
            Bukkit.getLogger().info("[EliteMobs] - elitemobs-dungeon");
        } catch (FlagConflictException | IllegalStateException e) {
            Bukkit.getLogger().warning("[EliteMobs] Warning: flag elitemob-dungeon already exists! This is normal if you've just now reloaded EliteMobs.");
            ELITEMOBS_DUNGEON = (StateFlag) registry.get("elitemobs-dungeon");
        }
        try {
            ELITEMOBS_EVENTS = new StateFlag("elitemobs-events", true);
            registry.register(ELITEMOBS_EVENTS);
            Bukkit.getLogger().info("[EliteMobs] - elitemobs-events");
        } catch (FlagConflictException | IllegalStateException e) {
            Bukkit.getLogger().warning("[EliteMobs] Warning: flag elitemob-events already exists! This is normal if you've just now reloaded EliteMobs.");
            ELITEMOBS_EVENTS = (StateFlag) registry.get("elitemobs-events");
        }
        try {
            ELITEMOBS_MINIMUM_LEVEL = new IntegerFlag("elitemobs-minimum-level");
            registry.register(ELITEMOBS_MINIMUM_LEVEL);
            Bukkit.getLogger().info("[EliteMobs] - elitemobs-minimum-level");
        } catch (FlagConflictException | IllegalStateException e) {
            Bukkit.getLogger().warning("[EliteMobs] Warning: flag elitemob-minimum-level already exists! This is normal if you've just now reloaded EliteMobs.");
            ELITEMOBS_MINIMUM_LEVEL = (IntegerFlag) registry.get("elitemobs-minimum-level");
        }
        try {
            ELITEMOBS_MAXIMUM_LEVEL = new IntegerFlag("elitemobs-maximum-level");
            registry.register(ELITEMOBS_MAXIMUM_LEVEL);
            Bukkit.getLogger().info("[EliteMobs] - elitemobs-maximum-level");
        } catch (FlagConflictException | IllegalStateException e) {
            Bukkit.getLogger().warning("[EliteMobs] Warning: flag elitemob-maximum-level already exists! This is normal if you've just now reloaded EliteMobs.");
            ELITEMOBS_MAXIMUM_LEVEL = (IntegerFlag) registry.get("elitemobs-maximum-level");
        }

        try {
            ELITEMOBS_EXPLOSION_REGEN = new StateFlag("elitemobs-explosion-regen", true);
            registry.register(ELITEMOBS_EXPLOSION_REGEN);
            Bukkit.getLogger().info("[EliteMobs] - elitemobs-explosion-regen");
        } catch (FlagConflictException | IllegalStateException e) {
            Bukkit.getLogger().warning("[EliteMobs] Warning: flag elitemob-explosion-regen already exists! This is normal if you've just now reloaded EliteMobs.");
            ELITEMOBS_EXPLOSION_REGEN = (StateFlag) registry.get("elitemobs-explosion-regen");
        }

        try {
            ELITEMOBS_EXPLOSION_BLOCK_DAMAGE = new StateFlag("elitemobs-explosion-block-damage", true);
            registry.register(ELITEMOBS_EXPLOSION_BLOCK_DAMAGE);
            Bukkit.getLogger().info("[EliteMobs] - elitemobs-explosion-block-damage");
        } catch (FlagConflictException | IllegalStateException e) {
            Bukkit.getLogger().warning("[EliteMobs] Warning: flag elitemobs-explosion-block-damage already exists! This is normal if you've just now reloaded EliteMobs.");
            ELITEMOBS_EXPLOSION_REGEN = (StateFlag) registry.get("elitemobs-explosion-block-damage");
        }

        return true;

    }

    public static void protectWorldMinidugeonArea(Location location, WorldDungeonPackage dungeonWorldPackage) {
        try {
            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionManager regions = container.get(BukkitAdapter.adapt(location.getWorld()));
            ProtectedRegion global = regions.getRegion("__global__");

            if (global == null) {
                // But we want a __global__, so let's create one
                global = new GlobalProtectedRegion("__global__");
                regions.addRegion(global);
            }

            protectMinidungeonArea(global, dungeonWorldPackage);
            DefaultDomain members = global.getMembers();
            members.addPlayer(UUID.fromString("198c4123-cafc-45df-ba79-02a421eb8ce7"));
            global.setOwners(members);
        } catch (Exception ex) {
            new WarningMessage("Failed to protect minidungeon world area!");
        }
    }

    public static void protectWorldMinidugeonArea(World world) {
        try {
            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionManager regions = container.get(BukkitAdapter.adapt(world));
            ProtectedRegion global = regions.getRegion("__global__");

            if (global == null) {
                // But we want a __global__, so let's create one
                global = new GlobalProtectedRegion("__global__");
                regions.addRegion(global);
            }

            protectMinidungeonArea(global);
            DefaultDomain members = global.getMembers();
            members.addPlayer(UUID.fromString("198c4123-cafc-45df-ba79-02a421eb8ce7"));
            global.setOwners(members);
        } catch (Exception ex) {
            new WarningMessage("Failed to protect minidungeon world area!");
        }
    }

    public static void protectWorldMinidugeonArea(Location location) {
        protectWorldMinidugeonArea(location.getWorld());
    }

    public static void protectMinidungeonArea(ProtectedRegion protectedRegion, EMPackage dungeonWorldPackage) {
        //elitemobs events
        protectedRegion.setFlag(ELITEMOBS_DUNGEON, allow);
        protectedRegion.setFlag(ELITEMOBS_ANTIEXPLOIT, deny);
        protectedRegion.setFlag(ELITEMOBS_EVENTS, deny);
        //WG events
        protectedRegion.setFlag(Flags.PASSTHROUGH, deny);
        protectedRegion.setFlag(Flags.INTERACT, deny);
        protectedRegion.setFlag(Flags.CREEPER_EXPLOSION, deny);
        protectedRegion.setFlag(Flags.FIRE_SPREAD, deny);
        protectedRegion.setFlag(Flags.LAVA_FIRE, deny);
        protectedRegion.setFlag(Flags.LAVA_FLOW, deny);
        protectedRegion.setFlag(Flags.SNOW_FALL, deny);
        protectedRegion.setFlag(Flags.SNOW_MELT, deny);
        protectedRegion.setFlag(Flags.ICE_FORM, deny);
        protectedRegion.setFlag(Flags.ICE_MELT, deny);
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
        //Bypass for redstone and doors
        protectedRegion.setFlag(Flags.USE, allow);
        protectedRegion.setFlag(Flags.GREET_MESSAGE, dungeonWorldPackage.getDungeonPackagerConfigFields().getRegionEnterMessage());
        protectedRegion.setFlag(Flags.FAREWELL_MESSAGE, dungeonWorldPackage.getDungeonPackagerConfigFields().getRegionLeaveMessage());
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
        //elitemobs events
        protectedRegion.setFlag(ELITEMOBS_DUNGEON, allow);
        protectedRegion.setFlag(ELITEMOBS_ANTIEXPLOIT, deny);
        protectedRegion.setFlag(ELITEMOBS_EVENTS, deny);
        //WG events
        protectedRegion.setFlag(Flags.PASSTHROUGH, deny);
        protectedRegion.setFlag(Flags.INTERACT, deny);
        protectedRegion.setFlag(Flags.CREEPER_EXPLOSION, deny);
        protectedRegion.setFlag(Flags.FIRE_SPREAD, deny);
        protectedRegion.setFlag(Flags.LAVA_FIRE, deny);
        protectedRegion.setFlag(Flags.LAVA_FLOW, deny);
        protectedRegion.setFlag(Flags.SNOW_FALL, deny);
        protectedRegion.setFlag(Flags.SNOW_MELT, deny);
        protectedRegion.setFlag(Flags.ICE_FORM, deny);
        protectedRegion.setFlag(Flags.ICE_MELT, deny);
        protectedRegion.setFlag(Flags.LEAF_DECAY, deny);
        protectedRegion.setFlag(Flags.GRASS_SPREAD, deny);
        protectedRegion.setFlag(Flags.MYCELIUM_SPREAD, deny);
        protectedRegion.setFlag(Flags.CROP_GROWTH, deny);
        protectedRegion.setFlag(Flags.SOIL_DRY, deny);
        //missing coral-fade
        //missing ravager-grief
        //protectedRegion.setFlag(Flags.GHAST_FIREBALL, deny); - this completely stops fireballs from working
        protectedRegion.setFlag(ELITEMOBS_EXPLOSION_BLOCK_DAMAGE, deny);
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
        //Bypass for redstone and doors
        protectedRegion.setFlag(Flags.USE, allow);
    }

    /**
     * Automatically creates a worldguard region protected as an EliteMobs minidungeon using two x y z vectors for the
     * locations of the diagonally opposed locations
     *
     * @param corner1
     * @param corner2
     */
    public static void defineMinidungeon(Location corner1, Location corner2, Location anchorLocation, String schematicName, SchematicPackage schematicPackage) {
        try {
            RegionContainer regionContainer = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionManager regionManager = regionContainer.get(BukkitAdapter.adapt(anchorLocation.getWorld()));
            BlockVector3 min = BlockVector3.at(corner1.getBlockX(), corner1.getBlockY(), corner1.getBlockZ());
            BlockVector3 max = BlockVector3.at(corner2.getBlockX(), corner2.getBlockY(), corner2.getBlockZ());
            ProtectedRegion region = new ProtectedCuboidRegion(schematicName.replace(".schem", ""), min, max);
            protectMinidungeonArea(region, schematicPackage);
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
