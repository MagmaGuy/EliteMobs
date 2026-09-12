package com.magmaguy.elitemobs.advancedcombat;

import org.bukkit.attribute.Attribute;

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

class CombatProtectionBehaviorTest extends CombatBehaviorFixture {
    @ParameterizedTest
    @CsvSource({"SIGNATURE,HASTE,5", "UTILITY,RESISTANCE,3"})
    void adventurerBuffConsumesStaminaAndLeavesBystandersUnchanged(AbilitySlot slot, String effect, int fundedCasts) {
        assertTrue(module.setClassLevelForAdministration(player, "adventurer", 1).applied());
        var type = effect.equals("HASTE") ? PotionEffectType.HASTE : PotionEffectType.RESISTANCE;
        var bystander = MockBukkit.getMock().addPlayer();
        bystander.teleport(player.getLocation());
        assertTrue(module.useAbility(player, slot).successful());
        var applied = player.getPotionEffect(type);
        assertNotNull(applied);
        assertEquals(0, applied.getAmplifier());
        assertEquals(100, applied.getDuration());
        assertFalse(bystander.hasPotionEffect(type));
        // Exhaust the remaining stamina through actual casts, then prove refusal has no effect.
        for (int cast = 1; cast < fundedCasts; cast++) {
            player.removePotionEffect(type);
            assertTrue(module.useAbility(player, slot).successful());
            assertNotNull(player.getPotionEffect(type));
        }
        player.removePotionEffect(type);
        assertFalse(module.useAbility(player, slot).successful());
        assertFalse(player.hasPotionEffect(type));
    }

    @Test
    void bulwarkGuardRestrictsOnlyItsCasterAndRetiresOnTeleportOrClassChange() throws Exception {
        assertTrue(module.setClassLevelForAdministration(player, "bulwark", 91).applied());
        assertFalse(module.useAbility(player, AbilitySlot.SIGNATURE).successful());
        for (int hit = 0; hit < 5; hit++) incomingDamage();
        var ally = MockBukkit.getMock().addPlayer();
        ally.teleport(player.getLocation().add(1, 0, 0));
        openParty(ally);
        var distant = CombatTestEntities.spawnElite(player.getLocation().add(0, 0, 30));
        try {
            for (var member : List.of(player, ally)) {
                member.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 100, 0));
                member.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 100, 0));
            }
            double enemyHealth = target().getLivingEntity().getHealth();
            assertTrue(module.useAbility(player, AbilitySlot.SIGNATURE).successful());
            assertEquals(player.getUniqueId(), target().getForcedTargetPlayerId());
            assertTrue(target().getPowerSuppression().isSuppressed(ElitePowerPauseReason.INTERRUPT));
            assertNull(distant.getForcedTargetPlayerId());
            assertFalse(distant.getPowerSuppression().isSuppressed());
            assertEquals(enemyHealth, target().getLivingEntity().getHealth());
            assertTrue(module.useAbility(player, AbilitySlot.SIGNATURE).successful());
            assertFalse(module.useAbility(player, AbilitySlot.SIGNATURE).successful());
            assertFalse(player.hasPotionEffect(PotionEffectType.SLOWNESS));
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 100, 1));
            assertFalse(player.hasPotionEffect(PotionEffectType.SLOWNESS));
            assertTrue(player.hasPotionEffect(PotionEffectType.POISON));
            assertTrue(ally.hasPotionEffect(PotionEffectType.SLOWNESS));
            assertEquals(10D, incomingDamage(ally));
            assertEquals(2.64075D, incomingDamage(), .000001,
                    "The planted guard must combine its reduction with the signature's self-protection");

            var origin = player.getLocation();
            var destination = origin.clone().add(1, 0, 0);
            destination.setYaw(45F);
            destination.setPitch(20F);
            var move = new org.bukkit.event.player.PlayerMoveEvent(player, origin, destination);
            Bukkit.getPluginManager().callEvent(move);
            assertEquals(origin.toVector(), move.getTo().toVector());
            assertEquals(45F, move.getTo().getYaw());
            assertEquals(20F, move.getTo().getPitch());
            var allyMove = new org.bukkit.event.player.PlayerMoveEvent(
                    ally, ally.getLocation(), ally.getLocation().add(1, 0, 0));
            var allyDestination = allyMove.getTo().clone();
            Bukkit.getPluginManager().callEvent(allyMove);
            assertEquals(allyDestination, allyMove.getTo());

            var cancelledTeleport = new org.bukkit.event.player.PlayerTeleportEvent(
                    player, origin, origin.clone().add(.5, 0, 0));
            cancelledTeleport.setCancelled(true);
            Bukkit.getPluginManager().callEvent(cancelledTeleport);
            assertEquals(2.64075D, incomingDamage(), .000001, "A cancelled teleport must preserve the guard");
            assertTrue(player.teleport(origin.clone().add(.5, 0, 0)));
            assertEquals(7.545D, incomingDamage(), .000001,
                    "Even a short teleport must remove the planted bonus without removing self-protection");
            assertTrue(player.teleport(origin));
            assertEquals(7.545D, incomingDamage(), .000001, "Returning to the origin must not revive the guard");
            for (int hit = 0; hit < 5; hit++) incomingDamage();
            assertTrue(module.useAbility(player, AbilitySlot.SIGNATURE).successful());
            assertEquals(2.64075D, incomingDamage(), .000001);
            assertTrue(module.selectForm(player, "spellcaster").accepted());
            assertEquals(10D, incomingDamage());
            assertNull(target().getForcedTargetPlayerId());
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 100, 0));
            assertTrue(player.hasPotionEffect(PotionEffectType.SLOWNESS));
            var retiredMove = new org.bukkit.event.player.PlayerMoveEvent(player, origin, destination);
            Bukkit.getPluginManager().callEvent(retiredMove);
            assertEquals(destination, retiredMove.getTo());
            MockBukkit.getMock().getScheduler().performTicks(60);
            assertFalse(target().getPowerSuppression().isSuppressed(ElitePowerPauseReason.INTERRUPT));
        } finally {
            distant.remove(RemovalReason.SHUTDOWN);
        }
    }

    @ParameterizedTest
    @CsvSource({"cryomancer,91,123,1,7.545,true", "battlemage,61,115,0,7.695,false"})
    void selfWardAppliesItsEffectsAndRevokesProtectionOnClassChange(
            String form, int level, int duration, int amplifier, double damage, boolean cleanses) {
        assertTrue(module.setClassLevelForAdministration(player, form, level).applied());
        player.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 100, 0));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 100, 0));
        player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 100, 0));

        assertTrue(module.useAbility(player, AbilitySlot.UTILITY).successful());
        assertEquals(!cleanses, player.hasPotionEffect(PotionEffectType.POISON));
        assertEquals(!cleanses, player.hasPotionEffect(PotionEffectType.SLOWNESS));
        assertTrue(player.hasPotionEffect(PotionEffectType.NIGHT_VISION));
        var shield = player.getPotionEffect(PotionEffectType.ABSORPTION);
        assertNotNull(shield);
        assertEquals(duration, shield.getDuration());
        assertEquals(amplifier, shield.getAmplifier());
        assertEquals(damage, incomingDamage(), 0.000001);

        assertTrue(module.selectForm(player, "spellcaster").accepted());
        assertEquals(10D, incomingDamage(), "Changing class must revoke the previous ward modifier");
    }

    @ParameterizedTest
    @CsvSource({"spiritcaller,61,UTILITY,.922,5.3865,17.6915",
            "soulwarden,91,SIGNATURE,1.964,5.2815,17.7365",
            "soulwarden,91,UTILITY,1.7185,5.2815,17.7365",
            "spiritbinder,91,UTILITY,.982,5.2815,17.7365"})
    void soulLinkHealsProtectsAndSharesActualDamageOnlyWithPartyMembersUntilClassChange(
            String form, int level, AbilitySlot slot, double healing,
            double linkedDamage, double casterHealthAfterShare) throws Exception {
        assertTrue(module.setClassLevelForAdministration(player, form, level).applied());
        var server = MockBukkit.getMock();
        var ally = server.addPlayer();
        var outsider = server.addPlayer();
        ally.teleport(player.getLocation().add(1, 0, 0));
        outsider.teleport(player.getLocation().add(2, 0, 0));
        openParty(ally);
        player.setHealth(8D);
        ally.setHealth(8D);
        outsider.setHealth(8D);

        assertTrue(module.useAbility(player, slot).successful());
        for (var member : List.of(player, ally)) {
            assertEquals(8D + healing, member.getHealth(), .000001);
            assertNotNull(member.getPotionEffect(PotionEffectType.ABSORPTION));
            member.removePotionEffect(PotionEffectType.ABSORPTION);
            member.setAbsorptionAmount(0D);
        }
        player.setHealth(20D);
        assertEquals(linkedDamage, incomingDamage(ally), .000001);
        assertEquals(casterHealthAfterShare, player.getHealth(), .000001,
                "The reduced ally hit must actually damage the other linked member");
        assertEquals(8D, outsider.getHealth());
        assertFalse(outsider.hasPotionEffect(PotionEffectType.ABSORPTION));
        assertEquals(10D, incomingDamage(outsider));

        assertTrue(module.selectForm(player, "spellcaster").accepted());
        assertEquals(10D, incomingDamage(ally));
        assertEquals(casterHealthAfterShare, player.getHealth(), .000001,
                "Retiring the caster must remove the party link as well as its damage reduction");
    }

    @ParameterizedTest
    @CsvSource({"false,1.848525,2", "true,1.422969807299,3"})
    void shieldbearerRedirectsDamageFromOnlyTheTwoWeakestNearbyAlliesUntilClassChange(
            boolean passivesActive, double redirectedCost, int shieldAmplifier) throws Exception {
        fullCombatActive = passivesActive;
        // Party members retain their default Spellcaster passive when combat is active.
        double ordinaryDamage = passivesActive ? 10.08D : 10D;
        double protectedDamage = passivesActive ? 7.60536D : 7.545D;
        double redirectedDamage = passivesActive ? 4.943484D : 4.90425D;
        assertTrue(module.setClassLevelForAdministration(player, "shieldbearer", 91).applied());
        for (int hit = 0; hit < 5; hit++) incomingDamage();
        var server = MockBukkit.getMock();
        var healthy = server.addPlayer();
        var weakest = server.addPlayer();
        var secondWeakest = server.addPlayer();
        var distant = server.addPlayer();
        var outsider = server.addPlayer();
        for (var member : List.of(healthy, weakest, secondWeakest, outsider))
            member.teleport(player.getLocation().add(1, 0, 0));
        distant.teleport(player.getLocation().add(40, 0, 0));
        openParty(healthy, weakest, secondWeakest, distant);
        // An 80-health ally makes Aegis' inherited shield increase cross a
        // potion-strength threshold. MockBukkit does not consume absorption.
        for (var member : List.of(healthy, weakest, secondWeakest))
            member.getAttribute(Attribute.MAX_HEALTH).setBaseValue(80D);
        healthy.setHealth(12D);
        weakest.setHealth(4D);
        secondWeakest.setHealth(8D);
        distant.setHealth(1D);
        outsider.setHealth(1D);

        assertTrue(module.useAbility(player, AbilitySlot.SIGNATURE).successful());
        for (var member : List.of(healthy, weakest, secondWeakest))
            assertEquals(shieldAmplifier, member.getPotionEffect(PotionEffectType.ABSORPTION).getAmplifier());
        assertEquals(passivesActive, player.getAttribute(Attribute.MOVEMENT_SPEED).getValue() < .1D);
        for (var member : List.of(player, healthy, weakest, secondWeakest)) {
            assertTrue(member.hasPotionEffect(PotionEffectType.ABSORPTION));
            member.removePotionEffect(PotionEffectType.ABSORPTION);
            member.setAbsorptionAmount(0D);
        }
        assertEquals(protectedDamage, incomingDamage(healthy), .000001);
        assertEquals(20D, player.getHealth());
        assertEquals(redirectedDamage, incomingDamage(weakest), .000001);
        // Guardian and Aegis both reduce this actual inherited redirect cost.
        assertEquals(20D - redirectedCost, player.getHealth(), .000001);
        assertEquals(redirectedDamage, incomingDamage(secondWeakest), .000001);
        assertEquals(20D - 2D * redirectedCost, player.getHealth(), .000001);
        for (var excluded : List.of(distant, outsider)) {
            assertFalse(excluded.hasPotionEffect(PotionEffectType.ABSORPTION));
            assertEquals(ordinaryDamage, incomingDamage(excluded));
        }
        assertTrue(module.selectForm(player, "spellcaster").accepted());
        for (var member : List.of(healthy, weakest, secondWeakest)) assertEquals(ordinaryDamage, incomingDamage(member));
        assertEquals(20D - 2D * redirectedCost, player.getHealth(), .000001);
    }

    @ParameterizedTest
    @CsvSource({"guardian,false,false", "bulwark,false,false", "juggernaut,true,false",
            "dreadnought,true,false", "templar,true,true"})
    void immunityUtilityRequiresCombatResourceThenCleansesBlocksAndReleasesControl(
            String form, boolean cleansesPoison, boolean blocksPoison) {
        int level = module.catalog().require(form).band().effectiveStart();
        assertTrue(module.setClassLevelForAdministration(player, form, level).applied());
        assertFalse(module.useAbility(player, AbilitySlot.UTILITY).successful(),
                "Resolve and Fury must be earned before casting");
        for (int hit = 0; hit < 5; hit++) incomingDamage();
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 100, 0));
        player.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 100, 0));
        player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 100, 0));

        assertTrue(module.useAbility(player, AbilitySlot.UTILITY).successful());
        assertFalse(player.hasPotionEffect(PotionEffectType.SLOWNESS));
        assertEquals(!cleansesPoison, player.hasPotionEffect(PotionEffectType.POISON));
        assertTrue(player.hasPotionEffect(PotionEffectType.NIGHT_VISION));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 100, 1));
        assertFalse(player.hasPotionEffect(PotionEffectType.SLOWNESS));
        player.removePotionEffect(PotionEffectType.POISON);
        player.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 100, 1));
        assertEquals(!blocksPoison, player.hasPotionEffect(PotionEffectType.POISON));
        assertTrue(incomingDamage() < 10D, "The utility must also apply its damage protection");

        assertTrue(module.selectForm(player, "spellcaster").accepted());
        assertEquals(10D, incomingDamage());
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 100, 0));
        assertTrue(player.hasPotionEffect(PotionEffectType.SLOWNESS),
                "The previous class must not retain immunity after switching");
    }

    @ParameterizedTest
    @CsvSource({"deathless,SIGNATURE,3.964,false", "deathless,SIGNATURE,3.964,true",
            "lich,UTILITY,2.982,false", "lich,UTILITY,2.982,true"})
    void deathGuardTriggersOnceAndCannotHealAfterClassChange(
            String form, AbilitySlot slot, double recoveredHealth, boolean changeClassBeforeRecovery) {
        assertTrue(module.setClassLevelForAdministration(player, form, 91).applied());
        if (form.equals("deathless")) {
            assertFalse(module.useAbility(player, slot).successful());
            for (int hit = 0; hit < 5; hit++) incomingDamage();
        }
        assertTrue(module.useAbility(player, slot).successful());
        assertEquals(7.545D, incomingDamage(), 0.000001, "A nonfatal hit must not consume the guard");
        player.setHealth(2D);
        player.removePotionEffect(PotionEffectType.ABSORPTION);
        player.setAbsorptionAmount(2D);

        assertEquals(3D, incomingDamage(), 0.000001, "The guard must count absorption when capping this custom event");
        assertEquals(2D, player.getHealth(), "Recovery is scheduled after damage processing");
        if (changeClassBeforeRecovery) assertTrue(module.selectForm(player, "spellcaster").accepted());
        MockBukkit.getMock().getScheduler().performOneTick();
        if (changeClassBeforeRecovery) {
            assertEquals(2D, player.getHealth(), "A retired class must not apply delayed recovery");
            assertFalse(player.hasPotionEffect(PotionEffectType.ABSORPTION));
            assertEquals(10D, incomingDamage());
        } else {
            assertEquals(recoveredHealth, player.getHealth(), 0.000001);
            assertEquals(1, player.getPotionEffect(PotionEffectType.ABSORPTION).getAmplifier());
            assertEquals(7.545D, incomingDamage(), 0.000001, "The consumed guard must not cap another hit");
        }
    }
}
