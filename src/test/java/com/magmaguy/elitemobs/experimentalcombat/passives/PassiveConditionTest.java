package com.magmaguy.elitemobs.experimentalcombat.passives;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PassiveConditionTest {

    @Test
    void conditionVocabularyUsesExplicitCombatFacts() {
        PassiveConditionContext context = new PassiveConditionContext(
                .24D, true, true, true,
                true, .35D, true, true, true, false,
                22D, true, true, true);

        assertTrue(PassiveCondition.HEALTH_BELOW_75.matches(context));
        assertTrue(PassiveCondition.HEALTH_BELOW_50.matches(context));
        assertTrue(PassiveCondition.HEALTH_BELOW_25.matches(context));
        assertTrue(PassiveCondition.MOVING.matches(context));
        assertTrue(PassiveCondition.RECENTLY_HIT.matches(context));
        assertTrue(PassiveCondition.GROUPED.matches(context));
        assertTrue(PassiveCondition.TARGET_WOUNDED.matches(context));
        assertTrue(PassiveCondition.TARGET_BOSS.matches(context));
        assertTrue(PassiveCondition.TARGET_CONTROLLED.matches(context));
        assertTrue(PassiveCondition.TARGET_ISOLATED.matches(context));
        assertTrue(PassiveCondition.LONG_RANGE.matches(context));
        assertTrue(PassiveCondition.CRITICAL_HIT.matches(context));
        assertTrue(PassiveCondition.RANGED_ATTACK.matches(context));
        assertTrue(PassiveCondition.CLASS_ABILITY_DAMAGE.matches(context));

        assertFalse(PassiveCondition.STANDING.matches(context));
        assertFalse(PassiveCondition.NOT_RECENTLY_HIT.matches(context));
        assertFalse(PassiveCondition.SOLO.matches(context));
        assertFalse(PassiveCondition.TARGET_HEALTHY.matches(context));
        assertFalse(PassiveCondition.TARGET_ORDINARY.matches(context));
        assertFalse(PassiveCondition.TARGET_UNCONTROLLED.matches(context));
        assertFalse(PassiveCondition.TARGET_GROUPED.matches(context));
        assertFalse(PassiveCondition.CLOSE_RANGE.matches(context));
    }

    @Test
    void targetConditionsNeverMatchAPlayerOnlyContext() {
        PassiveConditionContext context = PassiveConditionContext.playerOnly(.5D, false, false, false);

        assertFalse(PassiveCondition.TARGET_WOUNDED.matches(context));
        assertFalse(PassiveCondition.TARGET_ORDINARY.matches(context));
        assertFalse(PassiveCondition.TARGET_UNCONTROLLED.matches(context));
        assertFalse(PassiveCondition.TARGET_ISOLATED.matches(context));
        assertFalse(PassiveCondition.CLOSE_RANGE.matches(context));
    }

    @Test
    void spellAndTransientStateConditionsUseExplicitRuntimeFacts() {
        PassiveConditionContext classSpell = context(true, true, false, false, false,
                false, false, false);
        PassiveConditionContext summonedAbility = context(true, false, false, false, false,
                false, false, false);
        PassiveConditionContext areaSpell = context(true, true, true, false, false,
                false, false, false);
        PassiveConditionContext trap = context(true, true, false, true, false,
                false, false, false);
        PassiveConditionContext blast = context(true, true, true, false, true,
                false, false, false);
        PassiveConditionContext magicWeapon = context(false, false, false, false, false,
                true, false, false);
        PassiveConditionContext physicalAttack = context(false, false, false, false, false,
                false, false, false);
        PassiveConditionContext chainedKill = context(false, false, false, false, false,
                false, true, false);
        PassiveConditionContext brokenWard = context(false, false, false, false, false,
                false, false, true);

        assertTrue(PassiveCondition.SPELL_DAMAGE.matches(classSpell));
        assertTrue(PassiveCondition.SPELL_DAMAGE.matches(magicWeapon));
        assertFalse(PassiveCondition.SPELL_DAMAGE.matches(summonedAbility));
        assertTrue(PassiveCondition.CLASS_ABILITY_DAMAGE.matches(summonedAbility));
        assertTrue(PassiveCondition.AREA_CLASS_ABILITY_DAMAGE.matches(areaSpell));
        assertFalse(PassiveCondition.AREA_CLASS_ABILITY_DAMAGE.matches(classSpell));
        assertTrue(PassiveCondition.TRAP_CLASS_ABILITY_DAMAGE.matches(trap));
        assertTrue(PassiveCondition.BLAST_CLASS_ABILITY_DAMAGE.matches(blast));
        assertFalse(PassiveCondition.SPELL_DAMAGE.matches(physicalAttack));
        assertTrue(PassiveCondition.RECENT_ELITE_KILL.matches(chainedKill));
        assertFalse(PassiveCondition.RECENT_ELITE_KILL.matches(physicalAttack));
        assertTrue(PassiveCondition.WARD_BROKEN.matches(brokenWard));
        assertFalse(PassiveCondition.WARD_BROKEN.matches(physicalAttack));
    }

    private static PassiveConditionContext context(
            boolean classAbilityDamage,
            boolean nonSummonClassAbilityDamage,
            boolean areaClassAbilityDamage,
            boolean trapClassAbilityDamage,
            boolean blastClassAbilityDamage,
            boolean magicWeaponDamage,
            boolean recentEliteKill,
            boolean wardBroken) {
        return new PassiveConditionContext(
                1D, false, false, false,
                true, 1D, false, false, true, false,
                8D, false, false, classAbilityDamage,
                nonSummonClassAbilityDamage, areaClassAbilityDamage,
                trapClassAbilityDamage, blastClassAbilityDamage,
                magicWeaponDamage, recentEliteKill, wardBroken);
    }
}
