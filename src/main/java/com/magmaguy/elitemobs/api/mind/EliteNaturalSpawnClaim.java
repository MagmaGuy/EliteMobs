package com.magmaguy.elitemobs.api.mind;

/**
 * One external mode's exclusive decision for a natural creature spawn.
 *
 * <p>Providers either suppress the vanilla carrier or replace it with one exact native Mind
 * actor. Returning no claim leaves the historical EliteMobs conversion path unchanged.</p>
 */
public sealed interface EliteNaturalSpawnClaim
        permits EliteNaturalSpawnReplacement, EliteNaturalSpawnSuppression {
}
