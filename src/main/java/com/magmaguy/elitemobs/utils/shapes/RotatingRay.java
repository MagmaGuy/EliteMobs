package com.magmaguy.elitemobs.utils.shapes;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.utils.Lerp;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class RotatingRay extends Ray {


    private final int animationDuration;
    private final Location originalCenterLocation;
    private Location finalTarget = null;
    private Vector raySegment;

    public RotatingRay(boolean ignoresSolidBlocks,
                       double pointRadius,
                       Location target,
                       Location finalTarget,
                       Location target2,
                       double pitchPreRotation,
                       double yawPreRotation,
                       double pitchRotation,
                       double yawRotation,
                       int animationDuration) {
        super(ignoresSolidBlocks, pointRadius, target, target2);
        this.originalCenterLocation = target.clone();
        if (finalTarget == null)
            this.finalTarget = target;
        else
            this.finalTarget = finalTarget.clone();
        this.animationDuration = animationDuration;
        raySegment = target2.clone().subtract(target).toVector().normalize().multiply(pointRadius * 2);
        if (yawPreRotation != 0)
            raySegment.rotateAroundY(Math.toRadians(yawPreRotation));
        if (pitchPreRotation != 0) {
            Vector perpendicularVector = raySegment.clone().rotateAroundY(Math.toRadians(90));
            raySegment.rotateAroundAxis(perpendicularVector, Math.toRadians(pitchPreRotation));
        }
        locations = drawLine();
        if (animationDuration > 0) startRotating(animationDuration, pitchRotation, yawRotation);
    }

    protected List<Location> drawLine() {
        List<Location> locations = new ArrayList<>();
        Location currentLocation = originalCenterLocation.clone();
        locations.add(originalCenterLocation);
        for (int i = 0; i < maxDistance; i++) {
            if (currentLocation.distanceSquared(locations.get(locations.size() - 1)) >=
                    originalCenterLocation.distanceSquared(initialTargetLocation)) break;
            currentLocation.add(raySegment);
            if (!ignoresSolidBlocks && currentLocation.getBlock().getType().isSolid()) break;
            locations.add(currentLocation.clone());
            //currentLocation.getWorld().spawnParticle(Particle.FLAME, currentLocation, 1,0,0,0,0);
        }
        return locations;
    }


    private void startRotating(int totalTickDuration, double pitchRotation, double yawRotation) {
        double singleTickPitchRotation = pitchRotation != 0 ? pitchRotation / totalTickDuration : 0;
        double singleTickYawRotation = yawRotation != 0 ? yawRotation / totalTickDuration : 0;
        Vector perpendicularVector = raySegment.clone().rotateAroundY(Math.toRadians(90));
        new BukkitRunnable() {
            private int counter = 1;

            @Override
            public void run() {
                if (counter > animationDuration) {
                    cancel();
                    return;
                }
                counter++;

                if (finalTarget != null)
                    centerLocation = Lerp.lerpLocation(originalCenterLocation, finalTarget, counter / (double) animationDuration);

                if (singleTickPitchRotation > 0)
                    raySegment.rotateAroundAxis(perpendicularVector, Math.toRadians(singleTickPitchRotation));
                if (singleTickYawRotation > 0)
                    raySegment.rotateAroundY(Math.toRadians(singleTickYawRotation));
                locations = drawLine();
            }
        }.runTaskTimer(MetadataHandler.PLUGIN, 1L, 1L);
    }

}
