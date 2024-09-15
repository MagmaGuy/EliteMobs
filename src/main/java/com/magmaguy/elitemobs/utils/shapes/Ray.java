package com.magmaguy.elitemobs.utils.shapes;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class Ray extends Shape {
    protected final double maxDistance = 500;
    protected final boolean ignoresSolidBlocks;
    protected double thickness;
    protected Location centerLocation;
    protected Location initialTargetLocation;
    protected List<Location> locations = new ArrayList<>();
    protected Location currentSource;
    protected Location currentTarget;

    public Ray(boolean ignoresSolidBlocks, double pointRadius, Location centerLocation, Location initialTargetLocation) {
        this.ignoresSolidBlocks = ignoresSolidBlocks;
        this.thickness = pointRadius;
        this.centerLocation = centerLocation;
        this.initialTargetLocation = initialTargetLocation;
        this.currentSource = centerLocation;
        this.currentTarget = initialTargetLocation;
    }

    protected List<Location> drawLine(Location location1, Location location2) {
        currentSource = location1;
        currentTarget = location2;
        List<Location> locations = new ArrayList<>();
        Vector raySegment = location2.clone().subtract(location1).toVector().normalize().multiply(thickness);
        Location currentLocation = location1.clone();
        locations.add(location1);
        for (int i = 0; i < maxDistance; i++) {
            if (currentLocation.distanceSquared(location2) < Math.pow(thickness, 2)) break;
            currentLocation.add(raySegment);
            if (!ignoresSolidBlocks && currentLocation.getBlock().getType().isSolid()) break;
            locations.add(currentLocation.clone());
            //currentLocation.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, currentLocation, 1, 0,0,0,0);
        }
        return locations;
    }

    //Children override this
    @Override
    public boolean contains(Location position) {
        for (Location location : locations)
            if (location.distanceSquared(position) < Math.pow(thickness, 2)) return true;
        return false;
    }

    @Override
    public boolean contains(LivingEntity livingEntity) {
        for (Location location : locations){
            // Find the closest point on the AABB to the sphere center
            double closestX = clamp(location.getX(), livingEntity.getBoundingBox().getMinX(), livingEntity.getBoundingBox().getMaxX());
            double closestY = clamp(location.getY(), livingEntity.getBoundingBox().getMinY(), livingEntity.getBoundingBox().getMaxY());
            double closestZ = clamp(location.getZ(), livingEntity.getBoundingBox().getMinZ(), livingEntity.getBoundingBox().getMaxZ());

            // Compute the distance from the sphere center to this closest point
            double distanceSquared = location.distanceSquared(new Location(location.getWorld(),closestX, closestY, closestZ));

            // Check if the distance is less than or equal to the radius squared
            if  (distanceSquared <= (thickness * thickness)) return true;
        }
        return false;
    }

    private double clamp(double value, double min, double max) {
        if (value < min) return min;
        if (value > max) return max;
        return value;
    }

    //Children override this
    @Override
    public boolean borderContains(Location position) {
        return false;
    }

    //Children override this
    @Override
    public void visualize(Particle particle) {

    }

    //Children override this
    @Override
    public Location getCenter() {
        return centerLocation;
    }

    //Children override this
    @Override
    public List<Location> getEdgeLocations() {
        return null;
    }

    //Children override this
    @Override
    public List<Location> getLocations() {
        return locations;
    }

}
