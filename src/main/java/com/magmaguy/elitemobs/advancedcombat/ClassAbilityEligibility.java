package com.magmaguy.elitemobs.advancedcombat;

import org.bukkit.entity.Player;

import java.util.Objects;
import java.util.function.Predicate;

/**
 * Eligibility boundary for class active abilities and their temporary effects.
 *
 * <p>This deliberately does not represent [Alpha] Advanced Combat System's dungeon health, hunger, food,
 * passive, progression, recovery, or magic-weapon rules. Eligible EliteMobs combat content is one
 * entry path; a player's session-scoped outside-world class-control opt-in is the other.</p>
 */
public final class ClassAbilityEligibility {

    private static final Policy DENY_ALL = new Policy(ignored -> false, ignored -> false);
    private static volatile Policy policy = DENY_ALL;

    private ClassAbilityEligibility() {
    }

    public static boolean isEligible(Player player) {
        Objects.requireNonNull(player, "player");
        return policy.isEligible(player);
    }

    static void install(
            Predicate<Player> eligibleCombatContent,
            Predicate<Player> outsideControlOptIn) {
        policy = new Policy(
                Objects.requireNonNull(eligibleCombatContent, "eligibleCombatContent"),
                Objects.requireNonNull(outsideControlOptIn, "outsideControlOptIn"));
    }

    static void clear() {
        policy = DENY_ALL;
    }

    private record Policy(
            Predicate<Player> eligibleCombatContent,
            Predicate<Player> outsideControlOptIn) {

        private boolean isEligible(Player player) {
            return eligibleCombatContent.test(player) || outsideControlOptIn.test(player);
        }
    }
}
