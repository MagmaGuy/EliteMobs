package com.magmaguy.elitemobs.experimentalcombat.abilities;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Divides one expanding area effect into disjoint rings. A stationary target belongs to exactly
 * one pulse, and the last ring lands at the advertised duration.
 */
public record ExpandingPulsePlan(List<Pulse> pulses) {

    public ExpandingPulsePlan {
        pulses = List.copyOf(pulses);
        if (pulses.isEmpty()) throw new IllegalArgumentException("At least one pulse is required");
    }

    public static ExpandingPulsePlan create(double maximumRadius, int pulseCount, int durationTicks) {
        if (!Double.isFinite(maximumRadius) || maximumRadius <= 0D)
            throw new IllegalArgumentException("maximumRadius must be positive and finite");
        if (pulseCount < 1) throw new IllegalArgumentException("pulseCount must be positive");
        if (durationTicks < 0) throw new IllegalArgumentException("durationTicks cannot be negative");

        List<Pulse> pulses = new ArrayList<>(pulseCount);
        for (int index = 0; index < pulseCount; index++) {
            double innerRadius = maximumRadius * index / pulseCount;
            double outerRadius = maximumRadius * (index + 1D) / pulseCount;
            int delay = pulseCount == 1
                    ? 0
                    : (int) Math.round(durationTicks * index / (double) (pulseCount - 1));
            pulses.add(new Pulse(index, delay, innerRadius, outerRadius));
        }
        return new ExpandingPulsePlan(pulses);
    }

    public Optional<Pulse> pulseForDistance(double distance) {
        if (!Double.isFinite(distance) || distance < 0D) return Optional.empty();
        return pulses.stream().filter(pulse -> pulse.contains(distance)).findFirst();
    }

    public record Pulse(
            int index,
            int delayTicks,
            double innerRadiusExclusive,
            double outerRadiusInclusive) {

        public Pulse {
            if (index < 0 || delayTicks < 0)
                throw new IllegalArgumentException("Pulse index and delay cannot be negative");
            if (!Double.isFinite(innerRadiusExclusive)
                    || !Double.isFinite(outerRadiusInclusive)
                    || innerRadiusExclusive < 0D
                    || outerRadiusInclusive <= innerRadiusExclusive)
                throw new IllegalArgumentException("Pulse radii must define a positive ring");
        }

        public boolean contains(double distance) {
            if (!Double.isFinite(distance) || distance < 0D) return false;
            return (index == 0 ? distance >= 0D : distance > innerRadiusExclusive)
                    && distance <= outerRadiusInclusive;
        }
    }
}
