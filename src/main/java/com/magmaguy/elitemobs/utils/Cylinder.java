package com.magmaguy.elitemobs.utils;

import org.bukkit.Location;
import org.bukkit.util.Vector;

public class Cylinder {

    private final Vector center;
    private final double radius;
    private final double height;

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

}

