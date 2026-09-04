package com.magmaguy.elitemobs.mobconstructor;

import org.bukkit.entity.EntityType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NativeMindActorDefaultsTest {

    @Test
    void unconfiguredCarrierGetsDeterministicEliteName() {
        assertEquals(
                "&fLvl &2$level &fElite &2Camel Husk",
                NativeMindActorDefaults.nameTemplate(EntityType.CAMEL_HUSK));
    }

    @Test
    void unconfiguredCarrierPreservesItsNativeBaseHealth() {
        assertEquals(6.0D, NativeMindActorDefaults.baseHealth(null, 6.0D));
    }
}
