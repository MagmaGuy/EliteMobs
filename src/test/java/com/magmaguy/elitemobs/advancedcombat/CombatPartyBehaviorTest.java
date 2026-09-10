package com.magmaguy.elitemobs.advancedcombat;

import com.magmaguy.elitemobs.mobconstructor.ElitePowerPauseReason;
import com.magmaguy.elitemobs.api.internal.RemovalReason;
import com.magmaguy.elitemobs.advancedcombat.classes.AbilitySlot;
import org.bukkit.Bukkit;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionEffect;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockbukkit.mockbukkit.MockBukkit;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class CombatPartyBehaviorTest extends CombatBehaviorFixture {
    @ParameterizedTest
    @CsvSource({"shepherd,UTILITY,false,false,false,7.545,10,true", "mistweaver,UTILITY,true,false,false,7.545,10,true",
            "warlord,UTILITY,true,true,false,10,10,true", "marshal,UTILITY,false,true,false,7.695,10,true",
            "shieldbearer,UTILITY,false,true,true,7.545,10,true", "strategist,UTILITY,false,true,true,10,10,true",
            "conqueror,UTILITY,false,true,false,10,11.383,true", "arcane_knight,UTILITY,false,false,true,7.545,10,false",
            "strategist,SIGNATURE,false,true,true,10,11.84125,false"})
    void partyAbilitySpendsResourceAndBuffsOnlyNearbyMembersUntilClassChange(
            String form, AbilitySlot slot, boolean cleanses, boolean resolve, boolean shields,
            double protectedDamage, double buffedDamage, boolean speeds) throws Exception {
        int level = module.catalog().require(form).band().effectiveStart();
        assertTrue(module.setClassLevelForAdministration(player, form, level).applied());
        if (resolve) {
            assertFalse(module.useAbility(player, slot).successful());
            for (int hit = 0; hit < 5; hit++) incomingDamage();
        }
        var server = MockBukkit.getMock();
        var ally = server.addPlayer();
        var distant = server.addPlayer();
        var outsider = server.addPlayer();
        ally.teleport(player.getLocation().add(1, 0, 0));
        outsider.teleport(player.getLocation().add(2, 0, 0));
        distant.teleport(player.getLocation().add(0, 0, 40));
        openParty(ally, distant);
        for (var member : List.of(player, ally, distant, outsider)) {
            assertTrue(ClassAbilityEligibility.isEligible(member));
            member.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 100, 0));
            member.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 100, 0));
        }

        assertTrue(module.useAbility(player, slot).successful());
        for (var member : List.of(player, ally)) {
            assertEquals(speeds, member.hasPotionEffect(PotionEffectType.SPEED));
            if (speeds) assertEquals(1, member.getPotionEffect(PotionEffectType.SPEED).getAmplifier());
            assertEquals(!cleanses, member.hasPotionEffect(PotionEffectType.POISON));
            assertTrue(member.hasPotionEffect(PotionEffectType.NIGHT_VISION));
            assertEquals(shields, member.hasPotionEffect(PotionEffectType.ABSORPTION));
            var hit = outgoingEvent(member, target());
            Bukkit.getPluginManager().callEvent(hit);
            assertEquals(buffedDamage, hit.getDamage(), .000001);
        }
        for (var excluded : List.of(distant, outsider)) {
            assertFalse(excluded.hasPotionEffect(PotionEffectType.SPEED));
            assertFalse(excluded.hasPotionEffect(PotionEffectType.ABSORPTION));
            assertTrue(excluded.hasPotionEffect(PotionEffectType.POISON));
            assertEquals(10D, incomingDamage(excluded));
            var hit = outgoingEvent(excluded, target());
            Bukkit.getPluginManager().callEvent(hit);
            assertEquals(10D, hit.getDamage());
        }
        assertTrue(module.useAbility(player, slot).successful());
        for (var member : List.of(player, ally)) member.removePotionEffect(PotionEffectType.SPEED);
        assertFalse(module.useAbility(player, slot).successful(),
                "Two casts must leave too little resource for a third");
        for (var member : List.of(player, ally)) {
            assertFalse(member.hasPotionEffect(PotionEffectType.SPEED));
            assertEquals(protectedDamage, incomingDamage(member), .000001);
        }
        assertTrue(module.selectForm(player, "spellcaster").accepted());
        assertEquals(10D, incomingDamage());
        assertEquals(10D, incomingDamage(ally), "Changing the caster's class must revoke the ally's protection");
        assertEquals(10D, outgoingDamage());
        var retiredAllyHit = outgoingEvent(ally, target());
        Bukkit.getPluginManager().callEvent(retiredAllyHit);
        assertEquals(10D, retiredAllyHit.getDamage());
    }

    @Test
    void bannerlordFieldFollowsCasterBuffsPartyAndOwnsUtilityWindow() throws Exception {
        assertTrue(module.setClassLevelForAdministration(player, "bannerlord", 91).applied());
        assertFalse(module.useAbility(player, AbilitySlot.SIGNATURE).successful());
        for (int hit = 0; hit < 5; hit++) incomingDamage();
        var server = MockBukkit.getMock();
        var ally = server.addPlayer();
        var distant = server.addPlayer();
        var outsider = server.addPlayer();
        ally.teleport(player.getLocation().add(1, 0, 0));
        outsider.teleport(player.getLocation().add(2, 0, 0));
        distant.teleport(player.getLocation().add(0, 0, 40));
        openParty(ally, distant);
        var distantEnemy = CombatTestEntities.spawnElite(distant.getLocation().add(0, 0, 3));
        try {
            double enemyHealth = target().getLivingEntity().getHealth();
            target().addThreat(outsider, 1000D);
            assertFalse(module.useAbility(player, AbilitySlot.UTILITY).successful(), "Full Resolve alone is insufficient");
            player.getWorld().getChunkAt(player.getLocation()).load();
            assertTrue(module.useAbility(player, AbilitySlot.SIGNATURE).successful());
            assertEquals(player.getUniqueId(), target().getForcedTargetPlayerId());
            assertEquals(player, ((org.bukkit.entity.Mob) target().getLivingEntity()).getTarget());
            assertNull(distantEnemy.getForcedTargetPlayerId());
            assertEquals(enemyHealth, target().getLivingEntity().getHealth());
            assertTrue(target().getDamagers().isEmpty());
            for (var member : List.of(player, ally, distant, outsider)) {
                var hit = outgoingEvent(member, target());
                Bukkit.getPluginManager().callEvent(hit);
                assertEquals(member == player || member == ally ? 11.7185D : 10D, hit.getDamage(), .000001);
            }
            assertFalse(module.useAbility(player, AbilitySlot.SIGNATURE).successful(),
                    "One banner must spend enough Resolve to prevent immediate replacement");
            for (var member : List.of(player, ally)) member.removePotionEffect(PotionEffectType.SPEED);
            assertTrue(module.useAbility(player, AbilitySlot.UTILITY).successful());
            for (var member : List.of(player, ally)) {
                assertNotNull(member.getPotionEffect(PotionEffectType.SPEED));
                assertEquals(1, member.getPotionEffect(PotionEffectType.SPEED).getAmplifier());
                member.removePotionEffect(PotionEffectType.SPEED);
            }
            assertFalse(distant.hasPotionEffect(PotionEffectType.SPEED));
            assertFalse(outsider.hasPotionEffect(PotionEffectType.SPEED));

            player.teleport(distant.getLocation());
            player.getWorld().getChunkAt(player.getLocation()).load();
            server.getScheduler().performTicks(25);
            assertEquals(player.getUniqueId(), distantEnemy.getForcedTargetPlayerId(),
                    "The next banner pulse must target enemies near the caster's new location");
            assertNotNull(distant.getPotionEffect(PotionEffectType.SPEED));
            var newlyBuffedHit = outgoingEvent(distant, distantEnemy);
            Bukkit.getPluginManager().callEvent(newlyBuffedHit);
            assertEquals(11.7185D, newlyBuffedHit.getDamage(), .000001);
            assertFalse(ally.hasPotionEffect(PotionEffectType.SPEED), "The old location must not keep receiving pulses");
            assertFalse(outsider.hasPotionEffect(PotionEffectType.SPEED));

            assertTrue(module.selectForm(player, "spellcaster").accepted());
            for (var elite : List.of(target(), distantEnemy)) assertNull(elite.getForcedTargetPlayerId());
            for (var member : List.of(player, ally, distant)) {
                member.removePotionEffect(PotionEffectType.SPEED);
                var hit = outgoingEvent(member, distantEnemy);
                Bukkit.getPluginManager().callEvent(hit);
                assertEquals(10D, hit.getDamage());
            }
            server.getScheduler().performTicks(25);
            for (var member : List.of(player, ally, distant)) assertFalse(member.hasPotionEffect(PotionEffectType.SPEED));
            assertNull(distantEnemy.getForcedTargetPlayerId(), "A retired field must not taunt on a later pulse");
            assertTrue(module.selectForm(player, "bannerlord").accepted());
            for (int hit = 0; hit < 5; hit++) incomingDamage();
            assertFalse(module.useAbility(player, AbilitySlot.UTILITY).successful(), "The old banner cannot survive class change");
        } finally {
            distantEnemy.remove(RemovalReason.SHUTDOWN);
        }
    }

    @Test
    void hierophantUtilityCleansesItsPartyInterruptsNearbyElitesAndReleasesOnClose() throws Exception {
        assertTrue(module.setClassLevelForAdministration(player, "hierophant", 61).applied());
        var ally = MockBukkit.getMock().addPlayer();
        var outsider = MockBukkit.getMock().addPlayer();
        var distantAlly = MockBukkit.getMock().addPlayer();
        ally.teleport(player.getLocation().add(1, 0, 0));
        outsider.teleport(player.getLocation().add(2, 0, 0));
        distantAlly.teleport(player.getLocation().add(0, 0, 40));
        openParty(ally, distantAlly);
        var enemy = target().getLivingEntity();
        double healthBefore = enemy.getHealth();
        var distantEnemy = CombatTestEntities.spawnElite(player.getLocation().add(0, 0, 40));
        for (var member : List.of(player, ally, outsider, distantAlly)) {
            member.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 100, 0));
            member.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 100, 0));
        }
        try {
            assertTrue(module.useAbility(player, AbilitySlot.UTILITY).successful());
            for (var member : List.of(player, ally)) {
                assertFalse(member.hasPotionEffect(PotionEffectType.POISON));
                assertTrue(member.hasPotionEffect(PotionEffectType.NIGHT_VISION));
            }
            for (var excluded : List.of(outsider, distantAlly))
                assertTrue(excluded.hasPotionEffect(PotionEffectType.POISON));
            assertEquals(healthBefore, enemy.getHealth(), "Interruption must not become damage");
            assertTrue(target().getPowerSuppression().isSuppressed(ElitePowerPauseReason.INTERRUPT));
            assertFalse(distantEnemy.getPowerSuppression().isSuppressed());
            assertTrue(module.useAbility(player, AbilitySlot.UTILITY).successful());
            ally.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 100, 0));
            assertFalse(module.useAbility(player, AbilitySlot.UTILITY).successful());
            assertTrue(ally.hasPotionEffect(PotionEffectType.POISON), "A refused cast must not cleanse");
            module.close();
            assertFalse(target().getPowerSuppression().isSuppressed(ElitePowerPauseReason.INTERRUPT));
        } finally {
            distantEnemy.remove(RemovalReason.SHUTDOWN);
        }
    }

    @Test
    void shepherdFieldFollowsItsCasterScalesWithNearbyAlliesAndStopsOnClassChange() throws Exception {
        fullCombatActive = true;
        assertTrue(module.setClassLevelForAdministration(player, "shepherd", 91).applied());
        var server = MockBukkit.getMock();
        var ally = server.addPlayer();
        var outsider = server.addPlayer();
        ally.teleport(player.getLocation().add(0, 0, 130));
        outsider.teleport(ally.getLocation().add(1, 0, 0));
        openParty(ally);
        for (var member : List.of(player, ally, outsider)) member.setHealth(8D);
        assertTrue(player.getLocation().getChunk().load());
        assertTrue(ally.getLocation().getChunk().load());
        var cast = module.useAbility(player, AbilitySlot.SIGNATURE);
        assertTrue(cast.successful(), cast.toString());
        assertEquals(9.4953896D, player.getHealth(), .000001);
        assertEquals(8D, ally.getHealth());
        assertNotNull(player.getPotionEffect(PotionEffectType.ABSORPTION));

        assertTrue(player.teleport(ally.getLocation().add(1, 0, 0)));
        server.getScheduler().performTicks(22);
        assertEquals(9.4953896D, player.getHealth(), .000001);
        server.getScheduler().performOneTick();
        assertEquals(11.3774527475D, player.getHealth(), .000001);
        assertEquals(9.8820631475D, ally.getHealth(), .000001);
        assertNotNull(ally.getPotionEffect(PotionEffectType.ABSORPTION));
        assertEquals(8D, outsider.getHealth());
        assertFalse(outsider.hasPotionEffect(PotionEffectType.ABSORPTION));
        assertTrue(module.selectForm(player, "spellcaster").accepted());
        server.getScheduler().performTicks(120);
        assertEquals(11.3774527475D, player.getHealth(), .000001);
        assertEquals(9.8820631475D, ally.getHealth(), .000001,
                "Changing class must stop every remaining healing pulse");
    }

    @Test
    void pathfinderFieldStaysAtItsCastLocationAndStopsRefreshingAlliesOnClassChange() throws Exception {
        assertTrue(module.setClassLevelForAdministration(player, "pathfinder", 91).applied());
        var server = MockBukkit.getMock();
        var ally = server.addPlayer();
        var outsider = server.addPlayer();
        ally.teleport(player.getLocation().add(1, 0, 0));
        outsider.teleport(player.getLocation().add(2, 0, 0));
        openParty(ally);
        player.getLocation().getChunk().load();
        assertTrue(module.useAbility(player, AbilitySlot.UTILITY).successful());
        for (var member : List.of(player, ally)) {
            assertTrue(member.hasPotionEffect(PotionEffectType.SPEED));
            member.removePotionEffect(PotionEffectType.SPEED);
        }
        assertFalse(outsider.hasPotionEffect(PotionEffectType.SPEED));
        player.teleport(player.getLocation().add(40, 0, 0));
        server.getScheduler().performTicks(19);
        assertFalse(ally.hasPotionEffect(PotionEffectType.SPEED));
        server.getScheduler().performOneTick();
        assertNotNull(ally.getPotionEffect(PotionEffectType.SPEED));
        assertEquals(1, ally.getPotionEffect(PotionEffectType.SPEED).getAmplifier());
        assertFalse(player.hasPotionEffect(PotionEffectType.SPEED));
        assertFalse(outsider.hasPotionEffect(PotionEffectType.SPEED));

        assertTrue(module.selectForm(player, "spellcaster").accepted());
        ally.removePotionEffect(PotionEffectType.SPEED);
        server.getScheduler().performTicks(120);
        assertFalse(ally.hasPotionEffect(PotionEffectType.SPEED));
    }
}
