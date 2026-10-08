package com.magmaguy.elitemobs.wormhole;

import com.magmaguy.elitemobs.config.contentpackages.ContentPackagesConfigFields;
import com.magmaguy.elitemobs.dungeons.EMPackage;
import com.magmaguy.elitemobs.instanced.dungeons.DungeonInstance;
import org.bukkit.Location;
import org.bukkit.World;

public class WormholeNavigation {
    private WormholeNavigation() {
    }

    /**
     * Where to point a player heading for a destination: the destination itself in the same world, the portal that
     * starts its dungeon when it is inside a dungeon copy, otherwise the wormhole into its world landing closest to it.
     */
    public static Location findNavigationTarget(Location sourceLocation, Location destinationLocation) {
        if (sourceLocation == null || destinationLocation == null) return null;
        World sourceWorld = sourceLocation.getWorld();
        World destinationWorld = destinationLocation.getWorld();
        if (sourceWorld == null || destinationWorld == null) return null;
        if (sourceWorld.equals(destinationWorld)) return destinationLocation;
        String dungeon = instancedPackageForWorld(destinationWorld);
        if (dungeon != null) return findInstanceLauncher(sourceLocation, dungeon);
        return findDirectWormholeEntry(sourceLocation, destinationLocation);
    }

    /**
     * The source-world end of a wormhole linking straight to the destination's world. When several do, the one whose
     * far end lands closest to the destination wins, then the one closest to the source.
     */
    public static Location findDirectWormholeEntry(Location sourceLocation, Location destinationLocation) {
        if (sourceLocation == null || destinationLocation == null) return null;
        World sourceWorld = sourceLocation.getWorld();
        World destinationWorld = destinationLocation.getWorld();
        if (sourceWorld == null || destinationWorld == null || sourceWorld.equals(destinationWorld)) return null;
        Location best = null;
        double bestFarDistance = Double.MAX_VALUE;
        double bestNearDistance = Double.MAX_VALUE;
        for (Wormhole wormhole : Wormhole.getWormholes()) {
            // Flight portals open a menu rather than linking two worlds.
            if (wormhole.getWormholeConfigFields().isFlightPortal() || wormhole.getWormholeEntry2() == null) continue;
            Location entry1 = wormhole.getWormholeEntry1().getLocation();
            Location entry2 = wormhole.getWormholeEntry2().getLocation();
            Location near;
            Location far;
            if (isEntryInWorld(entry1, sourceWorld) && isEntryInWorld(entry2, destinationWorld)) {
                near = entry1;
                far = entry2;
            } else if (isEntryInWorld(entry2, sourceWorld) && isEntryInWorld(entry1, destinationWorld)) {
                near = entry2;
                far = entry1;
            } else continue;
            double farDistance = far.distanceSquared(destinationLocation);
            double nearDistance = near.distanceSquared(sourceLocation);
            if (farDistance < bestFarDistance || farDistance == bestFarDistance && nearDistance < bestNearDistance) {
                best = near;
                bestFarDistance = farDistance;
                bestNearDistance = nearDistance;
            }
        }
        return best;
    }

    /**
     * The source-world wormhole end nearest the source whose other end starts the given dungeon package. Either end of
     * a wormhole may name the package. Instance copies never link two worlds, so they are skipped.
     */
    public static Location findInstanceLauncher(Location sourceLocation, String packageFilename) {
        if (sourceLocation == null || sourceLocation.getWorld() == null || packageFilename == null) return null;
        World sourceWorld = sourceLocation.getWorld();
        Location best = null;
        double bestDistance = Double.MAX_VALUE;
        for (Wormhole wormhole : Wormhole.getWormholes()) {
            if (wormhole.isInstanceCopy() || wormhole.getWormholeConfigFields().isFlightPortal()
                    || wormhole.getWormholeEntry2() == null) continue;
            Location launcher = launcherEnd(wormhole.getWormholeEntry1(), wormhole.getWormholeEntry2(), packageFilename, sourceWorld);
            if (launcher == null)
                launcher = launcherEnd(wormhole.getWormholeEntry2(), wormhole.getWormholeEntry1(), packageFilename, sourceWorld);
            if (launcher == null) continue;
            double distance = launcher.distanceSquared(sourceLocation);
            if (distance < bestDistance) {
                best = launcher;
                bestDistance = distance;
            }
        }
        return best;
    }

    /**
     * @return the dungeon package a live dungeon copy belongs to, or null if the world is not a dungeon copy
     */
    public static String instancedPackageForWorld(World world) {
        if (world == null) return null;
        DungeonInstance instance = DungeonInstance.forWorld(world.getUID());
        if (instance == null || instance.getContentPackagesConfigFields() == null) return null;
        return instance.getContentPackagesConfigFields().getFilename();
    }

    /**
     * @return the instanced dungeon package whose blueprint world has this name, or null if there is none
     */
    public static String instancedPackageForWorldName(String worldName) {
        if (worldName == null) return null;
        for (EMPackage emPackage : EMPackage.getEmPackages().values()) {
            ContentPackagesConfigFields fields = emPackage.getContentPackagesConfigFields();
            if (fields == null || !worldName.equals(fields.getWorldName())) continue;
            if (fields.getContentType() == ContentPackagesConfigFields.ContentType.INSTANCED_DUNGEON
                    || fields.getContentType() == ContentPackagesConfigFields.ContentType.DYNAMIC_DUNGEON)
                return fields.getFilename();
        }
        return null;
    }

    // The near end, when the far end names the package and the near end stands in the source world.
    private static Location launcherEnd(WormholeEntry near, WormholeEntry far, String packageFilename, World sourceWorld) {
        if (!packageFilename.equals(far.getLocationString())) return null;
        Location location = near.getLocation();
        return isEntryInWorld(location, sourceWorld) ? location : null;
    }

    private static boolean isEntryInWorld(Location location, World world) {
        return location != null &&
                location.getWorld() != null &&
                location.getWorld().equals(world);
    }
}
