package com.magmaguy.elitemobs.experimentalcombat;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ClassIdentityLabelFormatterTest {

    @Test
    void placesEffectiveLevelBeforeTheStyledClassName() {
        assertEquals("&f[31] &bGuardian", ClassIdentityLabelFormatter.format("Guardian", 31));
    }
}
