package com.magmaguy.elitemobs.experimentalcombat.abilities;

import java.util.ArrayList;
import java.util.List;

/** Deterministic primary-and-scatter layout for cluster projectiles. */
final class ClusterImpactPlan {
    private ClusterImpactPlan() {
    }

    static List<Impact> create(double authoredRadius, int impactCount, double phaseRadians) {
        double radius = Math.max(1D, authoredRadius);
        int count = Math.max(1, Math.min(8, impactCount));
        double blastRadius = Math.max(1.25D, radius * .35D);
        double availableOffset = Math.max(0D, radius - blastRadius);
        List<Impact> impacts = new ArrayList<>(count);
        impacts.add(new Impact(0D, 0D, 0, blastRadius));
        for (int index = 1; index < count; index++) {
            double angle = phaseRadians + (index - 1D) * Math.PI * 2D / Math.max(1, count - 1);
            double distance = availableOffset * (.62D + .30D * (index % 2));
            impacts.add(new Impact(
                    Math.cos(angle) * distance,
                    Math.sin(angle) * distance,
                    3 + index * 3,
                    blastRadius));
        }
        return List.copyOf(impacts);
    }

    record Impact(double offsetX, double offsetZ, int delayTicks, double blastRadius) {
        Impact {
            if (!Double.isFinite(offsetX) || !Double.isFinite(offsetZ)
                    || !Double.isFinite(blastRadius) || blastRadius <= 0D || delayTicks < 0)
                throw new IllegalArgumentException("invalid cluster impact");
        }
    }
}
