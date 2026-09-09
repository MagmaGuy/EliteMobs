package com.magmaguy.elitemobs.experimentalcombat.abilities;

import com.magmaguy.elitemobs.experimentalcombat.classes.AbilityDefinition;
import com.magmaguy.elitemobs.experimentalcombat.classes.AbilitySlot;
import com.magmaguy.elitemobs.experimentalcombat.classes.ClassLineage;
import com.magmaguy.elitemobs.experimentalcombat.ClassAbilityEligibility;
import com.magmaguy.elitemobs.experimentalcombat.constructs.ClassAbilityConstructRuntime;
import com.magmaguy.elitemobs.experimentalcombat.constructs.ClassConstructVisualRegistry;
import com.magmaguy.elitemobs.experimentalcombat.presentation.BukkitClassAbilityPresentation;
import com.magmaguy.elitemobs.experimentalcombat.minions.ClassMinionManager;
import com.magmaguy.elitemobs.instanced.InstancePlayerMovement;
import com.magmaguy.magmacore.visuals.terrain.TerrainImpactRequest;
import com.magmaguy.magmacore.visuals.terrain.TerrainImpacts;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Horse;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Bukkit execution adapter for the fixed catalog. Each family delegates to reusable targeting,
 * movement, effect and modifier primitives; there is no per-ability event switch.
 */
public final class BukkitClassAbilityEngine implements ClassAbilityEngine {
    private static final double LEAP_MINIMUM_HORIZONTAL_VELOCITY = .8D;
    private static final double LEAP_VERTICAL_VELOCITY = .9D;
    private static final int LEAP_LANDING_TIMEOUT_TICKS = 80;
    private static final double PROJECTILE_BLOCKS_PER_TICK = 2.5D;

    private final Plugin plugin;
    private final FixedAbilityRegistry registry;
    private final AbilitySemantics semantics;
    private final AbilityTargeting targeting;
    private final TimedCombatModifiers modifiers;
    private final AbilityStateRuntime states;
    private final AbilityEffects effects;
    private final FrenzyRuntime frenzy;
    private final ClassAbilityProjectileCarrier projectileCarriers;
    private final DivineSteedManager divineSteeds;
    private final GuardianFlightManager guardianFlights;
    private final ClassMinionManager classMinions;
    private final BukkitBlinkPotionFlash blinkPotionFlash;
    private final ClassAbilityConstructRuntime constructs;
    private final BukkitClassAbilityPresentation presentations = new BukkitClassAbilityPresentation();
    private final AutoCloseable controlAttributionLease;
    private final Map<AbilityFamily, AbilityExecutor> executors = new EnumMap<>(AbilityFamily.class);
    private final Map<UUID, Set<BukkitTask>> tasksByCaster = new HashMap<>();
    private final Map<UUID, SanctuaryAnchor> sanctuaryAnchors = new HashMap<>();
    private boolean closed;

    public BukkitClassAbilityEngine(
            Plugin plugin, FixedAbilityRegistry registry, AbilitySemantics semantics,
            ClassMinionManager.OwnerProfileResolver ownerProfiles,
            ClassMinionManager.MinionDamageEvaluator minionDamageEvaluator,
            ClassAbilityDamage.Delivery damageDelivery) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.semantics = Objects.requireNonNull(semantics, "semantics");
        this.registry = Objects.requireNonNull(registry, "registry");
        this.targeting = new AbilityTargeting(semantics);
        this.modifiers = new TimedCombatModifiers(plugin, semantics);
        var damage = new ClassAbilityDamage(damageDelivery);
        this.states = new AbilityStateRuntime(plugin, semantics, registry, damage);
        this.effects = new AbilityEffects(semantics, modifiers, states, damage);
        this.controlAttributionLease = ClassControlAttribution.install((caster, target) ->
                states.hasOwnedTaunt(caster, target) || effects.controlledBy(caster, target));
        this.frenzy = new FrenzyRuntime(plugin);
        this.projectileCarriers = new ClassAbilityProjectileCarrier(plugin);
        this.divineSteeds = new DivineSteedManager(plugin);
        this.guardianFlights = new GuardianFlightManager(plugin);
        this.classMinions = new ClassMinionManager(
                plugin, ownerProfiles, minionDamageEvaluator, effects::applyMinionImpact,
                new ClassMinionManager.MinionRuntimeObserver() {
                    @Override
                    public void applied(
                            UUID casterId,
                            UUID targetId,
                            String abilityId,
                            AbilityEffect effect,
                            double amount) {
                        semantics.observe(AbilityRuntimeObservation.effect(
                                effect == AbilityEffect.LIFESTEAL
                                        ? AbilityRuntimeObservation.Kind.HEAL
                                        : AbilityRuntimeObservation.Kind.DAMAGE,
                                casterId, targetId, abilityId, amount, 0, effect));
                    }

                    @Override
                    public void removed(UUID casterId, UUID minionId, String abilityId) {
                        semantics.observe(new AbilityRuntimeObservation(
                                AbilityRuntimeObservation.Kind.SUMMON_CLEARED,
                                casterId, minionId, abilityId, 1D, 0, 1,
                                Set.of(), Set.of()));
                    }
                });
        this.blinkPotionFlash = new BukkitBlinkPotionFlash(plugin);
        this.constructs = new ClassAbilityConstructRuntime(
                plugin, new ClassAbilityConstructRuntime.LifecycleObserver() {
            @Override
            public void spawned(
                    UUID casterId,
                    String abilityId,
                    int durationTicks,
                    ClassConstructVisualRegistry.AnchorMode anchorMode) {
                semantics.observe(new AbilityRuntimeObservation(
                        AbilityRuntimeObservation.Kind.CONSTRUCT_SPAWNED,
                        casterId, casterId, abilityId, 1D, durationTicks, 1,
                        Set.of(), constructMechanics(anchorMode)));
            }

            @Override
            public void cleared(
                    UUID casterId,
                    String abilityId,
                    ClassConstructVisualRegistry.AnchorMode anchorMode) {
                semantics.observe(new AbilityRuntimeObservation(
                        AbilityRuntimeObservation.Kind.CONSTRUCT_CLEARED,
                        casterId, casterId, abilityId, 1D, 0, 1,
                        Set.of(), constructMechanics(anchorMode)));
            }
        });
        executors.put(AbilityFamily.MOUNTED_CHARGE, this::executeMountedCharge);
        executors.put(AbilityFamily.BALLISTIC_LEAP, this::executeLeap);
        executors.put(AbilityFamily.SAFE_DASH, this::executeDash);
        executors.put(AbilityFamily.SAFE_BLINK, this::executeBlink);
        executors.put(AbilityFamily.ALLY_FLIGHT, this::executeAllyFlight);
        executors.put(AbilityFamily.INSTANT, this::executeInstant);
        executors.put(AbilityFamily.PROJECTILE, this::executeProjectile);
        executors.put(AbilityFamily.SUMMON, this::executeSummon);
        executors.put(AbilityFamily.ZONE, this::executeZone);
        if (executors.size() != AbilityFamily.values().length)
            throw new IllegalStateException("Every ability family requires an executor");
    }

    @Override
    public AbilityResult execute(Player player, ClassLineage lineage, AbilitySlot slot, int effectiveLevel) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(lineage, "lineage");
        Objects.requireNonNull(slot, "slot");
        AbilityDefinition ability = abilityFor(lineage, slot);
        if (closed) return AbilityResult.failure(ability.id(), AbilityFailureReason.ENGINE_CLOSED);
        if (!Bukkit.isPrimaryThread()) return AbilityResult.failure(ability.id(), AbilityFailureReason.WRONG_THREAD);
        if (!player.isOnline() || !player.isValid() || player.isDead())
            return AbilityResult.failure(ability.id(), AbilityFailureReason.INVALID_PLAYER);
        if (!lineage.activeForm().band().containsEffectiveLevel(effectiveLevel))
            return AbilityResult.failure(ability.id(), AbilityFailureReason.INVALID_LEVEL);

        FixedAbilitySpec spec;
        try {
            spec = registry.require(ability.id());
        } catch (IllegalArgumentException exception) {
            return AbilityResult.failure(ability.id(), AbilityFailureReason.ABILITY_NOT_REGISTERED);
        }
        AbilityExecutor executor = executors.get(spec.family());
        BukkitClassAbilityPresentation.Session presentation = presentations.begin(player, lineage, spec);
        AbilityResult result = executor.execute(player, spec, effectiveLevel, presentation);
        if (result.successful()) presentation.commit();
        return result;
    }

    public FixedAbilityRegistry registry() {
        return registry;
    }

    private AbilityResult executeInstant(
            Player player,
            FixedAbilitySpec spec,
            int effectiveLevel,
            BukkitClassAbilityPresentation.Session presentation) {
        AbilityTargeting.TargetSelection selection = targeting.select(player, spec);
        if (!hasApplicableTarget(spec, selection))
            return AbilityResult.failure(spec.id(), AbilityFailureReason.NO_VALID_TARGET);
        if (!states.canActivate(player, spec, selection))
            return AbilityResult.failure(spec.id(), AbilityFailureReason.NO_VALID_TARGET);
        presentConstruct(player, spec, selection.origin());
        if (spec.executionTraits().mechanics().contains(AbilityMechanic.CHAINING_CAST)) {
            launchEffectChain(player, spec, selection, effectiveLevel, presentation);
            return AbilityResult.scheduled(spec.id(), AbilityContribution.NONE);
        }
        AbilityContribution initial = applyAndPresent(
                presentation, player, spec, selection, effectiveLevel);
        if (spec.executionTraits().mechanics().contains(AbilityMechanic.MISSING_HEALTH_SCALING)) {
            double levelScale = AbilityLevelScaling.multiplier(effectiveLevel);
            frenzy.activate(
                    player,
                    Math.max(0D, spec.tuning().modifierMultiplier() - 1D) * levelScale,
                    FrenzyScalingPolicy.BASE_MAXIMUM_SPEED_ADJUSTMENT * levelScale,
                    FrenzyScalingPolicy.durationTicks(spec.tuning(), effectiveLevel),
                    spec.id());
        }
        if (spec.executionTraits().mechanics().contains(AbilityMechanic.FOLLOW_CASTER_FIELD)) {
            launchFollowingPulses(player, spec, effectiveLevel, presentation);
            return AbilityResult.scheduled(spec.id(), initial);
        }
        return AbilityResult.success(spec.id(), initial);
    }

    private void launchEffectChain(
            Player caster,
            FixedAbilitySpec spec,
            AbilityTargeting.TargetSelection selection,
            int effectiveLevel,
            BukkitClassAbilityPresentation.Session presentation) {
        long casterLifecycleToken = states.lifecycleToken(caster);
        List<LivingEntity> enemies = new ArrayList<>(selection.enemies());
        List<Player> allies = new ArrayList<>(selection.allies());
        if (spec.executionTraits().mechanics().contains(AbilityMechanic.LOWEST_HEALTH_FIRST))
            allies.sort(java.util.Comparator.comparingDouble(BukkitClassAbilityEngine::healthFraction));
        List<Long> allyLifecycleTokens = allies.stream()
                .map(states::lifecycleToken)
                .toList();
        int count = Math.max(enemies.size(), allies.size());
        if (count == 0) return;
        BukkitTask task = new BukkitRunnable() {
            private int index;

            @Override
            public void run() {
                if (!currentCast(caster, casterLifecycleToken) || index >= count) {
                    BukkitClassAbilityEngine.this.cancelTracked(caster.getUniqueId(), this);
                    return;
                }
                LivingEntity enemy = index < enemies.size() ? enemies.get(index) : null;
                Player ally = index < allies.size() ? allies.get(index) : null;
                long allyLifecycleToken = index < allyLifecycleTokens.size()
                        ? allyLifecycleTokens.get(index) : Long.MIN_VALUE;
                List<LivingEntity> currentEnemies = enemy != null
                        && semantics.canTargetEnemy(caster, enemy, spec)
                        ? List.of(enemy) : List.of();
                List<Player> currentAllies = isCurrentChainAlly(caster, ally, allyLifecycleToken)
                        ? List.of(ally) : List.of();
                index++;
                if (currentEnemies.isEmpty() && currentAllies.isEmpty()) return;
                Location origin = !currentEnemies.isEmpty()
                        ? currentEnemies.get(0).getLocation()
                        : currentAllies.get(0).getLocation();
                AbilityContribution contribution = applyAndPresent(
                        presentation,
                        caster,
                        spec,
                        new AbilityTargeting.TargetSelection(currentEnemies, currentAllies, origin),
                        effectiveLevel);
                if (contribution.isMeaningful())
                    semantics.recordContribution(caster, spec.id(), contribution);
                origin.getWorld().spawnParticle(Particle.END_ROD, origin, 8, .25D, .3D, .25D, .02D);
            }
        }.runTaskTimer(plugin, 1L, 3L);
        track(caster.getUniqueId(), task);
    }

    private boolean isCurrentChainAlly(Player caster, Player candidate, long lifecycleToken) {
        if (candidate == null
                || !states.isCurrentLifecycle(candidate, lifecycleToken)
                || !candidate.isOnline()
                || !candidate.isValid()
                || candidate.isDead()
                || !ClassAbilityEligibility.isEligible(candidate)
                || !candidate.getWorld().equals(caster.getWorld())) return false;
        if (candidate.getUniqueId().equals(caster.getUniqueId())) return true;
        return semantics.alliesOf(caster).stream()
                .filter(Objects::nonNull)
                .anyMatch(current -> current.getUniqueId().equals(candidate.getUniqueId()));
    }

    private void launchFollowingPulses(
            Player caster,
            FixedAbilitySpec spec,
            int effectiveLevel,
            BukkitClassAbilityPresentation.Session presentation) {
        long casterLifecycleToken = states.lifecycleToken(caster);
        int pulses = Math.max(2, Math.min(5, spec.tuning().durationTicks() / 20));
        BukkitTask task = new BukkitRunnable() {
            private int completed;

            @Override
            public void run() {
                if (!currentCast(caster, casterLifecycleToken) || completed >= pulses) {
                    BukkitClassAbilityEngine.this.cancelTracked(caster.getUniqueId(), this);
                    return;
                }
                Location origin = caster.getLocation();
                AbilityTargeting.TargetSelection current = targeting.select(caster, spec, origin);
                AbilityContribution contribution = applyPeriodicAndPresent(
                        presentation, caster, spec, current, effectiveLevel);
                if (contribution.isMeaningful())
                    semantics.recordContribution(caster, spec.id(), contribution);
                origin.getWorld().spawnParticle(Particle.SWEEP_ATTACK, origin, 8,
                        Math.max(1D, spec.tuning().radius() * .5D), .4D,
                        Math.max(1D, spec.tuning().radius() * .5D), .03D);
                completed++;
            }
        }.runTaskTimer(plugin, 20L, 20L);
        track(caster.getUniqueId(), task);
    }

    private AbilityResult executeProjectile(
            Player player,
            FixedAbilitySpec spec,
            int effectiveLevel,
            BukkitClassAbilityPresentation.Session presentation) {
        Set<AbilityMechanic> mechanics = spec.executionTraits().mechanics();
        AbilityTargeting.TargetSelection selection = targeting.select(player, spec);
        if (mechanics.contains(AbilityMechanic.PIERCING_CAST)) {
            List<LivingEntity> intercepts = targeting.piercingEnemies(player, spec);
            Location origin = intercepts.isEmpty()
                    ? player.getLocation()
                    : intercepts.get(intercepts.size() - 1).getLocation();
            selection = new AbilityTargeting.TargetSelection(intercepts, List.of(), origin);
        }
        List<LivingEntity> visibleEnemies = selection.enemies().stream()
                .filter(enemy -> clearSegment(player.getEyeLocation(),
                        enemy.getLocation().add(0D, enemy.getHeight() * .55D, 0D)))
                .toList();
        selection = new AbilityTargeting.TargetSelection(
                visibleEnemies, selection.allies(), selection.origin());
        if (selection.enemies().isEmpty()) {
            // State prerequisites (corpses, wind-ups) still gate the cast; a missing target does
            // not. Like the magic weapons, a lockless cast fires a straight skill-shot instead.
            if (!states.canActivate(player, spec, selection))
                return AbilityResult.failure(spec.id(), AbilityFailureReason.NO_VALID_TARGET);
            return launchUnaimedProjectile(player, spec, effectiveLevel, presentation);
        }
        if (!states.canActivate(player, spec, selection))
            return AbilityResult.failure(spec.id(), AbilityFailureReason.NO_VALID_TARGET);

        long windUp = mechanics.contains(AbilityMechanic.WIND_UP) ? 15L : 0L;
        if (windUp > 0L) {
            player.getWorld().spawnParticle(Particle.ENCHANT, player.getEyeLocation(),
                    24, .35D, .35D, .35D, .03D);
            player.getWorld().playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, .7F, 1.5F);
        }
        if (mechanics.contains(AbilityMechanic.CHAINING_CAST)) {
            launchChain(player, spec, selection.enemies(), effectiveLevel, presentation);
        } else if (mechanics.contains(AbilityMechanic.PIERCING_CAST)) {
            List<LivingEntity> pierced = selection.enemies();
            launchProjectile(player, spec, pierced.get(pierced.size() - 1),
                    new AbilityTargeting.TargetSelection(pierced, selection.allies(), selection.origin()),
                    effectiveLevel, windUp, presentation);
        } else if (spec.target() == AbilityTarget.AIMED_ENEMY && spec.tuning().projectileCount() > 1) {
            LivingEntity target = selection.enemies().get(0);
            for (int index = 0; index < spec.tuning().projectileCount(); index++)
                launchProjectile(player, spec, target,
                        new AbilityTargeting.TargetSelection(List.of(target), selection.allies(), target.getLocation()),
                        effectiveLevel, windUp + index * 2L, presentation);
        } else {
            List<LivingEntity> targets = selection.enemies().stream()
                    .limit(spec.tuning().projectileCount())
                    .toList();
            for (int index = 0; index < targets.size(); index++) {
                LivingEntity target = targets.get(index);
                launchProjectile(player, spec, target,
                        new AbilityTargeting.TargetSelection(List.of(target), selection.allies(), target.getLocation()),
                        effectiveLevel, windUp + index * 2L, presentation);
            }
        }
        player.getWorld().spawnParticle(Particle.CRIT, player.getEyeLocation(),
                Math.max(4, spec.tuning().projectileCount() * 3), .3, .3, .3, .35);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ARROW_SHOOT, .8F, 1.15F);
        return AbilityResult.scheduled(spec.id(), AbilityContribution.NONE);
    }

    private AbilityResult executeZone(
            Player player,
            FixedAbilitySpec spec,
            int effectiveLevel,
            BukkitClassAbilityPresentation.Session presentation) {
        Location origin = spec.target() == AbilityTarget.AIMED_LOCATION
                ? targeting.aimedLocation(player, spec.tuning().range())
                : player.getLocation();
        if (origin.getWorld() == null || !origin.getWorld().isChunkLoaded(origin.getBlockX() >> 4, origin.getBlockZ() >> 4))
            return AbilityResult.failure(spec.id(), AbilityFailureReason.UNSAFE_DESTINATION);
        presentConstruct(player, spec, origin);

        int totalDuration = spec.effects().contains(AbilityEffect.HEAL)
                ? AbilityHealingPolicy.durationTicks(
                spec.tuning().durationTicks(),
                AbilityHealingType.PERIODIC,
                semantics.mechanicModifiers(player))
                : spec.tuning().durationTicks();

        if (spec.executionTraits().establishesMobilityAnchor())
            sanctuaryAnchors.put(player.getUniqueId(), new SanctuaryAnchor(origin,
                    expiresAt(totalDuration)));

        origin.getWorld().spawnParticle(Particle.ENCHANT, origin, 32,
                spec.tuning().radius() * .5D, .3D, spec.tuning().radius() * .5D, .03D);

        if (spec.executionTraits().mechanics().contains(AbilityMechanic.CLUSTER_PROJECTILE)) {
            launchClusterProjectile(player, spec, origin, effectiveLevel, presentation);
            return AbilityResult.scheduled(spec.id(), AbilityContribution.NONE);
        }
        if (spec.executionTraits().mechanics().contains(AbilityMechanic.EXPANDING_PULSES)) {
            launchExpandingPulses(player, spec, origin, effectiveLevel, presentation);
            return AbilityResult.scheduled(spec.id(), AbilityContribution.NONE);
        }
        if (spec.executionTraits().mechanics().contains(AbilityMechanic.PROJECTILE_BOMBARDMENT)) {
            launchProjectileBombardment(player, spec, origin, effectiveLevel, presentation);
            return AbilityResult.scheduled(spec.id(), AbilityContribution.NONE);
        }
        if (spec.executionTraits().mechanics().contains(AbilityMechanic.WIND_UP)) {
            launchWoundUpZone(player, spec, origin, effectiveLevel, presentation);
            return AbilityResult.scheduled(spec.id(), AbilityContribution.NONE);
        }

        AbilityTargeting.TargetSelection initialSelection = targeting.select(player, spec, origin);
        AbilityContribution initial = applyPeriodicAndPresent(
                presentation, player, spec, initialSelection, effectiveLevel);
        if (spec.tuning().repetitions() <= 1) return AbilityResult.success(spec.id(), initial);

        int period = Math.max(1, totalDuration / spec.tuning().repetitions());
        long casterLifecycleToken = states.lifecycleToken(player);
        BukkitTask task = new BukkitRunnable() {
            private int repetitions = 1;

            @Override
            public void run() {
                if (!currentCast(player, casterLifecycleToken)
                        || repetitions >= spec.tuning().repetitions()) {
                    BukkitClassAbilityEngine.this.cancelTracked(player.getUniqueId(), this);
                    return;
                }
                Location pulseOrigin = spec.executionTraits().mechanics()
                        .contains(AbilityMechanic.FOLLOW_CASTER_FIELD)
                        ? player.getLocation()
                        : origin;
                AbilityTargeting.TargetSelection selection = targeting.select(player, spec, pulseOrigin);
                AbilityContribution delayed = applyPeriodicAndPresent(
                        presentation, player, spec, selection, effectiveLevel);
                if (delayed.isMeaningful()) semantics.recordContribution(player, spec.id(), delayed);
                if (pulseOrigin.getWorld() != null)
                    pulseOrigin.getWorld().spawnParticle(Particle.ENCHANT, pulseOrigin, 18,
                            spec.tuning().radius() * .45D, .25D,
                            spec.tuning().radius() * .45D, .02D);
                repetitions++;
            }
        }.runTaskTimer(plugin, period, period);
        track(player.getUniqueId(), task);
        return AbilityResult.scheduled(spec.id(), initial);
    }

    private void launchExpandingPulses(
            Player caster,
            FixedAbilitySpec spec,
            Location origin,
            int effectiveLevel,
            BukkitClassAbilityPresentation.Session presentation) {
        long casterLifecycleToken = states.lifecycleToken(caster);
        ExpandingPulsePlan plan = ExpandingPulsePlan.create(
                spec.tuning().radius(),
                spec.tuning().repetitions(),
                spec.tuning().durationTicks());
        Set<UUID> struck = new HashSet<>();
        BukkitTask task = new BukkitRunnable() {
            private int age;
            private int nextPulse;

            @Override
            public void run() {
                if (!currentCast(caster, casterLifecycleToken)
                        || !caster.getWorld().equals(origin.getWorld())
                        || nextPulse >= plan.pulses().size()) {
                    BukkitClassAbilityEngine.this.cancelTracked(caster.getUniqueId(), this);
                    return;
                }
                while (nextPulse < plan.pulses().size()
                        && plan.pulses().get(nextPulse).delayTicks() <= age) {
                    ExpandingPulsePlan.Pulse pulse = plan.pulses().get(nextPulse++);
                    AbilityTargeting.TargetSelection candidates = targeting.select(caster, spec, origin);
                    List<LivingEntity> newlyReached = candidates.enemies().stream()
                            .filter(enemy -> pulse.contains(horizontalDistance(origin, enemy.getLocation())))
                            .filter(enemy -> struck.add(enemy.getUniqueId()))
                            .toList();
                    AbilityContribution contribution = applyPeriodicAndPresent(
                            presentation,
                            caster,
                            spec,
                            new AbilityTargeting.TargetSelection(newlyReached, List.of(), origin),
                            effectiveLevel);
                    if (contribution.isMeaningful())
                        semantics.recordContribution(caster, spec.id(), contribution);
                    renderExpandingRing(origin, pulse);
                }
                age++;
            }
        }.runTaskTimer(plugin, 1L, 1L);
        track(caster.getUniqueId(), task);
    }

    private static void renderExpandingRing(Location origin, ExpandingPulsePlan.Pulse pulse) {
        if (origin.getWorld() == null) return;
        int samples = Math.max(18, (int) Math.ceil(pulse.outerRadiusInclusive() * 5D));
        for (int sample = 0; sample < samples; sample++) {
            double angle = Math.PI * 2D * sample / samples;
            Location point = origin.clone().add(
                    Math.cos(angle) * pulse.outerRadiusInclusive(),
                    .12D,
                    Math.sin(angle) * pulse.outerRadiusInclusive());
            origin.getWorld().spawnParticle(Particle.DUST_PLUME, point,
                    1, .04D, .035D, .04D, .005D);
        }
        origin.getWorld().playSound(origin, Sound.ENTITY_GENERIC_EXPLODE,
                .55F, .72F + pulse.index() * .13F);
    }

    private void launchProjectileBombardment(
            Player caster,
            FixedAbilitySpec spec,
            Location origin,
            int effectiveLevel,
            BukkitClassAbilityPresentation.Session presentation) {
        long casterLifecycleToken = states.lifecycleToken(caster);
        int fallTicks = Math.max(4, Math.min(8, spec.tuning().durationTicks() / 10));
        long spreadSeed = caster.getUniqueId().getMostSignificantBits()
                ^ caster.getUniqueId().getLeastSignificantBits()
                ^ Double.doubleToLongBits(origin.getX())
                ^ Long.rotateLeft(Double.doubleToLongBits(origin.getZ()), 23)
                ^ System.nanoTime();
        ProjectileBombardmentPlan plan = ProjectileBombardmentPlan.create(
                spec.tuning().radius(),
                spec.tuning().durationTicks(),
                spec.tuning().repetitions(),
                spec.tuning().projectileCount(),
                fallTicks,
                spreadSeed);
        int finalImpactTick = plan.impacts().get(plan.impacts().size() - 1).impactTick();
        BukkitTask task = new BukkitRunnable() {
            private int age;

            @Override
            public void run() {
                if (!currentCast(caster, casterLifecycleToken)
                        || !caster.getWorld().equals(origin.getWorld())
                        || origin.getWorld() == null
                        || !origin.getWorld().isChunkLoaded(origin.getBlockX() >> 4, origin.getBlockZ() >> 4)) {
                    BukkitClassAbilityEngine.this.cancelTracked(caster.getUniqueId(), this);
                    return;
                }
                for (ProjectileBombardmentPlan.Impact impact : plan.impacts()) {
                    if (age < impact.launchTick() || age > impact.impactTick()) continue;
                    Location destination = bombardmentImpactLocation(origin, impact);
                    if (age == impact.impactTick()) {
                        applyBombardmentImpact(
                                caster, spec, destination, plan.impactRadius(), effectiveLevel, presentation);
                    } else {
                        renderBombardmentProjectile(destination, impact, age);
                    }
                }
                if (age++ >= finalImpactTick)
                    BukkitClassAbilityEngine.this.cancelTracked(caster.getUniqueId(), this);
            }
        }.runTaskTimer(plugin, 1L, 1L);
        track(caster.getUniqueId(), task);
    }

    private void observeProjectileImpact(Player caster, FixedAbilitySpec spec, int selectedEnemies) {
        semantics.observe(new AbilityRuntimeObservation(AbilityRuntimeObservation.Kind.PROJECTILE_IMPACT,
                caster.getUniqueId(), null, spec.id(), selectedEnemies, 0, 1,
                spec.effects(), spec.executionTraits().mechanics()));
    }

    private void applyBombardmentImpact(
            Player caster,
            FixedAbilitySpec spec,
            Location impact,
            double impactRadius,
            int effectiveLevel,
            BukkitClassAbilityPresentation.Session presentation) {
        List<LivingEntity> target = targeting.select(caster, spec, impact).enemies().stream()
                .filter(enemy -> enemy.getLocation().distanceSquared(impact) <= impactRadius * impactRadius)
                .limit(1)
                .toList();
        observeProjectileImpact(caster, spec, target.size());
        if (!target.isEmpty()) {
            AbilityContribution contribution = applyProjectileAndPresent(
                    presentation,
                    caster,
                    spec,
                    new AbilityTargeting.TargetSelection(target, List.of(), impact),
                    effectiveLevel);
            if (contribution.isMeaningful())
                semantics.recordContribution(caster, spec.id(), contribution);
        }
        impact.getWorld().spawnParticle(Particle.CRIT, impact, 8, .18D, .12D, .18D, .04D);
        impact.getWorld().spawnParticle(Particle.DUST_PLUME, impact, 5, .22D, .08D, .22D, .015D);
        impact.getWorld().playSound(impact, Sound.ENTITY_ARROW_HIT_PLAYER, .4F, 1.25F);
    }

    private static Location bombardmentImpactLocation(
            Location origin,
            ProjectileBombardmentPlan.Impact impact) {
        return origin.clone().add(impact.offsetX(), .15D, impact.offsetZ());
    }

    private void launchClusterProjectile(
            Player caster,
            FixedAbilitySpec spec,
            Location origin,
            int effectiveLevel,
            BukkitClassAbilityPresentation.Session presentation) {
        long casterLifecycleToken = states.lifecycleToken(caster);
        Location start = caster.getEyeLocation().clone();
        int travelTicks = Math.max(3, Math.min(12,
                (int) Math.ceil(start.distance(origin) / PROJECTILE_BLOCKS_PER_TICK)));
        List<ClusterImpactPlan.Impact> impacts = ClusterImpactPlan.create(
                spec.tuning().radius(),
                spec.tuning().repetitions(),
                Math.toRadians(caster.getLocation().getYaw()));
        int finalTick = travelTicks + impacts.get(impacts.size() - 1).delayTicks();
        BukkitTask task = new BukkitRunnable() {
            private int age;

            @Override
            public void run() {
                if (!currentCast(caster, casterLifecycleToken)
                        || !caster.getWorld().equals(origin.getWorld())) {
                    BukkitClassAbilityEngine.this.cancelTracked(caster.getUniqueId(), this);
                    return;
                }
                if (age < travelTicks) {
                    renderProjectile(spec, lerp(start, origin, (age + 1D) / travelTicks),
                            (age + 1D) / travelTicks);
                }
                for (ClusterImpactPlan.Impact impact : impacts) {
                    if (age != travelTicks + impact.delayTicks()) continue;
                    Location location = origin.clone().add(impact.offsetX(), .15D, impact.offsetZ());
                    AbilityTargeting.TargetSelection selection = targeting.selectArea(
                            caster, spec, location, impact.blastRadius());
                    observeProjectileImpact(caster, spec, selection.enemies().size());
                    AbilityContribution contribution = applyProjectileAndPresent(
                            presentation, caster, spec, selection, effectiveLevel);
                    if (contribution.isMeaningful())
                        semantics.recordContribution(caster, spec.id(), contribution);
                    location.getWorld().spawnParticle(Particle.EXPLOSION, location,
                            3, impact.blastRadius() * .35D, .2D,
                            impact.blastRadius() * .35D, .02D);
                    location.getWorld().playSound(location,
                            Sound.ENTITY_GENERIC_EXPLODE, .8F, 1.15F + impact.delayTicks() * .02F);
                }
                if (age++ >= finalTick)
                    BukkitClassAbilityEngine.this.cancelTracked(caster.getUniqueId(), this);
            }
        }.runTaskTimer(plugin, 1L, 1L);
        track(caster.getUniqueId(), task);
    }

    private static void renderBombardmentProjectile(
            Location impactLocation,
            ProjectileBombardmentPlan.Impact impact,
            int age) {
        double progress = (age - impact.launchTick())
                / (double) (impact.impactTick() - impact.launchTick());
        Location projectile = impactLocation.clone().add(0D, (1D - progress) * 9D, 0D);
        projectile.getWorld().spawnParticle(Particle.CRIT, projectile,
                2, .025D, .08D, .025D, .01D);
        projectile.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, projectile,
                1, .02D, .04D, .02D, .005D);
    }

    /**
     * A projectile cast with no acquired target still fires: a straight skill-shot toward the aim
     * point that hits the first authorized enemy it crosses, mirroring the magic weapons'
     * targetless bolts.
     */
    private AbilityResult launchUnaimedProjectile(
            Player player,
            FixedAbilitySpec spec,
            int effectiveLevel,
            BukkitClassAbilityPresentation.Session presentation) {
        long casterLifecycleToken = states.lifecycleToken(player);
        Location castOrigin = player.getEyeLocation().clone();
        Location destination = castOrigin.clone().add(
                castOrigin.getDirection().normalize().multiply(Math.max(4D, spec.tuning().range())));
        int projectiles = Math.max(1, spec.tuning().projectileCount());
        for (int index = 0; index < projectiles; index++) {
            BukkitTask launch = new BukkitRunnable() {
                @Override
                public void run() {
                    if (currentCast(player, casterLifecycleToken)
                            && player.getWorld().equals(destination.getWorld())) {
                        projectileCarriers.launch(
                                player,
                                destination,
                                spec.tuning().range(),
                                PROJECTILE_BLOCKS_PER_TICK,
                                (projectile, hitEntity) -> impactPhysicalProjectile(
                                        player, projectile, spec, hitEntity, castOrigin,
                                        casterLifecycleToken, effectiveLevel, presentation),
                                (point, progress) -> renderProjectile(spec, point, progress));
                    }
                    BukkitClassAbilityEngine.this.cancelTracked(player.getUniqueId(), this);
                }
            }.runTaskLater(plugin, Math.max(1L, index * 2L));
            track(player.getUniqueId(), launch);
        }
        player.getWorld().spawnParticle(Particle.CRIT, player.getEyeLocation(),
                Math.max(4, projectiles * 3), .3, .3, .3, .35);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ARROW_SHOOT, .8F, 1.15F);
        return AbilityResult.scheduled(spec.id(), AbilityContribution.NONE);
    }

    private void launchProjectile(
            Player caster,
            FixedAbilitySpec spec,
            LivingEntity primaryTarget,
            AbilityTargeting.TargetSelection impactSelection,
            int effectiveLevel,
            long delayTicks,
            BukkitClassAbilityPresentation.Session presentation) {
        if (!spec.executionTraits().mechanics().contains(AbilityMechanic.PIERCING_CAST)) {
            launchPhysicalProjectile(
                    caster, spec, primaryTarget, effectiveLevel, delayTicks, presentation);
            return;
        }

        launchSimulatedPiercingProjectile(
                caster, spec, primaryTarget, impactSelection, effectiveLevel, delayTicks, presentation);
    }

    private void launchPhysicalProjectile(
            Player caster,
            FixedAbilitySpec spec,
            LivingEntity primaryTarget,
            int effectiveLevel,
            long delayTicks,
            BukkitClassAbilityPresentation.Session presentation) {
        long casterLifecycleToken = states.lifecycleToken(caster);
        Location castOrigin = caster.getEyeLocation().clone();
        Location destination = primaryTarget.getLocation().add(0D, primaryTarget.getHeight() * .55D, 0D);
        BukkitTask launch = new BukkitRunnable() {
            @Override
            public void run() {
                if (currentCast(caster, casterLifecycleToken)
                        && caster.getWorld().equals(destination.getWorld())) {
                    projectileCarriers.launch(
                            caster,
                            destination,
                            spec.tuning().range(),
                            PROJECTILE_BLOCKS_PER_TICK,
                            (projectile, hitEntity) -> impactPhysicalProjectile(
                                    caster, projectile, spec, hitEntity, castOrigin,
                                    casterLifecycleToken,
                                    effectiveLevel, presentation),
                            (point, progress) -> renderProjectile(spec, point, progress));
                }
                BukkitClassAbilityEngine.this.cancelTracked(caster.getUniqueId(), this);
            }
        }.runTaskLater(plugin, Math.max(1L, delayTicks));
        track(caster.getUniqueId(), launch);
    }

    private void impactPhysicalProjectile(
            Player caster,
            org.bukkit.entity.Projectile projectile,
            FixedAbilitySpec spec,
            LivingEntity hitEntity,
            Location castOrigin,
            long casterLifecycleToken,
            int effectiveLevel,
            BukkitClassAbilityPresentation.Session presentation) {
        if (!currentCast(caster, casterLifecycleToken)
                || !semantics.canTargetEnemy(caster, hitEntity, spec)) return;
        Location impact = hitEntity.getLocation().add(0D, hitEntity.getHeight() * .55D, 0D);
        if (!validMinimumRangeImpact(spec, castOrigin, impact, hitEntity)) return;
        if (spec.executionTraits().mechanics().contains(AbilityMechanic.DELAYED_PAYLOAD)) {
            armPayload(caster, spec, hitEntity, effectiveLevel, presentation);
        } else {
            AbilityTargeting.TargetSelection selection =
                    new AbilityTargeting.TargetSelection(List.of(hitEntity), List.of(), impact);
            AbilityContribution contribution = effects.applyProjectile(
                    caster,
                    projectile,
                    spec,
                    selection,
                    effectiveLevel);
            presentation.effects(selection.origin(), selection.enemies(), selection.allies());
            if (contribution.isMeaningful())
                semantics.recordContribution(caster, spec.id(), contribution);
        }
        impact.getWorld().spawnParticle(impactParticle(spec), impact,
                14, .3D, .3D, .3D, .04D);
    }

    private void launchSimulatedPiercingProjectile(
            Player caster,
            FixedAbilitySpec spec,
            LivingEntity primaryTarget,
            AbilityTargeting.TargetSelection impactSelection,
            int effectiveLevel,
            long delayTicks,
            BukkitClassAbilityPresentation.Session presentation) {
        long casterLifecycleToken = states.lifecycleToken(caster);
        Location start = caster.getEyeLocation().clone();
        Location destination = primaryTarget.getLocation().add(0D, primaryTarget.getHeight() * .55D, 0D);
        int travelTicks = Math.max(2, Math.min(18,
                (int) Math.ceil(start.distance(destination) / PROJECTILE_BLOCKS_PER_TICK)));
        BukkitTask launch = new BukkitRunnable() {
            private int age;
            private Location previous = start.clone();

            @Override
            public void run() {
                boolean current = currentCast(caster, casterLifecycleToken);
                if (!current || !caster.getWorld().equals(start.getWorld()) || age >= travelTicks) {
                    if (current && age >= travelTicks) impact();
                    BukkitClassAbilityEngine.this.cancelTracked(caster.getUniqueId(), this);
                    return;
                }
                age++;
                double progress = age / (double) travelTicks;
                Location next = lerp(start, destination, progress);
                if (!clearSegment(previous, next)) {
                    next.getWorld().spawnParticle(Particle.SMOKE, next, 8, .12D, .12D, .12D, .01D);
                    BukkitClassAbilityEngine.this.cancelTracked(caster.getUniqueId(), this);
                    return;
                }
                renderProjectile(spec, next, progress);
                previous = next;
            }

            private void impact() {
                if (!primaryTarget.isValid() || primaryTarget.isDead()
                        || !primaryTarget.getWorld().equals(destination.getWorld())
                        || primaryTarget.getLocation().distanceSquared(destination) > 10D) return;
                if (!validMinimumRangeImpact(spec, start, destination, primaryTarget)) return;
                if (spec.executionTraits().mechanics().contains(AbilityMechanic.DELAYED_PAYLOAD)) {
                    armPayload(caster, spec, primaryTarget, effectiveLevel, presentation);
                    return;
                }
                List<LivingEntity> currentTargets = impactSelection.enemies().stream()
                        .filter(target -> semantics.canTargetEnemy(caster, target, spec))
                        .filter(target -> validMinimumRangeImpact(
                                spec, start, target.getLocation(), target))
                        .toList();
                if (currentTargets.isEmpty()) return;
                AbilityContribution contribution = applyAndPresent(
                        presentation,
                        caster,
                        spec,
                        new AbilityTargeting.TargetSelection(
                                currentTargets, impactSelection.allies(), destination),
                        effectiveLevel);
                if (contribution.isMeaningful())
                    semantics.recordContribution(caster, spec.id(), contribution);
                destination.getWorld().spawnParticle(impactParticle(spec), destination,
                        14, .3D, .3D, .3D, .04D);
            }
        }.runTaskTimer(plugin, Math.max(1L, delayTicks), 1L);
        track(caster.getUniqueId(), launch);
    }

    private void launchChain(
            Player caster,
            FixedAbilitySpec spec,
            List<LivingEntity> candidates,
            int effectiveLevel,
            BukkitClassAbilityPresentation.Session presentation) {
        long casterLifecycleToken = states.lifecycleToken(caster);
        List<LivingEntity> chain = candidates.stream()
                .filter(entity -> entity != null && entity.isValid() && !entity.isDead())
                .limit(spec.tuning().projectileCount())
                .toList();
        if (chain.isEmpty()) return;
        BukkitTask task = new BukkitRunnable() {
            private int index;
            private Location previous = caster.getEyeLocation().clone();

            @Override
            public void run() {
                if (!currentCast(caster, casterLifecycleToken) || index >= chain.size()) {
                    BukkitClassAbilityEngine.this.cancelTracked(caster.getUniqueId(), this);
                    return;
                }
                LivingEntity target = chain.get(index++);
                if (!target.isValid() || target.isDead() || !target.getWorld().equals(caster.getWorld())) return;
                Location destination = target.getLocation().add(0D, target.getHeight() * .5D, 0D);
                if (!clearSegment(previous, destination)) return;
                renderArc(previous, destination, spec);
                AbilityContribution contribution = applyAndPresent(
                        presentation,
                        caster,
                        spec,
                        new AbilityTargeting.TargetSelection(List.of(target), List.of(), destination),
                        effectiveLevel);
                if (contribution.isMeaningful())
                    semantics.recordContribution(caster, spec.id(), contribution);
                previous = destination;
            }
        }.runTaskTimer(plugin, 1L, 3L);
        track(caster.getUniqueId(), task);
    }

    private void armPayload(
            Player caster,
            FixedAbilitySpec spec,
            LivingEntity attached,
            int effectiveLevel,
            BukkitClassAbilityPresentation.Session presentation) {
        long casterLifecycleToken = states.lifecycleToken(caster);
        BukkitTask fuse = new BukkitRunnable() {
            private int age;

            @Override
            public void run() {
                if (!currentCast(caster, casterLifecycleToken)
                        || !attached.isValid() || attached.isDead()
                        || !caster.getWorld().equals(attached.getWorld())) {
                    BukkitClassAbilityEngine.this.cancelTracked(caster.getUniqueId(), this);
                    return;
                }
                age += 2;
                Location anchor = attached.getLocation().add(0D, attached.getHeight() * .65D, 0D);
                anchor.getWorld().spawnParticle(Particle.FLAME, anchor, 2, .12D, .12D, .12D, .01D);
                if (age < 30) return;
                List<LivingEntity> enemies = anchor.getWorld()
                        .getNearbyEntities(anchor, Math.max(2D, spec.tuning().radius()),
                                Math.max(2D, spec.tuning().radius()), Math.max(2D, spec.tuning().radius()))
                        .stream()
                        .filter(LivingEntity.class::isInstance)
                        .map(LivingEntity.class::cast)
                        .filter(entity -> semantics.canTargetEnemy(caster, entity, spec))
                        .toList();
                AbilityContribution contribution = applyAndPresent(
                        presentation,
                        caster,
                        spec,
                        new AbilityTargeting.TargetSelection(enemies, List.of(), anchor),
                        effectiveLevel);
                if (contribution.isMeaningful())
                    semantics.recordContribution(caster, spec.id(), contribution);
                anchor.getWorld().spawnParticle(Particle.EXPLOSION, anchor, 3, 1D, .4D, 1D, .03D);
                anchor.getWorld().playSound(anchor, Sound.ENTITY_GENERIC_EXPLODE, 1F, 1.25F);
                BukkitClassAbilityEngine.this.cancelTracked(caster.getUniqueId(), this);
            }
        }.runTaskTimer(plugin, 1L, 2L);
        track(caster.getUniqueId(), fuse);
    }

    private void launchWoundUpZone(
            Player caster,
            FixedAbilitySpec spec,
            Location origin,
            int effectiveLevel,
            BukkitClassAbilityPresentation.Session presentation) {
        long casterLifecycleToken = states.lifecycleToken(caster);
        states.beginWindUp(caster, spec, 30);
        BukkitTask task = new BukkitRunnable() {
            private int age;

            @Override
            public void run() {
                boolean current = currentCast(caster, casterLifecycleToken);
                if (!current || age >= 30) {
                    if (current && age >= 30) {
                        AbilityTargeting.TargetSelection selection = targeting.select(caster, spec, origin);
                        AbilityContribution contribution = applyAndPresent(
                                presentation, caster, spec, selection, effectiveLevel);
                        if (contribution.isMeaningful())
                            semantics.recordContribution(caster, spec.id(), contribution);
                        origin.getWorld().spawnParticle(Particle.EXPLOSION, origin, 6,
                                spec.tuning().radius() * .5D, .3D,
                                spec.tuning().radius() * .5D, .03D);
                        origin.getWorld().playSound(origin, Sound.ENTITY_GENERIC_EXPLODE, 1.4F, .55F);
                    }
                    BukkitClassAbilityEngine.this.cancelTracked(caster.getUniqueId(), this);
                    return;
                }
                age += 2;
                double radius = Math.max(.5D, spec.tuning().radius() * age / 30D);
                origin.getWorld().spawnParticle(Particle.DUST_PLUME, origin, 18,
                        radius, .08D, radius, .01D);
            }
        }.runTaskTimer(plugin, 1L, 2L);
        track(caster.getUniqueId(), task);
    }

    private static Location lerp(Location start, Location end, double progress) {
        return start.clone().add(end.toVector().subtract(start.toVector()).multiply(progress));
    }

    private static boolean clearSegment(Location start, Location end) {
        if (start.getWorld() == null || end.getWorld() == null || !start.getWorld().equals(end.getWorld()))
            return false;
        Vector delta = end.toVector().subtract(start.toVector());
        double distance = delta.length();
        if (distance < 1.0E-6D) return true;
        org.bukkit.util.RayTraceResult collision = start.getWorld().rayTraceBlocks(
                start, delta.normalize(), distance, org.bukkit.FluidCollisionMode.NEVER, true);
        return collision == null || collision.getHitPosition() == null;
    }

    private static void renderProjectile(FixedAbilitySpec spec, Location point, double progress) {
        Particle primary = impactParticle(spec);
        point.getWorld().spawnParticle(primary, point, 2, .03D, .03D, .03D, .005D);
        if (spec.id().startsWith("spellcaster") || spec.id().startsWith("mage")
                || spec.id().startsWith("occultist") || spec.id().startsWith("necromancer")
                || spec.id().startsWith("lich"))
            point.getWorld().spawnParticle(Particle.ENCHANT, point, 3,
                    .08D, .08D + Math.sin(progress * Math.PI) * .08D, .08D, .01D);
    }

    private static void renderArc(Location start, Location end, FixedAbilitySpec spec) {
        for (int step = 1; step <= 10; step++) {
            double progress = step / 10D;
            Location point = lerp(start, end, progress);
            point.add(0D, Math.sin(progress * Math.PI) * .7D, 0D);
            point.getWorld().spawnParticle(impactParticle(spec), point, 2,
                    .02D, .02D, .02D, .005D);
        }
    }

    private static Particle impactParticle(FixedAbilitySpec spec) {
        if (spec.effects().contains(AbilityEffect.FEAR)) return Particle.SOUL_FIRE_FLAME;
        if (spec.effects().contains(AbilityEffect.LIFESTEAL)) return Particle.SOUL;
        if (spec.effects().contains(AbilityEffect.SLOW)) return Particle.SNOWFLAKE;
        if (spec.id().startsWith("spellcaster") || spec.id().startsWith("mage")
                || spec.id().startsWith("occultist") || spec.id().startsWith("necromancer")
                || spec.id().startsWith("lich")) return Particle.END_ROD;
        return Particle.CRIT;
    }

    private AbilityResult executeSummon(
            Player player,
            FixedAbilitySpec spec,
            int effectiveLevel,
            BukkitClassAbilityPresentation.Session presentation) {
        Location anchor = spec.target() == AbilityTarget.AIMED_LOCATION
                ? targeting.aimedLocation(player, spec.tuning().range())
                : player.getLocation();
        ClassMinionManager.SummonResult summon = classMinions.summon(
                player, spec, effectiveLevel, anchor);
        if (summon.status() != ClassMinionManager.Status.SUCCESS) {
            AbilityFailureReason failure = switch (summon.status()) {
                case NO_CORPSE -> AbilityFailureReason.NO_CORPSE;
                case UNSAFE_DESTINATION -> AbilityFailureReason.UNSAFE_DESTINATION;
                case RUNTIME_UNAVAILABLE -> AbilityFailureReason.ENGINE_CLOSED;
                case SUCCESS -> throw new IllegalStateException("Successful summon was handled as failure");
            };
            return AbilityResult.failure(spec.id(), failure);
        }

        Location origin = summon.origin();
        semantics.observe(new AbilityRuntimeObservation(
                AbilityRuntimeObservation.Kind.SUMMON_SPAWNED,
                player.getUniqueId(), null, spec.id(), 0D,
                Math.max(20, spec.tuning().durationTicks()), summon.minions().size(),
                spec.effects(), spec.executionTraits().mechanics()));
        presentation.effects(origin, List.of(), List.of());
        if (!spec.effects().contains(AbilityEffect.HEAL)
                && !spec.effects().contains(AbilityEffect.SHIELD)
                && !spec.effects().contains(AbilityEffect.ALLY_PROTECT)) {
            return AbilityResult.success(spec.id(), AbilityContribution.NONE);
        }

        int duration = Math.max(20, spec.tuning().durationTicks());
        int pulseCount = Math.max(1, spec.tuning().repetitions());
        int pulsePeriod = Math.max(2, duration / pulseCount);
        long casterLifecycleToken = states.lifecycleToken(player);
        BukkitTask task = new BukkitRunnable() {
            private int completed;

            @Override
            public void run() {
                if (!currentCast(player, casterLifecycleToken)
                        || completed >= pulseCount) {
                    BukkitClassAbilityEngine.this.cancelTracked(player.getUniqueId(), this);
                    return;
                }
                List<LivingEntity> live = summon.minions().stream()
                        .filter(Entity::isValid)
                        .filter(entity -> !entity.isDead())
                        .filter(entity -> entity.getWorld().equals(player.getWorld()))
                        .toList();
                if (live.isEmpty()) {
                    BukkitClassAbilityEngine.this.cancelTracked(player.getUniqueId(), this);
                    return;
                }
                for (LivingEntity servant : live) {
                    Location pulseOrigin = servant.getLocation();
                    AbilityTargeting.TargetSelection selection = targeting.select(
                            player, spec, pulseOrigin);
                    AbilityContribution pulse = applyAndPresent(
                            presentation, player, spec, selection, effectiveLevel);
                    if (pulse.isMeaningful()) {
                        semantics.recordContribution(player, spec.id(), pulse);
                    }
                }
                completed++;
            }
        }.runTaskTimer(plugin, 1L, pulsePeriod);
        track(player.getUniqueId(), task);
        return AbilityResult.scheduled(spec.id(), AbilityContribution.NONE);
    }

    private AbilityResult executeMountedCharge(
            Player player,
            FixedAbilitySpec spec,
            int effectiveLevel,
            BukkitClassAbilityPresentation.Session presentation) {
        Set<UUID> struck = new HashSet<>();
        Optional<Horse> summoned = divineSteeds.summon(
                player,
                spec.tuning().durationTicks(),
                horse -> applyMountedChargeContacts(
                        player, horse, spec, effectiveLevel, struck, presentation));
        if (summoned.isEmpty())
            return AbilityResult.failure(spec.id(), AbilityFailureReason.PATH_BLOCKED);

        Location origin = summoned.get().getLocation();
        presentation.mobility(origin, origin);
        origin.getWorld().spawnParticle(Particle.END_ROD, origin.clone().add(0D, 1D, 0D),
                16, .65D, .55D, .65D, .025D);
        origin.getWorld().playSound(origin, Sound.ENTITY_HORSE_ARMOR, 1F, 1.2F);
        return AbilityResult.scheduled(spec.id(), AbilityContribution.NONE);
    }

    private void applyMountedChargeContacts(
            Player player,
            Horse horse,
            FixedAbilitySpec spec,
            int effectiveLevel,
            Set<UUID> struck,
            BukkitClassAbilityPresentation.Session presentation) {
        double contactRadius = Math.max(.75D, spec.tuning().radius());
        for (Entity entity : horse.getNearbyEntities(
                contactRadius, Math.max(1D, contactRadius), contactRadius)) {
            if (!(entity instanceof LivingEntity enemy)
                    || !semantics.canTargetEnemy(player, enemy, spec)
                    || !targeting.unobstructedFrom(horse.getLocation(), horse.getHeight(), enemy)
                    || !struck.add(enemy.getUniqueId())) continue;
            Location impactLocation = enemy.getLocation().add(0D, enemy.getHeight() * .5D, 0D);
            AbilityContribution impact = applyAndPresent(
                    presentation,
                    player,
                    spec,
                    new AbilityTargeting.TargetSelection(List.of(enemy), List.of(), impactLocation),
                    effectiveLevel);
            if (impact.isMeaningful()) semantics.recordContribution(player, spec.id(), impact);
            impactLocation.getWorld().spawnParticle(
                    Particle.CRIT, impactLocation, 10, .35D, .35D, .35D, .08D);
            impactLocation.getWorld().playSound(
                    impactLocation, Sound.ENTITY_HORSE_GALLOP, .8F, .8F);
        }
    }

    private AbilityResult executeDash(
            Player player,
            FixedAbilitySpec spec,
            int effectiveLevel,
            BukkitClassAbilityPresentation.Session presentation) {
        double clearDistance = SafeMovement.physicsDashClearDistance(player, spec.tuning().range());
        if (clearDistance < .5D)
            return AbilityResult.failure(spec.id(), AbilityFailureReason.PATH_BLOCKED);
        Vector direction = player.getEyeLocation().getDirection().setY(0);
        if (direction.lengthSquared() < 1.0E-6)
            return AbilityResult.failure(spec.id(), AbilityFailureReason.PATH_BLOCKED);
        Vector previousVelocity = player.getVelocity();
        Vector dashVelocity = direction.normalize()
                .multiply(Math.min(1.45D, .45D + clearDistance * .12D))
                .setY(Math.max(0D, previousVelocity.getY()));
        states.applyOwnedVelocity(player, dashVelocity);
        Location origin = player.getLocation().clone();
        presentation.mobility(origin, origin);
        player.getWorld().spawnParticle(Particle.CLOUD, player.getLocation(), 12, .3, .2, .3, .06);
        return AbilityResult.success(spec.id(), movement(clearDistance));
    }

    private AbilityResult executeBlink(
            Player player,
            FixedAbilitySpec spec,
            int effectiveLevel,
            BukkitClassAbilityPresentation.Session presentation) {
        Optional<BlinkPlanner.Plan> planned = BlinkPlanner.plan(player, spec.tuning().range());
        if (planned.isEmpty())
            return AbilityResult.failure(spec.id(), AbilityFailureReason.PATH_BLOCKED);

        BlinkPlanner.Plan plan = planned.get();
        if (!BlinkPlanner.stillValid(player, plan))
            return AbilityResult.failure(spec.id(), AbilityFailureReason.PATH_BLOCKED);

        Location origin = player.getLocation().clone();
        Location destination = plan.destination();
        if (!InstancePlayerMovement.teleportWithinWorld(
                player, destination, PlayerTeleportEvent.TeleportCause.PLUGIN)) {
            if (!rollbackUnexpectedMovement(player, origin))
                return committedUnexpectedMovement(spec.id(), origin, player.getLocation());
            return AbilityResult.failure(spec.id(), AbilityFailureReason.PATH_BLOCKED);
        }

        Location actual = player.getLocation();
        if (!arrivedAt(actual, destination)
                || !SafeMovement.safeStandingPlayerVolume(player, actual)) {
            if (!rollbackUnexpectedMovement(player, origin))
                return committedUnexpectedMovement(spec.id(), origin, player.getLocation());
            return AbilityResult.failure(spec.id(), AbilityFailureReason.UNSAFE_DESTINATION);
        }

        states.applyOwnedVelocity(player, new Vector());
        player.setFallDistance(0F);
        presentation.mobility(origin, actual);
        double distanceSquared = origin.distanceSquared(actual);
        if (BlinkPotionFlashPolicy.shouldFlash(true, distanceSquared)) {
            blinkPotionFlash.flash(player);
        }
        return AbilityResult.success(spec.id(), movement(Math.sqrt(distanceSquared)));
    }

    private AbilityResult executeAllyFlight(
            Player player,
            FixedAbilitySpec spec,
            int effectiveLevel,
            BukkitClassAbilityPresentation.Session presentation) {
        Location start = player.getLocation().clone();
        Optional<Player> aimedAlly = targeting.aimedAlly(player, spec);
        boolean started;
        if (aimedAlly.isPresent()) {
            Player ally = aimedAlly.get();
            long allyLifecycleToken = states.lifecycleToken(ally);
            started = guardianFlights.startToAlly(
                    player,
                    ally,
                    spec.tuning().range(),
                    spec.tuning().durationTicks(),
                    (caster, candidate) -> isCurrentChainAlly(
                            caster, candidate, allyLifecycleToken),
                    result -> completeAllyFlight(
                            player, spec, effectiveLevel, presentation,
                            result, allyLifecycleToken));
        } else {
            SanctuaryAnchor anchor = sanctuaryAnchors.get(player.getUniqueId());
            if (anchor == null || anchor.expiresAtNanos() <= System.nanoTime()
                    || !anchor.location().getWorld().equals(player.getWorld())) {
                sanctuaryAnchors.remove(player.getUniqueId());
                return AbilityResult.failure(spec.id(), AbilityFailureReason.NO_VALID_TARGET);
            }
            if (player.getLocation().distance(anchor.location()) > spec.tuning().range())
                return AbilityResult.failure(spec.id(), AbilityFailureReason.NO_VALID_TARGET);
            started = guardianFlights.startToAnchor(
                    player,
                    anchor.location(),
                    spec.tuning().range(),
                    spec.tuning().durationTicks(),
                    result -> completeAllyFlight(
                            player, spec, effectiveLevel, presentation,
                            result, null));
        }

        if (!started)
            return AbilityResult.failure(spec.id(), AbilityFailureReason.PATH_BLOCKED);
        presentation.mobility(start, start);
        return AbilityResult.scheduled(spec.id(), AbilityContribution.NONE);
    }

    private void completeAllyFlight(
            Player player,
            FixedAbilitySpec spec,
            int effectiveLevel,
            BukkitClassAbilityPresentation.Session presentation,
            GuardianFlightManager.FlightResult result,
            Long allyLifecycleToken) {
        if (closed) return;
        presentation.mobility(result.start(), result.end());
        AbilityContribution contribution = movement(result.travelledDistance());
        if (result.arrived()) {
            Player ally = result.ally();
            List<Player> recipients;
            if (ally == null) {
                recipients = List.of(player);
            } else if (allyLifecycleToken != null
                    && isCurrentChainAlly(player, ally, allyLifecycleToken)) {
                recipients = List.of(ally);
            } else {
                recipients = List.of();
            }
            if (!recipients.isEmpty()) {
                AbilityContribution support = applyAndPresent(
                        presentation,
                        player,
                        spec,
                        new AbilityTargeting.TargetSelection(
                                List.of(), recipients, result.end()),
                        effectiveLevel);
                contribution = contribution.plus(support);
            }
        }
        if (contribution.isMeaningful())
            semantics.recordContribution(player, spec.id(), contribution);
    }

    private AbilityResult executeLeap(
            Player player,
            FixedAbilitySpec spec,
            int effectiveLevel,
            BukkitClassAbilityPresentation.Session presentation) {
        Location start = player.getLocation().clone();
        Vector horizontal = player.getEyeLocation().getDirection().setY(0D);
        if (horizontal.lengthSquared() < 1.0E-8D) {
            double yaw = Math.toRadians(start.getYaw());
            horizontal = new Vector(-Math.sin(yaw), 0D, Math.cos(yaw));
        }
        horizontal.normalize().multiply(Math.max(
                LEAP_MINIMUM_HORIZONTAL_VELOCITY,
                spec.tuning().displacement()));
        Vector launchVelocity = horizontal.setY(LEAP_VERTICAL_VELOCITY);
        states.applyOwnedVelocity(player, launchVelocity);
        player.setFallDistance(0F);
        presentation.mobility(start, start);
        player.getWorld().spawnParticle(Particle.CLOUD, start.clone().add(0D, .15D, 0D),
                14, .35D, .1D, .35D, .06D);

        long casterLifecycleToken = states.lifecycleToken(player);
        BukkitTask task = new BukkitRunnable() {
            private int age;
            private boolean airborne;

            @Override
            public void run() {
                if (!currentCast(player, casterLifecycleToken)
                        || !player.getWorld().equals(start.getWorld())) {
                    BukkitClassAbilityEngine.this.cancelTracked(player.getUniqueId(), this);
                    return;
                }
                age++;
                airborne |= !player.isOnGround();
                player.setFallDistance(0F);
                player.getWorld().spawnParticle(Particle.CLOUD, player.getLocation(),
                        2, .16D, .12D, .16D, .01D);
                if ((airborne && player.isOnGround())
                        || (!airborne && age >= 4)
                        || age >= LEAP_LANDING_TIMEOUT_TICKS) {
                    completeLeap(player, spec, effectiveLevel, start, presentation);
                    BukkitClassAbilityEngine.this.cancelTracked(player.getUniqueId(), this);
                }
            }
        }.runTaskTimer(plugin, 1L, 1L);
        track(player.getUniqueId(), task);
        return AbilityResult.scheduled(spec.id(), AbilityContribution.NONE);
    }

    private void completeLeap(
            Player player,
            FixedAbilitySpec spec,
            int effectiveLevel,
            Location start,
            BukkitClassAbilityPresentation.Session presentation) {
        Location landing = player.getLocation().clone();
        player.setFallDistance(0F);
        AbilityTargeting.TargetSelection selection = targeting.select(player, spec, landing);
        AbilityContribution impact = applyAndPresent(
                presentation, player, spec, selection, effectiveLevel)
                .plus(movement(horizontalDistance(start, landing)));
        if (impact.isMeaningful()) semantics.recordContribution(player, spec.id(), impact);
        presentation.mobility(start, landing);
        long terrainSeed = player.getUniqueId().getMostSignificantBits()
                ^ player.getUniqueId().getLeastSignificantBits()
                ^ player.getWorld().getGameTime();
        TerrainImpacts.show(new TerrainImpactRequest(
                landing,
                spec.tuning().radius(),
                1D,
                spec.tuning().durationTicks(),
                48D,
                terrainSeed));
        player.getWorld().spawnParticle(Particle.EXPLOSION, landing, 3, 1.2, .2, 1.2, .02);
        player.getWorld().playSound(landing, Sound.ENTITY_GENERIC_EXPLODE, 1F, .75F);
    }

    private static double horizontalDistance(Location first, Location second) {
        if (first.getWorld() == null || second.getWorld() == null
                || !first.getWorld().equals(second.getWorld())) return 0D;
        double x = first.getX() - second.getX();
        double z = first.getZ() - second.getZ();
        return Math.sqrt(x * x + z * z);
    }

    private static boolean validMinimumRangeImpact(
            FixedAbilitySpec spec,
            Location castOrigin,
            Location impact,
            LivingEntity actualTarget) {
        if (castOrigin.getWorld() == null || impact.getWorld() == null
                || !castOrigin.getWorld().equals(impact.getWorld())
                || !castOrigin.getWorld().equals(actualTarget.getWorld())) return false;
        double minimumRange = Math.max(8D, spec.tuning().range() * .25D);
        return AbilityActivationPolicy.permitsImpact(
                spec.executionTraits().mechanics(),
                castOrigin.distance(impact),
                castOrigin.distance(actualTarget.getLocation()),
                minimumRange);
    }

    private AbilityContribution applyAndPresent(
            BukkitClassAbilityPresentation.Session presentation,
            Player caster,
            FixedAbilitySpec spec,
            AbilityTargeting.TargetSelection selection,
            int effectiveLevel) {
        AbilityContribution contribution = effects.apply(caster, spec, selection, effectiveLevel);
        presentation.effects(selection.origin(), selection.enemies(), selection.allies());
        return contribution;
    }

    private AbilityContribution applyPeriodicAndPresent(
            BukkitClassAbilityPresentation.Session presentation,
            Player caster,
            FixedAbilitySpec spec,
            AbilityTargeting.TargetSelection selection,
            int effectiveLevel) {
        AbilityContribution contribution = effects.applyPeriodic(
                caster, spec, selection, effectiveLevel);
        semantics.observe(new AbilityRuntimeObservation(
                AbilityRuntimeObservation.Kind.FIELD_PULSE,
                caster.getUniqueId(), null, spec.id(),
                contribution.damage() + contribution.effectiveHealing(),
                spec.tuning().durationTicks(),
                selection.enemies().size() + selection.allies().size(),
                spec.effects(), spec.executionTraits().mechanics()));
        presentation.effects(selection.origin(), selection.enemies(), selection.allies());
        return contribution;
    }

    private AbilityContribution applyProjectileAndPresent(
            BukkitClassAbilityPresentation.Session presentation,
            Player caster,
            FixedAbilitySpec spec,
            AbilityTargeting.TargetSelection selection,
            int effectiveLevel) {
        AbilityContribution contribution = effects.applyProjectileEffect(
                caster, spec, selection, effectiveLevel);
        presentation.effects(selection.origin(), selection.enemies(), selection.allies());
        return contribution;
    }

    private static boolean hasApplicableTarget(FixedAbilitySpec spec, AbilityTargeting.TargetSelection selection) {
        boolean enemyEffect = spec.effects().stream().anyMatch(effect -> switch (effect) {
            case DAMAGE, KNOCKBACK, PULL, LAUNCH, SLOW, WEAKEN, GLOW, PARTY_DAMAGE_MARK, TAUNT, INTERRUPT, FEAR, ROOT, BURN -> true;
            default -> false;
        });
        boolean allyEffect = spec.effects().stream().anyMatch(effect -> switch (effect) {
            case HEAL, SHIELD, CLEANSE, SELF_PROTECT, ALLY_PROTECT, SPEED, STRENGTH,
                    SPELL_STRENGTH, SELF_VULNERABLE -> true;
            default -> false;
        });
        return (enemyEffect && !selection.enemies().isEmpty())
                || (allyEffect && !selection.allies().isEmpty())
                || (!enemyEffect && !allyEffect);
    }

    private static AbilityDefinition abilityFor(ClassLineage lineage, AbilitySlot slot) {
        return switch (slot) {
            case MOBILITY -> lineage.mobility();
            case SIGNATURE -> lineage.signature();
            case UTILITY -> lineage.utility();
        };
    }

    private static AbilityContribution movement(double distance) {
        return new AbilityContribution(0, 0, 0, 0, 0, 0, Math.max(0D, distance));
    }

    private static boolean arrivedAt(Location actual, Location expected) {
        return actual.getWorld() != null
                && expected.getWorld() != null
                && actual.getWorld().equals(expected.getWorld())
                && actual.distanceSquared(expected) <= .05D * .05D;
    }

    private static boolean rollbackUnexpectedMovement(Player player, Location origin) {
        if (arrivedAt(player.getLocation(), origin)) return true;
        return InstancePlayerMovement.teleportWithinWorld(
                player, origin, PlayerTeleportEvent.TeleportCause.PLUGIN)
                && arrivedAt(player.getLocation(), origin);
    }

    /**
     * A third-party teleport listener moved the player somewhere other than the authorized
     * destination and refused the rollback. The cast must still commit so the displacement can
     * never be repeated for free.
     */
    private static AbilityResult committedUnexpectedMovement(
            String abilityId,
            Location origin,
            Location actual) {
        double distance = origin.getWorld() != null
                && actual.getWorld() != null
                && origin.getWorld().equals(actual.getWorld())
                ? origin.distance(actual)
                : 0D;
        return AbilityResult.success(abilityId, movement(distance));
    }

    private static double healthFraction(Player player) {
        org.bukkit.attribute.AttributeInstance maximum =
                player.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH);
        double max = maximum == null ? Math.max(1D, player.getHealth()) : Math.max(1D, maximum.getValue());
        return player.getHealth() / max;
    }

    private static long expiresAt(int durationTicks) {
        long now = System.nanoTime();
        long duration = durationTicks * 50_000_000L;
        return Long.MAX_VALUE - now < duration ? Long.MAX_VALUE : now + duration;
    }

    private boolean currentCast(Player caster, long lifecycleToken) {
        boolean runtimeEligible = !closed
                && caster != null
                && caster.isOnline()
                && caster.isValid()
                && !caster.isDead()
                && ClassAbilityEligibility.isEligible(caster);
        if (caster == null) return false;
        long currentGeneration = states.lifecycleToken(caster);
        return runtimeEligible && lifecycleToken == currentGeneration;
    }

    private void presentConstruct(Player player, FixedAbilitySpec spec, Location origin) {
        try {
            constructs.present(player, spec, origin);
        } catch (RuntimeException exception) {
            plugin.getLogger().log(Level.FINE,
                    "Could not display packet-only class construct for " + spec.id(), exception);
        }
    }

    private static Set<AbilityMechanic> constructMechanics(
            ClassConstructVisualRegistry.AnchorMode anchorMode) {
        return anchorMode == ClassConstructVisualRegistry.AnchorMode.FOLLOW_CASTER
                ? Set.of(AbilityMechanic.FOLLOW_CASTER_FIELD)
                : Set.of();
    }

    private void track(UUID casterId, BukkitTask task) {
        tasksByCaster.computeIfAbsent(casterId, ignored -> new HashSet<>()).add(task);
    }

    private void cancelTracked(UUID casterId, BukkitRunnable runnable) {
        int taskId = runnable.getTaskId();
        runnable.cancel();
        Set<BukkitTask> casterTasks = tasksByCaster.get(casterId);
        if (casterTasks == null) return;
        casterTasks.removeIf(task -> task.getTaskId() == taskId);
        if (casterTasks.isEmpty()) tasksByCaster.remove(casterId);
    }

    @Override
    public void deactivate(Player player) {
        Objects.requireNonNull(player, "player");
        UUID casterId = player.getUniqueId();
        Set<BukkitTask> casterTasks = tasksByCaster.remove(casterId);
        if (casterTasks != null)
            for (BukkitTask task : new ArrayList<>(casterTasks)) task.cancel();
        sanctuaryAnchors.remove(casterId);
        guardianFlights.cancel(player);
        classMinions.deactivate(player);
        blinkPotionFlash.deactivate(player);
        constructs.deactivate(player);
        divineSteeds.deactivate(player);
        projectileCarriers.deactivate(player);
        frenzy.clear(player);
        effects.clearSource(casterId);
        modifiers.clearSource(casterId);
        modifiers.clearTarget(casterId);
        states.clearSource(casterId);
        semantics.clearSource(player);
    }

    @Override
    public void close() {
        if (closed) return;
        closed = true;
        for (Set<BukkitTask> casterTasks : new ArrayList<>(tasksByCaster.values()))
            for (BukkitTask task : new ArrayList<>(casterTasks)) task.cancel();
        tasksByCaster.clear();
        sanctuaryAnchors.clear();
        try {
            controlAttributionLease.close();
        } catch (Exception ignored) {
        }
        guardianFlights.close();
        divineSteeds.close();
        projectileCarriers.close();
        classMinions.close();
        blinkPotionFlash.close();
        constructs.close();
        frenzy.close();
        effects.close();
        states.close();
        modifiers.close();
    }

    @FunctionalInterface
    private interface AbilityExecutor {
        AbilityResult execute(
                Player player,
                FixedAbilitySpec spec,
                int effectiveLevel,
                BukkitClassAbilityPresentation.Session presentation);
    }

    private record SanctuaryAnchor(Location location, long expiresAtNanos) {
        private SanctuaryAnchor {
            location = location.clone();
        }
    }
}
