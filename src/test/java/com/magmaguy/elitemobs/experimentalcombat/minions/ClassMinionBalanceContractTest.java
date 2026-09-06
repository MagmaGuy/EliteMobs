package com.magmaguy.elitemobs.experimentalcombat.minions;

import com.magmaguy.elitemobs.experimentalcombat.abilities.FixedAbilityRegistry;
import com.magmaguy.elitemobs.experimentalcombat.abilities.FixedAbilitySpec;
import com.magmaguy.elitemobs.experimentalcombat.content.BuiltInClassContent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClassMinionBalanceContractTest {

    private final FixedAbilityRegistry registry = BuiltInClassContent.abilityRegistry();

    @Test
    void summonHealthAndDamageContinueScalingPastTheSoftCap() {
        FixedAbilitySpec spec = registry.require("demonologist.signature");
        ClassMinionBalanceContract atSoftCap = ClassMinionBalanceContract.from(
                spec, ClassMinionTheme.NETHER, 100);
        ClassMinionBalanceContract afterSoftCap = ClassMinionBalanceContract.from(
                spec, ClassMinionTheme.NETHER, 101);

        assertTrue(afterSoftCap.maxHealth() > atSoftCap.maxHealth());
        assertTrue(afterSoftCap.scaledDamageMultiplierPerHit()
                > atSoftCap.scaledDamageMultiplierPerHit());
    }

    @Test
    void supportSummonsKeepDamageAtZeroButRetainARealSurvivabilityContract() {
        ClassMinionBalanceContract spirit = ClassMinionBalanceContract.from(
                registry.require("spiritbinder.signature"), ClassMinionTheme.SPIRIT, 100);

        assertEquals(0D, spirit.damageMultiplierPerHit());
        assertTrue(spirit.maxHealth() > 0D);
        assertTrue(spirit.uptimeTicks() > 0);
    }

}
