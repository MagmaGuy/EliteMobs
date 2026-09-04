package com.magmaguy.elitemobs.experimentalcombat.abilities;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import com.magmaguy.elitemobs.experimentalcombat.classes.AbilitySlot;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Semantic integration points that cannot be inferred safely from raw Bukkit entities.
 * Implementations bridge party membership, EliteMobs threat and participation accounting.
 */
public interface AbilitySemantics {
    boolean isEnemy(Player caster, LivingEntity candidate);

    default boolean canTargetEnemy(
            Player caster,
            LivingEntity candidate,
            FixedAbilitySpec spec) {
        return isEnemy(caster, candidate);
    }

    default boolean canApplyEnemyEffect(
            Player caster,
            LivingEntity candidate,
            FixedAbilitySpec spec,
            AbilityEffect effect) {
        return canTargetEnemy(caster, candidate, spec);
    }

    default Collection<Player> alliesOf(Player caster) {
        return List.of(caster);
    }

    default double healingMultiplier(Player caster) {
        return 1D;
    }

    default AbilityMechanicModifiers mechanicModifiers(Player caster) {
        return AbilityMechanicModifiers.NEUTRAL;
    }

    default void signal(Player player, AbilityRuntimeSignal signal, int durationTicks) {
    }

    default void requestThreat(ThreatRequest request) {
    }

    default void recordContribution(Player caster, String abilityId, AbilityContribution contribution) {
    }

    default void observe(AbilityRuntimeObservation observation) {
    }

    default void grantResource(Player caster, double amount) {
    }

    default void clearSource(Player caster) {
    }

    default List<String> activeFormIds(Player player) {
        return List.of();
    }

    record ThreatRequest(Player caster, List<LivingEntity> enemies, double amountPerEnemy, int forcedTargetTicks,
                         String abilityId) {
        public ThreatRequest {
            caster = Objects.requireNonNull(caster, "caster");
            enemies = List.copyOf(enemies);
            if (!Double.isFinite(amountPerEnemy) || amountPerEnemy < 0)
                throw new IllegalArgumentException("amountPerEnemy must be finite and non-negative");
            if (forcedTargetTicks < 0) throw new IllegalArgumentException("forcedTargetTicks must not be negative");
            Objects.requireNonNull(abilityId, "abilityId");
        }
    }
}
