package com.magmaguy.elitemobs.experimentalcombat;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.MobCombatSettingsConfig;
import com.magmaguy.elitemobs.config.PartyConfig;
import com.magmaguy.elitemobs.parties.PartyManager;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
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
                new MemoryStore(player.getUniqueId()), Runnable::run);
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
            "pyromancer,true,false", "occultist,true,false"})
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
    @CsvSource({"necromancer,61,8.27125", "plaguebringer,91,7.7905"})
    void areaDebuffSlowsAndWeakensEnemiesWithoutAffectingBystanders(
            String form, int level, double weakenedDamage) {
        assertTrue(module.setClassLevelForAdministration(player, form, level).applied());
        var enemy = target().getLivingEntity();
        var distant = CombatTestEntities.spawnElite(player.getLocation().add(0, 0, 20));
        var bystander = MockBukkit.getMock().addPlayer();
        bystander.teleport(player.getLocation().add(1, 0, 0));
        try {
            assertTrue(module.useAbility(player, AbilitySlot.UTILITY).successful());
            assertNotNull(enemy.getPotionEffect(PotionEffectType.SLOWNESS));
            assertFalse(distant.getLivingEntity().hasPotionEffect(PotionEffectType.SLOWNESS));
            assertFalse(bystander.hasPotionEffect(PotionEffectType.SLOWNESS));
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
    @CsvSource({"spellcaster,1,true,1180", "guardian,31,false,1240"})
    void scheduledRecoveryFundsAShieldOnlyAfterEnoughUpdates(
            String form, int level, boolean initiallyAffordable, int ticksBeforeAffordable) {
        assertTrue(module.setClassLevelForAdministration(player, form, level).applied());
        assertEquals(initiallyAffordable, module.useAbility(player, AbilitySlot.UTILITY).successful());
        player.removePotionEffect(PotionEffectType.ABSORPTION);
        var scheduler = MockBukkit.getMock().getScheduler();
        scheduler.performTicks(ticksBeforeAffordable);
        assertFalse(module.useAbility(player, AbilitySlot.UTILITY).successful());
        assertFalse(player.hasPotionEffect(PotionEffectType.ABSORPTION));
        scheduler.performOneTick();
        assertTrue(module.useAbility(player, AbilitySlot.UTILITY).successful(),
                "The production update task must earn enough resource for the shield");
        assertNotNull(player.getPotionEffect(PotionEffectType.ABSORPTION));
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
    @CsvSource({"shepherd,false", "mistweaver,true"})
    void partyUtilitySpendsGraceAndProtectsOnlyNearbyMembersUntilClassChange(
            String form, boolean cleanses) throws Exception {
        assertTrue(module.setClassLevelForAdministration(player, form, 91).applied());
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

        assertTrue(module.useAbility(player, AbilitySlot.UTILITY).successful());
        for (var member : List.of(player, ally)) {
            assertEquals(1, member.getPotionEffect(PotionEffectType.SPEED).getAmplifier());
            assertEquals(!cleanses, member.hasPotionEffect(PotionEffectType.POISON));
            assertTrue(member.hasPotionEffect(PotionEffectType.NIGHT_VISION));
        }
        for (var excluded : List.of(distant, outsider)) {
            assertFalse(excluded.hasPotionEffect(PotionEffectType.SPEED));
            assertTrue(excluded.hasPotionEffect(PotionEffectType.POISON));
            assertEquals(10D, incomingDamage(excluded));
        }
        assertTrue(module.useAbility(player, AbilitySlot.UTILITY).successful());
        for (var member : List.of(player, ally)) member.removePotionEffect(PotionEffectType.SPEED);
        assertFalse(module.useAbility(player, AbilitySlot.UTILITY).successful(),
                "Two 35-Grace casts must leave too little Grace for a third");
        for (var member : List.of(player, ally)) {
            assertFalse(member.hasPotionEffect(PotionEffectType.SPEED));
            assertEquals(7.545D, incomingDamage(member), .000001);
        }
        assertTrue(module.selectForm(player, "spellcaster").accepted());
        assertEquals(10D, incomingDamage());
        assertEquals(10D, incomingDamage(ally), "Changing the caster's class must revoke the ally's protection");
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

    @Test
    void prayerOfMendingUsesInheritedPassivesWhenFullCombatIsActive() {
        fullCombatActive = true;
        assertTrue(module.setClassLevelForAdministration(player, "priest", 31).applied());
        player.setHealth(8D);
        assertTrue(module.useAbility(player, AbilitySlot.SIGNATURE).successful());
        // Rank-31 Priest: 14% heal, rank scale 1.0775, Cleric/Priest passives +10.6%.
        assertEquals(8D + 20D * .14D * 1.0775D * 1.106D, player.getHealth(), 0.000001);
        assertEquals(9.5D, outgoingDamage(), 0.000001,
                "The same active lineage must also reach the damage event listener");
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
    @CsvSource({"bloodrager,true,false", "artillerist,false,true", "windrunner,false,false"})
    void resourceBurstFundsFurtherCastsAndAppliesTheSkillEffects(
            String form, boolean fury, boolean strength) {
        int level = module.catalog().require(form).band().effectiveStart();
        assertTrue(module.setClassLevelForAdministration(player, form, level).applied());
        if (fury) {
            assertFalse(module.useAbility(player, AbilitySlot.UTILITY).successful());
            incomingDamage();
            incomingDamage(); // Earn 40 Fury through the production damage handler.
        }
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

    @SuppressWarnings("removal")
    private double incomingDamage(PlayerMock victim) {
        var attacker = target();
        var hit = new EntityDamageByEntityEvent(attacker.getLivingEntity(), victim,
                EntityDamageEvent.DamageCause.ENTITY_ATTACK, 10D);
        var event = new PlayerDamagedByEliteMobEvent(attacker, victim, hit, null, 10D);
        Bukkit.getPluginManager().callEvent(event);
        return event.getDamage();
    }

    private static final class MemoryStore implements ClassProgressionStore {
        private StoredClassProfile profile;
        private final Map<String, StoredClassProgress> progress = new HashMap<>();

        MemoryStore(UUID playerId) {
            profile = new StoredClassProfile(playerId, "spellcaster", InputProfile.DEFAULT.storedId(),
                    StoredClassProfile.DEFAULT_FOCUS_SLOT, BuiltInClassContent.PERSISTENCE_VERSION);
        }

        public StoredClassProfile loadOrCreateProfile(UUID id, int version) { return profile; }
        public List<StoredClassProgress> loadAllProgress(UUID id) { return List.copyOf(progress.values()); }
        public StoredClassProgress loadProgressOrZero(UUID id, String form, int version) {
            return progress.getOrDefault(form, StoredClassProgress.zero(id, form, version));
        }
        public void saveProfile(StoredClassProfile value) { profile = value; }
        public void saveProgress(StoredClassProgress value) { progress.put(value.formId(), value); }
        public void savePlayerAggregate(StoredClassProfile value, Collection<StoredClassProgress> rows) {
            saveProfile(value);
            rows.forEach(this::saveProgress);
        }
    }
}
