package com.magmaguy.elitemobs.advancedcombat;

import com.magmaguy.elitemobs.mobconstructor.ElitePowerPauseReason;
import com.magmaguy.elitemobs.api.internal.RemovalReason;
import com.magmaguy.elitemobs.advancedcombat.classes.AbilitySlot;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.potion.PotionEffectType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockbukkit.mockbukkit.MockBukkit;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class CombatStrikeBehaviorTest extends CombatBehaviorFixture {
    @Test
    void battlemageSignatureDamagesNearbyElitesAndSpendsManaForProtection() {
        assertTrue(module.setClassLevelForAdministration(player, "battlemage", 61).applied());
        var enemy = target().getLivingEntity();
        double healthBefore = enemy.getHealth();
        var bystander = MockBukkit.getMock().addPlayer();
        bystander.teleport(player.getLocation().add(1, 0, 0));
        var distant = CombatTestEntities.spawnElite(player.getLocation().add(0, 0, 20));
        double distantHealth = distant.getLivingEntity().getHealth();
        try {
            assertTrue(module.useAbility(player, AbilitySlot.SIGNATURE).successful());
            assertTrue(enemy.getHealth() < healthBefore, "The cast must actually damage the nearby elite");
            assertEquals(distantHealth, distant.getLivingEntity().getHealth());
            assertEquals(20D, bystander.getHealth(), "The cast must not damage another player");
            assertFalse(bystander.hasPotionEffect(PotionEffectType.ABSORPTION));
            assertNotNull(player.getPotionEffect(PotionEffectType.ABSORPTION));
            assertTrue(incomingDamage() < 10D, "The signature must apply its self-protection");
            assertTrue(module.useAbility(player, AbilitySlot.SIGNATURE).successful());
            double healthBeforeDeniedCast = enemy.getHealth();
            player.removePotionEffect(PotionEffectType.ABSORPTION);
            assertFalse(module.useAbility(player, AbilitySlot.SIGNATURE).successful(),
                    "Two 45-Mana casts must prevent a third cast");
            assertEquals(healthBeforeDeniedCast, enemy.getHealth());
            assertFalse(player.hasPotionEffect(PotionEffectType.ABSORPTION));
            assertTrue(module.selectForm(player, "spellcaster").accepted());
            assertEquals(10D, incomingDamage());
        } finally {
            distant.remove(RemovalReason.SHUTDOWN);
        }
    }

    @Test
    void reaverSignatureDamagesAndHealsThenRevokesItsLifestealWindowOnClassChange() {
        assertTrue(module.setClassLevelForAdministration(player, "reaver", 61).applied());
        assertFalse(module.useAbility(player, AbilitySlot.SIGNATURE).successful());
        for (int hit = 0; hit < 3; hit++) incomingDamage();
        var enemy = target().getLivingEntity();
        double enemyHealth = enemy.getHealth();
        player.setHealth(8D);

        assertTrue(module.useAbility(player, AbilitySlot.SIGNATURE).successful());
        double damageDealt = enemyHealth - enemy.getHealth();
        assertTrue(damageDealt > 0D, "The cleave must actually damage its target");
        assertEquals(8D + damageDealt * .22D, player.getHealth(), 0.000001);
        player.setHealth(8D);
        var cancelled = outgoingEvent();
        cancelled.setCancelled(true);
        Bukkit.getPluginManager().callEvent(cancelled);
        assertEquals(8D, player.getHealth(), "A cancelled hit must not grant lifesteal");
        outgoingDamage();
        assertEquals(9.6D, player.getHealth(), 0.000001, "Later hits must use the armed lifesteal window");
        assertTrue(module.selectForm(player, "spellcaster").accepted());
        outgoingDamage();
        assertEquals(9.6D, player.getHealth(), "Changing class must revoke the previous lifesteal window");
    }

    @ParameterizedTest
    @CsvSource({"false", "true"})
    void harvesterChainsThroughWoundedEnemiesHealsAndStopsOnClassChange(boolean changeClass) {
        assertTrue(module.setClassLevelForAdministration(player, "harvester", 91).applied());
        for (int hit = 0; hit < 3; hit++) incomingDamage();
        var first = target().getLivingEntity();
        var second = CombatTestEntities.spawnElite(player.getLocation().add(0, 0, 4));
        var healthy = CombatTestEntities.spawnElite(player.getLocation().add(0, 0, 5));
        try {
            first.getAttribute(Attribute.MAX_HEALTH).setBaseValue(100D);
            second.getLivingEntity().getAttribute(Attribute.MAX_HEALTH).setBaseValue(100D);
            first.setHealth(40D);
            second.getLivingEntity().setHealth(30D);
            double healthyBefore = healthy.getLivingEntity().getHealth();
            player.setHealth(2D);
            assertTrue(module.useAbility(player, AbilitySlot.SIGNATURE).successful());
            assertEquals(40D, first.getHealth());
            var scheduler = MockBukkit.getMock().getScheduler();
            scheduler.performOneTick();
            double firstDamage = 40D - first.getHealth();
            assertEquals(40D, firstDamage);
            assertEquals(30D, second.getLivingEntity().getHealth());
            assertEquals(10.8D, player.getHealth(), .000001);
            if (changeClass) assertTrue(module.selectForm(player, "spellcaster").accepted());
            scheduler.performTicks(3);
            double secondDamage = 30D - second.getLivingEntity().getHealth();
            assertEquals(changeClass ? 0D : 30D, secondDamage);
            assertEquals(changeClass ? 10.8D : 17.4D, player.getHealth(), .000001,
                    "Lifesteal must use actual health removed, not the larger overkill damage");
            scheduler.performTicks(6);
            assertEquals(40D - firstDamage, first.getHealth());
            assertEquals(30D - secondDamage, second.getLivingEntity().getHealth());
            assertEquals(healthyBefore, healthy.getLivingEntity().getHealth());
        } finally {
            second.remove(RemovalReason.SHUTDOWN);
            healthy.remove(RemovalReason.SHUTDOWN);
        }
    }

    @Test
    void siegebreakerStrikeEnablesItsFollowupOnlyForBrokenTargetsAndReleasesEffects() throws Exception {
        assertTrue(module.setClassLevelForAdministration(player, "siegebreaker", 91).applied());
        target().setLevel(91);
        var enemy = target().getLivingEntity();
        enemy.getAttribute(Attribute.MAX_HEALTH).setBaseValue(2048D);
        enemy.setHealth(2048D);
        var behind = CombatTestEntities.spawnElite(player.getLocation().add(0, 0, -3));
        double behindHealth = behind.getLivingEntity().getHealth();
        var ally = MockBukkit.getMock().addPlayer();
        var outsider = MockBukkit.getMock().addPlayer();
        ally.teleport(player.getLocation().add(1, 0, 0));
        outsider.teleport(player.getLocation().add(2, 0, 0));
        openParty(ally);
        try {
            assertFalse(module.useAbility(player, AbilitySlot.SIGNATURE).successful());
            assertEquals(2048D, enemy.getHealth());
            assertFalse(target().getPowerSuppression().isSuppressed());
            for (int hit = 0; hit < 5; hit++) incomingDamage();
            assertFalse(module.useAbility(player, AbilitySlot.UTILITY).successful(),
                    "Full Fury alone must not bypass the defense-break prerequisite");
            assertFalse(enemy.hasPotionEffect(PotionEffectType.GLOWING));

            assertTrue(module.useAbility(player, AbilitySlot.SIGNATURE).successful());
            assertTrue(enemy.getHealth() > 0D && enemy.getHealth() < 2048D,
                    "The matched-level target must survive the strike, health: " + enemy.getHealth());
            assertTrue(target().getPowerSuppression().isSuppressed(ElitePowerPauseReason.INTERRUPT));
            assertTrue(incomingDamage() < 10D);
            assertEquals(behindHealth, behind.getLivingEntity().getHealth());
            assertFalse(behind.getPowerSuppression().isSuppressed());
            assertEquals(20D, outsider.getHealth());

            assertTrue(module.useAbility(player, AbilitySlot.UTILITY).successful());
            assertTrue(enemy.hasPotionEffect(PotionEffectType.GLOWING));
            assertFalse(behind.getLivingEntity().hasPotionEffect(PotionEffectType.GLOWING));
            assertFalse(outsider.hasPotionEffect(PotionEffectType.GLOWING));
            assertTrue(outgoingDamage() > 10D);
            var partyHit = outgoingEvent(ally, target());
            Bukkit.getPluginManager().callEvent(partyHit);
            assertTrue(partyHit.getDamage() > 10D);
            for (var hit : List.of(outgoingEvent(outsider, target()), outgoingEvent(player, behind))) {
                Bukkit.getPluginManager().callEvent(hit);
                assertEquals(10D, hit.getDamage());
            }
            assertTrue(module.useAbility(player, AbilitySlot.UTILITY).successful());
            enemy.removePotionEffect(PotionEffectType.GLOWING);
            assertFalse(module.useAbility(player, AbilitySlot.UTILITY).successful(),
                    "The follow-up must spend Fury, even while defense break remains active");
            assertFalse(enemy.hasPotionEffect(PotionEffectType.GLOWING));

            MockBukkit.getMock().getScheduler().performTicks(61);
            assertFalse(target().getPowerSuppression().isSuppressed(ElitePowerPauseReason.INTERRUPT));
            assertTrue(module.selectForm(player, "spellcaster").accepted());
            assertEquals(10D, incomingDamage());
            assertEquals(10D, outgoingDamage());
            var retiredPartyHit = outgoingEvent(ally, target());
            Bukkit.getPluginManager().callEvent(retiredPartyHit);
            assertEquals(10D, retiredPartyHit.getDamage());
            assertTrue(module.setClassLevelForAdministration(player, "siegebreaker", 91).applied());
            for (int hit = 0; hit < 5; hit++) incomingDamage();
            assertFalse(module.useAbility(player, AbilitySlot.UTILITY).successful(),
                    "Returning to the class must not revive its previous defense-break ownership");
        } finally {
            behind.remove(RemovalReason.SHUTDOWN);
        }
    }
}
