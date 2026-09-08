package com.magmaguy.elitemobs.experimentalcombat.progression;

import java.util.Objects;
import java.util.UUID;

/**
 * Raw persisted player-wide Experimental Combat selections.
 *
 * <p>Form and input identifiers are stable catalog identifiers. A null selection means that
 * the player has not selected that part of the profile yet. Other values are intentionally
 * tolerated here so the Module can repair corrupt legacy rows before publishing them.</p>
 */
public record StoredClassProfile(
        UUID playerId,
        String selectedFormId,
        String selectedInputId,
        int catalogVersion) {


    public StoredClassProfile {
        Objects.requireNonNull(playerId, "playerId");
    }
}
