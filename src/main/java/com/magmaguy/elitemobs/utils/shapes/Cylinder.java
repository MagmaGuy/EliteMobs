package com.magmaguy.elitemobs.utils.shapes;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Cylinder extends Shape {

    private final Vector center;
    private final double radius;
    private final double height;
    private final List<Vector> edgeVectors = null;
    private double borderRadius = 1;
    private Location centerLocation = null;
    private List<Vector> locationVectors = null;

    public Cylinder(Location centerLocation, double radius, double height, double borderRadius) {
        this.centerLocation = centerLocation.clone();
        this.center = centerLocation.toVector();
        this.radius = radius;
        this.height = height;
        this.borderRadius = borderRadius;
    }

    public Cylinder(Vector center, double radius, double height) {
        this.center = center;
        this.radius = radius;
        this.height = height;
    }

    public boolean contains(Vector position) {
        double dX = position.getX() - center.getX();
        double dZ = position.getZ() - center.getZ();
        return dX * dX + dZ * dZ < radius * radius && position.getY() < center.getY() + height && position.getY() >= center.getY();
    }

    public boolean borderContains(Location position) {
        Cylinder innerCylinder = new Cylinder(centerLocation, borderRadius, height, borderRadius);
        return contains(position) && !innerCylinder.contains(position);
    }

    public boolean contains(Location position) {
        return contains(position.toVector());
    }

    @Override
    public boolean contains(LivingEntity livingEntity) {
        BoundingBox boundingBox = livingEntity.getBoundingBox();
        if (centerLocation.getY() > boundingBox.getMinY() || centerLocation.getY() + height < boundingBox.getMaxY())
            return false;

        double circleDistanceX = Math.abs(centerLocation.getX() - boundingBox.getCenterX());
        double circleDistanceZ = Math.abs(centerLocation.getZ() - boundingBox.getCenterZ());

        if (circleDistanceX > (boundingBox.getWidthX() / 2d + radius)) return false;
        if (circleDistanceZ > (boundingBox.getWidthZ() / 2d + radius)) return false;

        if (circleDistanceX <= (boundingBox.getWidthX() / 2d)) return true;
        if (circleDistanceZ <= (boundingBox.getWidthZ() / 2d)) return true;

        double cornerDistanceSquared = Math.pow((circleDistanceX - boundingBox.getWidthX()) / 2D, 2) +
                Math.pow((circleDistanceZ - boundingBox.getWidthZ()) / 2D, 2);

        return cornerDistanceSquared <= radius * radius;
    }

    public void visualize(Particle particle) {
        getLocations().forEach(newLocation -> newLocation.getWorld().spawnParticle(particle, newLocation, 1, 0, 0, 0, 0));
    }

    @Override
    public Location getCenter() {
        return centerLocation;
    }

    @Override
    public List<Location> getEdgeLocations() {
        if (edgeVectors != null) return convert(edgeVectors);
        List<Location> edgeLocations = new ArrayList<>();
        getLocations().forEach(iteratedLocation -> {
            if (borderContains(iteratedLocation))
                edgeLocations.add(iteratedLocation);
        });
        return edgeLocations;
    }

    private List<Vector> getLocationVectors() {
        if (locationVectors != null) return locationVectors;
        locationVectors = new ArrayList<>();
        for (int x = (int) -radius; x < (int) radius; x++)
            for (int z = (int) -radius; z < (int) radius; z++)
                for (int y = 0; y < height; y++) {
                    Vector newVector = new Vector(x, y, z);
                    Location newLocation = centerLocation.clone().add(newVector);
                    if (contains(newLocation)) locationVectors.add(newVector);
                }
        return locationVectors;
    }

    @Override
    public List<Location> getLocations() {
        if (locationVectors != null) return convert(locationVectors);
        return convert(getLocationVectors());
    }

    private List<Location> convert(List<Vector> vectors) {
        return vectors.stream().map(edge -> centerLocation.clone().add(edge)).collect(Collectors.toList());
    }

}

