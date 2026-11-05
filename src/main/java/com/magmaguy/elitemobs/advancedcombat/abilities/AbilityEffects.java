package com.magmaguy.elitemobs.advancedcombat.abilities;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.combatsystem.CombatDamageContext;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.advancedcombat.ClassAbilityEligibility;
import com.magmaguy.elitemobs.advancedcombat.damage.AdvancedDamageScaling;
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
    private static final int CHILL_TICKS = 40;
    private static final double SHRINK_FACTOR = .75D;
    private static final double ENLARGE_FACTOR = 1.4D;
    private static final int ENLARGE_TICKS = 60;
    private static final double MAXIMUM_MISSING_HEALTH_PROTECTION = .30D;
    private static final int MISSING_HEALTH_PROTECTION_TICKS = 100;
    private static final int MINOR_DEBUFF_TICKS = 60;
    /** Night Vision flickers on the client in its last 10s, so it always outlasts that window. */
    private static final int NIGHT_VISION_MINIMUM_TICKS = 400;
    /** Foes this close to the centre of a {@link AbilityMechanic#FROZEN_CORE} zone are nearly frozen. */
    static final double FROZEN_CORE_RADIUS = 2D;
    /** Slowness V instead of the zone's usual Slowness III. */
    private static final int FROZEN_CORE_SLOW_AMPLIFIER = 4;
    private static final double MINOR_WEAKEN_MULTIPLIER = .92D;
    private static final List<AbilityEffect> MINOR_DEBUFFS = List.of(
            AbilityEffect.SLOW, AbilityEffect.WEAKEN, AbilityEffect.GLOW, AbilityEffect.BURN);

    private final ClassAbilityDamage damage;
    private final AbilitySemantics semantics;
    private final TimedCombatModifiers modifiers;
    private final AbilityStateRuntime states;
    private final EliteCrowdControlRuntime crowdControl;
    private final ObservedPotionApplications potions;
    private final BleedRuntime bleeds;
    private record AppliedControl(double amount, int ticks) { }

    /** The per-enemy half of a payload: main for big foes, the variant for ordinary ones. */
    private record EnemyPayload(
            Set<AbilityEffect> effects,
            AbilityTuning tuning,
            int levelDuration,
            int controlDuration,
            EliteControlEffectPlan slowPlan,
            double displacement) {
        static EnemyPayload of(Set<AbilityEffect> effects, AbilityTuning tuning, int effectiveLevel,
                               AbilityMechanicModifiers mechanicModifiers) {
            int levelDuration = ActiveAbilityLevelScaling.durationTicks(tuning.durationTicks(), effectiveLevel);
            return new EnemyPayload(effects, tuning, levelDuration,
                    scaledDuration(levelDuration, mechanicModifiers.controlDurationMultiplier()),
                    EliteControlEffectPlan.slow(2, levelDuration, mechanicModifiers),
                    ActiveAbilityLevelScaling.displacement(tuning.displacement(), effectiveLevel));
        }
    }


    AbilityEffects(
            AbilitySemantics semantics,
            TimedCombatModifiers modifiers,
            AbilityStateRuntime states, ClassAbilityDamage damage) {
        this.damage = damage;
        this.semantics = semantics;
        this.modifiers = modifiers;
        this.states = states;
        this.crowdControl = new EliteCrowdControlRuntime(MetadataHandler.PLUGIN);
        this.potions = new ObservedPotionApplications(MetadataHandler.PLUGIN);
        this.bleeds = new BleedRuntime(MetadataHandler.PLUGIN, damage);
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
            applyPotion(caster, target, spec, AbilityEffect.SLOW, AbilityRuntimeObservation.Kind.CONTROL,
                    new PotionEffect(PotionEffectType.SLOWNESS, duration, scaledAmplifier(1, potency), false, true, true),
                    ignored -> { });
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
            if (crowdControl.interrupt(caster, elite, Math.min(40, duration)))
                observeApplied(caster, target, spec, AbilityRuntimeObservation.Kind.CONTROL,
                        AbilityEffect.INTERRUPT, 1, crowdControl.remainingTicks(caster, target, AbilityEffect.INTERRUPT));
        }
        if (spec.effects().contains(AbilityEffect.FEAR)
                && authorized(caster, target, spec, AbilityEffect.FEAR)) {
            if (crowdControl.fear(caster, elite, Math.min(60, duration)))
                observeApplied(caster, target, spec, AbilityRuntimeObservation.Kind.CONTROL,
                        AbilityEffect.FEAR, 1, crowdControl.remainingTicks(caster, target, AbilityEffect.FEAR));
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
        EnemyPayload mainPayload = EnemyPayload.of(effects, tuning, effectiveLevel, mechanicModifiers);
        EnemyPayload ordinaryPayload = spec.ordinaryFoes()
                .map(variant -> EnemyPayload.of(variant.effects(), variant.tuning(),
                        effectiveLevel, mechanicModifiers))
                .orElse(null);
        int levelDuration = mainPayload.levelDuration();
        int controlDuration = mainPayload.controlDuration();
        double controlPotency = mechanicModifiers.controlPotencyMultiplier();
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
            Map<AbilityEffect, AppliedControl> appliedControls = new java.util.EnumMap<>(AbilityEffect.class);
            boolean observedPotionControl = false;
            EnemyPayload payload = ordinaryPayload != null && !semantics.isLargeOrBoss(enemy)
                    ? ordinaryPayload : mainPayload;
            Set<AbilityEffect> enemyEffects = payload.effects();
            AbilityTuning enemyTuning = payload.tuning();
            int enemyLevelDuration = payload.levelDuration();
            int enemyControlDuration = payload.controlDuration();
            EliteControlEffectPlan slowPlan = inFrozenCore(spec, enemy, selection.origin())
                    ? EliteControlEffectPlan.slow(FROZEN_CORE_SLOW_AMPLIFIER, enemyLevelDuration, mechanicModifiers)
                    : payload.slowPlan();
            double displacement = payload.displacement();
            double hitAmount = 0D;

            if (enemyEffects.contains(AbilityEffect.DAMAGE)
                    && enemyTuning.damageMultiplier() > 0D
                    && authorized(caster, enemy, spec, AbilityEffect.DAMAGE)) {
                EliteEntity elite = EntityTracker.getEliteMobEntity(enemy);
                double amount = AdvancedDamageScaling.classAbility(
                        elite,
                        effectiveLevel,
                        enemyTuning.damageMultiplier() * states.damageMultiplier(spec, enemy));
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
                    hitAmount = amount;
                    affectedEnemies.add(enemy);
                    semantics.observe(AbilityRuntimeObservation.effect(
                            AbilityRuntimeObservation.Kind.DAMAGE,
                            caster.getUniqueId(), enemy.getUniqueId(), spec.id(),
                            dealt, 0, AbilityEffect.DAMAGE));
                }
            }

            if (enemyEffects.contains(AbilityEffect.KNOCKBACK)
                    && authorized(caster, enemy, spec, AbilityEffect.KNOCKBACK)) {
                double changed = displaceAway(caster, enemy, displacement * controlPotency, .25D);
                if (changed > 0) appliedControls.put(AbilityEffect.KNOCKBACK, new AppliedControl(changed, 0));
            }
            if (enemyEffects.contains(AbilityEffect.PULL)
                    && authorized(caster, enemy, spec, AbilityEffect.PULL)) {
                double changed = displaceToward(caster, enemy, displacement * controlPotency);
                if (changed > 0) appliedControls.put(AbilityEffect.PULL, new AppliedControl(changed, 0));
            }
            if (enemyEffects.contains(AbilityEffect.LAUNCH)
                    && authorized(caster, enemy, spec, AbilityEffect.LAUNCH)) {
                double changed = displaceAway(caster, enemy, displacement * .45D * controlPotency,
                        Math.max(.55D, displacement * controlPotency));
                if (changed > 0) appliedControls.put(AbilityEffect.LAUNCH, new AppliedControl(changed, 0));
            }
            if (enemyEffects.contains(AbilityEffect.SLOW)
                    && authorized(caster, enemy, spec, AbilityEffect.SLOW)) {
                boolean extend = spec.executionTraits().mechanics()
                        .contains(AbilityMechanic.EXTEND_CONTROL_DURATION);
                // Preserve authored strength whether native stacking hides or rejects this slow.
                // Borrowing the current amplifier would create an unowned, longer root.
                int amplifier = slowPlan.potionAmplifier();
                int ownedRemaining = extend ? crowdControl.ownedSlowRemainingTicks(caster, enemy, amplifier) : 0;
                int duration = Math.max(20, slowPlan.durationTicks());
                if (ownedRemaining > 0) duration = Math.min(600, ownedRemaining + duration);
                EliteEntity elite = EntityTracker.getEliteMobEntity(enemy);
                observedPotionControl |= applyPotion(caster, enemy, spec, AbilityEffect.SLOW,
                        AbilityRuntimeObservation.Kind.CONTROL,
                        new PotionEffect(PotionEffectType.SLOWNESS, duration, amplifier, false, true, true),
                        actual -> {
                            if (elite != null) crowdControl.applySlowPotency(caster, elite,
                                    new EliteControlEffectPlan(actual.getDuration(), actual.getAmplifier(),
                                            slowPlan.additionalMovementSpeedAdjustment()));
                            if (ownedRemaining > 0) observeMechanicTriggered(caster, enemy, spec,
                                    AbilityMechanic.EXTEND_CONTROL_DURATION, AbilityEffect.SLOW,
                                    1D, actual.getDuration());
                        });
                if (extend) {
                    if (crowdControl.extendRoot(caster, elite, Math.max(20, enemyControlDuration)))
                        observeMechanicTriggered(caster, enemy, spec,
                                AbilityMechanic.EXTEND_CONTROL_DURATION,
                                AbilityEffect.SLOW, 1D, Math.max(20, enemyControlDuration));
                }
            }
            if (enemyEffects.contains(AbilityEffect.ROOT)
                    && authorized(caster, enemy, spec, AbilityEffect.ROOT)) {
                EliteEntity elite = EntityTracker.getEliteMobEntity(enemy);
                int rootTicks = Math.max(20, enemyControlDuration);
                boolean displaced = appliedControls.containsKey(AbilityEffect.KNOCKBACK)
                        || appliedControls.containsKey(AbilityEffect.PULL)
                        || appliedControls.containsKey(AbilityEffect.LAUNCH);
                if (displaced) {
                    // Rooting now would zero the velocity this cast just applied.
                    UUID enemyId = enemy.getUniqueId();
                    crowdControl.rootAfterLanding(caster, elite, rootTicks, () ->
                            AbilityControlEvidence.applied(caster.getUniqueId(), enemyId, spec,
                                    Set.of(AbilityEffect.ROOT), 1, rootTicks).forEach(semantics::observe));
                } else if (crowdControl.root(caster, elite, rootTicks))
                    appliedControls.put(AbilityEffect.ROOT, new AppliedControl(1, crowdControl.remainingTicks(caster, enemy, AbilityEffect.ROOT)));
            }
            if (enemyEffects.contains(AbilityEffect.WEAKEN)
                    && authorized(caster, enemy, spec, AbilityEffect.WEAKEN)) {
                int duration = Math.max(20, enemyControlDuration);
                double multiplier = modifiers.weakenOutgoing(
                        caster, enemy, enemyTuning.weakenMultiplier(), effectiveLevel,
                        controlPotency, duration, spec.id());
                observeApplied(caster, enemy, spec,
                        AbilityRuntimeObservation.Kind.MODIFIER_APPLIED,
                        AbilityEffect.WEAKEN, multiplier, duration);
                appliedControls.put(AbilityEffect.WEAKEN, new AppliedControl(multiplier, duration));
            }
            if (enemyEffects.contains(AbilityEffect.INTERRUPT)
                    && authorized(caster, enemy, spec, AbilityEffect.INTERRUPT)) {
                EliteEntity elite = EntityTracker.getEliteMobEntity(enemy);
                if (crowdControl.interrupt(
                        caster, elite, Math.max(10, Math.min(60, enemyControlDuration))))
                    appliedControls.put(AbilityEffect.INTERRUPT, new AppliedControl(1, crowdControl.remainingTicks(caster, enemy, AbilityEffect.INTERRUPT)));
            }
            if (enemyEffects.contains(AbilityEffect.FEAR)
                    && authorized(caster, enemy, spec, AbilityEffect.FEAR)) {
                EliteEntity elite = EntityTracker.getEliteMobEntity(enemy);
                if (crowdControl.fear(caster, elite, Math.max(30, enemyControlDuration)))
                    appliedControls.put(AbilityEffect.FEAR, new AppliedControl(1, crowdControl.remainingTicks(caster, enemy, AbilityEffect.FEAR)));
            }
            if (enemyEffects.contains(AbilityEffect.GLOW)
                    && authorized(caster, enemy, spec, AbilityEffect.GLOW)) {
                observedPotionControl |= applyPotion(caster, enemy, spec, AbilityEffect.GLOW,
                        AbilityRuntimeObservation.Kind.CONTROL,
                        new PotionEffect(PotionEffectType.GLOWING, Math.max(20, enemyLevelDuration), 0, false, true, true),
                        ignored -> { });
            }
            if (enemyEffects.contains(AbilityEffect.BURN)
                    && authorized(caster, enemy, spec, AbilityEffect.BURN)) {
                int before = enemy.getFireTicks();
                enemy.setFireTicks(Math.max(before, Math.max(20, Math.min(200, enemyLevelDuration))));
                int actual = enemy.getFireTicks();
                if (actual > before) appliedControls.put(AbilityEffect.BURN, new AppliedControl(1, actual));
            }
            if (enemyEffects.contains(AbilityEffect.CHILL)
                    && authorized(caster, enemy, spec, AbilityEffect.CHILL)) {
                observedPotionControl |= applyPotion(caster, enemy, spec, AbilityEffect.CHILL,
                        AbilityRuntimeObservation.Kind.CONTROL,
                        new PotionEffect(PotionEffectType.SLOWNESS, CHILL_TICKS, 0, false, true, true),
                        ignored -> { });
                coldVisual(enemy);
            }
            if (enemyEffects.contains(AbilityEffect.SHRINK)
                    && authorized(caster, enemy, spec, AbilityEffect.SHRINK)) {
                int shrinkTicks = Math.max(40, Math.min(200, enemyControlDuration));
                if (crowdControl.scale(caster, EntityTracker.getEliteMobEntity(enemy), SHRINK_FACTOR, shrinkTicks))
                    appliedControls.put(AbilityEffect.SHRINK, new AppliedControl(SHRINK_FACTOR, shrinkTicks));
            }
            if (enemyEffects.contains(AbilityEffect.ENLARGE)
                    && authorized(caster, enemy, spec, AbilityEffect.ENLARGE)) {
                if (crowdControl.scale(caster, EntityTracker.getEliteMobEntity(enemy), ENLARGE_FACTOR, ENLARGE_TICKS))
                    appliedControls.put(AbilityEffect.ENLARGE, new AppliedControl(ENLARGE_FACTOR, ENLARGE_TICKS));
            }
            if (enemyEffects.contains(AbilityEffect.BLEED) && hitAmount > 0D
                    && authorized(caster, enemy, spec, AbilityEffect.BLEED)) {
                UUID enemyId = enemy.getUniqueId();
                if (bleeds.bleed(caster, enemy, hitAmount, dealt -> semantics.observe(
                        AbilityRuntimeObservation.effect(AbilityRuntimeObservation.Kind.DAMAGE,
                                caster.getUniqueId(), enemyId, spec.id(), dealt, 0, AbilityEffect.BLEED))))
                    appliedControls.put(AbilityEffect.BLEED, new AppliedControl(
                            hitAmount * BleedRuntime.PULSE_SHARE,
                            BleedRuntime.PULSES * BleedRuntime.PERIOD_TICKS));
            }
            if (spec.executionTraits().mechanics().contains(AbilityMechanic.RANDOM_MINOR_DEBUFF))
                observedPotionControl |= applyMinorDebuff(caster, enemy, spec, effectiveLevel, controlPotency);
            if (enemyEffects.contains(AbilityEffect.PARTY_DAMAGE_MARK)
                    && authorized(caster, enemy, spec, AbilityEffect.PARTY_DAMAGE_MARK)) {
                double markMultiplier = ActiveAbilityLevelScaling.modifier(
                        enemyTuning.modifierMultiplier(), effectiveLevel);
                if (markMultiplier > 1D)
                    modifiers.markEnemy(caster, enemy, beneficiaryIds(caster), markMultiplier,
                            enemyLevelDuration, spec.id(), spec.executionTraits().mechanics()
                                    .contains(AbilityMechanic.HEALTH_SCALED_MARK));
                states.armDetonation(caster, spec, enemy, effectiveLevel);
                affectedEnemies.add(enemy);
                observeApplied(caster, enemy, spec,
                        AbilityRuntimeObservation.Kind.MODIFIER_APPLIED,
                        AbilityEffect.PARTY_DAMAGE_MARK,
                        markMultiplier, enemyLevelDuration);
            }
            if (enemyEffects.contains(AbilityEffect.TAUNT)
                    && authorized(caster, enemy, spec, AbilityEffect.TAUNT)) {
                tauntedEnemies.add(enemy);
                affectedEnemies.add(enemy);
            }
            if (!appliedControls.isEmpty() || observedPotionControl) {
                controlledEnemies++;
                affectedEnemies.add(enemy);
                appliedControls.forEach((effect, actual) -> AbilityControlEvidence.applied(
                        caster.getUniqueId(), enemy.getUniqueId(), spec,
                        Set.of(effect), actual.amount(), actual.ticks()).forEach(semantics::observe));
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
                if (spec.executionTraits().mechanics().contains(AbilityMechanic.PROXIMITY_FALLOFF))
                    fraction *= AbilityHealingPolicy.proximityMultiplier(
                            ally.getWorld().equals(selection.origin().getWorld())
                                    ? ally.getLocation().distance(selection.origin())
                                    : Double.POSITIVE_INFINITY,
                            tuning.radius());
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
                if (shieldAmount > 0 && Double.isFinite(shieldAmount)) {
                    int amplifier = Math.max(0, Math.min(255, (int) Math.ceil(shieldAmount / 4D) - 1));
                    double before = ally.getAbsorptionAmount();
                    supported |= potions.apply(caster.getUniqueId(), ally,
                            new PotionEffect(PotionEffectType.ABSORPTION, Math.max(20, effectDuration),
                                    amplifier, false, true, true),
                            actual -> observeApplied(caster, ally, spec, AbilityRuntimeObservation.Kind.SHIELD,
                                    AbilityEffect.SHIELD, Math.max(0, ally.getAbsorptionAmount() - before), actual.getDuration()),
                            () -> observeApplied(caster, ally, spec, AbilityRuntimeObservation.Kind.STATUS_BLOCKED,
                                    AbilityEffect.SHIELD, 0, 0));
                }
            }
            if (effects.contains(AbilityEffect.CLEANSE)) {
                cleanse(ally);
                observeApplied(caster, ally, spec,
                        AbilityRuntimeObservation.Kind.STATUS_APPLIED,
                        AbilityEffect.CLEANSE, 1D, effectDuration);
                supported = true;
            }
            if (effects.contains(AbilityEffect.SPEED) && !missingHealthScaling) {
                supported |= applyPotion(caster, ally, spec, AbilityEffect.SPEED,
                        AbilityRuntimeObservation.Kind.MODIFIER_APPLIED,
                        new PotionEffect(PotionEffectType.SPEED, Math.max(20, effectDuration), 1, false, true, true),
                        ignored -> { });
            }
            for (AbilityEffect effect : List.of(AbilityEffect.HASTE, AbilityEffect.RESISTANCE,
                    AbilityEffect.SLOW_FALLING, AbilityEffect.JUMP_BOOST, AbilityEffect.NIGHT_VISION)) {
                if (!effects.contains(effect)) continue;
                PotionEffectType type = switch (effect) {
                    case HASTE -> PotionEffectType.HASTE;
                    case RESISTANCE -> PotionEffectType.RESISTANCE;
                    case SLOW_FALLING -> PotionEffectType.SLOW_FALLING;
                    case NIGHT_VISION -> PotionEffectType.NIGHT_VISION;
                    default -> PotionEffectType.JUMP_BOOST;
                };
                int duration = effect == AbilityEffect.NIGHT_VISION
                        ? Math.max(NIGHT_VISION_MINIMUM_TICKS, effectDuration)
                        : Math.max(20, effectDuration);
                supported |= applyPotion(caster, ally, spec, effect,
                        AbilityRuntimeObservation.Kind.MODIFIER_APPLIED,
                        new PotionEffect(type, duration, 0, false, true, true), ignored -> { });
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

        if (spec.executionTraits().mechanics().contains(AbilityMechanic.MISSING_HEALTH_PROTECTION)) {
            double missing = 1D - Math.max(0D, Math.min(1D, healthFraction(caster)));
            double protection = 1D - MAXIMUM_MISSING_HEALTH_PROTECTION * missing;
            if (protection < 1D) {
                modifiers.modifyIncoming(caster, caster, protection,
                        MISSING_HEALTH_PROTECTION_TICKS, spec.id() + ".missing_health_protection");
                semantics.observe(AbilityRuntimeObservation.effect(
                        AbilityRuntimeObservation.Kind.MODIFIER_APPLIED,
                        caster.getUniqueId(), caster.getUniqueId(), spec.id(),
                        protection, MISSING_HEALTH_PROTECTION_TICKS, AbilityEffect.SELF_PROTECT));
            }
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
        if (spec.executionTraits().mechanics().contains(AbilityMechanic.PROXIMITY_FALLOFF))
            proximityRings(selection.origin(), tuning.radius());

        return new AbilityContribution(totalDamage, totalHealing, 0, threat,
                controlledEnemies, supportedAllies, 0);
    }

    void clearSource(UUID sourceId) {
        potions.clearSource(sourceId);
        crowdControl.clearSource(sourceId);
        bleeds.clearSource(sourceId);
    }

    void close() {
        potions.close();
        crowdControl.close();
        bleeds.close();
    }

    /** One random minor debuff per delivery: brief slow, weaken, reveal or burn. */
    private boolean applyMinorDebuff(Player caster, LivingEntity enemy, FixedAbilitySpec spec,
                                     int effectiveLevel, double potency) {
        AbilityEffect debuff = MINOR_DEBUFFS.get(
                java.util.concurrent.ThreadLocalRandom.current().nextInt(MINOR_DEBUFFS.size()));
        if (!authorized(caster, enemy, spec, debuff)) return false;
        return switch (debuff) {
            case SLOW -> applyMinorPotion(caster, enemy, spec, debuff,
                    new PotionEffect(PotionEffectType.SLOWNESS, MINOR_DEBUFF_TICKS, 0, false, true, true));
            case GLOW -> applyMinorPotion(caster, enemy, spec, debuff,
                    new PotionEffect(PotionEffectType.GLOWING, MINOR_DEBUFF_TICKS, 0, false, true, true));
            case WEAKEN -> {
                double multiplier = modifiers.weakenOutgoing(caster, enemy, MINOR_WEAKEN_MULTIPLIER,
                        effectiveLevel, potency, MINOR_DEBUFF_TICKS, spec.id() + ".minor_debuff");
                observeApplied(caster, enemy, spec, AbilityRuntimeObservation.Kind.MODIFIER_APPLIED,
                        AbilityEffect.WEAKEN, multiplier, MINOR_DEBUFF_TICKS);
                yield true;
            }
            default -> {
                int before = enemy.getFireTicks();
                enemy.setFireTicks(Math.max(before, MINOR_DEBUFF_TICKS));
                boolean burned = enemy.getFireTicks() > before;
                if (burned) observeApplied(caster, enemy, spec, AbilityRuntimeObservation.Kind.CONTROL,
                        AbilityEffect.BURN, 1D, enemy.getFireTicks());
                yield burned;
            }
        };
    }

    private boolean applyMinorPotion(Player caster, LivingEntity enemy, FixedAbilitySpec spec,
                                     AbilityEffect effect, PotionEffect requested) {
        return potions.apply(caster.getUniqueId(), enemy, requested,
                actual -> observeApplied(caster, enemy, spec, AbilityRuntimeObservation.Kind.CONTROL,
                        effect, actual.getAmplifier() + 1D, actual.getDuration()),
                () -> { });
    }

    private static void coldVisual(LivingEntity enemy) {
        org.bukkit.Location center = enemy.getLocation().add(0D, enemy.getHeight() * .55D, 0D);
        if (center.getWorld() == null) return;
        double spread = Math.max(.3D, enemy.getWidth() * .5D);
        center.getWorld().spawnParticle(Particle.SNOWFLAKE, center, 14, spread, enemy.getHeight() * .3D, spread, .01D);
        center.getWorld().spawnParticle(Particle.ITEM_SNOWBALL, center, 6, spread, enemy.getHeight() * .3D, spread, .02D);
    }

    /** Shows the three healing rings: strongest at the centre, weakest at the edge. */
    private static void proximityRings(org.bukkit.Location origin, double radius) {
        if (origin.getWorld() == null || radius <= 0D) return;
        Particle[] particles = {Particle.END_ROD, Particle.HAPPY_VILLAGER, Particle.COMPOSTER};
        for (int ring = 1; ring <= 3; ring++) {
            double ringRadius = radius * ring / 3D;
            int points = 12 + ring * 6;
            for (int point = 0; point < points; point++) {
                double angle = Math.PI * 2D * point / points;
                origin.getWorld().spawnParticle(particles[ring - 1],
                        origin.getX() + Math.cos(angle) * ringRadius,
                        origin.getY() + .2D,
                        origin.getZ() + Math.sin(angle) * ringRadius,
                        1, 0D, 0D, 0D, 0D);
            }
        }
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
                || effects.contains(AbilityEffect.HASTE)
                || effects.contains(AbilityEffect.RESISTANCE)
                || effects.contains(AbilityEffect.STRENGTH)
                || effects.contains(AbilityEffect.SELF_PROTECT)
                || effects.contains(AbilityEffect.SLOW_FALLING)
                || effects.contains(AbilityEffect.JUMP_BOOST)
                || effects.contains(AbilityEffect.NIGHT_VISION);
    }

    private static boolean inFrozenCore(FixedAbilitySpec spec, LivingEntity enemy, org.bukkit.Location center) {
        if (!spec.executionTraits().mechanics().contains(AbilityMechanic.FROZEN_CORE)
                || center == null || center.getWorld() == null
                || !center.getWorld().equals(enemy.getWorld())) return false;
        double dx = enemy.getLocation().getX() - center.getX();
        double dz = enemy.getLocation().getZ() - center.getZ();
        return dx * dx + dz * dz <= FROZEN_CORE_RADIUS * FROZEN_CORE_RADIUS;
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

    private boolean applyPotion(Player caster, LivingEntity target, FixedAbilitySpec spec,
                                AbilityEffect effect, AbilityRuntimeObservation.Kind kind,
                                PotionEffect requested, java.util.function.Consumer<PotionEffect> onApplied) {
        return potions.apply(caster.getUniqueId(), target, requested, actual -> {
            onApplied.accept(actual);
            observeApplied(caster, target, spec, kind, effect, actual.getAmplifier() + 1D, actual.getDuration());
        }, () -> observeApplied(caster, target, spec,
                AbilityRuntimeObservation.Kind.STATUS_BLOCKED, effect, 0, 0));
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

    private static double displaceAway(Player caster, LivingEntity target, double horizontal, double vertical) {
        Vector direction = target.getLocation().toVector().subtract(caster.getLocation().toVector());
        direction.setY(0);
        if (direction.lengthSquared() < 1.0E-6) {
            double yaw = Math.toRadians(caster.getLocation().getYaw());
            direction = new Vector(-Math.sin(yaw), 0, Math.cos(yaw));
        }
        direction.normalize().multiply(Math.max(.1D, horizontal)).setY(vertical);
        Vector before = target.getVelocity();
        target.setVelocity(before.clone().add(direction));
        return target.getVelocity().subtract(before).length();
    }

    private static double displaceToward(Player caster, LivingEntity target, double strength) {
        Vector direction = caster.getLocation().toVector().subtract(target.getLocation().toVector());
        if (direction.lengthSquared() < 1.0E-6) return 0;
        direction.normalize().multiply(Math.max(.1D, strength)).setY(.15D);
        Vector before = target.getVelocity();
        target.setVelocity(before.clone().add(direction));
        return target.getVelocity().subtract(before).length();
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
