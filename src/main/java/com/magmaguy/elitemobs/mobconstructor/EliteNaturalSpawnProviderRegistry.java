package com.magmaguy.elitemobs.mobconstructor;

import com.magmaguy.elitemobs.api.mind.EliteNaturalSpawnClaim;
import com.magmaguy.elitemobs.api.mind.EliteNaturalSpawnContext;
import com.magmaguy.elitemobs.api.mind.EliteNaturalSpawnProvider;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/** Owner-scoped, deterministic claim selection kept inside the EliteMobs mob stack. */
final class EliteNaturalSpawnProviderRegistry {
    private final List<Registration> registrations = new ArrayList<>();

    void register(Plugin owner, EliteNaturalSpawnProvider provider) {
        Objects.requireNonNull(owner, "owner");
        Objects.requireNonNull(provider, "provider");
        if (!owner.isEnabled()) {
            throw new IllegalStateException("Natural-spawn provider owner is not enabled");
        }
        Registration existing = registrations.stream()
                .filter(registration -> registration.owner() == owner)
                .findFirst()
                .orElse(null);
        if (existing == null) {
            registrations.add(new Registration(owner, provider));
            return;
        }
        if (existing.provider() != provider) {
            throw new IllegalArgumentException(
                    "Plugin " + owner.getName() + " already registered a natural-spawn provider");
        }
    }

    Optional<Selection> select(EliteNaturalSpawnContext context) {
        Objects.requireNonNull(context, "context");
        List<Selection> claims = new ArrayList<>();
        for (Registration registration : List.copyOf(registrations)) {
            if (!registration.owner().isEnabled()) continue;
            registration.provider().select(context).ifPresent(claim -> claims.add(
                    new Selection(registration.owner(), Objects.requireNonNull(claim, "claim"))));
        }
        if (claims.size() > 1) {
            String owners = claims.stream()
                    .map(selection -> selection.owner().getName())
                    .sorted()
                    .reduce((left, right) -> left + ", " + right)
                    .orElseThrow();
            throw new IllegalStateException(
                    "Conflicting natural-spawn claims from " + owners);
        }
        return claims.stream().findFirst();
    }

    void unregister(Plugin owner) {
        Objects.requireNonNull(owner, "owner");
        registrations.removeIf(registration -> registration.owner() == owner);
    }

    void clear() {
        registrations.clear();
    }

    record Selection(Plugin owner, EliteNaturalSpawnClaim claim) {
        Selection {
            Objects.requireNonNull(owner, "owner");
            Objects.requireNonNull(claim, "claim");
        }
    }

    private record Registration(Plugin owner, EliteNaturalSpawnProvider provider) {
    }
}
