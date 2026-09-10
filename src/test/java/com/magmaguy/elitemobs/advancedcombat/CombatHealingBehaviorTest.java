package com.magmaguy.elitemobs.advancedcombat;

import com.magmaguy.elitemobs.api.internal.RemovalReason;
import com.magmaguy.elitemobs.advancedcombat.classes.AbilitySlot;
import org.bukkit.potion.PotionEffectType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockbukkit.mockbukkit.MockBukkit;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class CombatHealingBehaviorTest extends CombatBehaviorFixture {
    @ParameterizedTest
    @CsvSource({"priest,31,3.017,false", "hierophant,61,4.61,true"})
    void partyHealSelectsItsRecipientsAndExcludesOutsiders(
            String form, int level, double healing, boolean healsCaster) throws Exception {
        assertTrue(module.setClassLevelForAdministration(player, form, level).applied());
        var server = MockBukkit.getMock();
        var first = server.addPlayer();
        var second = server.addPlayer();
        var third = server.addPlayer();
        var outsider = server.addPlayer();
        for (var member : List.of(first, second, third, outsider))
            member.teleport(player.getLocation().add(1, 0, 0));
        openParty(first, second, third);
        player.setHealth(16D);
        first.setHealth(2D);
        second.setHealth(4D);
        third.setHealth(8D);
        outsider.setHealth(1D);

        assertTrue(module.useAbility(player, AbilitySlot.SIGNATURE).successful());
        assertEquals(2D + healing, first.getHealth(), .000001);
        assertEquals(4D + healing, second.getHealth(), .000001);
        assertEquals(8D + healing, third.getHealth(), .000001);
        assertEquals(healsCaster ? 20D : 16D, player.getHealth(),
                "Priest selects the three most wounded members; Hierophant heals the nearby party");
        assertEquals(1D, outsider.getHealth(), "Low health alone must not make another player a party ally");
    }

    @ParameterizedTest
    @CsvSource({"exorcist,false,3.640733085,9.7265", "mistweaver,true,3.95565312,9.7845"})
    void mixedSignatureDamagesItsEnemySetAndHealsOnlyItsEligiblePartyRecipients(
            String form, boolean area, double healing, double outgoing) throws Exception {
        fullCombatActive = true;
        assertTrue(module.setClassLevelForAdministration(player, form, 91).applied());
        var enemy = target().getLivingEntity();
        var second = CombatTestEntities.spawnElite(player.getLocation().add(0, 0, 4));
        double firstHealth = enemy.getHealth(), secondHealth = second.getLivingEntity().getHealth();
        var ally = MockBukkit.getMock().addPlayer();
        var outsider = MockBukkit.getMock().addPlayer();
        ally.teleport(player.getLocation().add(1, 0, 0));
        outsider.teleport(player.getLocation().add(2, 0, 0));
        openParty(ally);
        player.setHealth(8D);
        ally.setHealth(4D);
        outsider.setHealth(3D);
        try {
            assertTrue(module.useAbility(player, AbilitySlot.SIGNATURE).successful());
            int damaged = (enemy.getHealth() < firstHealth ? 1 : 0)
                    + (second.getLivingEntity().getHealth() < secondHealth ? 1 : 0);
            assertEquals(area ? 2 : 1, damaged);
            assertEquals(area ? 8D + healing : 8D, player.getHealth(), .000001);
            assertEquals(4D + healing, ally.getHealth(), .000001);
            assertEquals(3D, outsider.getHealth(), "An outsider must be neither damaged nor healed");
            assertEquals(outgoing, outgoingDamage(), .000001);
        } finally {
            second.remove(RemovalReason.SHUTDOWN);
        }
    }

    @ParameterizedTest(name = "{displayName} [{index}] changeClass={0}")
    @CsvSource({"false", "true"})
    void seraphChainHealsTheMostWoundedPartyMemberFirstAndStopsOnClassChange(
            boolean changeClass) throws Exception {
        fullCombatActive = true;
        assertTrue(module.setClassLevelForAdministration(player, "seraph", 91).applied());
        var server = MockBukkit.getMock();
        var first = server.addPlayer();
        var second = server.addPlayer();
        var outsider = server.addPlayer();
        for (var member : List.of(first, second, outsider))
            member.teleport(player.getLocation().add(1, 0, 0));
        openParty(first, second);
        server.getScheduler().performTicks(20);
        for (var member : List.of(first, second)) {
            assertTrue(module.clearSelectedForm(member).accepted());
            assertNull(module.profile(member.getUniqueId()).orElseThrow().activeLineage());
        }
        // Pending support must survive ordinary reconciliation of allies with no selected class.
        server.getScheduler().performTicks(20 - server.getScheduler().getCurrentTick() % 20);
        player.setHealth(16D);
        // This health pool makes Oracle's inherited barrier bonus cross a potion tier.
        first.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH).setBaseValue(100D);
        first.setHealth(2D);
        second.setHealth(8D);
        outsider.setHealth(1D);

        assertEquals(9.12D, incomingDamage(), .000001, "Oracle and Seraph defend the grouped caster");
        assertEquals(9.2D, outgoingDamage(), .000001, "The healing lineage retains its attack penalty");
        assertTrue(module.useAbility(player, AbilitySlot.SIGNATURE).successful());
        assertEquals(2D, first.getHealth(), "The chain must run on the scheduler, not heal everyone immediately");
        server.getScheduler().performOneTick();
        assertEquals(28.9482895D, first.getHealth(), .000001);
        assertNotNull(first.getPotionEffect(PotionEffectType.ABSORPTION));
        assertEquals(1, first.getPotionEffect(PotionEffectType.ABSORPTION).getAmplifier());
        assertEquals(8D, second.getHealth());
        assertFalse(second.hasPotionEffect(PotionEffectType.ABSORPTION));
        assertEquals(16D, player.getHealth());
        if (changeClass) assertTrue(module.selectForm(player, "spellcaster").accepted());
        server.getScheduler().performTicks(3);
        assertEquals(changeClass ? 8D : 13.3896579D, second.getHealth(), .000001);
        assertEquals(!changeClass, second.hasPotionEffect(PotionEffectType.ABSORPTION));
        assertEquals(16D, player.getHealth());
        server.getScheduler().performTicks(6);
        assertEquals(changeClass ? 16D : 20D, player.getHealth());
        assertEquals(28.9482895D, first.getHealth(), .000001, "A chain must visit each recipient only once");
        assertEquals(1D, outsider.getHealth());
        assertFalse(outsider.hasPotionEffect(PotionEffectType.ABSORPTION));
    }

    @Test
    void prayerOfMendingSpendsGraceAndRecoversThroughScheduledUpdates() {
        assertTrue(module.setClassLevelForAdministration(player, "priest", 31).applied());
        player.setHealth(8D);

        assertTrue(module.useAbility(player, AbilitySlot.SIGNATURE).successful());
        assertEquals(11.017, player.getHealth(), 0.000001);
        assertFalse(module.useAbility(player, AbilitySlot.SIGNATURE).successful());
        assertEquals(11.017, player.getHealth(), 0.000001,
                "Insufficient Grace must stop the second heal before applying it");
        recoverAndCast(AbilitySlot.SIGNATURE);
        assertEquals(14.034D, player.getHealth(), .000001);
    }

    @ParameterizedTest(name = "{displayName} [{index}] changeClass={0}")
    @CsvSource({"false", "true"})
    void spiritcallerEchoUsesEffectiveHealingOnceAndCannotOutliveItsClass(
            boolean changeClass) {
        assertTrue(module.setClassLevelForAdministration(player, "spiritcaller", 61).applied());
        player.setHealth(19.5D);
        assertTrue(module.useAbility(player, AbilitySlot.SIGNATURE).successful());
        assertEquals(19.5D, player.getHealth(), "Arming the echo must not heal immediately");
        assertTrue(module.useAbility(player, AbilitySlot.UTILITY).successful());
        assertEquals(20D, player.getHealth());
        assertNotNull(player.getPotionEffect(PotionEffectType.ABSORPTION));
        player.setHealth(10D);
        if (changeClass) assertTrue(module.selectForm(player, "spellcaster").accepted());
        var scheduler = MockBukkit.getMock().getScheduler();
        scheduler.performTicks(29);
        assertEquals(10D, player.getHealth());
        scheduler.performOneTick();
        assertEquals(changeClass ? 10D : 10.3169375D, player.getHealth(), .000001,
                "The echo must scale the actual half-heart healed, excluding overhealing");
        scheduler.performTicks(30);
        assertEquals(changeClass ? 10D : 10.3169375D, player.getHealth(), .000001,
                "A consumed echo must not heal twice");
    }
}
