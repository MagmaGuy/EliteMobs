package com.magmaguy.elitemobs.experimentalcombat.abilities;

import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.api.EliteMobRemoveEvent;
import com.magmaguy.elitemobs.api.PlayerDamagedByEliteMobEvent;
import com.magmaguy.elitemobs.combatsystem.CombatDamageContext;
import com.magmaguy.elitemobs.experimentalcombat.ClassAbilityEligibility;
import com.magmaguy.elitemobs.skills.SkillType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/** Owns only explicit, time-bounded class damage modifiers. With no live entry, vanilla damage is untouched. */
final class TimedCombatModifiers implements Listener, AutoCloseable {
    private final AbilitySemantics semantics;
    private final Map<UUID, List<EnemyMark>> enemyMarks = new HashMap<>();
    private final Map<UUID, List<PlayerModifier>> outgoingModifiers = new HashMap<>();
    private final Map<UUID, List<PlayerModifier>> incomingModifiers = new HashMap<>();
    private final Map<UUID, List<PlayerModifier>> incomingPenalties = new HashMap<>();
    private final Map<PlayerDamagedByEliteMobEvent, DamageReductionAttribution> pendingWeakening =
            new IdentityHashMap<>();
    private final TimedOutgoingDamageReduction outgoingReductions =
            new TimedOutgoingDamageReduction(System::nanoTime);
    private boolean closed;

    TimedCombatModifiers(Plugin plugin, AbilitySemantics semantics) {
        this.semantics = Objects.requireNonNull(semantics, "semantics");
        Bukkit.getPluginManager().registerEvents(this, Objects.requireNonNull(plugin, "plugin"));
    }

    void markEnemy(Player source, Entity enemy, Set<UUID> beneficiaries, double multiplier,
                   int durationTicks, String abilityId) {
        markEnemy(source, enemy, beneficiaries, multiplier, durationTicks, abilityId, false);
    }

    void markEnemy(Player source, Entity enemy, Set<UUID> beneficiaries, double multiplier,
                   int durationTicks, String abilityId, boolean healthScaled) {
        if (closed || multiplier <= 1D || durationTicks <= 0) return;
        enemyMarks.computeIfAbsent(enemy.getUniqueId(), ignored -> new ArrayList<>())
                .add(new EnemyMark(source.getUniqueId(), Set.copyOf(beneficiaries), multiplier,
                        expiresAt(durationTicks), abilityId, healthScaled));
    }

    void modifyOutgoing(Player source, Player target, double multiplier, int durationTicks, String abilityId) {
        modifyOutgoing(source, target, multiplier, durationTicks, abilityId, ModifierDomain.ANY);
    }

    void modifySpellOutgoing(
            Player source,
            Player target,
            double multiplier,
            int durationTicks,
            String abilityId) {
        modifyOutgoing(source, target, multiplier, durationTicks, abilityId, ModifierDomain.SPELL);
    }

    private void modifyOutgoing(
            Player source,
            Player target,
            double multiplier,
            int durationTicks,
            String abilityId,
            ModifierDomain domain) {
        if (closed || multiplier <= 1D || durationTicks <= 0) return;
        outgoingModifiers.computeIfAbsent(target.getUniqueId(), ignored -> new ArrayList<>())
                .add(new PlayerModifier(source.getUniqueId(), multiplier, expiresAt(durationTicks),
                        abilityId, domain));
    }

    void modifyIncoming(Player source, Player target, double multiplier, int durationTicks, String abilityId) {
        if (closed || multiplier >= 1D || multiplier < 0D || durationTicks <= 0) return;
        incomingModifiers.computeIfAbsent(target.getUniqueId(), ignored -> new ArrayList<>())
                .add(new PlayerModifier(source.getUniqueId(), multiplier, expiresAt(durationTicks),
                        abilityId, ModifierDomain.ANY));
    }

    void modifyIncomingPenalty(
            Player source,
            Player target,
            double multiplier,
            int durationTicks,
            String abilityId) {
        if (closed || multiplier <= 1D || durationTicks <= 0) return;
        incomingPenalties.computeIfAbsent(target.getUniqueId(), ignored -> new ArrayList<>())
                .add(new PlayerModifier(source.getUniqueId(), multiplier, expiresAt(durationTicks),
                        abilityId, ModifierDomain.ANY));
    }

    double weakenOutgoing(
            Player source,
            Entity target,
            double authoredMultiplier,
            int effectiveLevel,
            double potencyMultiplier,
            int durationTicks,
            String abilityId) {
        if (closed) return 1D;
        return outgoingReductions.register(
                source.getUniqueId(), target.getUniqueId(), abilityId,
                authoredMultiplier, effectiveLevel, potencyMultiplier, durationTicks);
    }

    /** Removes this caster's modifiers without touching effects supplied by another caster. */
    void clearSource(UUID sourceId) {
        Objects.requireNonNull(sourceId, "sourceId");
        if (closed) return;
        enemyMarks.entrySet().removeIf(entry -> {
            entry.getValue().removeIf(mark -> mark.sourceId().equals(sourceId));
            return entry.getValue().isEmpty();
        });
        outgoingModifiers.entrySet().removeIf(entry -> {
            entry.getValue().removeIf(modifier -> modifier.sourceId().equals(sourceId));
            return entry.getValue().isEmpty();
        });
        incomingModifiers.entrySet().removeIf(entry -> {
            entry.getValue().removeIf(modifier -> modifier.sourceId().equals(sourceId));
            return entry.getValue().isEmpty();
        });
        incomingPenalties.entrySet().removeIf(entry -> {
            entry.getValue().removeIf(modifier -> modifier.sourceId().equals(sourceId));
            return entry.getValue().isEmpty();
        });
        outgoingReductions.clearSource(sourceId);
        pendingWeakening.entrySet().removeIf(entry -> entry.getValue().sourceId().equals(sourceId));
    }

    /** Removes every modifier attached to this player, regardless of who supplied it. */
    void clearTarget(UUID targetId) {
        Objects.requireNonNull(targetId, "targetId");
        if (closed) return;
        outgoingModifiers.remove(targetId);
        incomingModifiers.remove(targetId);
        incomingPenalties.remove(targetId);
        outgoingReductions.clearTarget(targetId);
        pendingWeakening.entrySet().removeIf(entry -> entry.getValue().targetId().equals(targetId));
        enemyMarks.entrySet().removeIf(entry -> {
            entry.getValue().removeIf(mark -> mark.beneficiaries().contains(targetId));
            return entry.getValue().isEmpty();
        });
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerDamagesElite(EliteMobDamagedByPlayerEvent event) {
        if (com.magmaguy.elitemobs.combatsystem.CombatDamageContext.isDamageTransferActive()) return;
        if (closed || event.getDamage() <= 0D) return;
        Player attacker = event.getPlayer();
        if (attacker == null || !ClassAbilityEligibility.isEligible(attacker)) return;
        long now = System.nanoTime();
        PlayerModifier outgoing = strongestOutgoing(
                attacker.getUniqueId(), now,
                CombatDamageContext.currentPlayerToEliteSource()
                        .map(CombatDamageContext.PlayerDamageSource::progressionSkill)
                        .orElse(null),
                CombatDamageContext.currentClassAbilityDamageDomain().orElse(null));
        if (outgoing != null)
            event.applyClassAbilityDamageMultiplier(outgoing.abilityId(), outgoing.multiplier());
        EnemyMark mark = strongestMark(
                event.getEntity().getUniqueId(), attacker.getUniqueId(), now);
        if (mark != null) {
            double multiplier = mark.multiplier(
                    healthFraction(event.getEliteMobEntity().getLivingEntity()));
            event.applyClassAbilityDamageMultiplier(mark.abilityId(), multiplier);
            semantics.observe(AbilityRuntimeObservation.effect(
                    AbilityRuntimeObservation.Kind.MODIFIER_TRIGGERED,
                    mark.sourceId(), event.getEntity().getUniqueId(), mark.abilityId(),
                    multiplier, 0, AbilityEffect.PARTY_DAMAGE_MARK));
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEliteDamagesPlayer(PlayerDamagedByEliteMobEvent event) {
        if (closed || event.getDamage() <= 0D) return;
        Player player = event.getPlayer();
        if (player == null) return;
        boolean recipientEligible = ClassAbilityEligibility.isEligible(player);
        if (!recipientEligible) return;
        double originalDamage = event.getDamage();
        Entity attacker = event.getAttacker();
        TimedOutgoingDamageReduction.Application weakening = attacker == null
                 ? null
                 : outgoingReductions.apply(
                         attacker.getUniqueId(), originalDamage, recipientEligible,
                         sourceId -> liveSourceNear(sourceId, attacker))
                .orElse(null);
        double weakenedDamage = weakening == null ? originalDamage : weakening.modifiedDamage();
        PlayerModifier protection = strongestProtection(player.getUniqueId(), System.nanoTime());
        PlayerModifier penalty = strongestPenalty(player.getUniqueId(), System.nanoTime());
        if (weakening == null && protection == null && penalty == null) return;
        double protectedDamage = Math.max(0D, weakenedDamage
                 * (protection == null ? 1D : protection.multiplier()));
        double modifiedDamage = Math.max(0D, protectedDamage
                 * (penalty == null ? 1D : penalty.multiplier()));
        event.setDamage(modifiedDamage);

        if (weakening != null) {
            double damageWithoutWeakening = Math.max(0D, originalDamage
                    * (protection == null ? 1D : protection.multiplier())
                    * (penalty == null ? 1D : penalty.multiplier()));
            double prevented = Math.max(0D, damageWithoutWeakening - modifiedDamage);
            if (prevented > 0D) {
                pendingWeakening.put(event, new DamageReductionAttribution(
                        weakening.sourceId(), attacker.getUniqueId(), weakening.abilityId(),
                        modifiedDamage, prevented));
            }
        }

        if (protectedDamage < weakenedDamage && protection != null) {
            Player source = Bukkit.getPlayer(protection.sourceId());
            if (source != null) {
                AbilityContribution contribution = new AbilityContribution(
                        0, 0, weakenedDamage - protectedDamage, 0, 0, 1, 0);
                semantics.recordContribution(source, protection.abilityId(), contribution);
            }
        }
    }

    /**
     * Finalizes mitigation evidence after every normal damage listener has run. MONITOR is the
     * canonical observation boundary for this custom event: later core listeners may cancel the
     * hit or transform its damage, but MONITOR listeners are observers by Bukkit contract.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onEliteDamageFinalized(PlayerDamagedByEliteMobEvent event) {
        DamageReductionAttribution attribution = pendingWeakening.remove(event);
        if (attribution == null) return;
        double prevented = attribution.survivingPreventedDamage(event.getDamage(), event.isCancelled());
        if (prevented <= 0D) return;
        Player source = Bukkit.getPlayer(attribution.sourceId());
        if (source != null) {
            semantics.recordContribution(source, attribution.abilityId(),
                    new AbilityContribution(0, 0, prevented, 0, 0, 1, 0));
        }
        semantics.observe(AbilityRuntimeObservation.effect(
                AbilityRuntimeObservation.Kind.MODIFIER_TRIGGERED,
                attribution.sourceId(), attribution.targetId(), attribution.abilityId(),
                prevented, 0, AbilityEffect.WEAKEN));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEliteRemoved(EliteMobRemoveEvent event) {
        Entity entity = event.getEntity();
        if (entity != null) outgoingReductions.clearTarget(entity.getUniqueId());
    }

    private EnemyMark strongestMark(UUID enemyId, UUID attackerId, long now) {
        List<EnemyMark> marks = enemyMarks.get(enemyId);
        if (marks == null) return null;
        marks.removeIf(mark -> mark.expiresAtNanos() <= now
                || !liveSourceNear(mark.sourceId(), Bukkit.getEntity(enemyId)));
        if (marks.isEmpty()) {
            enemyMarks.remove(enemyId);
            return null;
        }
        return marks.stream()
                .filter(mark -> mark.beneficiaries().contains(attackerId))
                .max(java.util.Comparator.comparingDouble(EnemyMark::baseMultiplier))
                .orElse(null);
    }

    private PlayerModifier strongestOutgoing(
            UUID playerId,
            long now,
            SkillType sourceSkill,
            CombatDamageContext.ClassAbilityDamageDomain classDomain) {
        List<PlayerModifier> modifiers = outgoingModifiers.get(playerId);
        if (modifiers == null) return null;
        Entity target = Bukkit.getPlayer(playerId);
        modifiers.removeIf(modifier -> modifier.expiresAtNanos() <= now
                || !liveSourceNear(modifier.sourceId(), target));
        if (modifiers.isEmpty()) {
            outgoingModifiers.remove(playerId);
            return null;
        }
        return modifiers.stream()
                .filter(modifier -> modifier.domain() == ModifierDomain.ANY
                        || isSpellDamage(sourceSkill, classDomain))
                .max(java.util.Comparator.comparingDouble(PlayerModifier::multiplier))
                .orElse(null);
    }

    private PlayerModifier strongestProtection(UUID playerId, long now) {
        List<PlayerModifier> modifiers = incomingModifiers.get(playerId);
        if (modifiers == null) return null;
        Entity target = Bukkit.getPlayer(playerId);
        modifiers.removeIf(modifier -> modifier.expiresAtNanos() <= now
                || !liveSourceNear(modifier.sourceId(), target));
        if (modifiers.isEmpty()) {
            incomingModifiers.remove(playerId);
            return null;
        }
        return modifiers.stream().min(java.util.Comparator.comparingDouble(PlayerModifier::multiplier)).orElse(null);
    }

    private PlayerModifier strongestPenalty(UUID playerId, long now) {
        List<PlayerModifier> modifiers = incomingPenalties.get(playerId);
        if (modifiers == null) return null;
        Entity target = Bukkit.getPlayer(playerId);
        modifiers.removeIf(modifier -> modifier.expiresAtNanos() <= now
                || !liveSourceNear(modifier.sourceId(), target));
        if (modifiers.isEmpty()) {
            incomingPenalties.remove(playerId);
            return null;
        }
        return modifiers.stream()
                .max(java.util.Comparator.comparingDouble(PlayerModifier::multiplier))
                .orElse(null);
    }

    static boolean isSpellDamage(
            SkillType sourceSkill,
            CombatDamageContext.ClassAbilityDamageDomain classDomain) {
        if (sourceSkill == SkillType.STAVES || sourceSkill == SkillType.WANDS) return true;
        return classDomain != null
                && classDomain.delivery() != CombatDamageContext.ClassAbilityDelivery.SUMMON;
    }

    private static boolean liveSourceNear(UUID sourceId, Entity target) {
        Player source = Bukkit.getPlayer(sourceId);
        return source != null
                && source.isOnline()
                && source.isValid()
                && !source.isDead()
                && ClassAbilityEligibility.isEligible(source)
                && target != null
                && target.isValid()
                && source.getWorld().equals(target.getWorld());
    }

    private static long expiresAt(int ticks) {
        long durationNanos = ticks * 50_000_000L;
        long now = System.nanoTime();
        return Long.MAX_VALUE - now < durationNanos ? Long.MAX_VALUE : now + durationNanos;
    }

    @Override
    public void close() {
        if (closed) return;
        closed = true;
        HandlerList.unregisterAll(this);
        enemyMarks.clear();
        outgoingModifiers.clear();
        incomingModifiers.clear();
        incomingPenalties.clear();
        pendingWeakening.clear();
        outgoingReductions.close();
    }

    private static double healthFraction(org.bukkit.entity.LivingEntity entity) {
        org.bukkit.attribute.AttributeInstance maximum = entity.getAttribute(
                org.bukkit.attribute.Attribute.MAX_HEALTH);
        double value = maximum == null ? Math.max(1D, entity.getHealth()) : Math.max(1D, maximum.getValue());
        return Math.max(0D, Math.min(1D, entity.getHealth() / value));
    }

    private record EnemyMark(UUID sourceId, Set<UUID> beneficiaries, double baseMultiplier,
                             long expiresAtNanos, String abilityId, boolean healthScaled) {
        double multiplier(double healthFraction) {
            return healthScaled
                    ? AbilityStateMath.healthScaledMark(baseMultiplier, healthFraction)
                    : baseMultiplier;
        }
    }

    private enum ModifierDomain {
        ANY,
        SPELL
    }

    private record PlayerModifier(UUID sourceId, double multiplier, long expiresAtNanos,
                                  String abilityId, ModifierDomain domain) {
    }
}
