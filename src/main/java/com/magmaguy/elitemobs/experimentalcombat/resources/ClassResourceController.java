package com.magmaguy.elitemobs.experimentalcombat.resources;

import com.magmaguy.elitemobs.experimentalcombat.classes.ClassResourceType;
import com.magmaguy.elitemobs.presentation.experience.ExperienceBarLease;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

/** Owns fixed class-resource rules and their client-only XP-bar leases. */
public final class ClassResourceController {
    private final Map<ClassResourceType, ClassResourceDefinition> resourceDefinitions;
    private final Map<UUID, ResourceState> states = new HashMap<>();
    private long currentTick;

    public ClassResourceController(Map<ClassResourceType, ClassResourceDefinition> resourceDefinitions) {
        Objects.requireNonNull(resourceDefinitions, "resourceDefinitions");
        if (!resourceDefinitions.keySet().equals(EnumSet.allOf(ClassResourceType.class)))
            throw new IllegalArgumentException("Every class resource type requires one resource definition");
        for (Map.Entry<ClassResourceType, ClassResourceDefinition> entry : resourceDefinitions.entrySet()) {
            Objects.requireNonNull(entry.getKey(), "resourceDefinitions contains a null type");
            ClassResourceDefinition definition = Objects.requireNonNull(
                    entry.getValue(), "resourceDefinitions contains a null definition");
            if (definition.type() != entry.getKey())
                throw new IllegalArgumentException("Resource key does not match definition " + definition.type());
        }
        this.resourceDefinitions = Map.copyOf(resourceDefinitions);
    }

    public void open(Player player, ClassResourceType type) {
        open(player, type, null);
    }

    /**
     * Opens the HUD for one combat context. A suspended state resumes only for the same run token;
     * entering another instance cannot inherit the previous run's resource economy.
     */
    public void open(Player player, ClassResourceType type, UUID runToken) {
        ResourceState existing = states.get(player.getUniqueId());
        if (existing != null && existing.type == type && Objects.equals(existing.runToken, runToken)) {
            if (existing.lease != null) existing.lease.close();
            existing.lease = ExperienceBarLease.acquire(player, "experimental_combat_resource");
            render(existing);
            return;
        }
        close(player);
        double initial = rules(type).initialAmount();
        ExperienceBarLease lease = ExperienceBarLease.acquire(player, "experimental_combat_resource");
        ResourceState state = new ResourceState(type, runToken, initial, 0L, lease);
        states.put(player.getUniqueId(), state);
        render(state);
    }

    public void close(Player player) {
        ResourceState state = states.remove(player.getUniqueId());
        if (state != null && state.lease != null) state.lease.close();
    }

    public void discard(Player player) {
        ResourceState state = states.remove(player.getUniqueId());
        if (state != null) ExperienceBarLease.discard(player);
    }

    /** Detaches the client HUD while retaining the exact run-scoped amount for reconnect. */
    public void suspend(Player player) {
        ResourceState state = states.get(player.getUniqueId());
        if (state == null) return;
        ExperienceBarLease.discard(player);
        state.lease = null;
    }

    public void tick(Iterable<? extends Player> players, Predicate<UUID> inCombat) {
        currentTick += 20L;
        for (Player player : players) {
            ResourceState state = states.get(player.getUniqueId());
            if (state == null) continue;
            boolean playerInCombat = inCombat.test(player.getUniqueId());
            ClassResourceDefinition rules = rules(state.type);
            double delta = playerInCombat ? rules.inCombatTickDelta() : rules.outOfCombatTickDelta();
            if (currentTick < state.recoveryBlockedUntil && delta > 0D) delta = 0D;
            set(state, state.amount + delta);
        }
    }

    public boolean trySpend(Player player, double amount) {
        requireSpendAmount(amount);
        ResourceState state = states.get(player.getUniqueId());
        if (state == null) return false;
        if (state.amount + 1.0E-9D < amount) return false;
        set(state, state.amount - amount);
        return true;
    }

    public boolean canAfford(Player player, double amount) {
        requireSpendAmount(amount);
        ResourceState state = states.get(player.getUniqueId());
        return state != null && state.amount + 1.0E-9D >= amount;
    }

    public void refund(Player player, double amount) {
        requireSpendAmount(amount);
        ResourceState state = states.get(player.getUniqueId());
        if (state != null) set(state, state.amount + amount);
    }

    /** Grants an explicit class-mechanic resource burst, clamped by the active resource rules. */
    public void grant(Player player, double amount) {
        if (!Double.isFinite(amount) || amount <= 0D) return;
        ResourceState state = states.get(player.getUniqueId());
        if (state != null) set(state, state.amount + amount);
    }

    public void onDamageDealt(Player player, double actualDamage) {
        ResourceState state = states.get(player.getUniqueId());
        if (state == null) return;
        double gain = rules(state.type).damageDealtHealthEquivalentGain();
        if (gain > 0D) gainScaledToHealth(player, state, actualDamage, gain);
    }

    public void onDamageReceived(Player player, double actualDamage) {
        ResourceState state = states.get(player.getUniqueId());
        if (state == null) return;
        ClassResourceDefinition rules = rules(state.type);
        if (rules.damageReceivedHealthEquivalentGain() > 0D)
            gainScaledToHealth(player, state, actualDamage, rules.damageReceivedHealthEquivalentGain());
        if (rules.damageReceivedFlatChange() != 0D)
            set(state, state.amount + rules.damageReceivedFlatChange());
        if (rules.recoveryDelayAfterDamageTicks() > 0L)
            state.recoveryBlockedUntil = currentTick + rules.recoveryDelayAfterDamageTicks();
    }

    public void onEffectiveHealing(Player player, double healthRestored) {
        ResourceState state = states.get(player.getUniqueId());
        if (state == null || healthRestored <= 0D) return;
        double gain = rules(state.type).healingHealthEquivalentGain();
        if (gain > 0D) gainScaledToHealth(player, state, healthRestored, gain);
    }

    public void onDamagePrevented(Player player, double damagePrevented) {
        ResourceState state = states.get(player.getUniqueId());
        if (state == null || damagePrevented <= 0D) return;
        double gain = rules(state.type).preventedDamageHealthEquivalentGain();
        if (gain > 0D) gainScaledToHealth(player, state, damagePrevented, gain);
    }

    public void onTaunt(Player player, int affectedEnemies) {
        ResourceState state = states.get(player.getUniqueId());
        if (state == null || affectedEnemies <= 0) return;
        ClassResourceDefinition rules = rules(state.type);
        if (rules.tauntGainPerEnemy() <= 0D) return;
        set(state, state.amount + Math.min(
                rules.tauntGainCap(), affectedEnemies * rules.tauntGainPerEnemy()));
    }

    public Optional<Snapshot> snapshot(UUID playerId) {
        ResourceState state = states.get(playerId);
        return state == null
                ? Optional.empty()
                : Optional.of(new Snapshot(state.type, state.amount, rules(state.type).maximum()));
    }

    public boolean isOpen(UUID playerId, ClassResourceType type) {
        ResourceState state = states.get(playerId);
        return state != null && state.type == type && state.lease != null;
    }

    public boolean isOpen(UUID playerId, ClassResourceType type, UUID runToken) {
        ResourceState state = states.get(playerId);
        return state != null && state.type == type && Objects.equals(state.runToken, runToken)
                && state.lease != null;
    }

    public void shutdown() {
        for (ResourceState state : states.values())
            if (state.lease != null) state.lease.close();
        states.clear();
    }

    private void gainScaledToHealth(
            Player player,
            ResourceState state,
            double healthAmount,
            double fullHealthEquivalentGain) {
        AttributeInstance maxHealth = player.getAttribute(Attribute.MAX_HEALTH);
        double maximumHealth = Math.max(1D, maxHealth == null ? player.getHealth() : maxHealth.getValue());
        set(state, state.amount + healthAmount / maximumHealth * fullHealthEquivalentGain);
    }

    private void set(ResourceState state, double amount) {
        double maximum = rules(state.type).maximum();
        double bounded = Math.max(0D, Math.min(maximum, amount));
        if (Math.abs(bounded - state.amount) < 1.0E-9D) return;
        state.amount = bounded;
        render(state);
    }

    private void render(ResourceState state) {
        double maximum = rules(state.type).maximum();
        if (state.lease != null)
            state.lease.render(state.amount / maximum, (int) Math.round(state.amount));
    }

    private ClassResourceDefinition rules(ClassResourceType type) {
        ClassResourceDefinition rules = resourceDefinitions.get(type);
        if (rules == null) throw new IllegalArgumentException("No resource rules for " + type);
        return rules;
    }

    private static void requireSpendAmount(double amount) {
        if (!Double.isFinite(amount) || amount < 0D)
            throw new IllegalArgumentException("Resource spend amount must be finite and non-negative");
    }

    public record Snapshot(ClassResourceType type, double amount, double maximum) {
        public double fraction() {
            return maximum <= 0D ? 0D : amount / maximum;
        }
    }

    private static final class ResourceState {
        private final ClassResourceType type;
        private final UUID runToken;
        private ExperienceBarLease lease;
        private double amount;
        private long recoveryBlockedUntil;

        private ResourceState(
                ClassResourceType type,
                UUID runToken,
                double amount,
                long recoveryBlockedUntil,
                ExperienceBarLease lease) {
            this.type = type;
            this.runToken = runToken;
            this.amount = amount;
            this.recoveryBlockedUntil = recoveryBlockedUntil;
            this.lease = lease;
        }
    }
}
