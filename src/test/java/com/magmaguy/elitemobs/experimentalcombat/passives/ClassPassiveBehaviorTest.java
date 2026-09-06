package com.magmaguy.elitemobs.experimentalcombat.passives;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.api.EliteMobDeathEvent;
import com.magmaguy.elitemobs.api.PlayerDamagedByEliteMobEvent;
import com.magmaguy.elitemobs.combatsystem.CombatDamageContext;
import com.magmaguy.elitemobs.combatsystem.CombatDamageContext.ClassAbilityDamageDomain;
import com.magmaguy.elitemobs.experimentalcombat.content.BuiltInClassContent;
import com.magmaguy.elitemobs.experimentalcombat.CombatTestEntities;
import com.magmaguy.elitemobs.experimentalcombat.progression.ActiveLineageSnapshot;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.api.internal.RemovalReason;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.util.Vector;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import org.mockbukkit.mockbukkit.entity.LivingEntityMock;
import org.mockbukkit.mockbukkit.plugin.PluginMock;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ClassPassiveBehaviorTest {
    private ServerMock server;
    private JavaPlugin previousPlugin;
    private PlayerMock player;
    private ClassPassiveRuntime runtime;
    private EliteEntity target;
    private boolean active;

    @BeforeEach
    void open() {
        server = MockBukkit.mock();
        previousPlugin = MetadataHandler.PLUGIN;
        MetadataHandler.PLUGIN = MockBukkit.loadWith(PluginMock.class, new java.io.ByteArrayInputStream("""
                name: PassiveBehaviorTest
                version: 1
                main: org.mockbukkit.mockbukkit.plugin.PluginMock
                authors: [Autotester]
                """.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
        player = server.addPlayer();
        target = CombatTestEntities.spawnElite(player.getLocation().add(0, 0, 6));
        active = true;
    }

    @AfterEach
    void close() {
        try {
            if (runtime != null) runtime.shutdown();
            if (target != null) target.remove(RemovalReason.SHUTDOWN);
        } finally {
            MockBukkit.unmock();
            MetadataHandler.PLUGIN = previousPlugin;
        }
    }

    @ParameterizedTest
    @CsvSource({
            "paladin,9.88,9.7465,9.7465,0.099,0.099",
            "berserker,10.2535,10.182,10.182,0.1,0.1",
            "ranger,10.152,10.121,10.334,0.106075,0.096845",
            "cleric,9.75,10,10,0.1,0.1",
            "spellcaster,10.152,10.08,10.08,0.1,0.1",
            "shieldbearer,9.6315,9.7465,9.7465,0.099,0.099",
            "siegebreaker,9.6475,10,10,0.1,0.1",
            "arbalist,10.3035,10.101,10.101,0.099,0.099",
            "spellblade,10.303,10.05,10.05,0.101515,0.101515",
            "artillerist,9.7355,10.101,10.101,0.099,0.099",
            "demonologist,11.022,10.82,10.82,0.1,0.1",
            "bannerlord,9.8535,10.05,10.05,0.10051,0.10051",
            "champion,10.2535,10.182,10.182,0.1,0.1",
            "arcane_knight,9.9,9.6975,9.6975,0.1,0.1"
    })
    void basePassiveChangesRealDamageEventsAndRevokesMovementOnExit(
            String form, double outgoing, double incoming, double followingIncoming,
            double movementBeforeHit, double movementAfterHit) {
        activate(form);

        assertEquals(movementBeforeHit, player.getAttribute(Attribute.MOVEMENT_SPEED).getValue(), 0.000001);
        assertEquals(outgoing, outgoingDamage(), 0.000001);
        assertEquals(incoming, incomingDamage(), 0.000001);
        assertEquals(movementAfterHit, player.getAttribute(Attribute.MOVEMENT_SPEED).getValue(), 0.000001);
        assertEquals(followingIncoming, incomingDamage(), 0.000001);

        active = false;
        runtime.reconcile(player, false);
        assertEquals(10D, outgoingDamage());
        assertEquals(10D, incomingDamage());
        assertEquals(0.1D, player.getAttribute(Attribute.MOVEMENT_SPEED).getValue(), 0.000001);
    }

    @ParameterizedTest
    @CsvSource({
            "sniper,10.3035,11.0135,10.101,10.101",
            "bowmaster,9.6645,11.2265,10.101,10.101",
            "battlemage,10.3855,9.8885,9.392,10.031",
            "crusher,10.8115,10.1015,9.1075,9.8175",
            "elementalist,10,10,10.568,10",
            "demolitionist,10.081,10.081,10.791,10.081"
    })
    void positionalPassiveUsesTheLiveTargetDistance(
            String form, double nearOutgoing, double farOutgoing, double nearIncoming, double farIncoming) {
        activate(form);
        assertEquals(nearOutgoing, outgoingDamage(), 0.000001);
        assertEquals(nearIncoming, incomingDamage(), 0.000001);
        assertTrue(target.getLivingEntity().teleport(player.getLocation().add(0, 0, 18)));
        assertEquals(farOutgoing, outgoingDamage(), 0.000001);
        assertEquals(farIncoming, incomingDamage(), 0.000001);
    }

    @ParameterizedTest
    @CsvSource({
            "bulwark,9.88,9.88,8.8945,9.7465",
            "colossus,10.9535,9.5335,8.9655,10.1725",
            "windrunner,9.584,11.004,10.121,10.121",
            "grovekeeper,9.85,9.85,9.6548,10.3648"
    })
    void movementPassiveReadsPlayerMotion(String form, double standingOutgoing, double movingOutgoing,
                                          double standingIncoming, double movingIncoming) {
        activate(form);
        assertEquals(standingOutgoing, outgoingDamage(), 0.000001);
        assertEquals(standingIncoming, incomingDamage(), 0.000001);
        player.setVelocity(new Vector(.2, 0, 0));
        assertEquals(movingOutgoing, outgoingDamage(), 0.000001);
        assertEquals(movingIncoming, incomingDamage(), 0.000001);
    }

    @ParameterizedTest
    @CsvSource({
            "bloodrager,4,10.2535,11.6735,10.182,11.105",
            "reaver,9,10.2535,10.5375,10.182,10.324",
            "deathless,4,10.1015,10.3855,9.8175,8.6815"
    })
    void emergencyPassiveReadsActualPlayerHealth(String form, double health, double healthyOutgoing,
                                                double woundedOutgoing, double healthyIncoming, double woundedIncoming) {
        activate(form);
        assertEquals(healthyOutgoing, outgoingDamage(), 0.000001);
        assertEquals(healthyIncoming, incomingDamage(), 0.000001);
        player.setHealth(health);
        assertEquals(woundedOutgoing, outgoingDamage(), 0.000001);
        assertEquals(woundedIncoming, incomingDamage(), 0.000001);
    }

    @ParameterizedTest
    @CsvSource({"slayer,10.354,11.206", "headsman,10.354,11.632", "harvester,9.502,11.064"})
    void executionPassiveReadsActualTargetHealth(String form, double healthyDamage, double woundedDamage) {
        activate(form);
        assertEquals(healthyDamage, outgoingDamage(), 0.000001);
        target.getLivingEntity().setHealth(target.getLivingEntity().getHealth() * .4D);
        assertEquals(woundedDamage, outgoingDamage(), 0.000001);
    }

    @ParameterizedTest
    @CsvSource({
            "inquisitor,11.064,10.354", "bloodstorm,9.8275,10.2535",
            "raincaller,9.7355,11.0135", "warmonger,9.6855,11.1055", "titanbane,11.49,10.638"
    })
    void crowdPassiveFindsAnotherRegisteredElite(String form, double isolated, double grouped) {
        activate(form);
        assertEquals(isolated, outgoingDamage(), 0.000001);
        var neighbor = CombatTestEntities.spawnElite(target.getLivingEntity().getLocation().add(1, 0, 0));
        try {
            assertEquals(grouped, outgoingDamage(), 0.000001);
        } finally {
            neighbor.remove(RemovalReason.SHUTDOWN);
        }
        assertEquals(isolated, outgoingDamage(), 0.000001);
    }

    @Test
    void tempestArcherNeedsBothMotionAndACrowdAndTakesMoreDamageAfterAHit() {
        activate("tempest_archer");
        assertEquals(10.152D, outgoingDamage(), 0.000001);
        player.setVelocity(new Vector(.2, 0, 0));
        assertEquals(10.152D, outgoingDamage(), 0.000001, "Motion alone must not grant the crowd bonus");
        var neighbor = CombatTestEntities.spawnElite(target.getLivingEntity().getLocation().add(1, 0, 0));
        try {
            assertEquals(11.004D, outgoingDamage(), 0.000001);
            player.setVelocity(new Vector());
            assertEquals(10.152D, outgoingDamage(), 0.000001, "A crowd alone must not grant the motion bonus");
            assertEquals(10.121D, incomingDamage(), 0.000001);
            assertEquals(10.76D, incomingDamage(), 0.000001);
            runtime.discard(player);
            assertEquals(10.121D, incomingDamage(), 0.000001, "Discard must clear the recent-hit penalty");
        } finally {
            neighbor.remove(RemovalReason.SHUTDOWN);
        }
    }

    @Test
    void harvesterKillBonusRequiresItsOwnKillAndAnotherEliteAndClearsOnExit() {
        activate("harvester");
        var neighbor = CombatTestEntities.spawnElite(target.getLivingEntity().getLocation().add(1, 0, 0));
        var victim = CombatTestEntities.spawnElite(player.getLocation().add(0, 0, 30));
        try {
            assertEquals(10.07D, outgoingDamage(), 0.000001);
            var dead = (LivingEntityMock) victim.getLivingEntity();
            dead.setKiller(server.addPlayer());
            Bukkit.getPluginManager().callEvent(new EliteMobDeathEvent(victim));
            assertEquals(10.07D, outgoingDamage(), 0.000001, "Another player's kill must not grant the bonus");
            dead.setKiller(player);
            Bukkit.getPluginManager().callEvent(new EliteMobDeathEvent(victim));
            assertEquals(10.78D, outgoingDamage(), 0.000001);
            neighbor.remove(RemovalReason.SHUTDOWN);
            assertEquals(9.502D, outgoingDamage(), 0.000001, "A recent kill alone must not grant the crowd bonus");
            neighbor = CombatTestEntities.spawnElite(target.getLivingEntity().getLocation().add(1, 0, 0));
            active = false;
            runtime.reconcile(player, false);
            active = true;
            runtime.reconcile(player, true);
            assertEquals(10.07D, outgoingDamage(), 0.000001, "A new session must not inherit the kill bonus");
        } finally {
            neighbor.remove(RemovalReason.SHUTDOWN);
            victim.remove(RemovalReason.SHUTDOWN);
        }
    }

    @ParameterizedTest
    @CsvSource({"dragonslayer,11.5815,9.5935", "titanbane,11.49,9.928"})
    void bossPassiveDistinguishesAnOrdinaryNaturalElite(String form, double boss, double ordinary) {
        activate(form);
        assertEquals(boss, outgoingDamage(), 0.000001);
        target.setNaturalEntity(true);
        assertEquals(ordinary, outgoingDamage(), 0.000001);
    }

    @ParameterizedTest
    @CsvSource({"deadeye,10.3035,11.5815", "dreadnought,10.1015,9.3915"})
    void criticalPassiveUsesTheDamageEventFlag(String form, double normal, double critical) {
        activate(form);
        assertEquals(normal, outgoingDamage(), 0.000001);
        var event = new EliteMobDamagedByPlayerEvent(target, player, 10D, false, true);
        Bukkit.getPluginManager().callEvent(event);
        assertEquals(critical, event.getDamage(), 0.000001);
    }

    @ParameterizedTest
    @CsvSource({"justicar,9.148", "inquisitor,8.935", "reaver,9.29"})
    void healingTradeoffChangesRegainHealthEventsAndStopsWhenInactive(String form, double amount) {
        activate(form);
        var heal = new EntityRegainHealthEvent(player, 10D, EntityRegainHealthEvent.RegainReason.CUSTOM);
        Bukkit.getPluginManager().callEvent(heal);
        assertEquals(amount, heal.getAmount(), 0.000001);
        assertFalse(heal.isCancelled());

        active = false;
        runtime.reconcile(player, false);
        var ordinary = new EntityRegainHealthEvent(player, 10D, EntityRegainHealthEvent.RegainReason.CUSTOM);
        Bukkit.getPluginManager().callEvent(ordinary);
        assertEquals(10D, ordinary.getAmount());
    }

    @Test
    void lichWardBreakOpensDamageRiskAndLeavingCombatClearsIt() {
        activate("lich");
        assertEquals(10D, incomingDamage());
        runtime.signalWardBroken(player, 100);
        assertEquals(10.568D, incomingDamage(), 0.000001);

        active = false;
        runtime.reconcile(player, false);
        active = true;
        runtime.reconcile(player, true);
        assertEquals(10D, incomingDamage(), "A new combat session must not inherit the broken-ward risk");
    }

    @Test
    void saboteurRangedPenaltyDoesNotLeakIntoMelee() {
        activate("saboteur");
        assertEquals(10.081D, outgoingDamage(), 0.000001);
        var ranged = new EliteMobDamagedByPlayerEvent(target, player, 10D, true);
        Bukkit.getPluginManager().callEvent(ranged);
        assertEquals(9.584D, ranged.getDamage(), 0.000001);
        assertEquals(10.081D, outgoingDamage(), 0.000001);
    }

    @Test
    void skirmisherGainsDamageWhileMovingButLosesSpeedAfterAHit() {
        activate("skirmisher");
        assertEquals(10.152D, outgoingDamage(), 0.000001);
        assertEquals(.102525D, player.getAttribute(Attribute.MOVEMENT_SPEED).getValue(), 0.000001);
        player.setVelocity(new Vector(.2, 0, 0));
        assertEquals(10.72D, outgoingDamage(), 0.000001);
        assertEquals(10.121D, incomingDamage(), 0.000001);
        assertEquals(.096845D, player.getAttribute(Attribute.MOVEMENT_SPEED).getValue(), 0.000001);
        assertEquals(10.405D, incomingDamage(), 0.000001);

        runtime.discard(player);
        assertEquals(.1D, player.getAttribute(Attribute.MOVEMENT_SPEED).getValue(), 0.000001);
    }

    @ParameterizedTest
    @CsvSource({"juggernaut,86", "dreadnought,89"})
    void controlResistanceShortensAppliedEffectsWithoutChangingTheirOtherProperties(String form, int duration) {
        activate(form);
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 100, 2, true, false, false));
        assertEquals(new PotionEffect(PotionEffectType.SLOWNESS, duration, 2, true, false, false),
                player.getPotionEffect(PotionEffectType.SLOWNESS));
        player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 100, 0));
        assertEquals(100, player.getPotionEffect(PotionEffectType.NIGHT_VISION).getDuration());

        active = false;
        runtime.reconcile(player, false);
        player.removePotionEffect(PotionEffectType.SLOWNESS);
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 100, 2));
        assertEquals(100, player.getPotionEffect(PotionEffectType.SLOWNESS).getDuration());
    }

    @ParameterizedTest
    @CsvSource({
            "mage,spell,10,10.284", "pyromancer,spell,10,10.355",
            "cryomancer,spell,10,9.929", "summoner,spell,10,9.858",
            "spiritbinder,spell,10,9.6095", "elementalist,area,10,10.27335",
            "saboteur,trap,10.081,11.075", "demolitionist,blast,10.081,11.217"
    })
    void abilityPassiveAppliesOnlyInsideItsDamageScope(String form, String kind, double ordinary, double ability) {
        activate(form);
        var domain = switch (kind) {
            case "spell" -> ClassAbilityDamageDomain.SINGLE_TARGET_DIRECT;
            case "area" -> ClassAbilityDamageDomain.AREA_DIRECT;
            case "trap" -> ClassAbilityDamageDomain.AREA_TRAP;
            case "blast" -> ClassAbilityDamageDomain.AREA_BLAST;
            default -> throw new IllegalArgumentException(kind);
        };
        assertEquals(ordinary, outgoingDamage(), 0.000001);
        CombatDamageContext.runClassAbilityDamage(domain,
                () -> assertEquals(ability, outgoingDamage(), 0.000001));
        // Summons and ordinary hits must not inherit spell/trap/blast-only modifiers.
        CombatDamageContext.runClassAbilityDamage(ClassAbilityDamageDomain.SINGLE_TARGET_SUMMON,
                () -> assertEquals(ordinary, outgoingDamage(), 0.000001));
        for (var weapon : List.of(SkillType.WANDS, SkillType.STAVES)) {
            CombatDamageContext.runPlayerToEliteBypass(
                    new CombatDamageContext.PlayerDamageSource(UUID.randomUUID(), weapon),
                    () -> assertEquals(kind.equals("spell") ? ability : ordinary, outgoingDamage(), 0.000001));
        }
        if (!kind.equals("spell")) {
            CombatDamageContext.runClassAbilityDamage(ClassAbilityDamageDomain.SINGLE_TARGET_DIRECT,
                    () -> assertEquals(ordinary, outgoingDamage(), 0.000001));
        }
        assertEquals(ordinary, outgoingDamage(), 0.000001);
    }

    private void activate(String form) {
        var lineage = BuiltInClassContent.catalog().lineageOf(form);
        // Isolate this form's contribution; progression/inheritance has its own tests.
        var snapshot = new ActiveLineageSnapshot(form, 1, lineage.activeForm().band().effectiveStart(),
                List.of(form), Map.of(form, 1));
        var passive = PassiveAggregate.resolve(lineage, snapshot, BuiltInClassContent.passiveRegistry());
        runtime = new ClassPassiveRuntime(id -> passive, ignored -> active);
        Bukkit.getPluginManager().registerEvents(runtime, MetadataHandler.PLUGIN);
        runtime.reconcile(player, true);
    }

    private double outgoingDamage() {
        var event = new EliteMobDamagedByPlayerEvent(target, player, 10D, false);
        Bukkit.getPluginManager().callEvent(event);
        return event.getDamage();
    }

    @SuppressWarnings("removal")
    private double incomingDamage() {
        var hit = new EntityDamageByEntityEvent(target.getLivingEntity(), player,
                EntityDamageEvent.DamageCause.ENTITY_ATTACK, 10D);
        var event = new PlayerDamagedByEliteMobEvent(target, player, hit, null, 10D);
        Bukkit.getPluginManager().callEvent(event);
        return event.getDamage();
    }
}
