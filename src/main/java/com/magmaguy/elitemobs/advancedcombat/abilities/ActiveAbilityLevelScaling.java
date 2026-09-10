package com.magmaguy.elitemobs.advancedcombat.abilities;

/**
 * Level scaling for non-damage active-ability levers.
 *
 * <p>Damage, healing, shields and servants already use {@link AbilityLevelScaling} at their
 * authoritative application boundary. This companion keeps duration, displacement and timed
 * modifier benefits on the same curve without scaling neutral value {@code 1.0} away from
 * neutrality.</p>
 */
public final class ActiveAbilityLevelScaling {
    private static final double MINIMUM_REDUCTION_MULTIPLIER = .05D;

    private ActiveAbilityLevelScaling() {
    }

    public static double amount(double authoredAmount, int effectiveLevel) {
        requireNonNegative(authoredAmount, "authoredAmount");
        return authoredAmount * AbilityLevelScaling.multiplier(effectiveLevel);
    }

    public static double displacement(double authoredDisplacement, int effectiveLevel) {
        return amount(authoredDisplacement, effectiveLevel);
    }

    public static int durationTicks(int authoredTicks, int effectiveLevel) {
        if (authoredTicks < 0) throw new IllegalArgumentException("authoredTicks must not be negative");
        if (authoredTicks == 0) return 0;
        double scaled = authoredTicks * AbilityLevelScaling.multiplier(effectiveLevel);
        return (int) Math.min(Integer.MAX_VALUE, Math.max(1L, Math.round(scaled)));
    }

    /** Scales a buff above 1 or a reduction below 1 by its distance from neutral. */
    public static double modifier(double authoredMultiplier, int effectiveLevel) {
        requireNonNegative(authoredMultiplier, "authoredMultiplier");
        double scale = AbilityLevelScaling.multiplier(effectiveLevel);
        if (authoredMultiplier > 1D)
            return 1D + (authoredMultiplier - 1D) * scale;
        if (authoredMultiplier < 1D)
            return Math.max(MINIMUM_REDUCTION_MULTIPLIER,
                    1D - (1D - authoredMultiplier) * scale);
        return 1D;
    }

    private static void requireNonNegative(double value, String field) {
        if (!Double.isFinite(value) || value < 0D)
            throw new IllegalArgumentException(field + " must be finite and non-negative");
    }
}
