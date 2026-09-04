package com.magmaguy.elitemobs.experimentalcombat.abilities;

import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.experimentalcombat.ClassAbilityEligibility;
import com.magmaguy.elitemobs.experimentalcombat.ExperimentalCombatEnemyAuthorization;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.EnumSet;
import java.util.Set;

/** Canonical legality and protection policy for hostile Experimental Combat effects. */
final class EliteAbilityTargetAuthorization {
    private static final Set<AbilityEffect> DISPLACEMENT_EFFECTS = EnumSet.of(
            AbilityEffect.KNOCKBACK,
            AbilityEffect.PULL,
            AbilityEffect.LAUNCH,
            AbilityEffect.FEAR);

    private EliteAbilityTargetAuthorization() {
    }

    static boolean canTarget(Player caster, LivingEntity candidate) {
        if (caster == null || !ClassAbilityEligibility.isEligible(caster)) return false;
        return ExperimentalCombatEnemyAuthorization.canTarget(caster, candidate);
    }

    static boolean canTarget(Player caster, LivingEntity candidate, FixedAbilitySpec spec) {
        if (!canTarget(caster, candidate)) return false;
        EliteEntity elite = EntityTracker.getEliteMobEntity(candidate);
        if (elite == null) return false;
        AttributeInstance maximumHealth = candidate.getAttribute(Attribute.MAX_HEALTH);
        double maximum = maximumHealth == null
                ? Math.max(1D, candidate.getHealth())
                : Math.max(1D, maximumHealth.getValue());
        double width = Math.max(
                candidate.getBoundingBox().getWidthX(),
                candidate.getBoundingBox().getWidthZ());
        AbilityEnemyPolicy.EnemyFacts facts = new AbilityEnemyPolicy.EnemyFacts(
                candidate.getHealth() / maximum,
                !elite.isNaturalEntity() || elite.getHealthMultiplier() > 1.5D,
                candidate.getHeight() >= 2.5D || width >= 1.4D);
        return AbilityEnemyPolicy.permits(spec.executionTraits().mechanics(), facts);
    }

    static boolean canApply(
            Player caster,
            LivingEntity candidate,
            FixedAbilitySpec spec,
            AbilityEffect effect) {
        if (!canTarget(caster, candidate, spec)) return false;
        EliteEntity elite = EntityTracker.getEliteMobEntity(candidate);
        if (elite == null) return false;
        if (DISPLACEMENT_EFFECTS.contains(effect) && displacementImmune(elite, candidate))
            return false;

        ClassAbilityAffectEliteEvent event = new ClassAbilityAffectEliteEvent(
                caster, elite, spec.id(), effect);
        Bukkit.getPluginManager().callEvent(event);
        return !event.isCancelled();
    }

    private static boolean displacementImmune(EliteEntity elite, LivingEntity candidate) {
        if (elite instanceof CustomBossEntity customBoss
                && customBoss.getCustomBossesConfigFields().isFrozen())
            return true;
        AttributeInstance resistance = candidate.getAttribute(Attribute.KNOCKBACK_RESISTANCE);
        return resistance != null && resistance.getValue() >= 1D;
    }
}
