package com.magmaguy.elitemobs.experimentalcombat;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.MobCombatSettingsConfig;
import com.magmaguy.elitemobs.config.PartyConfig;
import com.magmaguy.elitemobs.parties.PartyManager;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.mobconstructor.ElitePowerPauseReason;
import com.magmaguy.elitemobs.api.PlayerDamagedByEliteMobEvent;
import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.api.internal.RemovalReason;
import com.magmaguy.elitemobs.combatsystem.combattag.DungeonCombatRuntime;
import com.magmaguy.elitemobs.combatsystem.CombatDamageContext;
import com.magmaguy.elitemobs.experimentalcombat.classes.AbilitySlot;
import com.magmaguy.elitemobs.experimentalcombat.content.BuiltInClassContent;
import com.magmaguy.elitemobs.experimentalcombat.progression.*;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.presentation.actionbar.ActionBarCompositor;
import com.magmaguy.magmacore.instance.InstanceProtector;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionEffect;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import org.mockbukkit.mockbukkit.plugin.PluginMock;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class ExperimentalCombatBehaviorTest {
    @TempDir java.nio.file.Path configurationDirectory;
    private JavaPlugin previousPlugin;
    private PlayerMock player;
    private ExperimentalCombatModule module;
    private EliteEntity target;
    private boolean fullCombatActive;
    private boolean partyInitialized;

    @BeforeEach
    void openCombat() {
        var server = MockBukkit.mock();
        previousPlugin = MetadataHandler.PLUGIN;
        MetadataHandler.PLUGIN = MockBukkit.loadWith(PluginMock.class, new java.io.ByteArrayInputStream("""
                name: CombatBehaviorTest
                version: 1
                main: org.mockbukkit.mockbukkit.plugin.PluginMock
                authors: [Autotester]
                """.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
        new MobCombatSettingsConfig(configurationDirectory.resolve("MobCombatSettings.yml").toFile());
        player = server.addPlayer();
        InstanceProtector.addProtectedWorld(player.getWorld());
        fullCombatActive = false;
        openModule();
    }

    private void openModule() {
        var levels = new EnumMap<SkillType, Integer>(SkillType.class);
        for (var skill : SkillType.values()) levels.put(skill, 100);
        var progression = new ClassProgressionModule(BuiltInClassContent.catalog(),
                id -> Optional.of(new FoundationLevelSnapshot(id, levels)),
                new MemoryStore(), Runnable::run);
        progression.load(player.getUniqueId()).join();
        module = new ExperimentalCombatModule(new DungeonCombatRuntime(200, 20), availability -> progression,
                ignored -> fullCombatActive);
        module.startGameplay();
        ClassAbilityEligibility.install(module::controlsAlwaysAvailable, module::mechanicsActive);
        module.onControlModeChanged(player);
    }

    @AfterEach
    void closeCombat() {
        try {
            if (module != null) module.close();
            if (target != null) target.remove(RemovalReason.SHUTDOWN);
        } finally {
            try {
                if (partyInitialized) {
                    try {
                        PartyManager.shutdown();
                        MockBukkit.getMock().getScheduler().waitAsyncTasksFinished();
                    } finally {
                        PlayerData.closeConnection();
                    }
                }
            } finally {
                DungeonCombatRuntime.shutdownIfInitialized();
                ActionBarCompositor.shutdown();
                if (player != null) InstanceProtector.removeProtectedWorld(player.getWorld());
                MockBukkit.unmock();
                MetadataHandler.PLUGIN = previousPlugin;
            }
        }
    }

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
    @CsvSource({"spellcaster,1,1,1180,absorption", "guardian,31,0,1240,absorption",
            "arcane_knight,91,2,20,absorption", "occultist,31,2,20,glowing",
            "plaguebringer,91,3,360,slowness"})
    void scheduledRecoveryFundsUtilityOnlyAfterEnoughUpdates(
            String form, int level, int initialCasts, int ticksBeforeAffordable, String status) {
        assertTrue(module.setClassLevelForAdministration(player, form, level).applied());
        var effect = Objects.requireNonNull(org.bukkit.Registry.EFFECT.get(org.bukkit.NamespacedKey.minecraft(status)));
        var recipient = effect.equals(PotionEffectType.ABSORPTION) ? player : target().getLivingEntity();
        for (int cast = 0; cast < initialCasts; cast++)
            assertTrue(module.useAbility(player, AbilitySlot.UTILITY).successful());
        assertFalse(module.useAbility(player, AbilitySlot.UTILITY).successful());
        recipient.removePotionEffect(effect);
        var scheduler = MockBukkit.getMock().getScheduler();
        scheduler.performTicks(ticksBeforeAffordable);
        assertFalse(module.useAbility(player, AbilitySlot.UTILITY).successful());
        assertFalse(recipient.hasPotionEffect(effect));
        scheduler.performOneTick();
        assertTrue(module.useAbility(player, AbilitySlot.UTILITY).successful(),
                "The production update task must earn enough resource for the utility");
        assertNotNull(recipient.getPotionEffect(effect));
        assertFalse(module.useAbility(player, AbilitySlot.UTILITY).successful());
    }

    @Test
    void takingDamageDelaysFocusRecoveryBeforeAnotherMarkCanBeCast() {
        assertTrue(module.setClassLevelForAdministration(player, "ranger", 1).applied());
        var enemy = target().getLivingEntity();
        for (int cast = 0; cast < 4; cast++)
            assertTrue(module.useAbility(player, AbilitySlot.UTILITY).successful());
        incomingDamage();
        enemy.removePotionEffect(PotionEffectType.GLOWING);
        var scheduler = MockBukkit.getMock().getScheduler();
        scheduler.performTicks(60);
        assertFalse(module.useAbility(player, AbilitySlot.UTILITY).successful(),
                "Taking damage must postpone positive Focus recovery");
        scheduler.performTicks(20);
        assertFalse(module.useAbility(player, AbilitySlot.UTILITY).successful());
        assertFalse(enemy.hasPotionEffect(PotionEffectType.GLOWING));
        scheduler.performOneTick();
        assertTrue(module.useAbility(player, AbilitySlot.UTILITY).successful());
        assertTrue(enemy.hasPotionEffect(PotionEffectType.GLOWING));
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

    @ParameterizedTest(name = "{displayName} [{index}] changeClass={0}")
    @CsvSource({"false", "true"})
    void seraphChainHealsTheMostWoundedPartyMemberFirstAndStopsOnClassChange(
            boolean changeClass) throws Exception {
        assertTrue(module.setClassLevelForAdministration(player, "seraph", 91).applied());
        var server = MockBukkit.getMock();
        var first = server.addPlayer();
        var second = server.addPlayer();
        var outsider = server.addPlayer();
        for (var member : List.of(first, second, outsider))
            member.teleport(player.getLocation().add(1, 0, 0));
        openParty(first, second);
        player.setHealth(16D);
        first.setHealth(2D);
        second.setHealth(8D);
        outsider.setHealth(1D);

        assertTrue(module.useAbility(player, AbilitySlot.SIGNATURE).successful());
        assertEquals(2D, first.getHealth(), "The chain must run on the scheduler, not heal everyone immediately");
        server.getScheduler().performOneTick();
        assertEquals(6.1735D, first.getHealth(), .000001);
        assertNotNull(first.getPotionEffect(PotionEffectType.ABSORPTION));
        assertEquals(8D, second.getHealth());
        assertFalse(second.hasPotionEffect(PotionEffectType.ABSORPTION));
        assertEquals(16D, player.getHealth());
        if (changeClass) assertTrue(module.selectForm(player, "spellcaster").accepted());
        server.getScheduler().performTicks(3);
        assertEquals(changeClass ? 8D : 12.1735D, second.getHealth(), .000001);
        assertEquals(!changeClass, second.hasPotionEffect(PotionEffectType.ABSORPTION));
        assertEquals(16D, player.getHealth());
        server.getScheduler().performTicks(6);
        assertEquals(changeClass ? 16D : 20D, player.getHealth());
        assertEquals(6.1735D, first.getHealth(), .000001, "A chain must visit each recipient only once");
        assertEquals(1D, outsider.getHealth());
        assertFalse(outsider.hasPotionEffect(PotionEffectType.ABSORPTION));
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

    @Test
    void shieldbearerRedirectsDamageFromOnlyTheTwoWeakestNearbyAlliesUntilClassChange() throws Exception {
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
        healthy.setHealth(12D);
        weakest.setHealth(4D);
        secondWeakest.setHealth(8D);
        distant.setHealth(1D);
        outsider.setHealth(1D);

        assertTrue(module.useAbility(player, AbilitySlot.SIGNATURE).successful());
        for (var member : List.of(player, healthy, weakest, secondWeakest)) {
            assertTrue(member.hasPotionEffect(PotionEffectType.ABSORPTION));
            member.removePotionEffect(PotionEffectType.ABSORPTION);
            member.setAbsorptionAmount(0D);
        }
        assertEquals(7.545D, incomingDamage(healthy), .000001);
        assertEquals(20D, player.getHealth());
        assertEquals(4.90425D, incomingDamage(weakest), .000001);
        assertEquals(18.151475D, player.getHealth(), .000001);
        assertEquals(4.90425D, incomingDamage(secondWeakest), .000001);
        assertEquals(16.30295D, player.getHealth(), .000001);
        for (var excluded : List.of(distant, outsider)) {
            assertFalse(excluded.hasPotionEffect(PotionEffectType.ABSORPTION));
            assertEquals(10D, incomingDamage(excluded));
        }
        assertTrue(module.selectForm(player, "spellcaster").accepted());
        for (var member : List.of(healthy, weakest, secondWeakest)) assertEquals(10D, incomingDamage(member));
        assertEquals(16.30295D, player.getHealth(), .000001);
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
        var scheduler = MockBukkit.getMock().getScheduler();
        scheduler.performTicks(20);
        assertFalse(module.useAbility(player, AbilitySlot.SIGNATURE).successful());
        scheduler.performOneTick();
        assertTrue(module.useAbility(player, AbilitySlot.SIGNATURE).successful());
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

    private void openParty(PlayerMock... members) throws Exception {
        var config = new org.bukkit.configuration.file.YamlConfiguration();
        config.set("sidebarEnabled", false);
        var configFile = configurationDirectory.resolve("Party.yml").toFile();
        config.save(configFile);
        new PartyConfig(configFile);
        partyInitialized = true;
        PlayerData.initializeDatabaseConnection();
        var scheduler = MockBukkit.getMock().getScheduler();
        scheduler.performOneTick();
        long deadline = System.nanoTime() + java.util.concurrent.TimeUnit.SECONDS.toNanos(5);
        while (Bukkit.getOnlinePlayers().stream().anyMatch(member -> !PlayerData.isDataLoaded(member.getUniqueId()))
                && System.nanoTime() < deadline) Thread.sleep(5);
        for (var member : Bukkit.getOnlinePlayers())
            assertTrue(PlayerData.isDataLoaded(member.getUniqueId()), "Timed out loading temporary player data");
        scheduler.performOneTick();
        PartyManager.create(player);
        assertNotNull(PartyManager.getParty(player.getUniqueId()));
        for (var member : members) {
            assertTrue(PlayerData.isDataLoaded(member.getUniqueId()));
            member.addAttachment(MetadataHandler.PLUGIN, "elitemobs.party", true);
            PartyManager.invite(player, member.getName());
            PartyManager.accept(member);
            assertSame(PartyManager.getParty(player.getUniqueId()), PartyManager.getParty(member.getUniqueId()));
        }
    }

    private EliteEntity target() {
        if (target == null) {
            target = CombatTestEntities.spawnElite(player.getLocation().add(0, 0, 3));
        }
        return target;
    }

    private double outgoingDamage() {
        var event = outgoingEvent();
        Bukkit.getPluginManager().callEvent(event);
        return event.getDamage();
    }

    private EliteMobDamagedByPlayerEvent outgoingEvent() {
        return outgoingEvent(player, target());
    }

    @SuppressWarnings("removal")
    private EliteMobDamagedByPlayerEvent outgoingEvent(PlayerMock attacker, EliteEntity victim) {
        var hit = new EntityDamageByEntityEvent(attacker, victim.getLivingEntity(),
                EntityDamageEvent.DamageCause.ENTITY_ATTACK, 10D);
        return new EliteMobDamagedByPlayerEvent(victim, attacker, hit, 10D, false, false, 1D);
    }

    private double incomingDamage() {
        return incomingDamage(player);
    }

    private double incomingDamage(PlayerMock victim) {
        return incomingDamage(victim, target());
    }

    @SuppressWarnings("removal")
    private double incomingDamage(PlayerMock victim, EliteEntity attacker) {
        var hit = new EntityDamageByEntityEvent(attacker.getLivingEntity(), victim,
                EntityDamageEvent.DamageCause.ENTITY_ATTACK, 10D);
        var event = new PlayerDamagedByEliteMobEvent(attacker, victim, hit, null, 10D);
        Bukkit.getPluginManager().callEvent(event);
        return event.getDamage();
    }

    private static final class MemoryStore implements ClassProgressionStore {
        private final Map<UUID, StoredClassProfile> profiles = new HashMap<>();
        private final Map<UUID, Map<String, StoredClassProgress>> progress = new HashMap<>();

        private Map<String, StoredClassProgress> rows(UUID id) {
            return progress.computeIfAbsent(id, ignored -> new HashMap<>());
        }
        public StoredClassProfile loadOrCreateProfile(UUID id, int version) {
            return profiles.computeIfAbsent(id, ignored -> new StoredClassProfile(id, "spellcaster",
                    InputProfile.DEFAULT.storedId(), StoredClassProfile.DEFAULT_FOCUS_SLOT, version));
        }
        public List<StoredClassProgress> loadAllProgress(UUID id) { return List.copyOf(rows(id).values()); }
        public StoredClassProgress loadProgressOrZero(UUID id, String form, int version) {
            return rows(id).getOrDefault(form, StoredClassProgress.zero(id, form, version));
        }
        public void saveProfile(StoredClassProfile value) { profiles.put(value.playerId(), value); }
        public void saveProgress(StoredClassProgress value) { rows(value.playerId()).put(value.formId(), value); }
        public void savePlayerAggregate(StoredClassProfile value, Collection<StoredClassProgress> rows) {
            saveProfile(value);
            rows.forEach(this::saveProgress);
        }
    }
}
