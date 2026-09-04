package com.magmaguy.elitemobs.combatsystem;

import com.magmaguy.elitemobs.skills.SkillType;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;
import java.util.UUID;

/**
 * Owns one-shot overrides for programmatic damage calls.
 * <p>
 * Bukkit fires damage events synchronously. A caller opens a scope immediately around
 * {@code LivingEntity.damage(...)} and the matching EliteMobs filter consumes the override at the
 * beginning of that event. Consuming it before running downstream listeners prevents nested damage
 * events from inheriting the outer hit's override.
 */
public final class CombatDamageContext {

    private static final DamageOverride DEFAULT_OVERRIDE = new DamageOverride(false, 1.0);
    private static final ThreadLocal<Deque<PendingOverride>> PLAYER_TO_ELITE =
            ThreadLocal.withInitial(ArrayDeque::new);
    private static final ThreadLocal<Deque<PendingOverride>> ELITE_TO_PLAYER =
            ThreadLocal.withInitial(ArrayDeque::new);
    private static final ThreadLocal<Integer> ACTIVE_PLAYER_TO_ELITE_BYPASS = new ThreadLocal<>();
    private static final ThreadLocal<Deque<ClassAbilityDamageDomain>> ACTIVE_CLASS_ABILITY_DAMAGE =
            ThreadLocal.withInitial(ArrayDeque::new);
    private static final ThreadLocal<Deque<PlayerDamageSource>> ACTIVE_PLAYER_TO_ELITE_SOURCES =
            ThreadLocal.withInitial(ArrayDeque::new);

    private CombatDamageContext() {
    }

    public static Scope bypassPlayerToElite() {
        return push(PLAYER_TO_ELITE, new DamageOverride(true, 1.0));
    }

    public static Scope bypassEliteToPlayer() {
        return push(ELITE_TO_PLAYER, new DamageOverride(true, 1.0));
    }

    public static Scope multiplyEliteToPlayer(double multiplier) {
        double safeMultiplier = Double.isFinite(multiplier) && multiplier >= 0 ? multiplier : 1.0;
        return push(ELITE_TO_PLAYER, new DamageOverride(false, safeMultiplier));
    }

    public static void runPlayerToEliteBypass(Runnable damageCall) {
        runPlayerToEliteBypass(null, damageCall);
    }

    /**
     * Runs custom damage while retaining the cast-time progression identity until every
     * synchronous damage listener has finished. Delayed missiles must snapshot this at launch.
     */
    public static void runPlayerToEliteBypass(PlayerDamageSource source, Runnable damageCall) {
        Integer previousDepth = ACTIVE_PLAYER_TO_ELITE_BYPASS.get();
        ACTIVE_PLAYER_TO_ELITE_BYPASS.set(previousDepth == null ? 1 : previousDepth + 1);
        if (source != null) ACTIVE_PLAYER_TO_ELITE_SOURCES.get().addLast(source);
        try (Scope ignored = bypassPlayerToElite()) {
            damageCall.run();
        } finally {
            if (source != null) {
                Deque<PlayerDamageSource> sources = ACTIVE_PLAYER_TO_ELITE_SOURCES.get();
                sources.removeLastOccurrence(source);
                if (sources.isEmpty()) ACTIVE_PLAYER_TO_ELITE_SOURCES.remove();
            }
            if (previousDepth == null) ACTIVE_PLAYER_TO_ELITE_BYPASS.remove();
            else ACTIVE_PLAYER_TO_ELITE_BYPASS.set(previousDepth);
        }
    }

    /**
     * Runs class-sourced damage through the bypass pipeline while retaining its origin for
     * synchronous passive evaluation. Magic weapons and unrelated custom damage do not enter this
     * scope and therefore cannot accidentally trigger class-ability-only passives.
     */
    public static void runClassAbilityDamage(Runnable damageCall) {
        runClassAbilityDamage(ClassAbilityDamageDomain.SINGLE_TARGET_DIRECT, damageCall);
    }

    /**
     * Runs class damage with orthogonal target-shape and delivery identity for synchronous passive
     * evaluation. Nested scopes restore the outer domain exactly.
     */
    public static void runClassAbilityDamage(
            ClassAbilityDamageDomain domain,
            Runnable damageCall) {
        if (domain == null) throw new IllegalArgumentException("domain must not be null");
        if (damageCall == null) throw new IllegalArgumentException("damageCall must not be null");
        Deque<ClassAbilityDamageDomain> domains = ACTIVE_CLASS_ABILITY_DAMAGE.get();
        domains.addLast(domain);
        try {
            runPlayerToEliteBypass(damageCall);
        } finally {
            domains.removeLast();
            if (domains.isEmpty()) ACTIVE_CLASS_ABILITY_DAMAGE.remove();
        }
    }

    /**
     * True while the current thread is synchronously delivering damage opened by
     * {@link #runPlayerToEliteBypass(Runnable)}. Unlike the one-shot override, this remains visible
     * after the damage filter consumes the override and until every listener has completed.
     */
    public static boolean isPlayerToEliteBypassActive() {
        Integer depth = ACTIVE_PLAYER_TO_ELITE_BYPASS.get();
        return depth != null && depth > 0;
    }

    public static boolean isClassAbilityDamageActive() {
        Deque<ClassAbilityDamageDomain> domains = ACTIVE_CLASS_ABILITY_DAMAGE.get();
        boolean active = !domains.isEmpty();
        if (!active) ACTIVE_CLASS_ABILITY_DAMAGE.remove();
        return active;
    }

    public static Optional<ClassAbilityDamageDomain> currentClassAbilityDamageDomain() {
        Deque<ClassAbilityDamageDomain> domains = ACTIVE_CLASS_ABILITY_DAMAGE.get();
        ClassAbilityDamageDomain domain = domains.peekLast();
        if (domains.isEmpty()) ACTIVE_CLASS_ABILITY_DAMAGE.remove();
        return Optional.ofNullable(domain);
    }

    public static Optional<PlayerDamageSource> currentPlayerToEliteSource() {
        Deque<PlayerDamageSource> sources = ACTIVE_PLAYER_TO_ELITE_SOURCES.get();
        PlayerDamageSource source = sources.peekLast();
        if (sources.isEmpty()) ACTIVE_PLAYER_TO_ELITE_SOURCES.remove();
        return Optional.ofNullable(source);
    }

    public static void runEliteToPlayerBypass(Runnable damageCall) {
        try (Scope ignored = bypassEliteToPlayer()) {
            damageCall.run();
        }
    }

    public static void runEliteToPlayerMultiplier(double multiplier, Runnable damageCall) {
        try (Scope ignored = multiplyEliteToPlayer(multiplier)) {
            damageCall.run();
        }
    }

    public static DamageOverride consumePlayerToElite() {
        return consume(PLAYER_TO_ELITE);
    }

    public static DamageOverride consumeEliteToPlayer() {
        return consume(ELITE_TO_PLAYER);
    }

    private static Scope push(ThreadLocal<Deque<PendingOverride>> owner, DamageOverride override) {
        PendingOverride pendingOverride = new PendingOverride(override);
        Deque<PendingOverride> overrides = owner.get();
        overrides.addLast(pendingOverride);
        return () -> {
            Deque<PendingOverride> currentOverrides = owner.get();
            currentOverrides.removeLastOccurrence(pendingOverride);
            if (currentOverrides.isEmpty()) owner.remove();
        };
    }

    private static DamageOverride consume(ThreadLocal<Deque<PendingOverride>> owner) {
        Deque<PendingOverride> overrides = owner.get();
        PendingOverride pendingOverride = overrides.pollLast();
        if (overrides.isEmpty()) owner.remove();
        return pendingOverride == null ? DEFAULT_OVERRIDE : pendingOverride.override;
    }

    private static final class PendingOverride {
        private final DamageOverride override;

        private PendingOverride(DamageOverride override) {
            this.override = override;
        }
    }

    public record DamageOverride(boolean bypass, double specialMultiplier) {
    }

    public enum ClassAbilityTargetShape {
        SINGLE_TARGET,
        AREA
    }

    public enum ClassAbilityDelivery {
        DIRECT,
        PROJECTILE,
        PERIODIC,
        SUMMON
    }

    /** Stable fantasy-independent classification for passives that alter traps or blasts only. */
    public enum ClassAbilityArchetype {
        OTHER,
        TRAP,
        BLAST
    }

    public enum ClassAbilityStrikeQuality {
        NORMAL,
        GUARANTEED_CRITICAL
    }

    public record ClassAbilityDamageDomain(
            ClassAbilityTargetShape targetShape,
            ClassAbilityDelivery delivery,
            ClassAbilityArchetype archetype,
            ClassAbilityStrikeQuality strikeQuality) {
        public static final ClassAbilityDamageDomain SINGLE_TARGET_DIRECT =
                new ClassAbilityDamageDomain(ClassAbilityTargetShape.SINGLE_TARGET,
                        ClassAbilityDelivery.DIRECT, ClassAbilityArchetype.OTHER,
                        ClassAbilityStrikeQuality.NORMAL);
        public static final ClassAbilityDamageDomain SINGLE_TARGET_PROJECTILE =
                new ClassAbilityDamageDomain(ClassAbilityTargetShape.SINGLE_TARGET,
                        ClassAbilityDelivery.PROJECTILE, ClassAbilityArchetype.OTHER,
                        ClassAbilityStrikeQuality.NORMAL);
        public static final ClassAbilityDamageDomain AREA_DIRECT =
                new ClassAbilityDamageDomain(ClassAbilityTargetShape.AREA,
                        ClassAbilityDelivery.DIRECT, ClassAbilityArchetype.OTHER,
                        ClassAbilityStrikeQuality.NORMAL);
        public static final ClassAbilityDamageDomain AREA_PERIODIC =
                new ClassAbilityDamageDomain(ClassAbilityTargetShape.AREA,
                        ClassAbilityDelivery.PERIODIC, ClassAbilityArchetype.OTHER,
                        ClassAbilityStrikeQuality.NORMAL);
        public static final ClassAbilityDamageDomain AREA_TRAP =
                new ClassAbilityDamageDomain(ClassAbilityTargetShape.AREA,
                        ClassAbilityDelivery.PERIODIC, ClassAbilityArchetype.TRAP,
                        ClassAbilityStrikeQuality.NORMAL);
        public static final ClassAbilityDamageDomain AREA_BLAST =
                new ClassAbilityDamageDomain(ClassAbilityTargetShape.AREA,
                        ClassAbilityDelivery.DIRECT, ClassAbilityArchetype.BLAST,
                        ClassAbilityStrikeQuality.NORMAL);
        public static final ClassAbilityDamageDomain SINGLE_TARGET_SUMMON =
                new ClassAbilityDamageDomain(ClassAbilityTargetShape.SINGLE_TARGET,
                        ClassAbilityDelivery.SUMMON, ClassAbilityArchetype.OTHER,
                        ClassAbilityStrikeQuality.NORMAL);

        public ClassAbilityDamageDomain(
                ClassAbilityTargetShape targetShape,
                ClassAbilityDelivery delivery) {
            this(targetShape, delivery, ClassAbilityArchetype.OTHER,
                    ClassAbilityStrikeQuality.NORMAL);
        }

        public ClassAbilityDamageDomain(
                ClassAbilityTargetShape targetShape,
                ClassAbilityDelivery delivery,
                ClassAbilityArchetype archetype) {
            this(targetShape, delivery, archetype, ClassAbilityStrikeQuality.NORMAL);
        }

        public ClassAbilityDamageDomain {
            if (targetShape == null) throw new IllegalArgumentException("targetShape must not be null");
            if (delivery == null) throw new IllegalArgumentException("delivery must not be null");
            if (archetype == null) throw new IllegalArgumentException("archetype must not be null");
            if (strikeQuality == null) throw new IllegalArgumentException("strikeQuality must not be null");
        }
    }

    public record PlayerDamageSource(UUID attackId, SkillType progressionSkill) {
        public PlayerDamageSource {
            if (attackId == null) throw new IllegalArgumentException("attackId must not be null");
            if (progressionSkill == null || !progressionSkill.isWeaponSkill())
                throw new IllegalArgumentException("progressionSkill must be a weapon skill");
        }
    }

    @FunctionalInterface
    public interface Scope extends AutoCloseable {
        @Override
        void close();
    }
}
