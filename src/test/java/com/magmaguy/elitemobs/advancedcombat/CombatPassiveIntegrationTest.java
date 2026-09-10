package com.magmaguy.elitemobs.advancedcombat;

import com.magmaguy.elitemobs.advancedcombat.classes.AbilitySlot;
import org.bukkit.attribute.Attribute;
import org.bukkit.potion.PotionEffectType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockbukkit.mockbukkit.MockBukkit;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class CombatPassiveIntegrationTest extends CombatBehaviorFixture {
    @ParameterizedTest
    @CsvSource({"priest,31,SIGNATURE,false,11.336802,9.5", "hierophant,61,SIGNATURE,false,13.381253,9.25",
            "hierophant,61,SIGNATURE,true,13.839487,9.25", "spiritcaller,61,UTILITY,false,9.1406984,9.45"})
    void healingPassiveChangesRealRecoveryInItsPartyContext(
            String form, int level, AbilitySlot slot, boolean grouped,
            double healedHealth, double outgoing) throws Exception {
        fullCombatActive = true;
        assertTrue(module.setClassLevelForAdministration(player, form, level).applied());
        if (grouped) {
            var ally = MockBukkit.getMock().addPlayer();
            ally.teleport(player.getLocation().add(1, 0, 0));
            openParty(ally);
        }
        player.setHealth(8D);
        assertTrue(module.useAbility(player, slot).successful());
        assertEquals(healedHealth, player.getHealth(), 0.000001);
        assertEquals(outgoing, outgoingDamage(), 0.000001,
                "The same active lineage must also reach the damage event listener");
    }

    @Test
    void templarPassiveTradesDamageForHealingAndStopsWhenCombatEnds() throws Exception {
        int level = module.catalog().require("templar").band().effectiveStart();
        assertTrue(module.setClassLevelForAdministration(player, "templar", level).applied());
        assertTrue(target().getLivingEntity().teleport(player.getLocation().add(0, 0, 20)));
        var ally = MockBukkit.getMock().addPlayer();
        ally.teleport(player.getLocation().add(1, 0, 0));
        openParty(ally);
        double baselineHealing = 0D;
        for (int phase = 0; phase < 3; phase++) {
            fullCombatActive = phase == 1;
            module.onControlModeChanged(player);
            for (int hit = 0; hit < 5; hit++) incomingDamage();
            ally.setHealth(8D);
            assertTrue(module.useAbility(player, AbilitySlot.SIGNATURE).successful());
            double healing = ally.getHealth() - 8D;
            assertTrue(healing > 0D && ally.getHealth() < 20D,
                    "The comparison needs effective healing without the health cap");
            if (phase == 0) baselineHealing = healing;
            if (fullCombatActive) {
                assertEquals(1.039D, healing / baselineHealing, .000001,
                        "Level-91 Templar adds 1.42% healing to its inherited 2.48% benefit");
                assertEquals(9.3825D, outgoingDamage(), .000001,
                        "The healing benefit must retain Templar's own damage tradeoff");
                assertTrue(incomingDamage() < 10D, "The defensive lineage must reach incoming damage events");
                assertTrue(player.getAttribute(Attribute.MOVEMENT_SPEED).getValue() < .1D);
            } else {
                assertEquals(baselineHealing, healing, .000001);
                assertEquals(10D, outgoingDamage());
                assertEquals(10D, incomingDamage());
                assertEquals(.1D, player.getAttribute(Attribute.MOVEMENT_SPEED).getValue(), .000001);
            }
        }
    }

    @ParameterizedTest
    @CsvSource({"conqueror,61,.71", "tyrant,91,2.714"})
    void controlPassiveRequiresTheCastersRealTauntAndExpiresWithIt(
            String form, int level, double controlBonus) throws Exception {
        fullCombatActive = true;
        assertTrue(module.setClassLevelForAdministration(player, form, level).applied());
        var elite = target();
        elite.setLevel(level);
        elite.setMaxHealth();
        var enemy = (org.bukkit.entity.Mob) elite.getLivingEntity();
        enemy.setHealth(enemy.getAttribute(Attribute.MAX_HEALTH).getValue());
        double ordinary = outgoingDamage();
        enemy.setTarget(player);
        assertEquals(ordinary, outgoingDamage(), .000001,
                "Ordinary mob aggro must not activate a controlled-target passive");
        for (int hit = 0; hit < 5; hit++) incomingDamage();

        assertTrue(module.useAbility(player, AbilitySlot.SIGNATURE).successful());
        assertEquals(player.getUniqueId(), elite.getForcedTargetPlayerId());
        // Conqueror adds 7.1%. Tyrant adds inherited 10.1% and 11.36%, replacing a 5.68% penalty.
        assertEquals(ordinary + controlBonus, outgoingDamage(), .000001);
        enemy.setTarget(MockBukkit.getMock().addPlayer());
        assertEquals(ordinary + controlBonus, outgoingDamage(), .000001,
                "Control ownership must come from the cast, not the mob's aggro target");

        // These leases use monotonic elapsed time, not scheduler ticks. Keep the wait bounded.
        long deadline = System.nanoTime() + java.util.concurrent.TimeUnit.SECONDS.toNanos(8);
        while (outgoingDamage() > ordinary + .000001 && System.nanoTime() < deadline) {
            Thread.sleep(25L);
            MockBukkit.getMock().getScheduler().performOneTick();
        }
        assertEquals(ordinary, outgoingDamage(), .000001, "Expired taunts must release the passive bonus");
        fullCombatActive = false;
        module.onControlModeChanged(player);
        assertEquals(10D, outgoingDamage(), .000001);
    }

    @ParameterizedTest
    @CsvSource({"warlord,true,true", "marshal,true,false", "seraph,false,false", "soulwarden,false,false"})
    void partyPassiveTracksNearbyMembershipThroughDamageEvents(
            String form, boolean increasesDamage, boolean increasesRisk) throws Exception {
        fullCombatActive = true;
        int level = module.catalog().require(form).band().effectiveStart();
        assertTrue(module.setClassLevelForAdministration(player, form, level).applied());
        var server = MockBukkit.getMock();
        var ally = server.addPlayer();
        var outsider = server.addPlayer();
        ally.teleport(player.getLocation().add(1, 0, 0));
        outsider.teleport(player.getLocation().add(2, 0, 0));
        double soloOutgoing = outgoingDamage();
        double soloIncoming = incomingDamage();
        openParty(ally);

        double groupedOutgoing = outgoingDamage();
        double groupedIncoming = incomingDamage();
        if (increasesDamage) assertTrue(groupedOutgoing > soloOutgoing);
        else assertEquals(soloOutgoing, groupedOutgoing, .000001);
        if (increasesRisk) assertTrue(groupedIncoming > soloIncoming);
        else assertTrue(groupedIncoming < soloIncoming);

        assertTrue(ally.teleport(player.getLocation().add(0, 0, 129)));
        assertEquals(soloOutgoing, outgoingDamage(), .000001);
        assertEquals(soloIncoming, incomingDamage(), .000001,
                "A distant party member or nearby outsider must not activate grouped passives");
        assertTrue(ally.teleport(player.getLocation().add(1, 0, 0)));
        assertEquals(groupedOutgoing, outgoingDamage(), .000001);
        assertEquals(groupedIncoming, incomingDamage(), .000001);
    }

    @ParameterizedTest
    @CsvSource({"true", "false"})
    void strategistFundsAnExtraCastOnlyWithANearbyPartyMember(boolean nearby) throws Exception {
        fullCombatActive = true;
        assertTrue(module.setClassLevelForAdministration(player, "strategist", 91).applied());
        for (int hit = 0; hit < 10; hit++) incomingDamage();
        var ally = MockBukkit.getMock().addPlayer();
        assertTrue(ally.teleport(player.getLocation().add(0, 0, nearby ? 1 : 129)));
        openParty(ally);
        assertTrue(module.useAbility(player, AbilitySlot.UTILITY).successful());
        assertTrue(module.useAbility(player, AbilitySlot.UTILITY).successful());
        player.removePotionEffect(PotionEffectType.SPEED);
        assertEquals(nearby, module.useAbility(player, AbilitySlot.UTILITY).successful());
        assertEquals(nearby, player.hasPotionEffect(PotionEffectType.SPEED));
        assertFalse(module.useAbility(player, AbilitySlot.UTILITY).successful());
    }

    @Test
    void pathfinderAuraReachesNearbyPartyMembersWithoutStackingAndClearsOnExit() throws Exception {
        fullCombatActive = true;
        assertTrue(module.setClassLevelForAdministration(player, "pathfinder", 91).applied());
        double solo = player.getAttribute(Attribute.MOVEMENT_SPEED).getValue();
        var server = MockBukkit.getMock();
        var ally = server.addPlayer();
        var outsider = server.addPlayer();
        ally.teleport(player.getLocation().add(1, 0, 0));
        outsider.teleport(player.getLocation().add(2, 0, 0));
        openParty(ally);
        server.getScheduler().performTicks(21);
        assertTrue(module.setClassLevelForAdministration(ally, "ranger", 1).applied());
        assertEquals(solo + .00284D, player.getAttribute(Attribute.MOVEMENT_SPEED).getValue(), .000001);
        assertEquals(.108915D, ally.getAttribute(Attribute.MOVEMENT_SPEED).getValue(), .000001);
        assertEquals(.1D, outsider.getAttribute(Attribute.MOVEMENT_SPEED).getValue(), .000001);

        assertTrue(module.setClassLevelForAdministration(ally, "pathfinder", 91).applied());
        server.getScheduler().performTicks(20);
        for (var member : List.of(player, ally))
            assertEquals(solo + .00284D, member.getAttribute(Attribute.MOVEMENT_SPEED).getValue(), .000001,
                    "Two equal Pathfinder auras must not stack");
        assertTrue(ally.teleport(player.getLocation().add(0, 0, 129)));
        server.getScheduler().performTicks(20);
        for (var member : List.of(player, ally))
            assertEquals(solo, member.getAttribute(Attribute.MOVEMENT_SPEED).getValue(), .000001);
        module.close();
        for (var member : List.of(player, ally))
            assertEquals(.1D, member.getAttribute(Attribute.MOVEMENT_SPEED).getValue(), .000001);
    }
}
