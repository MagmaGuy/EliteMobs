package com.magmaguy.elitemobs.experimentalcombat.passives;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.api.EliteMobDeathEvent;
import com.magmaguy.elitemobs.api.PlayerDamagedByEliteMobEvent;
import com.magmaguy.elitemobs.combatsystem.CombatDamageContext;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.experimentalcombat.ExperimentalCombatRuntime;
import com.magmaguy.elitemobs.experimentalcombat.abilities.ClassControlAttribution;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.parties.PartyManager;
import com.magmaguy.elitemobs.skills.SkillType;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityKnockbackByEntityEvent;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;

/** Applies the resolved passive branch without coupling catalog/progression state to Bukkit events. */
public final class ClassPassiveRuntime implements Listener {

    private static final String MOVEMENT_MODIFIER_KEY = "experimental_class_movement";
    private static final Set<String> CONTROL_EFFECTS = Set.of(
            "slowness",
            "mining_fatigue",
            "nausea",
            "blindness",
            "weakness",
            "levitation",
            "darkness");

    private final Function<UUID, PassiveAggregate> passiveProvider;
    private final Predicate<Player> combatActive;
    private final Map<UUID, Double> appliedMovementAdjustments = new HashMap<>();
    private final Set<UUID> mechanicsActivePlayers = new HashSet<>();
    private final Set<UUID> applyingControlResistance = new HashSet<>();
    private final PassiveStateTracker state = new PassiveStateTracker();

    public ClassPassiveRuntime(Function<UUID, PassiveAggregate> passiveProvider) {
        this(passiveProvider, ExperimentalCombatRuntime::isActive);
    }

    ClassPassiveRuntime(Function<UUID, PassiveAggregate> passiveProvider, Predicate<Player> combatActive) {
        this.passiveProvider = Objects.requireNonNull(passiveProvider, "passiveProvider");
        this.combatActive = Objects.requireNonNull(combatActive, "combatActive");
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerDamagesElite(EliteMobDamagedByPlayerEvent event) {
        Player player = event.getPlayer();
        if (!combatActive.test(player)) return;
        PassiveAggregate passive = passiveProvider.apply(player.getUniqueId());
        boolean classAbilityDamage = CombatDamageContext.isClassAbilityDamageActive();
        Optional<CombatDamageContext.ClassAbilityDamageDomain> classAbilityDomain =
                CombatDamageContext.currentClassAbilityDamageDomain();
        SkillType sourceSkill = CombatDamageContext.currentPlayerToEliteSource()
                .map(CombatDamageContext.PlayerDamageSource::progressionSkill)
                .orElse(null);
        PassiveAggregate.Evaluation evaluation = passive.evaluate(context(
                player,
                passive,
                event.getEliteMobEntity(),
                event.isCriticalStrike(),
                event.isRangedAttack(),
                classAbilityDamage,
                classAbilityDomain.orElse(null),
                sourceSkill));
        event.setDamage(Math.max(0D, event.getDamage() * evaluation.outgoingDamageMultiplier()));
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEliteDamagesPlayer(PlayerDamagedByEliteMobEvent event) {
        Player player = event.getPlayer();
        if (!combatActive.test(player)) return;
        PassiveAggregate passive = passiveProvider.apply(player.getUniqueId());
        PassiveAggregate.Evaluation evaluation = passive.evaluate(context(
                player,
                passive,
                event.getEliteMobEntity(),
                false,
                event.getProjectile() != null,
                false,
                null,
                null));
        event.setDamage(Math.max(0D, event.getDamage() * evaluation.incomingDamageMultiplier()));
        state.recordHit(player.getUniqueId());
        applyMovementAdjustment(player, evaluatedMovement(player, passive));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerHeals(EntityRegainHealthEvent event) {
        if (!(event.getEntity() instanceof Player player) || !combatActive.test(player)) return;
        PassiveAggregate passive = passiveProvider.apply(player.getUniqueId());
        double multiplier = passive.evaluate(playerContext(player)).healingReceivedMultiplier();
        if (Math.abs(multiplier - 1D) < 1.0E-9D) return;
        event.setAmount(Math.max(0D, event.getAmount() * multiplier));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEliteDeath(EliteMobDeathEvent event) {
        LivingEntity dead = event.getEntityDeathEvent() == null
                ? event.getEntity() instanceof LivingEntity living ? living : null
                : event.getEntityDeathEvent().getEntity();
        Player killer = dead == null ? null : dead.getKiller();
        if (killer != null && combatActive.test(killer))
            state.recordEliteKill(killer.getUniqueId());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onKnockback(EntityKnockbackByEntityEvent event) {
        if (!(event.getEntity() instanceof Player player)
                || !combatActive.test(player)
                || eliteSource(event.getSourceEntity()) == null) return;
        double multiplier = PassiveRuntimePolicy.knockbackMultiplier(mechanics(player));
        if (Math.abs(multiplier - 1D) < 1.0E-9D) return;
        event.setFinalKnockback(event.getFinalKnockback().clone().multiply(multiplier));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPotionEffect(EntityPotionEffectEvent event) {
        if (!(event.getEntity() instanceof Player player)
                || !combatActive.test(player)
                || applyingControlResistance.contains(player.getUniqueId())) return;
        PotionEffect effect = event.getNewEffect();
        if (effect == null
                || !CONTROL_EFFECTS.contains(effect.getType().getKey().getKey())) return;
        int duration = PassiveRuntimePolicy.controlDurationTicks(effect.getDuration(), mechanics(player));
        if (duration == effect.getDuration()) return;

        event.setCancelled(true);
        PotionEffect resisted = new PotionEffect(
                effect.getType(),
                duration,
                effect.getAmplifier(),
                effect.isAmbient(),
                effect.hasParticles(),
                effect.hasIcon());
        UUID playerId = player.getUniqueId();
        applyingControlResistance.add(playerId);
        try {
            player.addPotionEffect(resisted, true);
        } finally {
            applyingControlResistance.remove(playerId);
        }
    }

    /** Ability-owned adapters consume only these typed mechanics, never passive display text. */
    public PassiveMechanics mechanics(Player player) {
        if (player == null || !combatActive.test(player)) return PassiveMechanics.NEUTRAL;
        return passiveProvider.apply(player.getUniqueId()).evaluate(playerContext(player)).mechanics();
    }

    /** Resolves grouped/solo healer clauses at cast time instead of using a stale base aggregate. */
    public double healingDoneMultiplier(Player player) {
        if (player == null || !combatActive.test(player)) return 1D;
        return passiveProvider.apply(player.getUniqueId())
                .evaluate(playerContext(player))
                .healingDoneMultiplier();
    }

    /**
     * Evaluates owner passives for class-owned damage that does not originate from a player hit.
     * Summons retain the broad class-ability domain while deliberately excluding spell-only traits.
     */
    public double outgoingClassAbilityDamageMultiplier(
            Player owner,
            EliteEntity target,
            CombatDamageContext.ClassAbilityDamageDomain domain) {
        if (owner == null
                || target == null
                || domain == null
                || !combatActive.test(owner)) return 1D;
        PassiveAggregate passive = passiveProvider.apply(owner.getUniqueId());
        return passive.evaluate(context(
                        owner,
                        passive,
                        target,
                        false,
                        domain.delivery() == CombatDamageContext.ClassAbilityDelivery.PROJECTILE,
                        true,
                        domain,
                        null))
                .outgoingDamageMultiplier();
    }

    /** Records the transient risk window opened when a class ward actually breaks. */
    public void signalWardBroken(Player player, int durationTicks) {
        if (player == null || !combatActive.test(player) || durationTicks <= 0) return;
        state.recordWardBroken(player.getUniqueId(), durationTicks);
    }

    public void reconcile(Player player, boolean mechanicsActive) {
        UUID playerId = player.getUniqueId();
        if (mechanicsActive) mechanicsActivePlayers.add(playerId);
        else {
            mechanicsActivePlayers.remove(playerId);
            state.clear(playerId);
        }
        double adjustment = mechanicsActive
                ? evaluatedMovement(player, passiveProvider.apply(player.getUniqueId()))
                : 0D;
        Double previous = appliedMovementAdjustments.get(player.getUniqueId());
        if (previous != null && Math.abs(previous - adjustment) < 1.0E-9D) return;
        applyMovementAdjustment(player, adjustment);
        if (Math.abs(adjustment) < 1.0E-9D) appliedMovementAdjustments.remove(player.getUniqueId());
        else appliedMovementAdjustments.put(player.getUniqueId(), adjustment);
    }

    public void discard(Player player) {
        UUID playerId = player.getUniqueId();
        appliedMovementAdjustments.remove(playerId);
        mechanicsActivePlayers.remove(playerId);
        applyingControlResistance.remove(playerId);
        state.clear(playerId);
        applyMovementAdjustment(player, 0D);
    }

    public void shutdown() {
        for (UUID playerId : appliedMovementAdjustments.keySet().toArray(UUID[]::new)) {
            Player player = org.bukkit.Bukkit.getPlayer(playerId);
            if (player != null) applyMovementAdjustment(player, 0D);
        }
        appliedMovementAdjustments.clear();
        mechanicsActivePlayers.clear();
        applyingControlResistance.clear();
        state.clearAll();
        HandlerList.unregisterAll(this);
    }

    /** Removes a modifier serialized before a graceful module shutdown could run. */
    public static void clearPersistedState(Player player) {
        applyMovementAdjustment(player, 0D);
    }

    private static void applyMovementAdjustment(Player player, double adjustment) {
        AttributeInstance movement = player.getAttribute(Attribute.MOVEMENT_SPEED);
        if (movement == null) return;
        NamespacedKey key = new NamespacedKey(MetadataHandler.PLUGIN, MOVEMENT_MODIFIER_KEY);
        movement.getModifiers().stream()
                .filter(modifier -> modifier.getKey().equals(key))
                .toList()
                .forEach(movement::removeModifier);
        if (Math.abs(adjustment) < 1.0E-9D) return;
        movement.addModifier(new AttributeModifier(
                key,
                Math.max(-.5D, Math.min(.5D, adjustment)),
                AttributeModifier.Operation.MULTIPLY_SCALAR_1,
                EquipmentSlotGroup.ANY));
    }

    private double evaluatedMovement(Player player, PassiveAggregate passive) {
        PassiveAggregate.Evaluation evaluation = passive.evaluate(playerContext(player));
        return Math.max(-.5D, Math.min(.5D,
                evaluation.movementSpeedAdjustment() + partyMovementAdjustment(player)));
    }

    private PassiveConditionContext playerContext(Player player) {
        UUID playerId = player.getUniqueId();
        return PassiveConditionContext.playerOnly(
                healthFraction(player),
                moving(player),
                state.recentlyHit(playerId),
                grouped(player),
                state.recentEliteKill(playerId),
                state.wardBroken(playerId));
    }

    private PassiveConditionContext context(
            Player player,
            PassiveAggregate passive,
            EliteEntity elite,
            boolean criticalHit,
            boolean rangedAttack,
            boolean classAbilityDamage,
            CombatDamageContext.ClassAbilityDamageDomain classAbilityDomain,
            SkillType sourceSkill) {
        LivingEntity target = elite == null ? null : elite.getLivingEntity();
        boolean present = target != null && target.isValid() && !target.isDead();
        boolean needsDensity = passive.requires(PassiveCondition.TARGET_ISOLATED)
                || passive.requires(PassiveCondition.TARGET_GROUPED);
        TargetDensity density = present && needsDensity ? targetDensity(target) : TargetDensity.NONE;
        AbilityDamageFacts damageFacts = abilityDamageFacts(
                classAbilityDamage, classAbilityDomain);
        return new PassiveConditionContext(
                healthFraction(player),
                moving(player),
                state.recentlyHit(player.getUniqueId()),
                grouped(player),
                present,
                present ? healthFraction(target) : 1D,
                present && (!elite.isNaturalEntity() || elite.getHealthMultiplier() > 1.5D),
                present && targetControlled(player, target),
                present && density.nearbyElites() == 0,
                present && density.nearbyElites() > 0,
                present ? player.getLocation().distance(target.getLocation()) : Double.POSITIVE_INFINITY,
                criticalHit,
                rangedAttack,
                damageFacts.classAbilityDamage(),
                damageFacts.nonSummonClassAbilityDamage(),
                damageFacts.areaClassAbilityDamage(),
                damageFacts.trapClassAbilityDamage(),
                damageFacts.blastClassAbilityDamage(),
                PassiveRuntimePolicy.isMagicWeaponSkill(sourceSkill),
                state.recentEliteKill(player.getUniqueId()),
                state.wardBroken(player.getUniqueId()));
    }

    private double partyMovementAdjustment(Player player) {
        List<Player> nearby = PartyManager.getNearbyMembers(player, player.getLocation());
        boolean grouped = nearby.stream()
                .anyMatch(member -> !member.getUniqueId().equals(player.getUniqueId()));
        List<PassiveMechanics> auras = nearby.stream()
                .filter(member -> mechanicsActivePlayers.contains(member.getUniqueId()))
                .map(this::mechanics)
                .toList();
        return PassiveRuntimePolicy.partyMovementAdjustment(auras, grouped);
    }

    private static EliteEntity eliteSource(Entity source) {
        EliteEntity direct = source == null ? null : EntityTracker.getEliteMobEntity(source);
        if (direct != null) return direct;
        if (!(source instanceof Projectile projectile)
                || !(projectile.getShooter() instanceof Entity shooter)) return null;
        return EntityTracker.getEliteMobEntity(shooter);
    }

    private static boolean grouped(Player player) {
        return PartyManager.getNearbyMembers(player, player.getLocation()).stream()
                .anyMatch(member -> !member.getUniqueId().equals(player.getUniqueId()));
    }

    private static boolean moving(Player player) {
        double horizontalSpeedSquared = player.getVelocity().getX() * player.getVelocity().getX()
                + player.getVelocity().getZ() * player.getVelocity().getZ();
        return player.isSprinting() || horizontalSpeedSquared > .01D;
    }

    static boolean targetControlled(Player player, LivingEntity target) {
        return ClassControlAttribution.controlledBy(player, target);
    }

    static AbilityDamageFacts abilityDamageFacts(
            boolean classAbilityDamage,
            CombatDamageContext.ClassAbilityDamageDomain domain) {
        if (!classAbilityDamage) return AbilityDamageFacts.NONE;
        boolean nonSummon = domain == null
                || domain.delivery() != CombatDamageContext.ClassAbilityDelivery.SUMMON;
        boolean area = domain != null
                && domain.targetShape() == CombatDamageContext.ClassAbilityTargetShape.AREA;
        boolean trap = domain != null
                && domain.archetype() == CombatDamageContext.ClassAbilityArchetype.TRAP;
        boolean blast = domain != null
                && domain.archetype() == CombatDamageContext.ClassAbilityArchetype.BLAST;
        return new AbilityDamageFacts(true, nonSummon, area, trap, blast);
    }

    private static TargetDensity targetDensity(LivingEntity target) {
        int nearbyElites = 0;
        for (Entity candidate : target.getWorld().getNearbyEntities(target.getLocation(), 7D, 5D, 7D)) {
            if (candidate == target || !(candidate instanceof LivingEntity living)) continue;
            if (EntityTracker.getEliteMobEntity(living) == null) continue;
            nearbyElites++;
            break;
        }
        return new TargetDensity(nearbyElites);
    }

    private static double healthFraction(LivingEntity entity) {
        AttributeInstance maximumHealth = entity.getAttribute(Attribute.MAX_HEALTH);
        double maximum = maximumHealth == null ? entity.getHealth() : maximumHealth.getValue();
        if (!Double.isFinite(maximum) || maximum <= 0D) return 1D;
        return Math.max(0D, Math.min(1D, entity.getHealth() / maximum));
    }

    private record TargetDensity(int nearbyElites) {
        private static final TargetDensity NONE = new TargetDensity(0);
    }

    record AbilityDamageFacts(
            boolean classAbilityDamage,
            boolean nonSummonClassAbilityDamage,
            boolean areaClassAbilityDamage,
            boolean trapClassAbilityDamage,
            boolean blastClassAbilityDamage) {
        private static final AbilityDamageFacts NONE = new AbilityDamageFacts(
                false, false, false, false, false);
    }
}
