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
    void derivesCountUptimeDamageCadenceAndCostFromCanonicalAbilityTuning() {
        FixedAbilitySpec spec = registry.require("summoner.signature");
        ClassMinionBalanceContract contract = ClassMinionBalanceContract.from(
                spec, ClassMinionTheme.ANIMAL, 61);

        assertEquals(spec.tuning().projectileCount(), contract.summonCount());
        assertEquals(spec.tuning().durationTicks(), contract.uptimeTicks());
        assertEquals(spec.tuning().durationTicks() / spec.tuning().repetitions(),
                contract.attackPeriodTicks());
        assertEquals(spec.tuning().damageMultiplier(), contract.damageMultiplierPerHit());
        assertEquals(spec.resourceCost(), contract.resourceCost());
        assertEquals(3, contract.ownerCap());
    }

    @Test
    void healthAndDamageScaleUpWithEffectiveClassLevel() {
        FixedAbilitySpec spec = registry.require("demonologist.signature");
        ClassMinionBalanceContract low = ClassMinionBalanceContract.from(
                spec, ClassMinionTheme.NETHER, 91);
        ClassMinionBalanceContract high = ClassMinionBalanceContract.from(
                spec, ClassMinionTheme.NETHER, 100);

        assertTrue(high.maxHealth() > low.maxHealth());
        assertTrue(high.scaledDamageMultiplierPerHit() > low.scaledDamageMultiplierPerHit());
    }

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

    @Test
    void physicalSummonsUseVisibleUptimeAndExplicitMultiSecondCadence() {
        for (String id : java.util.List.of(
                "necromancer.signature", "lich.signature", "plaguebringer.signature",
                "summoner.signature", "demonologist.signature", "spiritbinder.signature")) {
            FixedAbilitySpec spec = registry.require(id);
            ClassMinionTheme theme = ClassMinionTheme.forAbility(id).orElseThrow();
            ClassMinionBalanceContract contract = ClassMinionBalanceContract.from(spec, theme, 61);
            assertTrue(contract.uptimeTicks() >= 25 * 20, id);
            assertTrue(contract.uptimeTicks() <= 40 * 20, id);
            assertTrue(contract.attackPeriodTicks() >= 40, id);
        }
    }
}
