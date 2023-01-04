package com.magmaguy.elitemobs.utils.shapes;

import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class Dome extends Sphere {
    public Dome(double radius, Location centerLocation, double borderRadius) {
        super(radius, centerLocation, borderRadius);
    }

    @Override
    public boolean contains(Location position){
        return super.contains(position) && position.getY() > centerLocation.getY();
    }

    @Override
    public boolean borderContains(Location position){
        return super.borderContains(position) && position.getY() > centerLocation.getY();
    }

    @Override
    protected List<Vector> getLocationVectors() {
        if (locationVectors != null) return locationVectors;
        locationVectors = new ArrayList<>();
        for (int x = (int) -radius; x < (int) radius; x++)
            for (int z = (int) -radius; z < (int) radius; z++)
                for (int y = 0; y < radius; y++) {
                    Vector newVector = new Vector(x, y, z);
                    Location newLocation = centerLocation.clone().add(newVector);
                    if (contains(newLocation)) locationVectors.add(newVector);
                }
        return locationVectors;
    }
}
