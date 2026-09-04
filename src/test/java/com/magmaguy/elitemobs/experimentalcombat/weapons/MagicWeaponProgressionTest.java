package com.magmaguy.elitemobs.experimentalcombat.weapons;

import com.magmaguy.elitemobs.combatsystem.CombatDamageContext;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.freeminecraftmodels.api.magic.MagicWeaponKind;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MagicWeaponProgressionTest {

    @Test
    void stavesAndWandsAreIndependentCanonicalWeaponSkills() throws NoSuchFieldException {
        assertNotEquals(SkillType.STAVES, SkillType.WANDS);
        assertNotEquals(SkillType.STAVES.getColumnName(), SkillType.WANDS.getColumnName());
        assertTrue(Arrays.asList(SkillType.getWeaponSkills()).containsAll(
                java.util.List.of(SkillType.STAVES, SkillType.WANDS)));

        // The hydrated player profile owns separate persisted counters. This guards against
        // accidentally aliasing either prototype family to SPEARS or another existing skill.
        assertEquals(long.class, PlayerData.class.getDeclaredField("skillXP_STAVES").getType());
        assertEquals(long.class, PlayerData.class.getDeclaredField("skillXP_WANDS").getType());
    }

    @Test
    void fmmWeaponFamiliesBindToTheirOwnProgressionSkills() {
        assertEquals(SkillType.STAVES,
                FmmMagicWeaponAdapter.progressionSkill(MagicWeaponKind.STAFF));
        assertEquals(SkillType.WANDS,
                FmmMagicWeaponAdapter.progressionSkill(MagicWeaponKind.WAND));
        assertEquals("fmm_default_arcane_staff", ExperimentalMagicWeaponItems.STAFF_FMM_ITEM_ID);
        assertEquals("fmm_default_arcane_wand", ExperimentalMagicWeaponItems.WAND_FMM_ITEM_ID);
    }

    @Test
    void fullDebugLoadoutNamesBothRegisteredScalableItems() {
        assertEquals(
                java.util.List.of(
                        ExperimentalMagicWeaponItems.STAFF_ITEM_ID,
                        ExperimentalMagicWeaponItems.WAND_ITEM_ID),
                ExperimentalMagicWeaponItems.debugLoadoutIds());
    }

    @Test
    void delayedMagicDamageCarriesItsCastTimeSkillThroughTheEliteDamageCall() {
        UUID attackId = UUID.randomUUID();
        AtomicReference<CombatDamageContext.PlayerDamageSource> observed = new AtomicReference<>();

        CombatDamageContext.runPlayerToEliteBypass(
                new CombatDamageContext.PlayerDamageSource(attackId, SkillType.WANDS),
                () -> observed.set(CombatDamageContext.currentPlayerToEliteSource().orElseThrow()));

        assertEquals(attackId, observed.get().attackId());
        assertEquals(SkillType.WANDS, observed.get().progressionSkill());
        assertTrue(CombatDamageContext.currentPlayerToEliteSource().isEmpty());
    }
}
