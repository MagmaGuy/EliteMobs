package com.magmaguy.elitemobs.utils.shapes;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.magmacore.scripting.zones.Ray;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

/** A ray rotating around its fixed source endpoint. */
public class RotatingRay extends Ray {
    private final Vector initialDirection;
    private final Vector pitchAxis;
    private final double length;
    private Vector direction;

    public RotatingRay(boolean ignoresSolidBlocks, double pointRadius, Location target, Location target2,
                       double pitchPreRotation, double yawPreRotation, double pitchRotation, double yawRotation,
                       int animationDuration) {
        super(ignoresSolidBlocks, pointRadius, target, target2);
        validateEndpoints(target, target2, pointRadius, animationDuration);
        if (!Double.isFinite(pitchPreRotation) || !Double.isFinite(yawPreRotation)
                || !Double.isFinite(pitchRotation) || !Double.isFinite(yawRotation))
            throw new IllegalArgumentException("Ray rotations must be finite");
        length = target.distance(target2);
        if (length == 0) throw new IllegalArgumentException("Rotating ray endpoints must differ");
        centerLocation = target.clone();
        direction = target2.toVector().subtract(target.toVector()).normalize();
        direction.rotateAroundY(Math.toRadians(yawPreRotation));
        direction.rotateAroundAxis(perpendicular(direction), Math.toRadians(pitchPreRotation));
        initialDirection = direction.clone();
        pitchAxis = perpendicular(direction);
        locations = drawLine(centerLocation, target2);
        if (animationDuration > 0) {
            new BukkitRunnable() {
                private int frame;
                @Override
                public void run() {
                    try {
                        double progress = ++frame / (double) animationDuration;
                        direction = initialDirection.clone()
                                .rotateAroundAxis(pitchAxis, Math.toRadians(pitchRotation * progress))
                                .rotateAroundY(Math.toRadians(yawRotation * progress));
                        locations = drawLine(centerLocation, target2);
                        if (frame == animationDuration) cancel();
                    } catch (RuntimeException failure) {
                        cancel();
                        com.magmaguy.magmacore.util.Logger.warn("Stopped rotating ray after geometry failure: " + failure);
                    }
                }
            }.runTaskTimer(MetadataHandler.PLUGIN, 1L, 1L);
        }
    }

    static void validateEndpoints(Location first, Location second, double radius, int duration) {
        if (first == null || second == null || first.getWorld() == null || !first.getWorld().equals(second.getWorld()))
            throw new IllegalArgumentException("Ray endpoints must be in the same loaded world");
        first.checkFinite();
        second.checkFinite();
        if (!Double.isFinite(first.distanceSquared(second)) || !Double.isFinite(radius) || radius <= 0
                || !Double.isFinite(radius * radius) || !Double.isFinite(radius * 2) || duration < 0)
            throw new IllegalArgumentException("Ray radius must be finite and positive; duration must be nonnegative");
    }

    private static Vector perpendicular(Vector direction) {
        Vector axis = new Vector(direction.getZ(), 0, -direction.getX());
        return axis.lengthSquared() < 1.0e-20 ? new Vector(1, 0, 0) : axis.normalize();
    }

    @Override
    protected List<Location> drawLine(Location source, Location unusedTarget) {
        currentSource = source.clone();
        currentTarget = source.clone().add(direction.clone().normalize().multiply(length));
        currentTarget.checkFinite();
        List<Location> samples = new ArrayList<>();
        samples.add(currentSource.clone());
        double step = thickness * 2;
        for (int index = 1; index <= maxDistance && index * step <= length; index++) {
            Location sample = source.clone().add(direction.clone().multiply(index * step));
            sample.checkFinite();
            if (!ignoresSolidBlocks && sample.getBlock().getType().isSolid()) break;
            samples.add(sample);
        }
        return samples;
    }
}
