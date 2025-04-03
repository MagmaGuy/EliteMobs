package com.magmaguy.elitemobs.utils.shapes;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class Cone extends Shape {

    private final Location top;       // The apex of the cone
    private final Location bottom;    // The center of the base
    private final double baseRadius;  // The radius of the cone at the bottom
    private final double borderRadius; // Used for borderContains (an inner cone)
    private List<Location> locationList = null;
    private List<Location> edgeLocations = null;

    /**
     * Constructs a Cone shape.
     *
     * @param top          The apex (tip) of the cone.
     * @param bottom       The center of the base.
     * @param baseRadius   The radius at the bottom.
     * @param borderRadius The inner radius used for determining the cone’s border.
     */
    public Cone(Location top, Location bottom, double baseRadius, double borderRadius) {
        // We assume top and bottom are in the same world.
        this.top = top.clone();
        this.bottom = bottom.clone();
        this.baseRadius = baseRadius;
        this.borderRadius = borderRadius;
    }

    /**
     * Returns the axis vector from the top (apex) to the bottom (base center).
     */
    private Vector getAxis() {
        return bottom.toVector().subtract(top.toVector());
    }

    /**
     * Checks whether a given location is inside the cone.
     *
     * The method works by:
     * 1. Translating the position relative to the apex.
     * 2. Projecting that vector onto the cone's axis.
     * 3. Computing the fraction t along the axis (0 = at the apex, 1 = at the base).
     * 4. Verifying that the perpendicular distance is within the allowed radius (t * baseRadius).
     */
    @Override
    public boolean contains(Location position) {
        // Ensure the location is in the same world as the cone.
        if (!position.getWorld().equals(top.getWorld()))
            return false;

        Vector apex = top.toVector();
        Vector axis = getAxis();
        Vector p = position.toVector().subtract(apex);
        double axisLengthSquared = axis.lengthSquared();

        // Determine how far along the axis the point lies.
        double t = p.dot(axis) / axisLengthSquared;
        if (t < 0 || t > 1) {
            return false; // Outside the cone’s height range.
        }

        // Find the point on the axis that is closest to our position.
        Vector projection = axis.clone().multiply(t);
        double distanceSquared = p.subtract(projection).lengthSquared();
        double allowedRadius = t * baseRadius;
        return distanceSquared <= allowedRadius * allowedRadius;
    }

    /**
     * For a living entity we use its location (or center) for testing.
     */
    @Override
    public boolean contains(LivingEntity livingEntity) {
        return contains(livingEntity.getLocation());
    }

    /**
     * Determines if a location is on the “border” of the cone.
     * This is done by checking that the point is inside the cone but not inside a smaller (inner) cone.
     */
    @Override
    public boolean borderContains(Location position) {
        // Create an inner cone with a smaller base radius.
        Cone innerCone = new Cone(top, bottom, borderRadius, borderRadius);
        return contains(position) && !innerCone.contains(position);
    }

    /**
     * Visualizes the cone by spawning a particle at every location within the cone.
     */
    @Override
    public void visualize(Particle particle) {
        for (Location loc : getLocations()) {
            loc.getWorld().spawnParticle(particle, loc, 1, 0, 0, 0, 0);
        }
    }

    /**
     * The center of the cone is defined as the midpoint between the top and bottom.
     */
    @Override
    public Location getCenter() {
        return top.clone().add(bottom.toVector().subtract(top.toVector()).multiply(0.5));
    }

    /**
     * Returns the edge (border) locations of the cone.
     */
    @Override
    public List<Location> getEdgeLocations() {
        if (edgeLocations != null) return edgeLocations;
        edgeLocations = new ArrayList<>();
        for (Location loc : getLocations()) {
            if (borderContains(loc)) {
                edgeLocations.add(loc);
            }
        }
        return edgeLocations;
    }

    /**
     * Returns all the locations inside the cone.
     * We calculate a bounding box that conservatively contains the cone, then test each point.
     */
    @Override
    public List<Location> getLocations() {
        if (locationList != null) return locationList;
        locationList = new ArrayList<>();

        // Correctly calculate the bounding box by expanding all coordinates with baseRadius
        double minX = Math.min(top.getX(), bottom.getX()) - baseRadius;
        double maxX = Math.max(top.getX(), bottom.getX()) + baseRadius;
        double minY = Math.min(top.getY(), bottom.getY()) - baseRadius;
        double maxY = Math.max(top.getY(), bottom.getY()) + baseRadius;
        double minZ = Math.min(top.getZ(), bottom.getZ()) - baseRadius;
        double maxZ = Math.max(top.getZ(), bottom.getZ()) + baseRadius;

        // Iterate through the corrected bounding box
        for (double x = Math.floor(minX); x <= Math.ceil(maxX); x++) {
            for (double y = Math.floor(minY); y <= Math.ceil(maxY); y++) {
                for (double z = Math.floor(minZ); z <= Math.ceil(maxZ); z++) {
                    Location testLoc = new Location(top.getWorld(), x, y, z);
                    if (contains(testLoc)) {
                        locationList.add(testLoc);
                    }
                }
            }
        }
        return locationList;
    }
}
