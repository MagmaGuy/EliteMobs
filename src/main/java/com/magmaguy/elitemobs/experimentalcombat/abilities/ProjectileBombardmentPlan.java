package com.magmaguy.elitemobs.experimentalcombat.abilities;

import java.util.ArrayList;
import java.util.List;
import java.util.SplittableRandom;

/**
 * Plans visible projectile deliveries across a circular bombardment area. Each impact represents
 * one projectile and can therefore claim at most one damage delivery at runtime.
 */
public record ProjectileBombardmentPlan(
        List<Impact> impacts,
        double impactRadius) {

    public ProjectileBombardmentPlan {
        impacts = List.copyOf(impacts);
        if (impacts.isEmpty()) throw new IllegalArgumentException("At least one impact is required");
        if (!Double.isFinite(impactRadius) || impactRadius <= 0D)
            throw new IllegalArgumentException("impactRadius must be positive and finite");
    }

    public static ProjectileBombardmentPlan create(
            double areaRadius,
            int durationTicks,
            int volleyCount,
            int projectilesPerVolley,
            int fallTicks,
            long spreadSeed) {
        if (!Double.isFinite(areaRadius) || areaRadius <= 0D)
            throw new IllegalArgumentException("areaRadius must be positive and finite");
        if (volleyCount < 1 || projectilesPerVolley < 1)
            throw new IllegalArgumentException("Volley and projectile counts must be positive");
        if (fallTicks < 1 || durationTicks < fallTicks)
            throw new IllegalArgumentException("Duration must contain the projectile fall time");

        SplittableRandom random = new SplittableRandom(spreadSeed);
        List<Impact> impacts = new ArrayList<>(volleyCount * projectilesPerVolley);
        int deliveryIndex = 0;
        for (int volley = 0; volley < volleyCount; volley++) {
            int impactTick = volleyCount == 1
                    ? durationTicks
                    : fallTicks + (int) Math.round(
                    (durationTicks - fallTicks) * volley / (double) (volleyCount - 1));
            int launchTick = impactTick - fallTicks;
            for (int projectile = 0; projectile < projectilesPerVolley; projectile++) {
                double radialDistance = areaRadius * Math.sqrt(random.nextDouble());
                double angle = random.nextDouble(0D, Math.PI * 2D);
                impacts.add(new Impact(
                        deliveryIndex++,
                        volley,
                        projectile,
                        launchTick,
                        impactTick,
                        Math.cos(angle) * radialDistance,
                        Math.sin(angle) * radialDistance));
            }
        }
        double impactRadius = Math.max(1D, Math.min(1.5D, areaRadius / 5D));
        return new ProjectileBombardmentPlan(impacts, impactRadius);
    }

    public int maximumDamageDeliveries() {
        return impacts.size();
    }

    public record Impact(
            int deliveryIndex,
            int volleyIndex,
            int projectileIndex,
            int launchTick,
            int impactTick,
            double offsetX,
            double offsetZ) {

        public Impact {
            if (deliveryIndex < 0 || volleyIndex < 0 || projectileIndex < 0
                    || launchTick < 0 || impactTick <= launchTick)
                throw new IllegalArgumentException("Impact indices and timing must be valid");
            if (!Double.isFinite(offsetX) || !Double.isFinite(offsetZ))
                throw new IllegalArgumentException("Impact offsets must be finite");
        }
    }
}
