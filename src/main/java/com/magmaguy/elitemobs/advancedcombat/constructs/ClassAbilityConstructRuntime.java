package com.magmaguy.elitemobs.advancedcombat.constructs;

import com.magmaguy.elitemobs.advancedcombat.abilities.FixedAbilitySpec;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/** Bridges the code-owned visual catalog to the packet construct lifecycle. */
public final class ClassAbilityConstructRuntime implements AutoCloseable {
    private final ClassConstructVisualRegistry registry;
    private final PacketConstructService service;
    private final ConstructLifecycleTracker lifecycle = new ConstructLifecycleTracker();
    private final LifecycleObserver observer;

    public ClassAbilityConstructRuntime(Plugin plugin) {
        this(plugin, LifecycleObserver.NOOP);
    }

    public ClassAbilityConstructRuntime(Plugin plugin, LifecycleObserver observer) {
        this(ClassConstructVisualRegistry.builtIns(), new PacketConstructService(plugin), observer);
    }

    ClassAbilityConstructRuntime(
            ClassConstructVisualRegistry registry,
            PacketConstructService service,
            LifecycleObserver observer) {
        this.registry = Objects.requireNonNull(registry, "registry");
        this.service = Objects.requireNonNull(service, "service");
        this.observer = Objects.requireNonNull(observer, "observer");
    }

    /** Returns true when the ability has and successfully displayed a physical construct. */
    public boolean present(Player caster, FixedAbilitySpec spec, Location origin) {
        Objects.requireNonNull(caster, "caster");
        Objects.requireNonNull(spec, "spec");
        Objects.requireNonNull(origin, "origin");
        ClassConstructVisualRegistry.Plan plan = registry.find(spec.id()).orElse(null);
        if (plan == null) return false;
        int durationTicks = Math.max(1, spec.tuning().durationTicks());
        Optional<PacketConstructService.Handle> spawned = switch (plan.anchorMode()) {
            case FIXED -> service.spawn(
                    caster, origin, plan.definition(), durationTicks, this::cleared);
            case FOLLOW_CASTER -> service.spawnFollowing(
                    caster, caster::getLocation, plan.definition(), durationTicks, this::cleared);
        };
        if (spawned.isEmpty()) return false;
        ConstructLifecycleTracker.Lease lease = lifecycle.open(
                spawned.orElseThrow().id(), caster.getUniqueId(), spec.id(),
                durationTicks, plan.anchorMode());
        observer.spawned(lease.casterId(), lease.abilityId(), lease.durationTicks(), lease.anchorMode());
        return true;
    }

    private void cleared(UUID constructId) {
        lifecycle.close(constructId).ifPresent(lease ->
                observer.cleared(lease.casterId(), lease.abilityId(), lease.anchorMode()));
    }

    public void deactivate(Player player) {
        service.deactivate(player);
    }

    @Override
    public void close() {
        service.close();
    }

    public interface LifecycleObserver {
        LifecycleObserver NOOP = new LifecycleObserver() {
            @Override
            public void spawned(UUID casterId, String abilityId, int durationTicks,
                                ClassConstructVisualRegistry.AnchorMode anchorMode) {
            }

            @Override
            public void cleared(UUID casterId, String abilityId,
                                ClassConstructVisualRegistry.AnchorMode anchorMode) {
            }
        };

        void spawned(UUID casterId, String abilityId, int durationTicks,
                     ClassConstructVisualRegistry.AnchorMode anchorMode);

        void cleared(UUID casterId, String abilityId,
                     ClassConstructVisualRegistry.AnchorMode anchorMode);
    }
}
