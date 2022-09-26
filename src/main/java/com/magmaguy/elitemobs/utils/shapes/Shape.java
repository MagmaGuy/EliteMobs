package com.magmaguy.elitemobs.utils.shapes;

import org.bukkit.Location;
import org.bukkit.Particle;

public abstract class Shape {
    public abstract boolean contains(Location position);

    public abstract void visualize(Particle particle, int amount, double offsetX, double offsetY, double offsetZ, double speed);

    public abstract Location getCenter();
}
