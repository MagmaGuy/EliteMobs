package com.magmaguy.elitemobs.utils.shapes;

import lombok.Getter;
import org.apache.commons.math3.complex.Quaternion;
import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.util.FastMath;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.BoundingBox;
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
        RayCuboid rayCuboid = new RayCuboid(thickness, currentSource, currentTarget);
        return rayCuboid.AABBCheck(livingEntity);
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

    private class RayCuboid {
        @Getter
        private final double width;
        @Getter
        private final double height;
        @Getter
        private final Location centerLocation;
        @Getter
        private Quaternion rotation;
        private final double maxX;
        private final double minX;
        private final double maxY;
        private final double minY;
        private final double maxZ;
        private final double minZ;

        public RayCuboid(double width, Location sourceLocation, Location destinationLocation) {
            this.width = width;
            this.height = sourceLocation.toVector().subtract(destinationLocation.toVector()).length();
            this.centerLocation = sourceLocation;
            calculateRotation(destinationLocation);
            maxX = sourceLocation.getX() + width;
            minX = sourceLocation.getX() - width;
            maxY = sourceLocation.getY() + height;
            minY = 0;
            maxZ = sourceLocation.getZ() + width;
            minZ = sourceLocation.getZ() - width;
        }

        public static Quaternion shortestArcQuaternion(Vector vector0, Vector vector1) {
            // Based on Stan Melax's article in Game Programming Gems
            Quaternion quaternion = Quaternion.ZERO;
            // Copy, since cannot modify local
            Vector v0Copy = vector0.clone();
            Vector v1Copy = vector1.clone();
            v0Copy.normalize();
            v1Copy.normalize();

            double d = v0Copy.dot(v1Copy);
            // If dot == 1, vectors are the same
            if (d >= 1.0f) {
                return Quaternion.IDENTITY;
            }
            if (d < (1e-6f - 1.0f)) {
                // Generate an axis
                Vector axis = v0Copy.getCrossProduct(new Vector(1, 0, 0));
                if (axis.length() == 0) // pick another if colinear
                    axis = v0Copy.getCrossProduct(new Vector(0, 1, 0));
                axis.normalize();

                // Create a quaternion from an angle and an axis
                // The angle is in radians
                double angle = FastMath.PI / 4;
                // Set the quaternion using the formula
                quaternion = new Quaternion(Math.cos(angle / 2), axis.getX() * Math.sin(angle / 2), axis.getY() * Math.sin(angle / 2), axis.getZ() * Math.sin(angle / 2));
                quaternion.normalize();
            } else {
                double s = FastMath.sqrt((1 + d) * 2);
                double invs = 1 / s;

                // Compute cross product of two vectors
                Vector c = v0Copy.getCrossProduct(v1Copy);

                double x = c.getX() * invs;
                double y = c.getY() * invs;
                double z = c.getZ() * invs;
                double w = s * 0.5f;
                quaternion = new Quaternion(w, x, y, z);
                quaternion.normalize();
            }
            return quaternion;
        }

        private void calculateRotation(Location destinationLocation) {
            Location verticalEndpoint = centerLocation.clone().add(new Vector(0, height, 0));
            Vector vector1 = verticalEndpoint.clone().subtract(centerLocation).toVector();

            Vector vector2 = destinationLocation.clone().subtract(centerLocation).toVector();
            rotation = shortestArcQuaternion(vector1, vector2);
        }

        public boolean AABBCheck(LivingEntity livingEntity) {
            BoundingBox boundingBox = livingEntity.getBoundingBox();
            if (!centerLocation.getWorld().equals(livingEntity.getWorld())) return false;

            List<Vector> vectorList = new ArrayList<>();
            vectorList.add(rotateVectorByQuaternion(new Vector(boundingBox.getMinX(), boundingBox.getMinY(), boundingBox.getMinZ())));
            vectorList.add(rotateVectorByQuaternion(new Vector(boundingBox.getMinX(), boundingBox.getMinY(), boundingBox.getMaxZ())));
            vectorList.add(rotateVectorByQuaternion(new Vector(boundingBox.getMinX(), boundingBox.getMaxY(), boundingBox.getMinZ())));
            vectorList.add(rotateVectorByQuaternion(new Vector(boundingBox.getMinX(), boundingBox.getMaxY(), boundingBox.getMaxZ())));
            vectorList.add(rotateVectorByQuaternion(new Vector(boundingBox.getMaxX(), boundingBox.getMinY(), boundingBox.getMinZ())));
            vectorList.add(rotateVectorByQuaternion(new Vector(boundingBox.getMaxX(), boundingBox.getMinY(), boundingBox.getMaxZ())));
            vectorList.add(rotateVectorByQuaternion(new Vector(boundingBox.getMaxX(), boundingBox.getMaxY(), boundingBox.getMinZ())));
            vectorList.add(rotateVectorByQuaternion(new Vector(boundingBox.getMaxX(), boundingBox.getMaxY(), boundingBox.getMaxZ())));
            Double newPlayerMinX = null, newPlayerMaxX = null, newPlayerMinY = null, newPlayerMaxY = null, newPlayerMinZ = null, newPlayerMaxZ = null;
            for (Vector vector : vectorList) {
                if (newPlayerMinX == null || newPlayerMinX > vector.getX()) newPlayerMinX = vector.getX();
                if (newPlayerMaxX == null || newPlayerMaxX < vector.getX()) newPlayerMaxX = vector.getX();
                if (newPlayerMinY == null || newPlayerMinY > vector.getY()) newPlayerMinY = vector.getY();
                if (newPlayerMaxY == null || newPlayerMaxY < vector.getY()) newPlayerMaxY = vector.getY();
                if (newPlayerMinZ == null || newPlayerMinZ > vector.getZ()) newPlayerMinZ = vector.getZ();
                if (newPlayerMaxZ == null || newPlayerMaxZ < vector.getZ()) newPlayerMaxZ = vector.getZ();
            }

            return (minX <= newPlayerMinX && maxX >= newPlayerMinX ||
                    minX <= newPlayerMaxX && maxX >= newPlayerMaxX)
                    &&
                    (minY <= newPlayerMinY && maxY >= newPlayerMinY ||
                            minY <= newPlayerMaxY && maxY >= newPlayerMaxY)
                    &&
                    (minZ <= newPlayerMinZ && maxZ >= newPlayerMinZ ||
                            minZ <= newPlayerMaxZ && maxZ >= newPlayerMaxZ);
        }

        private Vector rotateVectorByQuaternion(Vector originalVector) {
            Vector vector = originalVector.clone().subtract(centerLocation.toVector());
            Vector3D localVector = new Vector3D(vector.getX(), vector.getY(), vector.getZ());
            Rotation localRotation = new Rotation(rotation.getQ0(), rotation.getQ1(), rotation.getQ2(), rotation.getQ3(), false);
            Vector3D result = localRotation.applyTo(localVector);
            return new Vector(result.getX(), result.getY(), result.getZ()).add(centerLocation.toVector());
        }
    }
}
