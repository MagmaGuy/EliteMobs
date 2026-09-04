package com.magmaguy.elitemobs.api.mind;

import org.junit.jupiter.api.Test;

import java.util.EnumSet;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EliteMindBodyCapabilitiesTest {
    private static final EliteMindBodyCapabilities CAPABILITIES = new EliteMindBodyCapabilities(
            EnumSet.allOf(EliteMindBodyLocomotion.class),
            EnumSet.of(
                    EliteMindBodyLocomotion.GROUNDED,
                    EliteMindBodyLocomotion.FLYING,
                    EliteMindBodyLocomotion.AQUATIC,
                    EliteMindBodyLocomotion.AMPHIBIOUS),
            true,
            0.0625D,
            16.0D,
            false,
            true,
            false);

    @Test
    void acceptsEverySupportedProfileAndCollisionMode() {
        for (EliteMindBodyLocomotion locomotion : EliteMindBodyLocomotion.values()) {
            assertDoesNotThrow(() -> CAPABILITIES.validate(
                    new EliteMindBodyProfile(locomotion, 2.0D, false)));
        }
        assertTrue(CAPABILITIES.canPathfind(EliteMindBodyLocomotion.FLYING));
        assertFalse(CAPABILITIES.canPathfind(EliteMindBodyLocomotion.STATIONARY));
    }

    @Test
    void rejectsScaleOutsideNativeBounds() {
        assertThrows(IllegalArgumentException.class, () -> CAPABILITIES.validate(
                new EliteMindBodyProfile(EliteMindBodyLocomotion.GROUNDED, 16.1D, true)));
    }

    @Test
    void rejectsDisabledCollisionWhenAdapterCannotToggleIt() {
        EliteMindBodyCapabilities fixedCollision = new EliteMindBodyCapabilities(
                EnumSet.of(EliteMindBodyLocomotion.GROUNDED),
                EnumSet.of(EliteMindBodyLocomotion.GROUNDED),
                false,
                1.0D,
                1.0D,
                false,
                false,
                false);

        assertThrows(IllegalArgumentException.class, () -> fixedCollision.validate(
                new EliteMindBodyProfile(EliteMindBodyLocomotion.GROUNDED, 1.0D, false)));
    }
}
