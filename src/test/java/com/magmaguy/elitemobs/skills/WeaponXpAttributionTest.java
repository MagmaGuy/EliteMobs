package com.magmaguy.elitemobs.skills;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WeaponXpAttributionTest {

    @Test
    void castTimeWandAndStaffDamageKeepIndependentXpShares() {
        Map<SkillType, Long> shares = WeaponXpAttribution.distribute(
                1_000L,
                100D,
                Map.of(SkillType.WANDS, 60D, SkillType.STAVES, 40D));

        assertEquals(600L, shares.get(SkillType.WANDS));
        assertEquals(400L, shares.get(SkillType.STAVES));
    }

    @Test
    void sourceLessDamageNeverInventsWeaponXp() {
        assertTrue(WeaponXpAttribution.distribute(1_000L, 100D, Map.of()).isEmpty());
    }
}
