package com.magmaguy.elitemobs.advancedcombat.progression;

import java.util.Optional;

/** External runtime/content gate for one root class tree. */
@FunctionalInterface
public interface ClassContentAvailability {
    ClassContentAvailability ALL_AVAILABLE = ignored -> Optional.empty();

    Optional<String> unavailableReason(String rootFormId);
}
