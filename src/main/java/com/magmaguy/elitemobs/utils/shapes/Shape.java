package com.magmaguy.elitemobs.utils.shapes;

import org.bukkit.Location;
import org.bukkit.Particle;

import java.util.List;

public abstract class Shape {
    public abstract boolean contains(Location position);

    public abstract boolean borderContains(Location position);

    public abstract void visualize(Particle particle);

    public abstract Location getCenter();

    public abstract List<Location> getEdgeLocations();

    public abstract List<Location> getLocations();
}
