package com.magmaguy.elitemobs.utils.shapes;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Sphere extends Shape {

    protected double radius;
    protected List<Vector> locationVectors = null;
    protected List<Vector> edgeVectors = null;
    protected double borderRadius = 1;
    protected Location centerLocation;

    public Sphere(double radius, Location centerLocation, double borderRadius) {
        this.radius = radius;
        this.centerLocation = centerLocation.clone();
        this.borderRadius = borderRadius;
    }

    @Override
    public boolean contains(Location position) {
        return centerLocation.distanceSquared(position) < Math.pow(radius, 2);
    }

    @Override
    public boolean borderContains(Location position) {
        Sphere innerSphere = new Sphere(borderRadius, centerLocation, borderRadius);
        return contains(position) && !innerSphere.contains(position);
    }

    @Override
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

    protected List<Vector> getLocationVectors() {
        if (locationVectors != null) return locationVectors;
        locationVectors = new ArrayList<>();
        for (int x = (int) -radius; x < (int) radius; x++)
            for (int z = (int) -radius; z < (int) radius; z++)
                for (int y = (int)-radius; y < radius; y++) {
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
