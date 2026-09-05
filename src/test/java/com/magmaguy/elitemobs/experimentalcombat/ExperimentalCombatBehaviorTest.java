package com.magmaguy.elitemobs.experimentalcombat;

import com.magmaguy.elitemobs.MetadataHandler;
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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import org.mockbukkit.mockbukkit.plugin.PluginMock;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class ExperimentalCombatBehaviorTest {
    private JavaPlugin previousPlugin;
    private PlayerMock player;
    private ExperimentalCombatModule module;
    private EliteEntity target;
    private boolean fullCombatActive;

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
        module.registerGameplayListeners();
        ClassAbilityEligibility.install(module::controlsAlwaysAvailable, module::mechanicsActive);
        module.onControlModeChanged(player);
    }

    @AfterEach
    void closeCombat() {
        try {
            if (module != null) module.close();
            if (target != null) target.remove(RemovalReason.SHUTDOWN);
        } finally {
            DungeonCombatRuntime.shutdownIfInitialized();
            ActionBarCompositor.shutdown();
            if (player != null) InstanceProtector.removeProtectedWorld(player.getWorld());
            MockBukkit.unmock();
            MetadataHandler.PLUGIN = previousPlugin;
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

    @Test
    void prayerOfMendingHealsWithoutGrantingEnoughGraceToRepeat() {
        assertTrue(module.setClassLevelForAdministration(player, "priest", 31).applied());
        player.setHealth(8D);

        assertTrue(module.useAbility(player, AbilitySlot.SIGNATURE).successful());
        assertEquals(11.017, player.getHealth(), 0.000001);
        assertFalse(module.useAbility(player, AbilitySlot.SIGNATURE).successful());
        assertEquals(11.017, player.getHealth(), 0.000001,
                "Insufficient Grace must stop the second heal before applying it");
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
    void berserkerSignatureSpendsEarnedFuryForDamageAndSpeedUntilClassChange() {
        assertTrue(module.setClassLevelForAdministration(player, "berserker", 1).applied());
        assertFalse(module.useAbility(player, AbilitySlot.SIGNATURE).successful());
        incomingDamage();
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

    private EliteEntity target() {
        if (target == null) {
            target = CombatTestEntities.spawnElite(player.getLocation().add(0, 0, 3));
        }
        return target;
    }

    private double outgoingDamage() {
        var event = new EliteMobDamagedByPlayerEvent(target(), player, 10D, false);
        Bukkit.getPluginManager().callEvent(event);
        return event.getDamage();
    }

    @SuppressWarnings("removal")
    private double incomingDamage() {
        var attacker = target();
        var hit = new EntityDamageByEntityEvent(attacker.getLivingEntity(), player,
                EntityDamageEvent.DamageCause.ENTITY_ATTACK, 10D);
        var event = new PlayerDamagedByEliteMobEvent(attacker, player, hit, null, 10D);
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
