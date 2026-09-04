package com.magmaguy.elitemobs.experimentalcombat.abilities;

/**
 * Final potion and movement values delivered by a slowing class ability.
 *
 * <p>The plan is the boundary between continuous class scaling and Minecraft's integer potion
 * amplifiers. Runtime code must apply every field so a fractional control-power bonus remains
 * observable instead of disappearing during amplifier rounding.</p>
 */
public record EliteControlEffectPlan(
        int durationTicks,
        int potionAmplifier,
        double additionalMovementSpeedAdjustment) {

    public EliteControlEffectPlan {
        if (durationTicks < 0) throw new IllegalArgumentException("durationTicks must not be negative");
        if (potionAmplifier < 0 || potionAmplifier > 255)
            throw new IllegalArgumentException("potionAmplifier must be between 0 and 255");
        if (!Double.isFinite(additionalMovementSpeedAdjustment)
                || additionalMovementSpeedAdjustment > 0D
                || additionalMovementSpeedAdjustment < -.95D)
            throw new IllegalArgumentException(
                    "additionalMovementSpeedAdjustment must be finite and between -0.95 and 0");
    }

    public static EliteControlEffectPlan slow(
            int authoredAmplifier,
            int levelScaledDurationTicks,
            AbilityMechanicModifiers modifiers) {
        if (authoredAmplifier < 0)
            throw new IllegalArgumentException("authoredAmplifier must not be negative");
        if (levelScaledDurationTicks < 0)
            throw new IllegalArgumentException("levelScaledDurationTicks must not be negative");
        if (modifiers == null) throw new IllegalArgumentException("modifiers must not be null");
        double desiredReduction = Math.min(.95D,
                .15D * (authoredAmplifier + 1D) * modifiers.controlPotencyMultiplier());
        int wholePotionTiers = Math.max(1,
                Math.min(6, (int) Math.floor(desiredReduction / .15D + 1.0E-9D)));
        double potionReduction = wholePotionTiers * .15D;
        double potionMultiplier = Math.max(.05D, 1D - potionReduction);
        double desiredMultiplier = Math.max(.05D, 1D - desiredReduction);
        double remainingAdjustment = Math.max(-.95D,
                desiredMultiplier / potionMultiplier - 1D);
        return new EliteControlEffectPlan(
                scaledDuration(levelScaledDurationTicks, modifiers.controlDurationMultiplier()),
                wholePotionTiers - 1,
                remainingAdjustment);
    }

    /** Final movement multiplier after the potion and continuous adjustment are both applied. */
    public double movementSpeedMultiplier() {
        double potionAdjustment = -.15D * (potionAmplifier + 1D);
        return Math.max(0D,
                (1D + potionAdjustment) * (1D + additionalMovementSpeedAdjustment));
    }

    private static int scaledDuration(int authoredTicks, double multiplier) {
        if (authoredTicks <= 0) return 0;
        return (int) Math.min(Integer.MAX_VALUE,
                Math.max(1L, Math.round(authoredTicks * multiplier)));
    }
}
