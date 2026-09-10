package com.magmaguy.elitemobs.advancedcombat;

import com.magmaguy.elitemobs.mobconstructor.ElitePowerPauseReason;
import com.magmaguy.elitemobs.api.internal.RemovalReason;
import com.magmaguy.elitemobs.advancedcombat.classes.AbilitySlot;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionEffect;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockbukkit.mockbukkit.MockBukkit;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class CombatControlBehaviorTest extends CombatBehaviorFixture {
    @ParameterizedTest(name = "{displayName} [{index}] {0}")
    @CsvSource({"paladin,1,true,10", "warlord,31,false,11.293"})
    void tauntingSignaturesSpendResolveWithoutDamageAndRetireTheirPartyModifiers(
            String form, int level, boolean protectsCaster, double outgoing) throws Exception {
        assertTrue(module.setClassLevelForAdministration(player, form, level).applied());
        var elite = target();
        var enemy = (org.bukkit.entity.Mob) elite.getLivingEntity();
        var rival = MockBukkit.getMock().addPlayer();
        rival.teleport(player.getLocation().add(1, 0, 0));
        var outsider = MockBukkit.getMock().addPlayer();
        outsider.teleport(player.getLocation().add(2, 0, 0));
        openParty(rival);
        elite.addThreat(rival, 1000D);
        assertEquals(rival, enemy.getTarget());
        double healthBefore = enemy.getHealth();
        var distant = CombatTestEntities.spawnElite(player.getLocation().add(0, 0, 20));
        try {
            assertFalse(module.useAbility(player, AbilitySlot.SIGNATURE).successful());
            assertNull(elite.getForcedTargetPlayerId());
            for (int hit = 0; hit < 5; hit++) incomingDamage();

            assertTrue(module.useAbility(player, AbilitySlot.SIGNATURE).successful());
            assertEquals(player.getUniqueId(), elite.getForcedTargetPlayerId());
            assertEquals(player, enemy.getTarget(), "Taunt must override the higher-threat opponent");
            assertTrue(elite.getAggro().getOrDefault(player, 0D) > 0D);
            assertEquals(healthBefore, enemy.getHealth());
            assertTrue(elite.getDamagers().isEmpty(), "Taunt must not invent damage or kill credit");
            assertNull(distant.getForcedTargetPlayerId());
            assertTrue(distant.getAggro().isEmpty());

            assertTrue(module.useAbility(player, AbilitySlot.SIGNATURE).successful());
            double threatBeforeDeniedCast = elite.getAggro().get(player);
            assertFalse(module.useAbility(player, AbilitySlot.SIGNATURE).successful(),
                    "Two casts must exhaust Resolve despite the taunt resource return");
            assertEquals(threatBeforeDeniedCast, elite.getAggro().get(player));
            assertEquals(protectsCaster, incomingDamage() < 10D);
            assertEquals(10D, incomingDamage(rival), "Protection belongs only to the caster");
            for (var member : List.of(player, rival)) {
                var hit = outgoingEvent(member, elite);
                Bukkit.getPluginManager().callEvent(hit);
                assertEquals(outgoing, hit.getDamage(), .000001);
            }
            var outsiderHit = outgoingEvent(outsider, elite);
            Bukkit.getPluginManager().callEvent(outsiderHit);
            assertEquals(10D, outsiderHit.getDamage());

            assertTrue(module.selectForm(player, "spellcaster").accepted());
            assertNull(elite.getForcedTargetPlayerId(), "Changing class must retire its taunt lease");
            assertEquals(10D, incomingDamage());
            for (var member : List.of(player, rival)) {
                var hit = outgoingEvent(member, elite);
                Bukkit.getPluginManager().callEvent(hit);
                assertEquals(10D, hit.getDamage(), .000001);
            }
        } finally {
            distant.remove(RemovalReason.SHUTDOWN);
        }
    }

    @ParameterizedTest(name = "{displayName} [{index}] {0}")
    @CsvSource({"spellblade,false", "arcane_knight,true"})
    void interruptingStrikesSelectEnemiesSpendManaAndReleaseTimedControl(
            String form, boolean radialAndWeakening) {
        assertTrue(module.setClassLevelForAdministration(player, form, 91).applied());
        var front = target();
        var behind = CombatTestEntities.spawnElite(player.getLocation().add(0, 0, -3));
        var distant = CombatTestEntities.spawnElite(player.getLocation().add(0, 0, 25));
        var bystander = MockBukkit.getMock().addPlayer();
        bystander.teleport(player.getLocation().add(0, 0, 2));
        try {
            for (var elite : List.of(front, behind, distant)) {
                elite.setLevel(91);
                elite.getLivingEntity().getAttribute(Attribute.MAX_HEALTH).setBaseValue(2048D);
                elite.getLivingEntity().setHealth(2048D);
            }
            assertTrue(module.useAbility(player, AbilitySlot.SIGNATURE).successful());
            assertTrue(front.getLivingEntity().getHealth() < 2048D);
            assertTrue(front.getPowerSuppression().isSuppressed(ElitePowerPauseReason.INTERRUPT));
            assertEquals(radialAndWeakening, behind.getLivingEntity().getHealth() < 2048D,
                    "Spellblade selects forward enemies; Arcane Knight also hits behind the caster");
            assertEquals(radialAndWeakening,
                    behind.getPowerSuppression().isSuppressed(ElitePowerPauseReason.INTERRUPT));
            assertEquals(2048D, distant.getLivingEntity().getHealth());
            assertFalse(distant.getPowerSuppression().isSuppressed());
            assertEquals(20D, bystander.getHealth());
            assertEquals(radialAndWeakening, incomingDamage() < 10D);
            assertEquals(radialAndWeakening, incomingDamage(player, behind) < 10D);
            assertEquals(10D, incomingDamage(player, distant));

            assertTrue(module.useAbility(player, AbilitySlot.SIGNATURE).successful());
            double healthBeforeDeniedCast = front.getLivingEntity().getHealth();
            assertFalse(module.useAbility(player, AbilitySlot.SIGNATURE).successful());
            assertEquals(healthBeforeDeniedCast, front.getLivingEntity().getHealth(),
                    "Insufficient Mana must reject the strike before it deals damage");
            assertTrue(module.selectForm(player, "spellcaster").accepted());
            for (var elite : List.of(front, behind, distant))
                assertEquals(10D, incomingDamage(player, elite));
            MockBukkit.getMock().getScheduler().performTicks(60);
            for (var elite : List.of(front, behind, distant))
                assertFalse(elite.getPowerSuppression().isSuppressed(ElitePowerPauseReason.INTERRUPT),
                        "The strike's timed interruption must expire without retaining a power lock");
        } finally {
            behind.remove(RemovalReason.SHUTDOWN);
            distant.remove(RemovalReason.SHUTDOWN);
        }
    }

    @ParameterizedTest
    @CsvSource({"reaver,true", "slayer,false"})
    void detectionUtilitySpendsEarnedFuryAndRevealsOnlyEligibleNearbyElites(String form, boolean woundedOnly) {
        int level = module.catalog().require(form).band().effectiveStart();
        assertTrue(module.setClassLevelForAdministration(player, form, level).applied());
        var wounded = target().getLivingEntity();
        wounded.setHealth(wounded.getAttribute(Attribute.MAX_HEALTH).getValue() * .25D);
        assertFalse(module.useAbility(player, AbilitySlot.UTILITY).successful());
        assertFalse(wounded.hasPotionEffect(PotionEffectType.GLOWING));
        assertFalse(player.hasPotionEffect(PotionEffectType.SPEED));
        incomingDamage();
        incomingDamage();
        wounded.setHealth(wounded.getAttribute(Attribute.MAX_HEALTH).getValue() * .25D);
        var healthy = CombatTestEntities.spawnElite(player.getLocation().add(1, 0, 2));
        var distant = CombatTestEntities.spawnElite(player.getLocation().add(0, 0, 20));
        var bystander = MockBukkit.getMock().addPlayer();
        bystander.teleport(player.getLocation().add(1, 0, 0));
        bystander.setHealth(4D);
        distant.getLivingEntity().setHealth(distant.getLivingEntity().getAttribute(Attribute.MAX_HEALTH).getValue() * .25D);
        try {
            var cast = module.useAbility(player, AbilitySlot.UTILITY);
            assertTrue(cast.successful(), () -> cast.failureReason() + "; target health=" + wounded.getHealth()
                    + "/" + wounded.getAttribute(Attribute.MAX_HEALTH).getValue());
            assertTrue(wounded.hasPotionEffect(PotionEffectType.GLOWING));
            assertEquals(!woundedOnly, healthy.getLivingEntity().hasPotionEffect(PotionEffectType.GLOWING));
            assertFalse(distant.getLivingEntity().hasPotionEffect(PotionEffectType.GLOWING));
            assertFalse(bystander.hasPotionEffect(PotionEffectType.GLOWING));
            assertFalse(bystander.hasPotionEffect(PotionEffectType.SPEED));
            assertEquals(1, player.getPotionEffect(PotionEffectType.SPEED).getAmplifier());
            player.removePotionEffect(PotionEffectType.SPEED);
            wounded.removePotionEffect(PotionEffectType.GLOWING);
            assertFalse(module.useAbility(player, AbilitySlot.UTILITY).successful());
            assertFalse(player.hasPotionEffect(PotionEffectType.SPEED));
            assertFalse(wounded.hasPotionEffect(PotionEffectType.GLOWING));
        } finally {
            healthy.remove(RemovalReason.SHUTDOWN);
            distant.remove(RemovalReason.SHUTDOWN);
        }
    }

    @ParameterizedTest
    @CsvSource({"ranger,false,false", "skirmisher,false,true", "elementalist,true,false",
            "pyromancer,true,false", "occultist,true,false", "tempest_archer,false,true"})
    void nearbyMarkBenefitsOnlyPartyMembersAndRevokesItsModifiersOnClassChange(
            String form, boolean weakens, boolean grantsSpeed) throws Exception {
        int level = module.catalog().require(form).band().effectiveStart();
        assertTrue(module.setClassLevelForAdministration(player, form, level).applied());
        var marked = target().getLivingEntity();
        var distant = CombatTestEntities.spawnElite(player.getLocation().add(0, 0, 20));
        var bystander = MockBukkit.getMock().addPlayer();
        bystander.teleport(player.getLocation().add(1, 0, 0));
        var ally = MockBukkit.getMock().addPlayer();
        ally.teleport(player.getLocation().add(2, 0, 0));
        openParty(ally);
        assertTrue(ClassAbilityEligibility.isEligible(ally));
        assertTrue(ClassAbilityEligibility.isEligible(bystander));
        try {
            assertTrue(module.useAbility(player, AbilitySlot.UTILITY).successful());
            assertTrue(marked.hasPotionEffect(PotionEffectType.GLOWING));
            assertFalse(distant.getLivingEntity().hasPotionEffect(PotionEffectType.GLOWING));
            assertFalse(bystander.hasPotionEffect(PotionEffectType.GLOWING));
            assertEquals(grantsSpeed, player.hasPotionEffect(PotionEffectType.SPEED));
            assertTrue(outgoingDamage() > 10D, "The mark must modify actual damage events for its caster");
            var distantHit = outgoingEvent(player, distant);
            Bukkit.getPluginManager().callEvent(distantHit);
            assertEquals(10D, distantHit.getDamage(), "The mark must not become a caster-wide damage buff");
            var allyHit = outgoingEvent(ally, target());
            Bukkit.getPluginManager().callEvent(allyHit);
            assertTrue(allyHit.getDamage() > 10D, "A nearby party member must benefit from the mark");
            var outsiderHit = outgoingEvent(bystander, target());
            Bukkit.getPluginManager().callEvent(outsiderHit);
            assertEquals(10D, outsiderHit.getDamage(), "An eligible non-party player must not benefit");
            assertEquals(weakens, incomingDamage() < 10D);

            assertTrue(module.selectForm(player, "spellcaster").accepted());
            assertEquals(10D, outgoingDamage());
            assertEquals(10D, incomingDamage());
            var retiredAllyHit = outgoingEvent(ally, target());
            Bukkit.getPluginManager().callEvent(retiredAllyHit);
            assertEquals(10D, retiredAllyHit.getDamage());
        } finally {
            distant.remove(RemovalReason.SHUTDOWN);
        }
    }

    @ParameterizedTest
    @CsvSource({"necromancer,61,8.27125,false", "plaguebringer,91,7.7905,false",
            "tyrant,91,10,true"})
    void areaDebuffSlowsAndWeakensEnemiesWithoutAffectingBystanders(
            String form, int level, double weakenedDamage, boolean reveals) {
        assertTrue(module.setClassLevelForAdministration(player, form, level).applied());
        if (reveals) {
            assertFalse(module.useAbility(player, AbilitySlot.UTILITY).successful());
            for (int hit = 0; hit < 5; hit++) incomingDamage();
        }
        var enemy = target().getLivingEntity();
        var distant = CombatTestEntities.spawnElite(player.getLocation().add(0, 0, 20));
        var bystander = MockBukkit.getMock().addPlayer();
        bystander.teleport(player.getLocation().add(1, 0, 0));
        try {
            assertTrue(module.useAbility(player, AbilitySlot.UTILITY).successful());
            assertNotNull(enemy.getPotionEffect(PotionEffectType.SLOWNESS));
            assertEquals(reveals, enemy.hasPotionEffect(PotionEffectType.GLOWING));
            assertFalse(distant.getLivingEntity().hasPotionEffect(PotionEffectType.SLOWNESS));
            assertFalse(distant.getLivingEntity().hasPotionEffect(PotionEffectType.GLOWING));
            assertFalse(bystander.hasPotionEffect(PotionEffectType.SLOWNESS));
            assertFalse(bystander.hasPotionEffect(PotionEffectType.GLOWING));
            assertFalse(player.hasPotionEffect(PotionEffectType.SLOWNESS));
            assertEquals(weakenedDamage, incomingDamage(), .000001);
            assertEquals(10D, outgoingDamage(), "Weakening must not become a caster damage buff");

            assertTrue(module.selectForm(player, "spellcaster").accepted());
            assertEquals(10D, incomingDamage(), "Retiring the class must revoke its weakening modifier");
        } finally {
            distant.remove(RemovalReason.SHUTDOWN);
        }
    }

    @Test
    void justicarWeakensOnlyRecentAttackersInReachUntilClassChange() {
        assertTrue(module.setClassLevelForAdministration(player, "justicar", 61).applied());
        for (int hit = 0; hit < 5; hit++) incomingDamage();
        var uninvolved = CombatTestEntities.spawnElite(player.getLocation().add(0, 0, 4));
        var distant = CombatTestEntities.spawnElite(player.getLocation().add(0, 0, 20));
        try {
            assertEquals(10D, incomingDamage(player, distant));
            assertTrue(module.useAbility(player, AbilitySlot.UTILITY).successful());
            assertEquals(7.695D, incomingDamage(), .000001);
            assertEquals(10D, incomingDamage(player, uninvolved),
                    "A nearby elite that did not attack the caster must not be weakened");
            assertEquals(10D, incomingDamage(player, distant),
                    "A recent attacker outside the ability radius must not be weakened");
            assertTrue(module.selectForm(player, "spellcaster").accepted());
            assertEquals(10D, incomingDamage());
        } finally {
            uninvolved.remove(RemovalReason.SHUTDOWN);
            distant.remove(RemovalReason.SHUTDOWN);
        }
    }

    @ParameterizedTest
    @CsvSource({"false,135,1", "true,154,.9302909090909091"})
    void trapperExtendsExistingSlowAndItsPassiveStrengthensControl(
            boolean passives, int addedDuration, double movementMultiplier) {
        fullCombatActive = passives;
        assertTrue(module.setClassLevelForAdministration(player, "trapper", 91).applied());
        var enemy = target().getLivingEntity();
        enemy.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 80, 2));
        var movement = enemy.getAttribute(Attribute.MOVEMENT_SPEED);
        double before = movement.getValue();
        var outsider = MockBukkit.getMock().addPlayer();
        outsider.teleport(player.getLocation().add(1, 0, 0));

        assertTrue(module.useAbility(player, AbilitySlot.UTILITY).successful());
        assertEquals(80 + addedDuration, enemy.getPotionEffect(PotionEffectType.SLOWNESS).getDuration());
        assertEquals(2, enemy.getPotionEffect(PotionEffectType.SLOWNESS).getAmplifier());
        assertEquals(before * movementMultiplier, movement.getValue(), .000001);
        assertTrue(enemy.hasPotionEffect(PotionEffectType.GLOWING));
        assertFalse(outsider.hasPotionEffect(PotionEffectType.SLOWNESS));
        assertFalse(outsider.hasPotionEffect(PotionEffectType.GLOWING));
        assertTrue(module.useAbility(player, AbilitySlot.UTILITY).successful());
        assertEquals(80 + addedDuration * 2, enemy.getPotionEffect(PotionEffectType.SLOWNESS).getDuration());
        assertEquals(before * movementMultiplier, movement.getValue(), .000001,
                "Recasting must extend the slow without stacking its passive strength");
        module.close();
        assertEquals(before, movement.getValue(), .000001,
                "Closing combat must release the extra movement modifier");
    }
}
