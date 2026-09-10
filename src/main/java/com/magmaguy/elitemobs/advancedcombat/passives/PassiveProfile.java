package com.magmaguy.elitemobs.advancedcombat.passives;

/**
 * Fixed, code-owned mechanical profile for one class passive.
 *
 * <p>Values are additive fractions. A value of {@code .05} means five percent. The base is
 * granted as soon as the form is active; the per-level value lets inherited passives continue
 * growing under descendants without mutating the stored parent level.</p>
 */
public record PassiveProfile(
        double outgoingDamageBase,
        double outgoingDamagePerLevel,
        double incomingDamageBase,
        double incomingDamagePerLevel,
        double movementSpeedBase,
        double movementSpeedPerLevel,
        double healingDoneBase,
        double healingDonePerLevel,
        double abilityCostReductionBase,
        double abilityCostReductionPerLevel,
        double healingReceivedBase,
        double healingReceivedPerLevel) {

    public PassiveProfile {
        requireFinite(outgoingDamageBase, "outgoingDamageBase");
        requireFinite(outgoingDamagePerLevel, "outgoingDamagePerLevel");
        requireFinite(incomingDamageBase, "incomingDamageBase");
        requireFinite(incomingDamagePerLevel, "incomingDamagePerLevel");
        requireFinite(movementSpeedBase, "movementSpeedBase");
        requireFinite(movementSpeedPerLevel, "movementSpeedPerLevel");
        requireFinite(healingDoneBase, "healingDoneBase");
        requireFinite(healingDonePerLevel, "healingDonePerLevel");
        requireFinite(abilityCostReductionBase, "abilityCostReductionBase");
        requireFinite(abilityCostReductionPerLevel, "abilityCostReductionPerLevel");
        requireFinite(healingReceivedBase, "healingReceivedBase");
        requireFinite(healingReceivedPerLevel, "healingReceivedPerLevel");
    }

    public Contribution atContributionLevel(int level) {
        if (level < 1) throw new IllegalArgumentException("Passive contribution level must be positive");
        return new Contribution(
                outgoingDamageBase + outgoingDamagePerLevel * level,
                incomingDamageBase + incomingDamagePerLevel * level,
                movementSpeedBase + movementSpeedPerLevel * level,
                healingDoneBase + healingDonePerLevel * level,
                abilityCostReductionBase + abilityCostReductionPerLevel * level,
                healingReceivedBase + healingReceivedPerLevel * level);
    }

    private static void requireFinite(double value, String name) {
        if (!Double.isFinite(value)) throw new IllegalArgumentException(name + " must be finite");
    }

    public record Contribution(
            double outgoingDamage,
            double incomingDamage,
            double movementSpeed,
            double healingDone,
            double abilityCostReduction,
            double healingReceived) {

        public Contribution plus(Contribution other) {
            return new Contribution(
                    outgoingDamage + other.outgoingDamage,
                    incomingDamage + other.incomingDamage,
                    movementSpeed + other.movementSpeed,
                    healingDone + other.healingDone,
                    abilityCostReduction + other.abilityCostReduction,
                    healingReceived + other.healingReceived);
        }
    }
}
