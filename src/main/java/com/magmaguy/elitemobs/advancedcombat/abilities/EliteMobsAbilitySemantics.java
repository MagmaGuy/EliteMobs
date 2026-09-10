package com.magmaguy.elitemobs.advancedcombat.abilities;

import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.advancedcombat.classes.AbilitySlot;
import com.magmaguy.elitemobs.parties.PartyManager;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Canonical EliteMobs enemy/party adapter. Threat-only mutation and class-participation persistence
 * remain explicit callbacks because neither currently has a safe public core mutator.
 */
public final class EliteMobsAbilitySemantics implements AbilitySemantics {
    private final ThreatSink threatSink;
    private final ContributionSink contributionSink;
    private final HealingMultiplier healingMultiplier;
    private final ResourceSink resourceSink;
    private final ActiveLineageProvider activeLineageProvider;
    private final SourceCleanup sourceCleanup;
    private final MechanicModifierProvider mechanicModifierProvider;
    private final RuntimeSignalSink runtimeSignalSink;
    private final ObservationSink observationSink;

    public EliteMobsAbilitySemantics(
            ThreatSink threatSink,
            ContributionSink contributionSink,
            HealingMultiplier healingMultiplier,
            ResourceSink resourceSink,
            ActiveLineageProvider activeLineageProvider,
            SourceCleanup sourceCleanup,
            MechanicModifierProvider mechanicModifierProvider,
            RuntimeSignalSink runtimeSignalSink,
            ObservationSink observationSink) {
        this.threatSink = Objects.requireNonNull(threatSink, "threatSink");
        this.contributionSink = Objects.requireNonNull(contributionSink, "contributionSink");
        this.healingMultiplier = Objects.requireNonNull(healingMultiplier, "healingMultiplier");
        this.resourceSink = Objects.requireNonNull(resourceSink, "resourceSink");
        this.activeLineageProvider = Objects.requireNonNull(activeLineageProvider, "activeLineageProvider");
        this.sourceCleanup = Objects.requireNonNull(sourceCleanup, "sourceCleanup");
        this.mechanicModifierProvider = Objects.requireNonNull(
                mechanicModifierProvider, "mechanicModifierProvider");
        this.runtimeSignalSink = Objects.requireNonNull(runtimeSignalSink, "runtimeSignalSink");
        this.observationSink = Objects.requireNonNull(observationSink, "observationSink");
    }

    @Override
    public boolean isEnemy(Player caster, LivingEntity candidate) {
        return candidate != null && EntityTracker.getEliteMobEntity(candidate) != null;
    }

    @Override
    public boolean canTargetEnemy(Player caster, LivingEntity candidate, FixedAbilitySpec spec) {
        return EliteAbilityTargetAuthorization.canTarget(caster, candidate, spec);
    }

    @Override
    public boolean canApplyEnemyEffect(
            Player caster,
            LivingEntity candidate,
            FixedAbilitySpec spec,
            AbilityEffect effect) {
        return EliteAbilityTargetAuthorization.canApply(caster, candidate, spec, effect);
    }

    @Override
    public Collection<Player> alliesOf(Player caster) {
        var casterMatch = com.magmaguy.elitemobs.playerdata.database.PlayerData.getMatchInstance(caster);
        List<Player> nearbyParty = PartyManager.getNearbyMembers(caster, caster.getLocation()).stream()
                .filter(ally -> com.magmaguy.elitemobs.playerdata.database.PlayerData.getMatchInstance(ally) == casterMatch)
                .toList();
        if (nearbyParty.stream().anyMatch(player -> player.getUniqueId().equals(caster.getUniqueId())))
            return nearbyParty;
        java.util.ArrayList<Player> allies = new java.util.ArrayList<>(nearbyParty.size() + 1);
        allies.add(caster);
        allies.addAll(nearbyParty);
        return List.copyOf(allies);
    }

    @Override
    public void requestThreat(ThreatRequest request) {
        threatSink.apply(request);
    }

    @Override
    public void recordContribution(Player caster, String abilityId, AbilityContribution contribution) {
        contributionSink.record(caster, abilityId, contribution);
    }

    @Override
    public void observe(AbilityRuntimeObservation observation) {
        observationSink.record(observation);
    }

    @Override
    public double healingMultiplier(Player caster) {
        double multiplier = healingMultiplier.multiplier(caster);
        return Double.isFinite(multiplier) ? Math.max(0D, multiplier) : 1D;
    }

    @Override
    public AbilityMechanicModifiers mechanicModifiers(Player caster) {
        AbilityMechanicModifiers modifiers = mechanicModifierProvider.modifiers(caster);
        return modifiers == null ? AbilityMechanicModifiers.NEUTRAL : modifiers;
    }

    @Override
    public void signal(Player player, AbilityRuntimeSignal signal, int durationTicks) {
        runtimeSignalSink.signal(player, signal, Math.max(1, durationTicks));
    }

    @Override
    public void grantResource(Player caster, double amount) {
        resourceSink.grant(caster, amount);
    }

    @Override
    public List<String> activeFormIds(Player player) {
        return List.copyOf(activeLineageProvider.formIds(player));
    }

    @Override
    public void clearSource(Player caster) {
        sourceCleanup.clear(caster);
    }

    @FunctionalInterface
    public interface ThreatSink {
        void apply(ThreatRequest request);
    }

    @FunctionalInterface
    public interface ContributionSink {
        void record(Player caster, String abilityId, AbilityContribution contribution);
    }

    @FunctionalInterface
    public interface HealingMultiplier {
        double multiplier(Player caster);
    }

    @FunctionalInterface
    public interface ResourceSink {
        void grant(Player caster, double amount);
    }

    @FunctionalInterface
    public interface ActiveLineageProvider {
        List<String> formIds(Player player);
    }

    @FunctionalInterface
    public interface SourceCleanup {
        void clear(Player player);
    }

    @FunctionalInterface
    public interface MechanicModifierProvider {
        AbilityMechanicModifiers modifiers(Player player);
    }

    @FunctionalInterface
    public interface RuntimeSignalSink {
        void signal(Player player, AbilityRuntimeSignal signal, int durationTicks);
    }

    @FunctionalInterface
    public interface ObservationSink {
        void record(AbilityRuntimeObservation observation);
    }
}
