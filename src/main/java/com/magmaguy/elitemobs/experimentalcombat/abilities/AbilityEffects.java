package com.magmaguy.elitemobs.experimentalcombat.abilities;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.combatsystem.CombatDamageContext;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.experimentalcombat.ClassAbilityEligibility;
import com.magmaguy.elitemobs.experimentalcombat.damage.ExperimentalDamageScaling;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

final class AbilityEffects {
    private static final double DEFAULT_PROTECTION_MULTIPLIER = 0.80D;
    private static final double DEFAULT_STRENGTH_MULTIPLIER = 1.15D;

    private final ClassAbilityDamage damage;
    private final AbilitySemantics semantics;
    private final TimedCombatModifiers modifiers;
    private final AbilityStateRuntime states;
    private final EliteCrowdControlRuntime crowdControl;

    AbilityEffects(
            AbilitySemantics semantics,
            TimedCombatModifiers modifiers,
            AbilityStateRuntime states, ClassAbilityDamage damage) {
        this.damage = damage;
        this.semantics = semantics;
        this.modifiers = modifiers;
        this.states = states;
        this.crowdControl = new EliteCrowdControlRuntime(MetadataHandler.PLUGIN);
    }

    AbilityContribution apply(Player caster, FixedAbilitySpec spec,
                              AbilityTargeting.TargetSelection selection, int effectiveLevel) {
        CombatDamageContext.ClassAbilityDelivery delivery = spec.family() == AbilityFamily.PROJECTILE
                ? CombatDamageContext.ClassAbilityDelivery.PROJECTILE
                : CombatDamageContext.ClassAbilityDelivery.DIRECT;
        return apply(caster, null, spec, selection, effectiveLevel,
                AbilityHealingType.BURST, delivery);
    }

    boolean controlledBy(Player caster, LivingEntity target) {
        return crowdControl.controlledBy(caster, target);
    }

    /** Applies the non-damage portion of a servant's canonical signature at attack impact. */
    void applyMinionImpact(
            Player caster,
            FixedAbilitySpec spec,
            LivingEntity target,
            int effectiveLevel) {
        if (!valid(target) || !semantics.canTargetEnemy(caster, target, spec)) return;
        AbilityMechanicModifiers mechanicModifiers = semantics.mechanicModifiers(caster);
        int duration = Math.max(20, Math.min(100, scaledDuration(
                ActiveAbilityLevelScaling.durationTicks(
                        spec.tuning().durationTicks(), effectiveLevel),
                mechanicModifiers.controlDurationMultiplier())));
        double potency = mechanicModifiers.controlPotencyMultiplier();
        if (spec.effects().contains(AbilityEffect.SLOW)
                && authorized(caster, target, spec, AbilityEffect.SLOW)) {
            target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, duration,
                    scaledAmplifier(1, potency), false, true, true), true);
        }
        if (spec.effects().contains(AbilityEffect.WEAKEN)
                && authorized(caster, target, spec, AbilityEffect.WEAKEN)) {
            double multiplier = modifiers.weakenOutgoing(
                    caster, target, spec.tuning().weakenMultiplier(),
                    effectiveLevel, potency, duration, spec.id());
            observeApplied(caster, target, spec,
                    AbilityRuntimeObservation.Kind.MODIFIER_APPLIED,
                    AbilityEffect.WEAKEN, multiplier, duration);
        }
        EliteEntity elite = EntityTracker.getEliteMobEntity(target);
        if (elite == null) return;
        if (spec.effects().contains(AbilityEffect.INTERRUPT)
                && authorized(caster, target, spec, AbilityEffect.INTERRUPT)) {
            crowdControl.interrupt(elite, Math.min(40, duration));
        }
        if (spec.effects().contains(AbilityEffect.FEAR)
                && authorized(caster, target, spec, AbilityEffect.FEAR)) {
            crowdControl.fear(caster, elite, Math.min(60, duration));
        }
    }

    AbilityContribution applyPeriodic(Player caster, FixedAbilitySpec spec,
                                      AbilityTargeting.TargetSelection selection, int effectiveLevel) {
        return apply(caster, null, spec, selection, effectiveLevel,
                AbilityHealingType.PERIODIC, CombatDamageContext.ClassAbilityDelivery.PERIODIC);
    }

    AbilityContribution applyProjectileEffect(
            Player caster,
            FixedAbilitySpec spec,
            AbilityTargeting.TargetSelection selection,
            int effectiveLevel) {
        return apply(caster, null, spec, selection, effectiveLevel,
                AbilityHealingType.BURST, CombatDamageContext.ClassAbilityDelivery.PROJECTILE);
    }

    AbilityContribution applyProjectile(
            Player caster,
            Projectile projectile,
            FixedAbilitySpec spec,
            AbilityTargeting.TargetSelection selection,
            int effectiveLevel) {
        return apply(caster, Objects.requireNonNull(projectile, "projectile"),
                spec, selection, effectiveLevel, AbilityHealingType.BURST,
                CombatDamageContext.ClassAbilityDelivery.PROJECTILE);
    }

    private AbilityContribution apply(
            Player caster,
            Projectile projectile,
            FixedAbilitySpec spec,
            AbilityTargeting.TargetSelection selection,
            int effectiveLevel,
            AbilityHealingType healingType,
            CombatDamageContext.ClassAbilityDelivery delivery) {
        selection = new AbilityTargeting.TargetSelection(
                selection.enemies(),
                uniqueValidAllies(caster, selection.allies()),
                selection.origin());
        selection = states.qualifyTargets(caster, spec, selection);
        Set<AbilityEffect> effects = spec.effects();
        AbilityTuning tuning = spec.tuning();
        AbilityMechanicModifiers mechanicModifiers = semantics.mechanicModifiers(caster);
        int levelDuration = ActiveAbilityLevelScaling.durationTicks(
                tuning.durationTicks(), effectiveLevel);
        int controlDuration = scaledDuration(
                levelDuration, mechanicModifiers.controlDurationMultiplier());
        double controlPotency = mechanicModifiers.controlPotencyMultiplier();
        EliteControlEffectPlan slowPlan = EliteControlEffectPlan.slow(
                2, levelDuration, mechanicModifiers);
        double displacement = ActiveAbilityLevelScaling.displacement(
                tuning.displacement(), effectiveLevel);
        boolean missingHealthScaling = spec.executionTraits().mechanics()
                .contains(AbilityMechanic.MISSING_HEALTH_SCALING);
        double classRankScale = AbilityLevelScaling.multiplier(effectiveLevel);
        double totalDamage = 0D;
        double totalHealing = 0D;
        int controlledEnemies = 0;
        int supportedAllies = 0;

        List<LivingEntity> enemies = new ArrayList<>(selection.enemies());
        if (effects.contains(AbilityEffect.TAUNT)
                && enemies.isEmpty()
                && !AbilityEnemyPolicy.requiresQualifiedSelection(
                spec.executionTraits().mechanics()))
            enemies.addAll(nearbyEnemies(caster, spec, Math.max(8D, tuning.radius())));
        if (spec.executionTraits().mechanics().contains(AbilityMechanic.SINGLE_ENEMY))
            enemies = enemies.stream().limit(1).toList();
        int qualifiedEnemyCount = enemies.size();
        double healingMultiplier = semantics.healingMultiplier(caster)
                * AbilityHealingPolicy.multiplier(
                mechanicModifiers, healingType, qualifiedEnemyCount);

        states.activate(caster, spec, selection, effectiveLevel);
        double availableRetaliation = spec.executionTraits().mechanics()
                .contains(AbilityMechanic.RETALIATION_RELEASE)
                ? states.retaliationAvailable(caster, spec)
                : 0D;
        boolean retaliationAvailable = availableRetaliation > 0D;
        double releasedRetaliation = 0D;
        List<LivingEntity> tauntedEnemies = new ArrayList<>();
        Set<LivingEntity> affectedEnemies = new LinkedHashSet<>();

        for (LivingEntity enemy : enemies) {
            if (!valid(enemy) || !semantics.canTargetEnemy(caster, enemy, spec)) continue;
            EnumSet<AbilityEffect> appliedControls = EnumSet.noneOf(AbilityEffect.class);

            if (effects.contains(AbilityEffect.DAMAGE)
                    && tuning.damageMultiplier() > 0D
                    && authorized(caster, enemy, spec, AbilityEffect.DAMAGE)) {
                EliteEntity elite = EntityTracker.getEliteMobEntity(enemy);
                double amount = ExperimentalDamageScaling.classAbility(
                        elite,
                        effectiveLevel,
                        tuning.damageMultiplier() * states.damageMultiplier(spec, enemy));
                double retaliationBonus = retaliationAvailable ? availableRetaliation : 0D;
                amount += retaliationBonus;
                double dealt = damage.apply(caster, projectile, enemy, amount,
                        damageDomain(spec, delivery));
                if (dealt > 0D && retaliationBonus > 0D) {
                    releasedRetaliation = states.consumeRetaliation(caster, spec, enemy);
                    retaliationAvailable = false;
                }
                totalDamage += dealt;
                if (dealt > 0D) {
                    affectedEnemies.add(enemy);
                    semantics.observe(AbilityRuntimeObservation.effect(
                            AbilityRuntimeObservation.Kind.DAMAGE,
                            caster.getUniqueId(), enemy.getUniqueId(), spec.id(),
                            dealt, 0, AbilityEffect.DAMAGE));
                }
            }

            if (effects.contains(AbilityEffect.KNOCKBACK)
                    && authorized(caster, enemy, spec, AbilityEffect.KNOCKBACK)) {
                displaceAway(caster, enemy, displacement * controlPotency, .25D);
                appliedControls.add(AbilityEffect.KNOCKBACK);
            }
            if (effects.contains(AbilityEffect.PULL)
                    && authorized(caster, enemy, spec, AbilityEffect.PULL)) {
                displaceToward(caster, enemy, displacement * controlPotency);
                appliedControls.add(AbilityEffect.PULL);
            }
            if (effects.contains(AbilityEffect.LAUNCH)
                    && authorized(caster, enemy, spec, AbilityEffect.LAUNCH)) {
                displaceAway(caster, enemy, displacement * .45D * controlPotency,
                        Math.max(.55D, displacement * controlPotency));
                appliedControls.add(AbilityEffect.LAUNCH);
            }
            if (effects.contains(AbilityEffect.SLOW)
                    && authorized(caster, enemy, spec, AbilityEffect.SLOW)) {
                PotionEffect existing = enemy.getPotionEffect(PotionEffectType.SLOWNESS);
                boolean extend = spec.executionTraits().mechanics()
                        .contains(AbilityMechanic.EXTEND_CONTROL_DURATION);
                int existingDuration = existing == null ? 0 : existing.getDuration();
                int duration = extend
                        ? Math.min(600, existingDuration + Math.max(20, slowPlan.durationTicks()))
                        : Math.max(Math.max(20, slowPlan.durationTicks()), existingDuration);
                int amplifier = Math.max(
                        slowPlan.potionAmplifier(),
                        existing == null ? 0 : existing.getAmplifier());
                enemy.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS,
                        duration, amplifier, false, true, true), true);
                EliteEntity elite = EntityTracker.getEliteMobEntity(enemy);
                if (elite != null && amplifier == slowPlan.potionAmplifier())
                    crowdControl.applySlowPotency(caster, elite, slowPlan);
                appliedControls.add(AbilityEffect.SLOW);
                if (extend) {
                    if (crowdControl.extendRoot(caster, elite, Math.max(20, controlDuration)))
                        observeMechanicTriggered(caster, enemy, spec,
                                AbilityMechanic.EXTEND_CONTROL_DURATION,
                                AbilityEffect.SLOW, 1D, Math.max(20, controlDuration));
                }
            }
            if (effects.contains(AbilityEffect.ROOT)
                    && authorized(caster, enemy, spec, AbilityEffect.ROOT)) {
                EliteEntity elite = EntityTracker.getEliteMobEntity(enemy);
                if (crowdControl.root(caster, elite, Math.max(20, controlDuration)))
                    appliedControls.add(AbilityEffect.ROOT);
            }
            if (effects.contains(AbilityEffect.WEAKEN)
                    && authorized(caster, enemy, spec, AbilityEffect.WEAKEN)) {
                int duration = Math.max(20, controlDuration);
                double multiplier = modifiers.weakenOutgoing(
                        caster, enemy, tuning.weakenMultiplier(), effectiveLevel,
                        controlPotency, duration, spec.id());
                observeApplied(caster, enemy, spec,
                        AbilityRuntimeObservation.Kind.MODIFIER_APPLIED,
                        AbilityEffect.WEAKEN, multiplier, duration);
                appliedControls.add(AbilityEffect.WEAKEN);
            }
            if (effects.contains(AbilityEffect.INTERRUPT)
                    && authorized(caster, enemy, spec, AbilityEffect.INTERRUPT)) {
                EliteEntity elite = EntityTracker.getEliteMobEntity(enemy);
                if (crowdControl.interrupt(
                        elite, Math.max(10, Math.min(60, controlDuration))))
                    appliedControls.add(AbilityEffect.INTERRUPT);
            }
            if (effects.contains(AbilityEffect.FEAR)
                    && authorized(caster, enemy, spec, AbilityEffect.FEAR)) {
                EliteEntity elite = EntityTracker.getEliteMobEntity(enemy);
                if (crowdControl.fear(caster, elite, Math.max(30, controlDuration)))
                    appliedControls.add(AbilityEffect.FEAR);
            }
            if (effects.contains(AbilityEffect.GLOW)
                    && authorized(caster, enemy, spec, AbilityEffect.GLOW)) {
                enemy.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING,
                        Math.max(20, levelDuration), 0, false, true, true));
                appliedControls.add(AbilityEffect.GLOW);
            }
            if (effects.contains(AbilityEffect.BURN)
                    && authorized(caster, enemy, spec, AbilityEffect.BURN)) {
                enemy.setFireTicks(Math.max(enemy.getFireTicks(),
                        Math.max(20, Math.min(200, levelDuration))));
                appliedControls.add(AbilityEffect.BURN);
            }
            if (effects.contains(AbilityEffect.PARTY_DAMAGE_MARK)
                    && authorized(caster, enemy, spec, AbilityEffect.PARTY_DAMAGE_MARK)) {
                double markMultiplier = ActiveAbilityLevelScaling.modifier(
                        tuning.modifierMultiplier(), effectiveLevel);
                if (markMultiplier > 1D)
                    modifiers.markEnemy(caster, enemy, beneficiaryIds(caster), markMultiplier,
                            levelDuration, spec.id(), spec.executionTraits().mechanics()
                                    .contains(AbilityMechanic.HEALTH_SCALED_MARK));
                states.armDetonation(caster, spec, enemy, effectiveLevel);
                affectedEnemies.add(enemy);
                observeApplied(caster, enemy, spec,
                        AbilityRuntimeObservation.Kind.MODIFIER_APPLIED,
                        AbilityEffect.PARTY_DAMAGE_MARK,
                        markMultiplier, levelDuration);
            }
            if (effects.contains(AbilityEffect.TAUNT)
                    && authorized(caster, enemy, spec, AbilityEffect.TAUNT)) {
                tauntedEnemies.add(enemy);
                affectedEnemies.add(enemy);
            }
            if (!appliedControls.isEmpty()) {
                controlledEnemies++;
                affectedEnemies.add(enemy);
                AbilityControlEvidence.applied(
                                caster.getUniqueId(), enemy.getUniqueId(), spec,
                                appliedControls, controlPotency, controlDuration)
                        .forEach(semantics::observe);
            }
        }

        List<Player> selectedAllies = new ArrayList<>(selection.allies());
        if (spec.executionTraits().mechanics().contains(AbilityMechanic.CASTER_ONLY_FIELD))
            selectedAllies.removeIf(ally -> !ally.getUniqueId().equals(caster.getUniqueId()));
        if (spec.executionTraits().mechanics().contains(AbilityMechanic.CASTER_ONLY_SUPPORT))
            selectedAllies.removeIf(ally -> !ally.getUniqueId().equals(caster.getUniqueId()));
        if (spec.executionTraits().mechanics().contains(AbilityMechanic.PARTY_BUFF)) {
            double radius = Math.max(8D, tuning.radius());
            for (Player ally : semantics.alliesOf(caster))
                if (ally != null && ally.isOnline() && ally.getWorld().equals(caster.getWorld())
                        && ally.getLocation().distanceSquared(selection.origin()) <= radius * radius)
                    selectedAllies.add(ally);
        }
        List<Player> allies = uniqueValidAllies(caster, selectedAllies);
        if (spec.executionTraits().mechanics().contains(AbilityMechanic.LOWEST_HEALTH_FIRST))
            allies = allies.stream()
                    .sorted(java.util.Comparator.comparingDouble(AbilityEffects::healthFraction))
                    .limit(tuning.projectileCount())
                    .toList();
        if (allies.isEmpty() && hasCasterBuff(effects)
                && spec.target() != AbilityTarget.AIMED_LOCATION) allies = List.of(caster);
        int authoredEffectDuration = effects.contains(AbilityEffect.HEAL)
                ? ActiveAbilityLevelScaling.durationTicks(
                AbilityHealingPolicy.durationTicks(
                        tuning.durationTicks(), healingType, mechanicModifiers), effectiveLevel)
                : levelDuration;
        int effectDuration = states.linkedDurationTicks(caster, spec, authoredEffectDuration);
        for (Player ally : allies) {
            boolean supported = false;
            if (effects.contains(AbilityEffect.HEAL) && tuning.healingFraction() > 0D
                    && !states.suppressesImmediate(spec, AbilityEffect.HEAL)) {
                double fraction = tuning.healingFraction();
                if (spec.executionTraits().mechanics().contains(AbilityMechanic.GROUP_SCALING))
                    fraction *= 1D + Math.min(.75D, Math.max(0, allies.size() - 1) * .15D);
                if (ally.getUniqueId().equals(caster.getUniqueId()))
                    fraction *= spec.executionTraits().selfHealingMultiplier();
                double healed = heal(ally, maximumHealth(ally) * fraction * classRankScale
                        * healingMultiplier);
                totalHealing += healed;
                supported |= healed > 0D;
                if (healed > 0D) {
                    semantics.observe(AbilityRuntimeObservation.effect(
                            AbilityRuntimeObservation.Kind.HEAL,
                            caster.getUniqueId(), ally.getUniqueId(), spec.id(),
                            healed, effectDuration, AbilityEffect.HEAL));
                }
                states.scheduleHealEcho(caster, ally, healed, spec);
            }
            if (effects.contains(AbilityEffect.SHIELD) && tuning.shieldFraction() > 0D
                    && !states.suppressesImmediate(spec, AbilityEffect.SHIELD)) {
                double shieldAmount = maximumHealth(ally) * tuning.shieldFraction()
                        * classRankScale * mechanicModifiers.shieldStrengthMultiplier();
                applyAbsorption(ally, shieldAmount, effectDuration);
                semantics.observe(AbilityRuntimeObservation.effect(
                        AbilityRuntimeObservation.Kind.SHIELD,
                        caster.getUniqueId(), ally.getUniqueId(), spec.id(),
                        shieldAmount, effectDuration, AbilityEffect.SHIELD));
                supported = true;
            }
            if (effects.contains(AbilityEffect.CLEANSE)) {
                cleanse(ally);
                observeApplied(caster, ally, spec,
                        AbilityRuntimeObservation.Kind.STATUS_APPLIED,
                        AbilityEffect.CLEANSE, 1D, effectDuration);
                supported = true;
            }
            if (effects.contains(AbilityEffect.SPEED) && !missingHealthScaling) {
                ally.addPotionEffect(new PotionEffect(PotionEffectType.SPEED,
                        Math.max(20, effectDuration), 1, false, true, true));
                observeApplied(caster, ally, spec,
                        AbilityRuntimeObservation.Kind.MODIFIER_APPLIED,
                        AbilityEffect.SPEED, 2D, effectDuration);
                supported = true;
            }
            if (effects.contains(AbilityEffect.STRENGTH) && !missingHealthScaling) {
                double multiplier = tuning.modifierMultiplier() > 1D
                        ? tuning.modifierMultiplier()
                        : DEFAULT_STRENGTH_MULTIPLIER;
                multiplier = ActiveAbilityLevelScaling.modifier(multiplier, effectiveLevel);
                modifiers.modifyOutgoing(caster, ally, multiplier, effectDuration, spec.id());
                observeApplied(caster, ally, spec,
                        AbilityRuntimeObservation.Kind.MODIFIER_APPLIED,
                        AbilityEffect.STRENGTH, multiplier, effectDuration);
                supported = true;
            }
            if (effects.contains(AbilityEffect.SPELL_STRENGTH)) {
                double multiplier = tuning.modifierMultiplier() > 1D
                        ? tuning.modifierMultiplier()
                        : DEFAULT_STRENGTH_MULTIPLIER;
                multiplier = ActiveAbilityLevelScaling.modifier(multiplier, effectiveLevel);
                modifiers.modifySpellOutgoing(
                        caster, ally, multiplier, effectDuration, spec.id());
                observeApplied(caster, ally, spec,
                        AbilityRuntimeObservation.Kind.MODIFIER_APPLIED,
                        AbilityEffect.SPELL_STRENGTH, multiplier, effectDuration);
                supported = true;
            }
            if (effects.contains(AbilityEffect.ALLY_PROTECT)
                    && !states.suppressesImmediate(spec, AbilityEffect.ALLY_PROTECT)) {
                double protection = tuning.modifierMultiplier() > 0D && tuning.modifierMultiplier() < 1D
                        ? tuning.modifierMultiplier()
                        : DEFAULT_PROTECTION_MULTIPLIER;
                protection = ActiveAbilityLevelScaling.modifier(protection, effectiveLevel);
                modifiers.modifyIncoming(caster, ally, protection, effectDuration, spec.id());
                observeApplied(caster, ally, spec,
                        AbilityRuntimeObservation.Kind.MODIFIER_APPLIED,
                        AbilityEffect.ALLY_PROTECT, protection, effectDuration);
                supported = true;
            }
            if (supported) supportedAllies++;
        }

        if (effects.contains(AbilityEffect.SELF_PROTECT)) {
            double protection = ActiveAbilityLevelScaling.modifier(
                    DEFAULT_PROTECTION_MULTIPLIER, effectiveLevel);
            modifiers.modifyIncoming(caster, caster, protection,
                    effectDuration, spec.id());
            observeApplied(caster, caster, spec,
                    AbilityRuntimeObservation.Kind.MODIFIER_APPLIED,
                    AbilityEffect.SELF_PROTECT,
                    protection, effectDuration);
            if (allies.stream().noneMatch(ally -> ally.getUniqueId().equals(caster.getUniqueId()))) supportedAllies++;
        }
        if (effects.contains(AbilityEffect.SELF_VULNERABLE)) {
            double vulnerability = tuning.modifierMultiplier() > 1D
                    ? tuning.modifierMultiplier()
                    : 1.15D;
            vulnerability = ActiveAbilityLevelScaling.modifier(vulnerability, effectiveLevel);
            modifiers.modifyIncomingPenalty(
                    caster, caster, vulnerability, effectDuration, spec.id());
            observeApplied(caster, caster, spec,
                    AbilityRuntimeObservation.Kind.MODIFIER_APPLIED,
                    AbilityEffect.SELF_VULNERABLE, vulnerability, effectDuration);
            supportedAllies++;
        }

        if (effects.contains(AbilityEffect.LIFESTEAL) && totalDamage > 0D) {
            double healed = heal(caster, totalDamage * .22D * healingMultiplier);
            totalHealing += healed;
            if (healed > 0D)
                semantics.observe(AbilityRuntimeObservation.effect(
                        AbilityRuntimeObservation.Kind.HEAL,
                        caster.getUniqueId(), caster.getUniqueId(), spec.id(),
                        healed, 0, AbilityEffect.LIFESTEAL));
        }

        if (releasedRetaliation > 0D
                && spec.executionTraits().mechanics().contains(AbilityMechanic.RETALIATION_HEAL)
                && !allies.isEmpty()) {
            double budget = AbilityStateMath.retaliationHealing(releasedRetaliation, .45D)
                    * healingMultiplier;
            double share = budget / allies.size();
            for (Player ally : allies) {
                double healed = heal(ally, share);
                totalHealing += healed;
                if (healed > 0D)
                    semantics.observe(AbilityRuntimeObservation.effect(
                            AbilityRuntimeObservation.Kind.HEAL,
                            caster.getUniqueId(), ally.getUniqueId(), spec.id(),
                            healed, 0, AbilityEffect.HEAL));
            }
        }

        double threat = 0D;
        if (!tauntedEnemies.isEmpty()) {
            double amountPerEnemy = Math.max(attackDamage(caster) * 10D, 25D);
            int forcedTicks = Math.max(20, controlDuration);
            boolean extendsTaunt = spec.executionTraits().mechanics()
                    .contains(AbilityMechanic.EXTEND_TAUNT);
            semantics.requestThreat(new AbilitySemantics.ThreatRequest(
                    caster, tauntedEnemies, amountPerEnemy, forcedTicks, spec.id()));
            Set<UUID> extendedTargets = states.registerTaunts(
                    caster, tauntedEnemies, forcedTicks, extendsTaunt);
            for (LivingEntity taunted : tauntedEnemies) {
                semantics.observe(AbilityRuntimeObservation.effect(
                        AbilityRuntimeObservation.Kind.TAUNT,
                        caster.getUniqueId(), taunted.getUniqueId(), spec.id(),
                        amountPerEnemy, forcedTicks, AbilityEffect.TAUNT));
                if (extendedTargets.contains(taunted.getUniqueId())) {
                    observeMechanicTriggered(caster, taunted, spec,
                            AbilityMechanic.EXTEND_TAUNT, AbilityEffect.TAUNT,
                            amountPerEnemy, forcedTicks);
                }
            }
            threat = amountPerEnemy * tauntedEnemies.size();
        }

        if (spec.executionTraits().mechanics().contains(AbilityMechanic.OPENS_DEFENSE_BREAK))
            states.registerDefenseBreaks(caster, affectedEnemies, controlDuration);

        if (!affectedEnemies.isEmpty())
            caster.getWorld().spawnParticle(Particle.CRIT, selection.origin(),
                    Math.min(40, 4 + affectedEnemies.size() * 3), 0.8, 0.5, 0.8, .05);
        if (!allies.isEmpty())
            caster.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, selection.origin(),
                    Math.min(30, 3 + allies.size() * 2), .7, .5, .7, .02);

        return new AbilityContribution(totalDamage, totalHealing, 0, threat,
                controlledEnemies, supportedAllies, 0);
    }

    void close() {
        crowdControl.close();
    }

    private List<LivingEntity> nearbyEnemies(Player caster, FixedAbilitySpec spec, double radius) {
        List<LivingEntity> enemies = new ArrayList<>();
        for (Entity entity : caster.getWorld().getNearbyEntities(caster.getLocation(), radius, radius, radius))
            if (entity instanceof LivingEntity living
                    && semantics.canTargetEnemy(caster, living, spec)) enemies.add(living);
        return enemies;
    }

    private boolean authorized(
            Player caster,
            LivingEntity enemy,
            FixedAbilitySpec spec,
            AbilityEffect effect) {
        return semantics.canApplyEnemyEffect(caster, enemy, spec, effect);
    }

    private Set<UUID> beneficiaryIds(Player caster) {
        Set<UUID> ids = new LinkedHashSet<>();
        ids.add(caster.getUniqueId());
        for (Player ally : semantics.alliesOf(caster))
            if (ally != null && ally.isOnline()) ids.add(ally.getUniqueId());
        return Set.copyOf(ids);
    }

    private List<Player> uniqueValidAllies(Player caster, Collection<Player> input) {
        Set<UUID> currentParty = new LinkedHashSet<>();
        currentParty.add(caster.getUniqueId());
        for (Player ally : semantics.alliesOf(caster))
            if (ally != null) currentParty.add(ally.getUniqueId());
        Map<UUID, Player> allies = new LinkedHashMap<>();
        for (Player player : input)
            if (player != null
                    && currentParty.contains(player.getUniqueId())
                    && player.isOnline()
                    && player.isValid()
                    && !player.isDead()
                    && ClassAbilityEligibility.isEligible(player)
                    && player.getWorld().equals(caster.getWorld()))
                allies.put(player.getUniqueId(), player);
        return List.copyOf(allies.values());
    }

    private static boolean hasCasterBuff(Set<AbilityEffect> effects) {
        return effects.contains(AbilityEffect.HEAL)
                || effects.contains(AbilityEffect.SHIELD)
                || effects.contains(AbilityEffect.CLEANSE)
                || effects.contains(AbilityEffect.SPEED)
                || effects.contains(AbilityEffect.STRENGTH)
                || effects.contains(AbilityEffect.SELF_PROTECT);
    }

    private static double heal(Player target, double amount) {
        if (!target.isOnline() || target.isDead() || amount <= 0D || !Double.isFinite(amount)) return 0D;
        double maximum = maximumHealth(target);
        double before = target.getHealth();
        double missing = Math.max(0D, maximum - before);
        if (missing <= 0D) return 0D;
        EntityRegainHealthEvent event = new EntityRegainHealthEvent(
                target,
                Math.min(missing, amount),
                EntityRegainHealthEvent.RegainReason.CUSTOM);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled() || event.getAmount() <= 0D || !Double.isFinite(event.getAmount())) return 0D;
        target.setHealth(Math.min(maximum, before + event.getAmount()));
        return Math.max(0D, target.getHealth() - before);
    }

    private static void applyAbsorption(Player target, double amount, int durationTicks) {
        if (amount <= 0D || !Double.isFinite(amount)) return;
        int amplifier = Math.max(0, Math.min(255, (int) Math.ceil(amount / 4D) - 1));
        target.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION,
                Math.max(20, durationTicks), amplifier, false, true, true), true);
    }

    private void observeApplied(
            Player caster,
            LivingEntity target,
            FixedAbilitySpec spec,
            AbilityRuntimeObservation.Kind kind,
            AbilityEffect effect,
            double amount,
            int durationTicks) {
        semantics.observe(AbilityRuntimeObservation.effect(
                kind, caster.getUniqueId(), target.getUniqueId(), spec.id(),
                amount, Math.max(0, durationTicks), effect));
    }

    private void observeMechanicTriggered(
            Player caster,
            LivingEntity target,
            FixedAbilitySpec spec,
            AbilityMechanic mechanic,
            AbilityEffect effect,
            double amount,
            int durationTicks) {
        semantics.observe(AbilityControlEvidence.mechanicTriggered(
                caster.getUniqueId(), target.getUniqueId(), spec,
                mechanic, effect, amount, durationTicks));
    }

    private static void cleanse(Player target) {
        for (PotionEffectType type : List.of(
                PotionEffectType.SLOWNESS,
                PotionEffectType.WEAKNESS,
                PotionEffectType.POISON,
                PotionEffectType.WITHER,
                PotionEffectType.BLINDNESS,
                PotionEffectType.NAUSEA,
                PotionEffectType.MINING_FATIGUE,
                PotionEffectType.LEVITATION,
                PotionEffectType.DARKNESS,
                PotionEffectType.HUNGER,
                PotionEffectType.UNLUCK))
            target.removePotionEffect(type);
    }

    private static void displaceAway(Player caster, LivingEntity target, double horizontal, double vertical) {
        Vector direction = target.getLocation().toVector().subtract(caster.getLocation().toVector());
        if (direction.lengthSquared() < 1.0E-6) direction = caster.getLocation().getDirection();
        direction.setY(0).normalize().multiply(Math.max(.1D, horizontal)).setY(vertical);
        target.setVelocity(target.getVelocity().add(direction));
    }

    private static void displaceToward(Player caster, LivingEntity target, double strength) {
        Vector direction = caster.getLocation().toVector().subtract(target.getLocation().toVector());
        if (direction.lengthSquared() < 1.0E-6) return;
        direction.normalize().multiply(Math.max(.1D, strength)).setY(.15D);
        target.setVelocity(target.getVelocity().add(direction));
    }

    private static int scaledDuration(int authoredTicks, double multiplier) {
        if (authoredTicks <= 0) return 0;
        return (int) Math.min(Integer.MAX_VALUE,
                Math.max(1L, Math.round(authoredTicks * multiplier)));
    }

    private static int scaledAmplifier(int authoredAmplifier, double potencyMultiplier) {
        return Math.max(0, Math.min(255,
                (int) Math.round((authoredAmplifier + 1D) * potencyMultiplier) - 1));
    }

    private static CombatDamageContext.ClassAbilityDamageDomain damageDomain(
            FixedAbilitySpec spec,
            CombatDamageContext.ClassAbilityDelivery delivery) {
        Set<AbilityMechanic> mechanics = spec.executionTraits().mechanics();
        boolean area = spec.family() == AbilityFamily.ZONE
                || spec.target() == AbilityTarget.NEARBY_ENEMIES
                || spec.target() == AbilityTarget.MIXED_NEARBY
                || spec.target() == AbilityTarget.AIMED_LOCATION
                || spec.target() == AbilityTarget.FORWARD_ENEMIES
                || mechanics.contains(AbilityMechanic.PIERCING_CAST)
                || mechanics.contains(AbilityMechanic.CHAINING_CAST)
                || mechanics.contains(AbilityMechanic.CLUSTER_PROJECTILE)
                || mechanics.contains(AbilityMechanic.PROJECTILE_BOMBARDMENT)
                || mechanics.contains(AbilityMechanic.EXPANDING_PULSES);
        CombatDamageContext.ClassAbilityArchetype archetype =
                mechanics.contains(AbilityMechanic.DELAYED_PAYLOAD)
                        ? CombatDamageContext.ClassAbilityArchetype.TRAP
                        : mechanics.contains(AbilityMechanic.CLUSTER_PROJECTILE)
                        || mechanics.contains(AbilityMechanic.DETONATING_MARK)
                        ? CombatDamageContext.ClassAbilityArchetype.BLAST
                        : CombatDamageContext.ClassAbilityArchetype.OTHER;
        return new CombatDamageContext.ClassAbilityDamageDomain(
                area
                        ? CombatDamageContext.ClassAbilityTargetShape.AREA
                        : CombatDamageContext.ClassAbilityTargetShape.SINGLE_TARGET,
                delivery,
                archetype,
                mechanics.contains(AbilityMechanic.GUARANTEED_CRITICAL)
                        ? CombatDamageContext.ClassAbilityStrikeQuality.GUARANTEED_CRITICAL
                        : CombatDamageContext.ClassAbilityStrikeQuality.NORMAL);
    }

    private static double attackDamage(Player player) {
        AttributeInstance attribute = player.getAttribute(Attribute.ATTACK_DAMAGE);
        return attribute == null ? 1D : Math.max(1D, attribute.getValue());
    }

    private static double maximumHealth(LivingEntity entity) {
        AttributeInstance attribute = entity.getAttribute(Attribute.MAX_HEALTH);
        return attribute == null ? Math.max(1D, entity.getHealth()) : Math.max(1D, attribute.getValue());
    }

    private static double healthFraction(Player player) {
        return player.getHealth() / maximumHealth(player);
    }

    private static boolean valid(LivingEntity entity) {
        return entity != null && entity.isValid() && !entity.isDead() && entity.getHealth() > 0D;
    }
}
