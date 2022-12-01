package com.magmaguy.elitemobs.utils.shapes;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Cuboid extends Shape {
    private Location centerLocation = null;
    private List<Vector> locationVectors = null;
    private List<Vector> edgeVectors = null;
    private double x, y, z, xBorder, yBorder, zBorder;

    public Cuboid(Double x, Double y, Double z, Double xBorder, Double yBorder, Double zBorder, Location centerLocation) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.xBorder = xBorder;
        this.yBorder = yBorder;
        this.zBorder = zBorder;
        this.centerLocation = centerLocation;
        if (x != 0 && z == 0) this.z = x;
        if (xBorder != 0 && zBorder == 0) this.zBorder = xBorder;
    }

    @Override
    public boolean contains(Location position) {
        Vector localVector = position.clone().subtract(centerLocation).toVector();
        if (Math.abs(localVector.getX()) > x) return false;
        if (Math.abs(localVector.getY()) > y) return false;
        if (Math.abs(localVector.getZ()) > z) return false;
        return true;
    }

    @Override
    public boolean borderContains(Location position) {
        Vector localVector = position.clone().subtract(centerLocation).toVector();
        if (!(Math.abs(localVector.getX()) <= x / 2D && Math.abs(localVector.getX()) >= xBorder / 2D ||
                Math.abs(localVector.getZ()) <= z / 2D && Math.abs(localVector.getZ()) >= zBorder / 2D)) return false;
        if (y > 0 && Math.abs(localVector.getY()) <= y && Math.abs(localVector.getY()) >= yBorder) return false;
        return true;
    }

    @Override
    public void visualize(Particle particle) {

    }

    @Override
    public Location getCenter() {
        return centerLocation;
    }

    @Override
    public List<Location> getEdgeLocations() {
        return convert(getEdgeVectors());
    }

    public List<Vector> getEdgeVectors() {
        if (edgeVectors != null) return edgeVectors;
        edgeVectors = new ArrayList<>();
        for (Vector vector : getLocationVectors())
            if (vector.getY() <= yBorder &&
                    (Math.abs(vector.getX()) >= xBorder / 2d || Math.abs(vector.getZ()) >= zBorder / 2d))
                edgeVectors.add(vector);
        return edgeVectors;
    }

    private List<Vector> getLocationVectors() {
        if (locationVectors != null) return locationVectors;
        locationVectors = new ArrayList<>();
        double xHalf = x / 2d;
        double zHalf = z / 2d;
        for (int localX = 0; localX < x; localX++)
            for (int localZ = 0; localZ < z; localZ++)
                for (int localY = 0; localY < y; localY++)
                    locationVectors.add(new Vector(localX - xHalf, localY, localZ - zHalf));

        return locationVectors;
    }

    @Override
    public List<Location> getLocations() {
        return convert(getLocationVectors());
    }

    private List<Location> convert(List<Vector> vectors) {
        return vectors.stream().map(edge -> centerLocation.clone().add(edge)).collect(Collectors.toList());
    }

}
