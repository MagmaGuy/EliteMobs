package com.magmaguy.elitemobs.api.mind;

import java.util.Optional;

/**
 * Selects an exact natural-spawn claim without mutating the world.
 *
 * <p>EliteMobs evaluates every registered provider before it commits a claim. Providers must
 * therefore keep this callback side-effect free. More than one claim fails closed instead of
 * choosing a plugin by listener order.</p>
 */
@FunctionalInterface
public interface EliteNaturalSpawnProvider {
    Optional<EliteNaturalSpawnClaim> select(EliteNaturalSpawnContext context);
}
