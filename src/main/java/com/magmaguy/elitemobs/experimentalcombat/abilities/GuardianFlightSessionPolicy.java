package com.magmaguy.elitemobs.experimentalcombat.abilities;

/** Pure ordering for Guardian Flight terminal conditions. */
final class GuardianFlightSessionPolicy {
    static final int STALLED_TICKS_BEFORE_OBSTRUCTION = 3;

    private GuardianFlightSessionPolicy() {
    }

    static Decision evaluate(Frame frame) {
        if (!frame.casterValid()) return Decision.CASTER_INVALID;
        if (!frame.targetValid()
                || !Double.isFinite(frame.targetDistance())
                || frame.targetDistance() < 0D) return Decision.TARGET_INVALID;
        if (frame.elapsedTicks() >= frame.maximumTicks()) return Decision.EXPIRED;
        if (frame.targetDistance() > frame.maximumTargetDistance()) return Decision.OUT_OF_RANGE;
        if (frame.targetDistance() <= frame.arrivalDistance()) return Decision.ARRIVED;
        if (frame.pathObstructed()
                || frame.stalledTicks() >= STALLED_TICKS_BEFORE_OBSTRUCTION)
            return Decision.OBSTRUCTED;
        return Decision.CONTINUE;
    }

    record Frame(
            boolean casterValid,
            boolean targetValid,
            int elapsedTicks,
            int maximumTicks,
            double targetDistance,
            double maximumTargetDistance,
            double arrivalDistance,
            boolean pathObstructed,
            int stalledTicks) {
        Frame {
            if (elapsedTicks < 0 || maximumTicks < 1 || stalledTicks < 0)
                throw new IllegalArgumentException("Guardian Flight tick counts must be valid");
            if (!Double.isFinite(maximumTargetDistance) || maximumTargetDistance <= 0D
                    || !Double.isFinite(arrivalDistance) || arrivalDistance < 0D)
                throw new IllegalArgumentException("Guardian Flight distances must be valid");
        }
    }

    enum Decision {
        CONTINUE,
        ARRIVED,
        OBSTRUCTED,
        TARGET_INVALID,
        CASTER_INVALID,
        OUT_OF_RANGE,
        EXPIRED
    }
}
