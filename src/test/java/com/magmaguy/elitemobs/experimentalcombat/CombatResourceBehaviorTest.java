package com.magmaguy.elitemobs.experimentalcombat;

import java.util.Objects;

import com.magmaguy.elitemobs.combatsystem.CombatDamageContext;
import com.magmaguy.elitemobs.experimentalcombat.classes.AbilitySlot;
import org.bukkit.attribute.Attribute;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionEffect;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockbukkit.mockbukkit.MockBukkit;
import static org.junit.jupiter.api.Assertions.*;

class CombatResourceBehaviorTest extends CombatBehaviorFixture {
    @Test
    void manaWardAppliesShieldAndSpendsManaBeforeAnotherCast() {
        assertTrue(module.useAbility(player, AbilitySlot.UTILITY).successful());
        var shield = player.getPotionEffect(PotionEffectType.ABSORPTION);
        assertNotNull(shield);
        assertEquals(201, shield.getDuration());
        assertEquals(0, shield.getAmplifier());
        assertEquals(7.995, incomingDamage(), 0.000001);

        player.removePotionEffect(PotionEffectType.ABSORPTION);
        assertFalse(module.useAbility(player, AbilitySlot.UTILITY).successful());
        assertFalse(player.hasPotionEffect(PotionEffectType.ABSORPTION),
                "A cast without enough mana must not apply its effect");

        module.close();
        module = null;
        assertEquals(10D, incomingDamage(), "Closing combat must remove its damage modifier");
    }

    @ParameterizedTest
    @CsvSource({"spellcaster,1,1,absorption", "guardian,31,0,absorption",
            "arcane_knight,91,2,absorption", "occultist,31,2,glowing",
            "plaguebringer,91,3,slowness"})
    void scheduledRecoveryFundsUtilityOnlyAfterEnoughUpdates(
            String form, int level, int initialCasts, String status) {
        assertTrue(module.setClassLevelForAdministration(player, form, level).applied());
        var effect = Objects.requireNonNull(org.bukkit.Registry.EFFECT.get(org.bukkit.NamespacedKey.minecraft(status)));
        var recipient = effect.equals(PotionEffectType.ABSORPTION) ? player : target().getLivingEntity();
        for (int cast = 0; cast < initialCasts; cast++)
            assertTrue(module.useAbility(player, AbilitySlot.UTILITY).successful());
        assertFalse(module.useAbility(player, AbilitySlot.UTILITY).successful());
        recipient.removePotionEffect(effect);
        assertFalse(recipient.hasPotionEffect(effect));
        recoverAndCast(AbilitySlot.UTILITY);
        assertNotNull(recipient.getPotionEffect(effect));
        assertFalse(module.useAbility(player, AbilitySlot.UTILITY).successful());
    }

    @Test
    void takingDamageSlowsFocusRecoveryBeforeAnotherMarkCanBeCast() {
        int uninterrupted = updatesUntilRecoveredMark(false);
        int damaged = updatesUntilRecoveredMark(true);
        assertTrue(damaged > uninterrupted,
                "Accepted damage must reset the Focus recovery bonus, not prevent baseline recovery");
    }

    @SuppressWarnings("removal")
    private int updatesUntilRecoveredMark(boolean interruptRecovery) {
        assertTrue(module.selectForm(player, "spellcaster").accepted());
        assertTrue(module.setClassLevelForAdministration(player, "ranger", 1).applied());
        var enemy = target().getLivingEntity();
        for (int cast = 0; cast < 4; cast++)
            assertTrue(module.useAbility(player, AbilitySlot.UTILITY).successful());
        enemy.removePotionEffect(PotionEffectType.GLOWING);
        assertFalse(module.useAbility(player, AbilitySlot.UTILITY).successful());
        var scheduler = MockBukkit.getMock().getScheduler();
        for (int update = 1; update <= 120; update++) {
            if (interruptRecovery)
                MockBukkit.getMock().getPluginManager().callEvent(new org.bukkit.event.entity.EntityDamageEvent(
                        player, org.bukkit.event.entity.EntityDamageEvent.DamageCause.FALL, 1D));
            scheduler.performTicks(20);
            if (module.useAbility(player, AbilitySlot.UTILITY).successful()) {
                assertTrue(enemy.hasPotionEffect(PotionEffectType.GLOWING));
                return update;
            }
            assertFalse(enemy.hasPotionEffect(PotionEffectType.GLOWING));
        }
        return fail("Focus must recover enough for a mark even without its damage-free bonus");
    }

    @ParameterizedTest
    @CsvSource({"bloodrager,true,false", "artillerist,false,true", "windrunner,false,false",
            "tempest_archer,false,true"})
    void resourceBurstFundsFurtherCastsAndAppliesTheSkillEffects(
            String form, boolean fury, boolean strength) {
        int level = module.catalog().require(form).band().effectiveStart();
        assertTrue(module.setClassLevelForAdministration(player, form, level).applied());
        if (fury) {
            assertFalse(module.useAbility(player, AbilitySlot.UTILITY).successful());
            incomingDamage();
            incomingDamage(); // Earn 40 Fury through the production damage handler.
        }
        target(); // Targeted resource bursts must have an eligible enemy before casting.
        // Without the committed resource burst, Fury cannot fund cast two and Focus cannot fund cast three.
        for (int cast = 0; cast < 3; cast++) {
            player.removePotionEffect(PotionEffectType.SPEED);
            player.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 100, 0));
            assertTrue(module.useAbility(player, AbilitySlot.UTILITY).successful());
            assertTrue(player.hasPotionEffect(PotionEffectType.SPEED));
            assertEquals(1, player.getPotionEffect(PotionEffectType.SPEED).getAmplifier());
            assertEquals(!fury, player.hasPotionEffect(PotionEffectType.POISON));
        }
        assertEquals(strength, outgoingDamage() > 10D);
        assertTrue(module.selectForm(player, "spellcaster").accepted());
        assertEquals(10D, outgoingDamage());
    }

    @Test
    void berserkerSignatureNeedsFuryThatHasNotDecayedThenAppliesDamageAndSpeedUntilClassChange() {
        assertTrue(module.setClassLevelForAdministration(player, "berserker", 1).applied());
        assertFalse(module.useAbility(player, AbilitySlot.SIGNATURE).successful());
        incomingDamage();
        incomingDamage();
        MockBukkit.getMock().getScheduler().performOneTick();
        assertFalse(module.useAbility(player, AbilitySlot.SIGNATURE).successful(),
                "An out-of-combat update must decay Fury below this cast's cost");
        incomingDamage();
        assertTrue(module.useAbility(player, AbilitySlot.SIGNATURE).successful());
        assertEquals(11.8045D, outgoingDamage(), 0.000001);
        assertTrue(player.hasPotionEffect(PotionEffectType.SPEED));
        assertFalse(module.useAbility(player, AbilitySlot.SIGNATURE).successful());
        assertTrue(module.selectForm(player, "spellcaster").accepted());
        assertEquals(10D, outgoingDamage());
    }

    @Test
    void bloodragerFrenzyTracksHealthAfterCastingAndRevokesItsAttributeOnClassChange() {
        assertTrue(module.setClassLevelForAdministration(player, "bloodrager", 31).applied());
        for (int hit = 0; hit < 5; hit++) incomingDamage();
        assertTrue(module.useAbility(player, AbilitySlot.SIGNATURE).successful());
        assertEquals(10D, outgoingDamage(), "Frenzy must not grant its missing-health bonus at full health");
        player.setHealth(10D);
        MockBukkit.getMock().getScheduler().performOneTick();
        assertEquals(11.18525D, outgoingDamage(), 0.000001);
        assertEquals(.12155D, player.getAttribute(Attribute.MOVEMENT_SPEED).getValue(), 0.000001);
        player.setHealth(20D);
        MockBukkit.getMock().getScheduler().performOneTick();
        assertEquals(10D, outgoingDamage());
        assertEquals(.1D, player.getAttribute(Attribute.MOVEMENT_SPEED).getValue(), 0.000001);
        player.setHealth(10D);
        assertTrue(module.selectForm(player, "spellcaster").accepted());
        MockBukkit.getMock().getScheduler().performOneTick();
        assertEquals(10D, outgoingDamage());
        assertEquals(.1D, player.getAttribute(Attribute.MOVEMENT_SPEED).getValue(), 0.000001);
    }

    @Test
    void deathlessUtilitySpendsEarnedFuryToHealShieldAndProtect() {
        assertTrue(module.setClassLevelForAdministration(player, "deathless", 91).applied());
        assertFalse(module.useAbility(player, AbilitySlot.UTILITY).successful());
        for (int hit = 0; hit < 3; hit++) incomingDamage();
        player.setHealth(4D);
        assertTrue(module.useAbility(player, AbilitySlot.UTILITY).successful());
        assertEquals(5.2275D, player.getHealth(), 0.000001);
        var shield = player.getPotionEffect(PotionEffectType.ABSORPTION);
        assertNotNull(shield);
        assertEquals(1, shield.getAmplifier());
        assertEquals(7.545D, incomingDamage(), 0.000001);
        assertFalse(module.useAbility(player, AbilitySlot.UTILITY).successful());
        assertEquals(5.2275D, player.getHealth(), 0.000001);
        assertTrue(module.selectForm(player, "spellcaster").accepted());
        assertEquals(10D, incomingDamage());
    }

    @Test
    void demonologistUtilityTradesVulnerabilityForSpellOnlyDamageAndShield() {
        assertTrue(module.setClassLevelForAdministration(player, "demonologist", 91).applied());
        assertTrue(module.useAbility(player, AbilitySlot.UTILITY).successful());
        assertTrue(player.hasPotionEffect(PotionEffectType.ABSORPTION));
        assertEquals(10D, outgoingDamage(), "The spell buff must not increase ordinary weapon damage");
        CombatDamageContext.runClassAbilityDamage(CombatDamageContext.ClassAbilityDamageDomain.SINGLE_TARGET_DIRECT,
                () -> assertEquals(12.7005D, outgoingDamage(), 0.000001));
        assertEquals(12.7005D, incomingDamage(), 0.000001);
        assertTrue(module.selectForm(player, "spellcaster").accepted());
        assertEquals(10D, incomingDamage());
        CombatDamageContext.runClassAbilityDamage(CombatDamageContext.ClassAbilityDamageDomain.SINGLE_TARGET_DIRECT,
                () -> assertEquals(10D, outgoingDamage()));
    }

    @ParameterizedTest
    @CsvSource({"mage,31,11.5085,false", "spellblade,91,11.964,true"})
    void damageBuffAppliesItsEffectsUntilClassChange(String form, int level, double damage, boolean warcasting) {
        assertTrue(module.setClassLevelForAdministration(player, form, level).applied());
        assertTrue(module.useAbility(player, AbilitySlot.UTILITY).successful());
        assertEquals(damage, outgoingDamage(), 0.000001);
        assertEquals(warcasting, player.hasPotionEffect(PotionEffectType.ABSORPTION));
        assertEquals(warcasting, player.hasPotionEffect(PotionEffectType.SPEED));
        if (warcasting) {
            assertEquals(98, player.getPotionEffect(PotionEffectType.ABSORPTION).getDuration());
            assertEquals(0, player.getPotionEffect(PotionEffectType.ABSORPTION).getAmplifier());
            assertEquals(1, player.getPotionEffect(PotionEffectType.SPEED).getAmplifier());
        }

        assertTrue(module.selectForm(player, "spellcaster").accepted());
        assertEquals(10D, outgoingDamage());
    }
}
