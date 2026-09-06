package com.magmaguy.elitemobs.experimentalcombat.abilities;

import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.api.PlayerDamagedByEliteMobEvent;
import com.magmaguy.elitemobs.combatsystem.CombatDamageContext;
import com.magmaguy.elitemobs.experimentalcombat.ClassAbilityEligibility;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerVelocityEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Owns the stateful combat contracts that cannot be represented by one immediate Bukkit effect.
 * Links, guards and windows all use the same source-scoped lifecycle so changing class or leaving
 * Experimental Combat cannot leak a benefit into normal gameplay.
 */
final class AbilityStateRuntime implements Listener, AutoCloseable {
    private static final double REDIRECT_FRACTION = .35D;
    private static final double REDIRECT_EFFICIENCY = .70D;
    private static final double SHARE_FRACTION = .30D;
    private static final Set<PotionEffectType> CONTROL_EFFECTS = Set.of(
            PotionEffectType.SLOWNESS,
            PotionEffectType.LEVITATION,
            PotionEffectType.MINING_FATIGUE,
            PotionEffectType.BLINDNESS,
            PotionEffectType.DARKNESS);
    private static final Set<PotionEffectType> DEBUFF_EFFECTS = Set.of(
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
            PotionEffectType.UNLUCK);
    private static final long RECENT_ATTACKER_TICKS = 160L;
    private final Plugin plugin;
    private final AbilitySemantics semantics;
    private final FixedAbilityRegistry registry;
    private final Map<UUID, List<RedirectLink>> redirectsByTarget = new HashMap<>();
    private final Map<UUID, DeathGuard> deathGuards = new HashMap<>();
    private final Map<UUID, TriggeredBarrier> triggeredBarriers = new HashMap<>();
    private final Map<UUID, TriggeredProtection> triggeredProtections = new HashMap<>();
    private final Map<UUID, ShareLink> sharesByMember = new HashMap<>();
    private final Map<UUID, TimedSource> lifestealWindows = new HashMap<>();
    private final Map<UUID, TimedSource> controlImmunity = new HashMap<>();
    private final Map<UUID, TimedSource> debuffImmunity = new HashMap<>();
    private final Map<UUID, PlantedGuard> plantedGuards = new HashMap<>();
    private final Map<UUID, DetonatingMark> detonatingMarks = new HashMap<>();
    private final Map<UUID, RetaliationBudget> storedRetaliation = new HashMap<>();
    private final Map<UUID, Map<UUID, Long>> recentAttackers = new HashMap<>();
    private final Map<UUID, Map<UUID, Long>> tauntsByCaster = new HashMap<>();
    private final Map<UUID, Map<UUID, Long>> defenseBreaksByCaster = new HashMap<>();
    private final Map<UUID, TimedSource> activeFields = new HashMap<>();
    private final Map<UUID, TimedSource> activeWindUps = new HashMap<>();
    private final Map<UUID, PendingHealEcho> pendingHealEchoes = new HashMap<>();
    private final Map<UUID, Long> lifecycleGenerations = new HashMap<>();
    private final Set<UUID> redistributing = new HashSet<>();
    private final Set<UUID> ownedVelocities = new HashSet<>();
    private final AbilityStateOwnership stateOwnership;
    private boolean closed;

    AbilityStateRuntime(
            Plugin plugin,
            AbilitySemantics semantics,
            FixedAbilityRegistry registry) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.semantics = Objects.requireNonNull(semantics, "semantics");
        this.registry = Objects.requireNonNull(registry, "registry");
        this.stateOwnership = buildStateOwnership();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    boolean canActivate(Player caster, FixedAbilitySpec spec, AbilityTargeting.TargetSelection selection) {
        Set<AbilityMechanic> mechanics = spec.executionTraits().mechanics();
        AbilityTargeting.TargetSelection qualified = qualifyTargets(caster, spec, selection);
        double nearestEnemyDistance = qualified.enemies().stream()
                .mapToDouble(enemy -> enemy.getLocation().distance(caster.getLocation()))
                .min()
                .orElse(Double.POSITIVE_INFINITY);
        double lowestAllyHealth = qualified.allies().stream()
                .mapToDouble(AbilityStateRuntime::healthFraction)
                .min()
                .orElse(Double.POSITIVE_INFINITY);
        double minimumRange = Math.max(8D, spec.tuning().range() * .25D);
        boolean moving = caster.isSprinting()
                || caster.getVelocity().getX() * caster.getVelocity().getX()
                + caster.getVelocity().getZ() * caster.getVelocity().getZ() > .01D;
        if (!AbilityActivationPolicy.permits(
                mechanics, moving, nearestEnemyDistance, minimumRange, lowestAllyHealth)) return false;
        if ((mechanics.contains(AbilityMechanic.DAMAGE_REDIRECT)
                || mechanics.contains(AbilityMechanic.MULTI_ALLY_REDIRECT))
                && qualified.allies().stream().noneMatch(
                ally -> !ally.getUniqueId().equals(caster.getUniqueId()))) return false;
        if (mechanics.contains(AbilityMechanic.RECENT_ATTACKERS)
                && qualified.enemies().isEmpty()) return false;
        if (mechanics.contains(AbilityMechanic.TAUNTED_TARGETS_ONLY)
                && qualified.enemies().isEmpty()) return false;
        if (mechanics.contains(AbilityMechanic.REQUIRES_DEFENSE_BREAK)
                && qualified.enemies().isEmpty()) return false;
        long now = System.nanoTime();
        if (mechanics.contains(AbilityMechanic.REQUIRES_ACTIVE_FIELD)
                && live(activeFields, caster.getUniqueId(), now) == null) return false;
        return !mechanics.contains(AbilityMechanic.REQUIRES_WIND_UP)
                || live(activeWindUps, caster.getUniqueId(), now) != null;
    }

    AbilityTargeting.TargetSelection qualifyTargets(
            Player caster,
            FixedAbilitySpec spec,
            AbilityTargeting.TargetSelection selection) {
        Set<AbilityMechanic> mechanics = spec.executionTraits().mechanics();
        List<LivingEntity> enemies = new ArrayList<>(selection.enemies());
        List<Player> allies = new ArrayList<>(selection.allies());
        long now = System.nanoTime();

        if (mechanics.contains(AbilityMechanic.RECENT_ATTACKERS)) {
            Player defended = allies.stream()
                    .filter(ally -> !ally.getUniqueId().equals(caster.getUniqueId()))
                    .findFirst()
                    .orElse(caster);
            enemies = new ArrayList<>(recentAttackers(defended, now).stream()
                    .filter(enemy -> enemy.getWorld().equals(caster.getWorld()))
                    .filter(enemy -> semantics.canTargetEnemy(caster, enemy, spec))
                    .filter(enemy -> withinAbilityReach(enemy, selection.origin(), spec))
                    .toList());
        }
        if (mechanics.contains(AbilityMechanic.TAUNTED_TARGETS_ONLY))
            enemies.removeIf(enemy -> !hasLiveStatus(
                    tauntsByCaster, caster.getUniqueId(), enemy.getUniqueId(), now));
        if (mechanics.contains(AbilityMechanic.REQUIRES_DEFENSE_BREAK))
            enemies.removeIf(enemy -> !hasLiveStatus(
                    defenseBreaksByCaster, caster.getUniqueId(), enemy.getUniqueId(), now));
        if (mechanics.contains(AbilityMechanic.LOW_HEALTH_ALLY_ONLY))
            allies.removeIf(ally -> healthFraction(ally) > AbilityActivationPolicy.LOW_HEALTH_ALLY_FRACTION);
        if (mechanics.contains(AbilityMechanic.CASTER_ONLY_FIELD))
            allies.removeIf(ally -> !ally.getUniqueId().equals(caster.getUniqueId()));
        if (mechanics.contains(AbilityMechanic.CASTER_ONLY_SUPPORT))
            allies.removeIf(ally -> !ally.getUniqueId().equals(caster.getUniqueId()));

        return new AbilityTargeting.TargetSelection(enemies, allies, selection.origin());
    }

    void activate(Player caster, FixedAbilitySpec spec,
                  AbilityTargeting.TargetSelection selection, int effectiveLevel) {
        if (closed) return;
        Set<AbilityMechanic> mechanics = spec.executionTraits().mechanics();
        int durationTicks = Math.max(20, ActiveAbilityLevelScaling.durationTicks(
                spec.tuning().durationTicks(), effectiveLevel));
        long expires = expiresAt(durationTicks);
        double levelScale = AbilityLevelScaling.multiplier(effectiveLevel);
        AbilityMechanicModifiers mechanicModifiers = semantics.mechanicModifiers(caster);
        double burstHealingMultiplier = semantics.healingMultiplier(caster)
                * AbilityHealingPolicy.multiplier(
                mechanicModifiers, AbilityHealingType.BURST, selection.enemies().size());

        if (mechanics.contains(AbilityMechanic.FOLLOW_CASTER_FIELD)) {
            activeFields.compute(caster.getUniqueId(), (ignored, current) ->
                    current != null && current.expiresAtNanos() > System.nanoTime()
                            ? current
                            : new TimedSource(caster.getUniqueId(), expires, spec.id()));
            observeStateArmed(caster, caster, spec, AbilityMechanic.FOLLOW_CASTER_FIELD,
                    spec.tuning().radius(), durationTicks);
        }

        if (mechanics.contains(AbilityMechanic.DAMAGE_REDIRECT)
                || mechanics.contains(AbilityMechanic.MULTI_ALLY_REDIRECT)) {
            int maximum = mechanics.contains(AbilityMechanic.MULTI_ALLY_REDIRECT) ? 2 : 1;
            AbilityMechanic redirectMechanic = maximum > 1
                    ? AbilityMechanic.MULTI_ALLY_REDIRECT
                    : AbilityMechanic.DAMAGE_REDIRECT;
            List<Player> redirectedAllies = selection.allies().stream()
                    .filter(ally -> !ally.getUniqueId().equals(caster.getUniqueId()))
                    .sorted(Comparator.comparingDouble(AbilityStateRuntime::healthFraction))
                    .limit(maximum)
                    .toList();
            for (Player ally : redirectedAllies) {
                redirectsByTarget.computeIfAbsent(
                                ally.getUniqueId(), ignored -> new ArrayList<>())
                        .add(new RedirectLink(caster.getUniqueId(), REDIRECT_FRACTION,
                                REDIRECT_EFFICIENCY,
                                mechanicModifiers.redirectedDamageMultiplier(),
                                expires, spec.id(), redirectMechanic));
                observeStateArmed(caster, ally, spec, redirectMechanic,
                        REDIRECT_FRACTION, durationTicks);
            }
        }

        if (mechanics.contains(AbilityMechanic.DEATH_GUARD)) {
            List<Player> targets = selection.allies().isEmpty() ? List.of(caster) : selection.allies();
            for (Player target : targets) {
                DeathGuard guard = new DeathGuard(
                        caster.getUniqueId(),
                        spec.tuning().healingFraction() * levelScale * burstHealingMultiplier,
                        spec.tuning().shieldFraction() * levelScale
                                * mechanicModifiers.shieldStrengthMultiplier(),
                        mechanics.contains(AbilityMechanic.WARD_BREAK_SIGNAL),
                        Math.max(20, Math.min(200, durationTicks)),
                        expires, spec.id());
                deathGuards.put(target.getUniqueId(), guard);
                double projectedRecovery = maximumHealth(target)
                        * (guard.healingFraction() + guard.shieldFraction());
                observeStateArmed(caster, target, spec, AbilityMechanic.DEATH_GUARD,
                        projectedRecovery, durationTicks);
            }
        }

        if (mechanics.contains(AbilityMechanic.BARRIER_BREAK_HEAL)) {
            for (Player target : selection.allies()) {
                TriggeredBarrier barrier = new TriggeredBarrier(
                        caster.getUniqueId(), spec.tuning().healingFraction() * levelScale
                                * burstHealingMultiplier,
                        target.getAbsorptionAmount(),
                        maximumHealth(target) * spec.tuning().shieldFraction() * levelScale
                                * mechanicModifiers.shieldStrengthMultiplier(),
                        expires, spec.id());
                triggeredBarriers.put(target.getUniqueId(), barrier);
                observeStateArmed(caster, target, spec, AbilityMechanic.BARRIER_BREAK_HEAL,
                        maximumHealth(target) * barrier.healingFraction(), durationTicks);
            }
        }

        if (mechanics.contains(AbilityMechanic.THREAT_TRIGGERED_PROTECTION)) {
            for (Player target : selection.allies()) {
                TriggeredProtection protection = new TriggeredProtection(
                        caster.getUniqueId(),
                        maximumHealth(target) * spec.tuning().shieldFraction() * levelScale
                                * mechanicModifiers.shieldStrengthMultiplier(),
                        expires, spec.id());
                triggeredProtections.put(target.getUniqueId(), protection);
                observeStateArmed(caster, target, spec,
                        AbilityMechanic.THREAT_TRIGGERED_PROTECTION,
                        protection.protectionAmount(), durationTicks);
            }
        }

        if (mechanics.contains(AbilityMechanic.DAMAGE_SHARE)) {
            List<UUID> members = selection.allies().stream()
                    .filter(Player::isOnline)
                    .map(Player::getUniqueId)
                    .distinct()
                    .toList();
            if (members.size() > 1) {
                ShareLink link = new ShareLink(caster.getUniqueId(), members, SHARE_FRACTION,
                        expires, spec.id());
                for (UUID member : members) {
                    sharesByMember.put(member, link);
                    observeStateArmed(caster.getUniqueId(), member, spec,
                            AbilityMechanic.DAMAGE_SHARE, SHARE_FRACTION, durationTicks);
                }
            }
        }

        if (mechanics.contains(AbilityMechanic.LIFESTEAL_WINDOW)) {
            lifestealWindows.put(caster.getUniqueId(), new TimedSource(
                    caster.getUniqueId(), expires, spec.id()));
            observeStateArmed(caster, caster, spec, AbilityMechanic.LIFESTEAL_WINDOW,
                    .16D, durationTicks);
        }

        if (mechanics.contains(AbilityMechanic.CONTROL_IMMUNITY)) {
            List<Player> targets = selection.allies().isEmpty() ? List.of(caster) : selection.allies();
            for (Player target : targets) {
                controlImmunity.put(target.getUniqueId(), new TimedSource(
                        caster.getUniqueId(), expires, spec.id()));
                CONTROL_EFFECTS.forEach(target::removePotionEffect);
                observeStateArmed(caster, target, spec, AbilityMechanic.CONTROL_IMMUNITY,
                        1D, durationTicks);
            }
        }

        if (mechanics.contains(AbilityMechanic.DEBUFF_IMMUNITY)) {
            List<Player> targets = selection.allies().isEmpty() ? List.of(caster) : selection.allies();
            for (Player target : targets) {
                debuffImmunity.put(target.getUniqueId(), new TimedSource(
                        caster.getUniqueId(), expires, spec.id()));
                DEBUFF_EFFECTS.forEach(target::removePotionEffect);
                observeStateArmed(caster, target, spec, AbilityMechanic.DEBUFF_IMMUNITY,
                        1D, durationTicks);
            }
        }

        if (mechanics.contains(AbilityMechanic.PLANTED_GUARD)) {
            plantedGuards.put(caster.getUniqueId(), new PlantedGuard(
                    caster.getUniqueId(), caster.getLocation(), expires, spec.id()));
            observeStateArmed(caster, caster, spec, AbilityMechanic.PLANTED_GUARD,
                    .35D, durationTicks);
        }

        if (mechanics.contains(AbilityMechanic.SUSTAINED_TETHER)) {
            scheduleTether(caster, spec, selection, effectiveLevel);
            selection.allies().stream().findFirst().ifPresent(target ->
                    observeStateArmed(caster, target, spec, AbilityMechanic.SUSTAINED_TETHER,
                            spec.tuning().healingFraction(), durationTicks));
        }

        if (mechanics.contains(AbilityMechanic.HEAL_ECHO)) {
            double echoFraction = ActiveAbilityLevelScaling.amount(.55D, effectiveLevel);
            pendingHealEchoes.put(caster.getUniqueId(), new PendingHealEcho(
                    expires, spec.id(), echoFraction));
            observeStateArmed(caster, caster, spec, AbilityMechanic.HEAL_ECHO,
                    echoFraction, durationTicks);
        }
    }

    void armDetonation(
            Player caster,
            FixedAbilitySpec spec,
            LivingEntity enemy,
            int effectiveLevel) {
        if (closed || !spec.executionTraits().mechanics().contains(AbilityMechanic.DETONATING_MARK)) return;
        int durationTicks = Math.max(20, ActiveAbilityLevelScaling.durationTicks(
                spec.tuning().durationTicks(), effectiveLevel));
        detonatingMarks.put(enemy.getUniqueId(), new DetonatingMark(
                caster.getUniqueId(),
                expiresAt(durationTicks),
                spec.id()));
        semantics.observe(AbilityRuntimeObservation.state(
                AbilityRuntimeObservation.Kind.STATE_ARMED,
                caster.getUniqueId(), enemy.getUniqueId(), spec.id(),
                .55D, durationTicks,
                AbilityMechanic.DETONATING_MARK));
    }

    void beginWindUp(Player caster, FixedAbilitySpec spec, int durationTicks) {
        if (closed || !spec.executionTraits().mechanics().contains(AbilityMechanic.WIND_UP)) return;
        activeWindUps.put(caster.getUniqueId(), new TimedSource(
                caster.getUniqueId(), expiresAt(durationTicks), spec.id()));
        observeStateArmed(caster, caster, spec, AbilityMechanic.WIND_UP,
                1D, Math.max(1, durationTicks));
    }

    Set<UUID> registerTaunts(
            Player caster,
            Collection<? extends LivingEntity> enemies,
            int durationTicks,
            boolean extendExisting) {
        return registerStatuses(tauntsByCaster, caster, enemies, durationTicks, extendExisting);
    }

    boolean hasOwnedTaunt(Player caster, LivingEntity target) {
        if (closed || caster == null || target == null) return false;
        return hasLiveStatus(tauntsByCaster, caster.getUniqueId(),
                target.getUniqueId(), System.nanoTime());
    }

    void registerDefenseBreaks(
            Player caster,
            Collection<? extends LivingEntity> enemies,
            int durationTicks) {
        registerStatuses(defenseBreaksByCaster, caster, enemies, durationTicks, false);
    }

    int linkedDurationTicks(Player caster, FixedAbilitySpec spec, int authoredTicks) {
        Set<AbilityMechanic> mechanics = spec.executionTraits().mechanics();
        TimedSource linked = null;
        long now = System.nanoTime();
        if (mechanics.contains(AbilityMechanic.REQUIRES_ACTIVE_FIELD))
            linked = live(activeFields, caster.getUniqueId(), now);
        if (mechanics.contains(AbilityMechanic.REQUIRES_WIND_UP))
            linked = live(activeWindUps, caster.getUniqueId(), now);
        if (linked == null) return authoredTicks;
        long remaining = Math.max(0L, linked.expiresAtNanos() - now);
        int remainingTicks = (int) Math.min(Integer.MAX_VALUE, Math.max(1L, remaining / 50_000_000L));
        return Math.min(Math.max(1, authoredTicks), remainingTicks);
    }

    boolean suppressesImmediate(FixedAbilitySpec spec, AbilityEffect effect) {
        Set<AbilityMechanic> mechanics = spec.executionTraits().mechanics();
        if (mechanics.contains(AbilityMechanic.HEAL_ECHO) && effect == AbilityEffect.HEAL) return true;
        if (mechanics.contains(AbilityMechanic.BARRIER_BREAK_HEAL) && effect == AbilityEffect.HEAL) return true;
        if (!mechanics.contains(AbilityMechanic.THREAT_TRIGGERED_PROTECTION)) return false;
        return effect == AbilityEffect.SHIELD || effect == AbilityEffect.ALLY_PROTECT;
    }

    double damageMultiplier(FixedAbilitySpec spec, LivingEntity target) {
        double multiplier = 1D;
        if (spec.executionTraits().mechanics().contains(AbilityMechanic.EXECUTE_DAMAGE)) {
            AttributeInstance maximum = target.getAttribute(Attribute.MAX_HEALTH);
            double max = maximum == null ? Math.max(1D, target.getHealth()) : Math.max(1D, maximum.getValue());
            double missing = 1D - Math.max(0D, Math.min(1D, target.getHealth() / max));
            multiplier *= 1D + missing * 1.5D;
        }
        return multiplier;
    }

    double retaliationAvailable(Player caster, FixedAbilitySpec spec) {
        RetaliationBudget stored = storedRetaliation.get(caster.getUniqueId());
        return stored != null && stored.abilityId().equals(spec.id()) ? stored.amount() : 0D;
    }

    double consumeRetaliation(Player caster, FixedAbilitySpec spec, LivingEntity target) {
        RetaliationBudget stored = storedRetaliation.get(caster.getUniqueId());
        if (stored == null || !stored.abilityId().equals(spec.id())
                || !storedRetaliation.remove(caster.getUniqueId(), stored)) return 0D;
        semantics.observe(AbilityRuntimeObservation.state(
                AbilityRuntimeObservation.Kind.RETALIATION_RELEASED,
                caster.getUniqueId(), target.getUniqueId(), spec.id(),
                stored.amount(), 0, AbilityMechanic.RETALIATION_RELEASE));
        return stored.amount();
    }

    void scheduleHealEcho(Player caster, Player target, double effectiveHealing, FixedAbilitySpec spec) {
        if (effectiveHealing <= 0D || spec.executionTraits().mechanics()
                .contains(AbilityMechanic.HEAL_ECHO)) return;
        PendingHealEcho pending = pendingHealEchoes.get(caster.getUniqueId());
        if (pending == null) return;
        if (pending.expiresAtNanos() <= System.nanoTime()) {
            pendingHealEchoes.remove(caster.getUniqueId(), pending);
            return;
        }
        if (!pendingHealEchoes.remove(caster.getUniqueId(), pending)) return;
        observeStateConsumed(caster.getUniqueId(), target.getUniqueId(),
                pending.abilityId(), effectiveHealing, AbilityMechanic.HEAL_ECHO);
        UUID sourceId = caster.getUniqueId();
        UUID targetId = target.getUniqueId();
        long sourceGeneration = lifecycleGeneration(sourceId);
        long targetGeneration = lifecycleGeneration(targetId);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!delayedAllyStillValid(sourceId, sourceGeneration,
                    target, targetId, targetGeneration, 24D)) return;
            double healed = heal(target, effectiveHealing * pending.echoFraction());
            if (healed > 0D) {
                semantics.recordContribution(caster, pending.abilityId(),
                        new AbilityContribution(0, healed, 0, 0, 0, 1, 0));
                observeEffect(AbilityRuntimeObservation.Kind.HEAL,
                        sourceId, targetId, pending.abilityId(), healed, 0, AbilityEffect.HEAL);
            }
        }, 30L);
    }

    private FixedAbilitySpec retaliationAbility(Player player) {
        FixedAbilitySpec active = null;
        for (String formId : semantics.activeFormIds(player)) {
            FixedAbilitySpec candidate;
            try {
                candidate = registry.require(formId + ".signature");
            } catch (IllegalArgumentException ignored) {
                continue;
            }
            if (candidate.executionTraits().mechanics()
                    .contains(AbilityMechanic.RETALIATION_RELEASE)) active = candidate;
        }
        return active;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerDamaged(PlayerDamagedByEliteMobEvent event) {
        if (closed || event.getDamage() <= 0D) return;
        Player target = event.getPlayer();
        if (!ClassAbilityEligibility.isEligible(target)) return;
        long now = System.nanoTime();
        double damage = event.getDamage();
        LivingEntity attacker = event.getEliteMobEntity().getLivingEntity();
        if (valid(attacker)) recentAttackers
                .computeIfAbsent(target.getUniqueId(), ignored -> new HashMap<>())
                .put(attacker.getUniqueId(), expiresAt((int) RECENT_ATTACKER_TICKS));

        PlantedGuard planted = livePlanted(target, now);
        if (planted != null) damage *= .35D;

        FixedAbilitySpec retaliationAbility = retaliationAbility(target);
        if (retaliationAbility != null && valid(attacker) && hasLiveStatus(
                tauntsByCaster, target.getUniqueId(), attacker.getUniqueId(), now)) {
            double cap = maximumHealth(target) * 2D;
            RetaliationBudget budget = RetaliationBudget.accumulate(
                    storedRetaliation.get(target.getUniqueId()), damage * .35D, cap,
                    retaliationAbility.id(), attacker.getUniqueId());
            storedRetaliation.put(target.getUniqueId(), budget);
            semantics.observe(AbilityRuntimeObservation.state(
                    AbilityRuntimeObservation.Kind.RETALIATION_STORED,
                    target.getUniqueId(), attacker.getUniqueId(), retaliationAbility.id(),
                    budget.amount(), 0, AbilityMechanic.RETALIATION_RELEASE));
        }

        boolean redistributedDamage = redistributing.contains(target.getUniqueId());
        RedirectLink redirect = redistributedDamage
                ? null
                : strongestRedirect(target.getUniqueId(), now);
        if (redirect != null) {
            Player protector = Bukkit.getPlayer(redirect.sourceId());
            if (validLinkedPlayers(protector, target, 24D)) {
                double redirected = damage * redirect.fraction();
                damage -= redirected;
                double received = redirected * redirect.efficiency()
                        * redirect.redirectedDamageMultiplier();
                dealSharedDamage(protector, event.getEliteMobEntity().getLivingEntity(),
                        received);
                semantics.recordContribution(protector, redirect.abilityId(),
                        new AbilityContribution(0, 0,
                                Math.max(0D, redirected - received), 0, 0, 1, 0));
                semantics.observe(new AbilityRuntimeObservation(
                        AbilityRuntimeObservation.Kind.DAMAGE_REDIRECTED,
                        redirect.sourceId(), target.getUniqueId(), redirect.abilityId(),
                        redirected, 0, 1, Set.of(AbilityEffect.ALLY_PROTECT),
                        Set.of(redirect.mechanic())));
            }
        }

        ShareLink share = liveShare(target.getUniqueId(), now);
        if (share != null && !redistributedDamage) {
            List<Player> recipients = share.members().stream()
                    .filter(id -> !id.equals(target.getUniqueId()))
                    .map(Bukkit::getPlayer)
                    .filter(Objects::nonNull)
                    .filter(Player::isOnline)
                    .filter(player -> player.getWorld().equals(target.getWorld()))
                    .filter(player -> player.getLocation().distanceSquared(target.getLocation()) <= 20D * 20D)
                    .toList();
            if (!recipients.isEmpty()) {
                double shared = damage * share.fraction();
                damage -= shared;
                double each = shared / recipients.size();
                for (Player recipient : recipients)
                    dealSharedDamage(recipient, event.getEliteMobEntity().getLivingEntity(), each);
                semantics.observe(new AbilityRuntimeObservation(
                        AbilityRuntimeObservation.Kind.DAMAGE_SHARED,
                        share.sourceId(), target.getUniqueId(), share.abilityId(),
                        shared, 0, recipients.size(), Set.of(),
                        Set.of(AbilityMechanic.DAMAGE_SHARE)));
            }
        }

        TriggeredBarrier barrier = liveBarrier(target.getUniqueId(), now);
        if (barrier != null) {
            double ownedRemaining = Math.min(
                    barrier.ownedAbsorption(),
                    Math.max(0D, target.getAbsorptionAmount() - barrier.baselineAbsorption()));
            if (AbilityStateMath.breaksOwnedBarrier(damage, ownedRemaining)) {
                triggeredBarriers.remove(target.getUniqueId());
                observeStateConsumed(barrier.sourceId(), target.getUniqueId(),
                        barrier.abilityId(), ownedRemaining, AbilityMechanic.BARRIER_BREAK_HEAL);
                UUID targetId = target.getUniqueId();
                long sourceGeneration = lifecycleGeneration(barrier.sourceId());
                long targetGeneration = lifecycleGeneration(targetId);
                Bukkit.getScheduler().runTask(plugin, () -> {
                    if (!delayedAllyStillValid(barrier.sourceId(), sourceGeneration,
                            target, targetId, targetGeneration, 24D)) return;
                    double healed = heal(target, maximumHealth(target) * barrier.healingFraction());
                    Player source = Bukkit.getPlayer(barrier.sourceId());
                    if (source != null && healed > 0D) {
                        semantics.recordContribution(source, barrier.abilityId(),
                                new AbilityContribution(0, healed, 0, 0, 0, 1, 0));
                        observeEffect(AbilityRuntimeObservation.Kind.HEAL,
                                barrier.sourceId(), targetId, barrier.abilityId(),
                                healed, 0, AbilityEffect.HEAL);
                    }
                });
            }
        }

        TriggeredProtection protection = liveProtection(target.getUniqueId(), now);
        if (protection != null
                && AbilityStateMath.isMajorThreat(damage, target.getHealth(), maximumHealth(target))) {
            triggeredProtections.remove(target.getUniqueId());
            observeStateConsumed(protection.sourceId(), target.getUniqueId(),
                    protection.abilityId(), protection.protectionAmount(),
                    AbilityMechanic.THREAT_TRIGGERED_PROTECTION);
            double prevented = Math.min(damage, protection.protectionAmount());
            damage -= prevented;
            Player source = Bukkit.getPlayer(protection.sourceId());
            if (source != null && prevented > 0D)
                semantics.recordContribution(source, protection.abilityId(),
                        new AbilityContribution(0, 0, prevented, 0, 0, 1, 0));
        }

        DeathGuard guard = liveGuard(target.getUniqueId(), now);
        if (guard != null && damage >= target.getHealth() + target.getAbsorptionAmount() - .01D) {
            deathGuards.remove(target.getUniqueId());
            observeStateConsumed(guard.sourceId(), target.getUniqueId(),
                    guard.abilityId(), damage, AbilityMechanic.DEATH_GUARD);
            if (guard.emitWardBroken())
                semantics.signal(target, AbilityRuntimeSignal.WARD_BROKEN, guard.wardBrokenTicks());
            double survivableDamage = AbilityStateMath.deathGuardDamageCeiling(
                    target.getHealth(), target.getAbsorptionAmount());
            double prevented = Math.max(0D, damage - survivableDamage);
            damage = Math.min(damage, survivableDamage);
            UUID targetId = target.getUniqueId();
            long sourceGeneration = lifecycleGeneration(guard.sourceId());
            long targetGeneration = lifecycleGeneration(targetId);
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (!delayedAllyStillValid(guard.sourceId(), sourceGeneration,
                        target, targetId, targetGeneration, 24D)) return;
                double healed = heal(target, maximumHealth(target) * guard.healingFraction());
                double shield = maximumHealth(target) * guard.shieldFraction();
                applyAbsorption(target, shield, 80);
                Player source = Bukkit.getPlayer(guard.sourceId());
                if (source != null) {
                    semantics.recordContribution(source, guard.abilityId(),
                            new AbilityContribution(0, healed, prevented, 0, 0, 1, 0));
                    if (healed > 0D)
                        observeEffect(AbilityRuntimeObservation.Kind.HEAL,
                                guard.sourceId(), targetId, guard.abilityId(),
                                healed, 0, AbilityEffect.HEAL);
                    if (shield > 0D)
                        observeEffect(AbilityRuntimeObservation.Kind.SHIELD,
                                guard.sourceId(), targetId, guard.abilityId(),
                                shield, 80, AbilityEffect.SHIELD);
                }
            });
        }

        event.setDamage(Math.max(0D, damage));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerDamagesElite(EliteMobDamagedByPlayerEvent event) {
        if (closed || event.getDamage() <= 0D) return;
        Player player = event.getPlayer();
        long now = System.nanoTime();
        TimedSource window = live(lifestealWindows, player.getUniqueId(), now);
        if (window != null) {
            double multiplier = semantics.healingMultiplier(player)
                    * AbilityHealingPolicy.multiplier(
                    semantics.mechanicModifiers(player), AbilityHealingType.BURST, 1);
            double healed = heal(player, event.getDamage() * .16D * multiplier);
            if (healed > 0D) {
                semantics.recordContribution(player, window.abilityId(),
                        new AbilityContribution(0, healed, 0, 0, 0, 1, 0));
                observeEffect(AbilityRuntimeObservation.Kind.HEAL,
                        player.getUniqueId(), player.getUniqueId(), window.abilityId(),
                        healed, 0, AbilityEffect.HEAL);
            }
        }

        DetonatingMark mark = detonatingMarks.get(event.getEntity().getUniqueId());
        if (mark == null || mark.expiresAtNanos() <= now
                || !mark.sourceId().equals(player.getUniqueId())) {
            if (mark != null && mark.expiresAtNanos() <= now)
                detonatingMarks.remove(event.getEntity().getUniqueId());
            return;
        }
        detonatingMarks.remove(event.getEntity().getUniqueId());
        observeStateConsumed(mark.sourceId(), event.getEntity().getUniqueId(),
                mark.abilityId(), event.getDamage(), AbilityMechanic.DETONATING_MARK);
        Location impact = event.getEntity().getLocation().clone();
        double explosionDamage = Math.max(1D, event.getDamage() * .55D);
        FixedAbilitySpec detonationSpec = registry.require(mark.abilityId());
        UUID sourceId = player.getUniqueId();
        long generation = lifecycleGeneration(sourceId);
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (closed || !player.isOnline() || impact.getWorld() == null
                    || !isCurrentLifecycleGeneration(sourceId, generation)) return;
            impact.getWorld().spawnParticle(Particle.EXPLOSION, impact, 3, 1D, .35D, 1D, .03D);
            impact.getWorld().playSound(impact, Sound.ENTITY_GENERIC_EXPLODE, .8F, 1.35F);
            for (org.bukkit.entity.Entity nearby : impact.getWorld().getNearbyEntities(impact, 4D, 4D, 4D)) {
                if (!(nearby instanceof LivingEntity living)
                        || !semantics.canApplyEnemyEffect(
                        player, living, detonationSpec, AbilityEffect.DAMAGE)) continue;
                double dealt = classAbilityDamage(
                        player, living, explosionDamage,
                        CombatDamageContext.ClassAbilityDamageDomain.AREA_BLAST);
                if (dealt > 0D)
                    observeEffect(AbilityRuntimeObservation.Kind.DAMAGE,
                            sourceId, living.getUniqueId(), mark.abilityId(),
                            dealt, 0, AbilityEffect.DAMAGE);
            }
        });
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPotionEffect(EntityPotionEffectEvent event) {
        if (closed || !(event.getEntity() instanceof Player player)
                || event.getNewEffect() == null) return;
        PotionEffectType type = event.getNewEffect().getType();
        long now = System.nanoTime();
        TimedSource immunity = null;
        AbilityMechanic mechanic = null;
        if (CONTROL_EFFECTS.contains(type)) {
            immunity = live(controlImmunity, player.getUniqueId(), now);
            mechanic = AbilityMechanic.CONTROL_IMMUNITY;
        }
        if (immunity == null && DEBUFF_EFFECTS.contains(type)) {
            immunity = live(debuffImmunity, player.getUniqueId(), now);
            mechanic = AbilityMechanic.DEBUFF_IMMUNITY;
        }
        if (immunity != null) {
            event.setCancelled(true);
            semantics.observe(new AbilityRuntimeObservation(
                    AbilityRuntimeObservation.Kind.STATUS_BLOCKED,
                    immunity.sourceId(), player.getUniqueId(), immunity.abilityId(),
                    1D, 0, 1, Set.of(), Set.of(mechanic)));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onVelocity(PlayerVelocityEvent event) {
        if (closed) return;
        if (ownedVelocities.contains(event.getPlayer().getUniqueId())) return;
        TimedSource immunity = live(
                controlImmunity, event.getPlayer().getUniqueId(), System.nanoTime());
        if (immunity != null) {
            event.setCancelled(true);
            semantics.observe(new AbilityRuntimeObservation(
                    AbilityRuntimeObservation.Kind.STATUS_BLOCKED,
                    immunity.sourceId(), event.getPlayer().getUniqueId(), immunity.abilityId(),
                    1D, 0, 1, Set.of(), Set.of(AbilityMechanic.CONTROL_IMMUNITY)));
        }
    }

    void applyOwnedVelocity(Player player, Vector velocity) {
        UUID playerId = player.getUniqueId();
        ownedVelocities.add(playerId);
        try {
            player.setVelocity(velocity);
        } finally {
            ownedVelocities.remove(playerId);
        }
    }

    private AbilityStateOwnership buildStateOwnership() {
        return AbilityStateOwnership.builder()
                .bind(AbilityStateOwnership.Bucket.REDIRECTS, sourceId -> {
                    redirectsByTarget.entrySet().removeIf(entry -> {
                        entry.getValue().removeIf(link -> link.sourceId().equals(sourceId));
                        return entry.getKey().equals(sourceId) || entry.getValue().isEmpty();
                    });
                }, redirectsByTarget::clear)
                .bind(AbilityStateOwnership.Bucket.DEATH_GUARDS,
                        sourceId -> deathGuards.entrySet().removeIf(entry ->
                                entry.getKey().equals(sourceId)
                                        || entry.getValue().sourceId().equals(sourceId)),
                        deathGuards::clear)
                .bind(AbilityStateOwnership.Bucket.TRIGGERED_BARRIERS,
                        sourceId -> triggeredBarriers.entrySet().removeIf(entry ->
                                entry.getKey().equals(sourceId)
                                        || entry.getValue().sourceId().equals(sourceId)),
                        triggeredBarriers::clear)
                .bind(AbilityStateOwnership.Bucket.TRIGGERED_PROTECTIONS,
                        sourceId -> triggeredProtections.entrySet().removeIf(entry ->
                                entry.getKey().equals(sourceId)
                                        || entry.getValue().sourceId().equals(sourceId)),
                        triggeredProtections::clear)
                .bind(AbilityStateOwnership.Bucket.DAMAGE_SHARES,
                        sourceId -> sharesByMember.entrySet().removeIf(entry ->
                                entry.getKey().equals(sourceId)
                                        || entry.getValue().sourceId().equals(sourceId)),
                        sharesByMember::clear)
                .bind(AbilityStateOwnership.Bucket.LIFESTEAL_WINDOWS,
                        sourceId -> lifestealWindows.entrySet().removeIf(entry ->
                                entry.getKey().equals(sourceId)
                                        || entry.getValue().sourceId().equals(sourceId)),
                        lifestealWindows::clear)
                .bind(AbilityStateOwnership.Bucket.CONTROL_IMMUNITY,
                        sourceId -> controlImmunity.entrySet().removeIf(entry ->
                                entry.getKey().equals(sourceId)
                                        || entry.getValue().sourceId().equals(sourceId)),
                        controlImmunity::clear)
                .bind(AbilityStateOwnership.Bucket.DEBUFF_IMMUNITY,
                        sourceId -> debuffImmunity.entrySet().removeIf(entry ->
                                entry.getKey().equals(sourceId)
                                        || entry.getValue().sourceId().equals(sourceId)),
                        debuffImmunity::clear)
                .bind(AbilityStateOwnership.Bucket.PLANTED_GUARDS,
                        sourceId -> plantedGuards.entrySet().removeIf(entry ->
                                entry.getKey().equals(sourceId)
                                        || entry.getValue().sourceId().equals(sourceId)),
                        plantedGuards::clear)
                .bind(AbilityStateOwnership.Bucket.DETONATING_MARKS,
                        sourceId -> detonatingMarks.entrySet().removeIf(entry ->
                                entry.getKey().equals(sourceId)
                                        || entry.getValue().sourceId().equals(sourceId)),
                        detonatingMarks::clear)
                .bind(AbilityStateOwnership.Bucket.RETALIATION,
                        storedRetaliation::remove, storedRetaliation::clear)
                .bind(AbilityStateOwnership.Bucket.RECENT_ATTACKERS,
                        sourceId -> clearStatusMap(recentAttackers, sourceId), recentAttackers::clear)
                .bind(AbilityStateOwnership.Bucket.TAUNTS,
                        sourceId -> clearStatusMap(tauntsByCaster, sourceId), tauntsByCaster::clear)
                .bind(AbilityStateOwnership.Bucket.DEFENSE_BREAKS,
                        sourceId -> clearStatusMap(defenseBreaksByCaster, sourceId),
                        defenseBreaksByCaster::clear)
                .bind(AbilityStateOwnership.Bucket.ACTIVE_FIELDS,
                        activeFields::remove, activeFields::clear)
                .bind(AbilityStateOwnership.Bucket.ACTIVE_WIND_UPS,
                        activeWindUps::remove, activeWindUps::clear)
                .bind(AbilityStateOwnership.Bucket.PENDING_HEAL_ECHOES,
                        pendingHealEchoes::remove, pendingHealEchoes::clear)
                .bind(AbilityStateOwnership.Bucket.LIFECYCLE_GENERATIONS,
                        sourceId -> lifecycleGenerations.compute(sourceId, (ignored, current) ->
                                current == null || current == Long.MAX_VALUE ? 1L : current + 1L),
                        lifecycleGenerations::clear)
                .bind(AbilityStateOwnership.Bucket.REDISTRIBUTION_GUARDS,
                        redistributing::remove, redistributing::clear)
                .bind(AbilityStateOwnership.Bucket.OWNED_VELOCITIES,
                        ownedVelocities::remove, ownedVelocities::clear)
                .build();
    }

    private static void clearStatusMap(
            Map<UUID, Map<UUID, Long>> statuses,
            UUID sourceId) {
        statuses.remove(sourceId);
        statuses.values().forEach(targets -> targets.remove(sourceId));
        statuses.entrySet().removeIf(entry -> entry.getValue().isEmpty());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent event) {
        // Teleports use a separate Bukkit handler list from ordinary movement.
        if (!closed && event.getTo() != null) plantedGuards.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        if (closed || event.getTo() == null) return;
        Player player = event.getPlayer();
        PlantedGuard guard = livePlanted(player, System.nanoTime());
        if (guard == null) return;
        if (event.getFrom().getX() == event.getTo().getX()
                && event.getFrom().getY() == event.getTo().getY()
                && event.getFrom().getZ() == event.getTo().getZ()) return;
        Location rooted = event.getFrom().clone();
        rooted.setYaw(event.getTo().getYaw());
        rooted.setPitch(event.getTo().getPitch());
        event.setTo(rooted);
    }

    void clearSource(UUID sourceId) {
        semantics.observe(new AbilityRuntimeObservation(
                AbilityRuntimeObservation.Kind.LIFECYCLE_CLEARED,
                sourceId, null, "lifecycle", 0D, 0, 1, Set.of(), Set.of()));
        stateOwnership.clearSource(sourceId);
    }

    private void observeStateArmed(
            Player source,
            Player target,
            FixedAbilitySpec spec,
            AbilityMechanic mechanic,
            double amount,
            int durationTicks) {
        observeStateArmed(source.getUniqueId(), target.getUniqueId(), spec,
                mechanic, amount, durationTicks);
    }

    private void observeStateArmed(
            UUID sourceId,
            UUID targetId,
            FixedAbilitySpec spec,
            AbilityMechanic mechanic,
            double amount,
            int durationTicks) {
        semantics.observe(AbilityRuntimeObservation.state(
                AbilityRuntimeObservation.Kind.STATE_ARMED,
                sourceId, targetId, spec.id(), Math.max(0D, amount),
                Math.max(1, durationTicks), mechanic));
    }

    private void observeStateConsumed(
            UUID sourceId,
            UUID targetId,
            String abilityId,
            double amount,
            AbilityMechanic mechanic) {
        semantics.observe(AbilityRuntimeObservation.state(
                AbilityRuntimeObservation.Kind.STATE_CONSUMED,
                sourceId, targetId, abilityId, Math.max(0D, amount),
                0, mechanic));
    }

    private void observeEffect(
            AbilityRuntimeObservation.Kind kind,
            UUID sourceId,
            UUID targetId,
            String abilityId,
            double amount,
            int durationTicks,
            AbilityEffect effect) {
        semantics.observe(AbilityRuntimeObservation.effect(
                kind, sourceId, targetId, abilityId,
                Math.max(0D, amount), Math.max(0, durationTicks), effect));
    }

    private void scheduleTether(Player caster, FixedAbilitySpec spec,
                                AbilityTargeting.TargetSelection selection, int effectiveLevel) {
        if (selection.allies().isEmpty()) return;
        Player target = selection.allies().get(0);
        AbilityMechanicModifiers modifiers = semantics.mechanicModifiers(caster);
        int durationTicks = ActiveAbilityLevelScaling.durationTicks(
                AbilityHealingPolicy.durationTicks(
                        spec.tuning().durationTicks(), AbilityHealingType.PERIODIC, modifiers),
                effectiveLevel);
        int pulses = Math.max(3, Math.min(8, (int) Math.ceil(durationTicks / 20D)));
        double levelScale = AbilityLevelScaling.multiplier(effectiveLevel);
        double healingMultiplier = semantics.healingMultiplier(caster)
                * AbilityHealingPolicy.multiplier(
                modifiers, AbilityHealingType.PERIODIC, selection.enemies().size());
        UUID sourceId = caster.getUniqueId();
        UUID targetId = target.getUniqueId();
        long sourceGeneration = lifecycleGeneration(sourceId);
        long targetGeneration = lifecycleGeneration(targetId);
        for (int pulse = 1; pulse <= pulses; pulse++) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (!delayedAllyStillValid(sourceId, sourceGeneration,
                        target, targetId, targetGeneration, 24D)) return;
                double healed = heal(target, maximumHealth(target)
                        * spec.tuning().healingFraction() * .28D * levelScale
                        * healingMultiplier);
                if (healed > 0D) {
                    semantics.recordContribution(caster, spec.id(),
                            new AbilityContribution(0, healed, 0, 0, 0, 1, 0));
                    observeEffect(AbilityRuntimeObservation.Kind.HEAL,
                            sourceId, targetId, spec.id(), healed, 0, AbilityEffect.HEAL);
                    scheduleHealEcho(caster, target, healed, spec);
                }
            }, pulse * 20L);
        }
    }

    private RedirectLink strongestRedirect(UUID targetId, long now) {
        List<RedirectLink> links = redirectsByTarget.get(targetId);
        if (links == null) return null;
        links.removeIf(link -> link.expiresAtNanos() <= now);
        if (links.isEmpty()) {
            redirectsByTarget.remove(targetId);
            return null;
        }
        return links.stream().max(Comparator.comparingDouble(RedirectLink::fraction)).orElse(null);
    }

    private DeathGuard liveGuard(UUID targetId, long now) {
        DeathGuard guard = deathGuards.get(targetId);
        if (guard != null && guard.expiresAtNanos() <= now) {
            deathGuards.remove(targetId);
            return null;
        }
        return guard;
    }

    private TriggeredBarrier liveBarrier(UUID targetId, long now) {
        TriggeredBarrier barrier = triggeredBarriers.get(targetId);
        if (barrier != null && barrier.expiresAtNanos() <= now) {
            triggeredBarriers.remove(targetId);
            return null;
        }
        return barrier;
    }

    private TriggeredProtection liveProtection(UUID targetId, long now) {
        TriggeredProtection protection = triggeredProtections.get(targetId);
        if (protection != null && protection.expiresAtNanos() <= now) {
            triggeredProtections.remove(targetId);
            return null;
        }
        return protection;
    }

    private ShareLink liveShare(UUID memberId, long now) {
        ShareLink link = sharesByMember.get(memberId);
        if (link == null) return null;
        if (link.expiresAtNanos() <= now) {
            link.members().forEach(id -> sharesByMember.remove(id, link));
            return null;
        }
        return link;
    }

    private PlantedGuard livePlanted(Player player, long now) {
        PlantedGuard guard = plantedGuards.get(player.getUniqueId());
        if (guard == null) return null;
        if (guard.expiresAtNanos() <= now
                || guard.origin().getWorld() == null
                || !guard.origin().getWorld().equals(player.getWorld())
                || guard.origin().distanceSquared(player.getLocation()) > 1.5D * 1.5D) {
            plantedGuards.remove(player.getUniqueId());
            return null;
        }
        return guard;
    }

    private List<LivingEntity> recentAttackers(Player target, long now) {
        Map<UUID, Long> attackers = recentAttackers.get(target.getUniqueId());
        if (attackers == null) return List.of();
        attackers.entrySet().removeIf(entry -> entry.getValue() <= now);
        if (attackers.isEmpty()) {
            recentAttackers.remove(target.getUniqueId());
            return List.of();
        }
        return attackers.keySet().stream()
                .map(Bukkit::getEntity)
                .filter(LivingEntity.class::isInstance)
                .map(LivingEntity.class::cast)
                .filter(AbilityStateRuntime::valid)
                .toList();
    }

    private static boolean withinAbilityReach(
            LivingEntity enemy,
            Location origin,
            FixedAbilitySpec spec) {
        if (origin.getWorld() == null || !origin.getWorld().equals(enemy.getWorld())) return false;
        double reach = Math.max(1D, Math.max(spec.tuning().range(), spec.tuning().radius()));
        return enemy.getLocation().distanceSquared(origin) <= reach * reach;
    }

    private static boolean hasLiveStatus(
            Map<UUID, Map<UUID, Long>> statuses,
            UUID sourceId,
            UUID targetId,
            long now) {
        Map<UUID, Long> targets = statuses.get(sourceId);
        if (targets == null) return false;
        targets.entrySet().removeIf(entry -> entry.getValue() <= now);
        if (targets.isEmpty()) {
            statuses.remove(sourceId);
            return false;
        }
        return targets.containsKey(targetId);
    }

    private static Set<UUID> registerStatuses(
            Map<UUID, Map<UUID, Long>> statuses,
            Player caster,
            Collection<? extends LivingEntity> targets,
            int durationTicks,
            boolean extendExisting) {
        long now = System.nanoTime();
        long duration = Math.max(1, durationTicks) * 50_000_000L;
        Map<UUID, Long> byTarget = statuses.computeIfAbsent(
                caster.getUniqueId(), ignored -> new HashMap<>());
        return TimedStatusLeasePolicy.apply(
                byTarget,
                targets.stream().map(LivingEntity::getUniqueId).toList(),
                now,
                duration,
                extendExisting);
    }

    private static TimedSource live(Map<UUID, TimedSource> states, UUID playerId, long now) {
        TimedSource state = states.get(playerId);
        if (state != null && state.expiresAtNanos() <= now) {
            states.remove(playerId);
            return null;
        }
        return state;
    }

    private void dealSharedDamage(Player target, LivingEntity attacker, double amount) {
        if (target == null || !target.isOnline() || target.isDead() || amount <= 0D) return;
        UUID targetId = target.getUniqueId();
        if (!redistributing.add(targetId)) return;
        try {
            CombatDamageContext.runEliteToPlayerBypass(() -> target.damage(amount, attacker));
        } finally {
            redistributing.remove(targetId);
        }
    }

    private long lifecycleGeneration(UUID playerId) {
        return lifecycleGenerations.getOrDefault(playerId, 0L);
    }

    private boolean isCurrentLifecycleGeneration(UUID playerId, long generation) {
        return lifecycleGeneration(playerId) == generation;
    }

    long lifecycleToken(Player player) {
        return lifecycleGeneration(player.getUniqueId());
    }

    boolean isCurrentLifecycle(Player player, long token) {
        return isCurrentLifecycleGeneration(player.getUniqueId(), token);
    }

    private boolean delayedAllyStillValid(
            UUID sourceId,
            long sourceGeneration,
            Player target,
            UUID targetId,
            long targetGeneration,
            double maximumDistance) {
        if (closed
                || !isCurrentLifecycleGeneration(sourceId, sourceGeneration)
                || !isCurrentLifecycleGeneration(targetId, targetGeneration)) return false;
        Player source = Bukkit.getPlayer(sourceId);
        return source != null
                && source.isOnline()
                && source.isValid()
                && !source.isDead()
                && ClassAbilityEligibility.isEligible(source)
                && target.isOnline()
                && target.isValid()
                && !target.isDead()
                && ClassAbilityEligibility.isEligible(target)
                && source.getWorld().equals(target.getWorld())
                && source.getLocation().distanceSquared(target.getLocation())
                <= maximumDistance * maximumDistance;
    }

    private static boolean validLinkedPlayers(Player source, Player target, double maximumDistance) {
        return source != null && source.isOnline() && !source.isDead()
                && source.getWorld().equals(target.getWorld())
                && source.getLocation().distanceSquared(target.getLocation()) <= maximumDistance * maximumDistance;
    }

    private static double healthFraction(Player player) {
        return player.getHealth() / maximumHealth(player);
    }

    private static boolean valid(LivingEntity entity) {
        return entity != null
                && entity.isValid()
                && !entity.isDead()
                && entity.getHealth() > 0D;
    }

    private static double classAbilityDamage(
            Player source,
            LivingEntity target,
            double amount,
            CombatDamageContext.ClassAbilityDamageDomain domain) {
        if (!valid(target) || !Double.isFinite(amount) || amount <= 0D) return 0D;
        double before = target.getHealth() + target.getAbsorptionAmount();
        CombatDamageContext.runClassAbilityDamage(domain, () -> target.damage(amount, source));
        if (!target.isValid()) return Math.min(before, amount);
        double after = Math.max(0D, target.getHealth())
                + Math.max(0D, target.getAbsorptionAmount());
        return Math.max(0D, before - after);
    }

    private static double heal(Player target, double amount) {
        if (!target.isOnline() || target.isDead() || !Double.isFinite(amount) || amount <= 0D) return 0D;
        double before = target.getHealth();
        double bounded = Math.min(Math.max(0D, maximumHealth(target) - before), amount);
        if (bounded <= 0D) return 0D;
        EntityRegainHealthEvent event = new EntityRegainHealthEvent(
                target, bounded, EntityRegainHealthEvent.RegainReason.CUSTOM);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled() || !Double.isFinite(event.getAmount()) || event.getAmount() <= 0D) return 0D;
        target.setHealth(Math.min(maximumHealth(target), before + event.getAmount()));
        return Math.max(0D, target.getHealth() - before);
    }

    private static void applyAbsorption(Player target, double amount, int durationTicks) {
        if (!Double.isFinite(amount) || amount <= 0D) return;
        int amplifier = Math.max(0, Math.min(255, (int) Math.ceil(amount / 4D) - 1));
        target.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION,
                Math.max(20, durationTicks), amplifier, false, true, true), true);
    }

    private static double maximumHealth(LivingEntity entity) {
        AttributeInstance attribute = entity.getAttribute(Attribute.MAX_HEALTH);
        return attribute == null ? Math.max(1D, entity.getHealth()) : Math.max(1D, attribute.getValue());
    }

    private static long expiresAt(int ticks) {
        long now = System.nanoTime();
        long duration = Math.max(1, ticks) * 50_000_000L;
        return Long.MAX_VALUE - now < duration ? Long.MAX_VALUE : now + duration;
    }

    @Override
    public void close() {
        if (closed) return;
        closed = true;
        HandlerList.unregisterAll(this);
        stateOwnership.clearAll();
    }

    private record RedirectLink(UUID sourceId, double fraction, double efficiency,
                                double redirectedDamageMultiplier,
                                long expiresAtNanos, String abilityId,
                                AbilityMechanic mechanic) {
    }

    private record DeathGuard(UUID sourceId, double healingFraction, double shieldFraction,
                              boolean emitWardBroken, int wardBrokenTicks,
                              long expiresAtNanos, String abilityId) {
    }

    private record TriggeredBarrier(UUID sourceId, double healingFraction,
                                    double baselineAbsorption, double ownedAbsorption,
                                    long expiresAtNanos, String abilityId) {
    }

    private record TriggeredProtection(UUID sourceId, double protectionAmount,
                                       long expiresAtNanos, String abilityId) {
    }

    private record PendingHealEcho(
            long expiresAtNanos,
            String abilityId,
            double echoFraction) {
        private PendingHealEcho {
            if (!Double.isFinite(echoFraction) || echoFraction <= 0D)
                throw new IllegalArgumentException("echoFraction must be positive");
        }
    }

    private record ShareLink(UUID sourceId, List<UUID> members, double fraction,
                             long expiresAtNanos, String abilityId) {
        private ShareLink {
            members = List.copyOf(members);
        }
    }

    private record TimedSource(UUID sourceId, long expiresAtNanos, String abilityId) {
    }

    private record PlantedGuard(UUID sourceId, Location origin,
                                long expiresAtNanos, String abilityId) {
        private PlantedGuard {
            origin = origin.clone();
        }
    }

    private record DetonatingMark(UUID sourceId, long expiresAtNanos, String abilityId) {
    }
}
