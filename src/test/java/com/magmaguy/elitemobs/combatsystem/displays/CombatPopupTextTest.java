package com.magmaguy.elitemobs.combatsystem.displays;

import com.magmaguy.elitemobs.experimentalcombat.presentation.ClassPresentationTheme;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CombatPopupTextTest {
    @Test
    void ordinaryDamageKeepsTheExistingSingleValue() {
        assertEquals("15.0", CombatPopupText.damageAmount(false, 100D, 15D, 0D));
    }

    @Test
    void classAbilityBonusIsShownBesideTheUnchangedTotal() {
        assertEquals(
                "15.0 &8[" + ClassPresentationTheme.abilityDamage("+5.0") + "&8]",
                CombatPopupText.damageAmount(false, 100D, 15D, 5D));
    }

    @Test
    void scaledCombatUsesPercentagesForBothValues() {
        assertEquals(
                "15% &8[" + ClassPresentationTheme.abilityDamage("+5.0%") + "&8]",
                CombatPopupText.damageAmount(true, 100D, 15D, 5D));
    }
}
