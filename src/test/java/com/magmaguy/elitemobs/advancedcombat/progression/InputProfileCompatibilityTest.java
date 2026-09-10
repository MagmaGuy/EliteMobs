package com.magmaguy.elitemobs.advancedcombat.progression;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class InputProfileCompatibilityTest {

    @Test
    void fAbilityLayerKeepsTheExistingStoredProfileId() {
        assertEquals("java_hotbar_layer", InputProfile.JAVA_HOTBAR_LAYER.storedId());
        assertSame(
                InputProfile.JAVA_HOTBAR_LAYER,
                InputProfile.fromStoredId("java_hotbar_layer").orElseThrow());
    }
}
