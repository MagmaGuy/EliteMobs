package com.magmaguy.elitemobs.advancedcombat.abilities;

import com.magmaguy.elitemobs.combatsystem.CombatDamageContext;
import com.magmaguy.elitemobs.skills.SkillType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TimedCombatModifierDomainTest {
    @Test
    void spellWindowIncludesMagicWeaponsAndDirectAbilitiesButExcludesSummons() {
        assertTrue(TimedCombatModifiers.isSpellDamage(SkillType.WANDS, null));
        assertTrue(TimedCombatModifiers.isSpellDamage(
                null, CombatDamageContext.ClassAbilityDamageDomain.SINGLE_TARGET_DIRECT));
        assertFalse(TimedCombatModifiers.isSpellDamage(
                null, CombatDamageContext.ClassAbilityDamageDomain.SINGLE_TARGET_SUMMON));
        assertFalse(TimedCombatModifiers.isSpellDamage(SkillType.SWORDS, null));
    }
}
