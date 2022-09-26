package com.magmaguy.elitemobs.utils.shapes;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.util.Vector;

public class Cylinder extends Shape {

    private final Vector center;
    private final double radius;
    private final double height;
    private Location centerLocation = null;

    public Cylinder(Location centerLocation, double radius, double height) {
        this.centerLocation = centerLocation;
        this.center = centerLocation.toVector();
        this.radius = radius;
        this.height = height;
    }

    public Cylinder(Vector center, double radius, double height) {
        this.center = center;
        this.radius = radius;
        this.height = height;
    }

    public boolean contains(Vector position) {
        double dX = position.getX() - center.getX();
        double dZ = position.getZ() - center.getZ();
        return dX * dX + dZ * dZ < radius * radius && position.getY() < center.getY() + height;
    }

    public boolean contains(Location position) {
        return contains(position.toVector());
    }

    public void visualize(Particle particle, int count, double offsetX, double offsetY, double offsetZ, double speed) {
        for (int x = (int) -radius; x < (int) radius; x++)
            for (int z = (int) -radius; z < (int) radius; z++)
                for (int y = 0; y < height; y++) {
                    Location newLocation = centerLocation.clone().add(new Vector(x, y, z));
                    if (contains(newLocation))
                        newLocation.getWorld().spawnParticle(particle, newLocation, count, offsetX, offsetY, offsetZ, speed);
                }
    }

    @Override
    public Location getCenter() {
        return centerLocation;
    }

}

