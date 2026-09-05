package com.magmaguy.elitemobs.experimentalcombat;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.api.PlayerDamagedByEliteMobEvent;
import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.api.internal.RemovalReason;
import com.magmaguy.elitemobs.combatsystem.combattag.DungeonCombatRuntime;
import com.magmaguy.elitemobs.experimentalcombat.classes.AbilitySlot;
import com.magmaguy.elitemobs.experimentalcombat.content.BuiltInClassContent;
import com.magmaguy.elitemobs.experimentalcombat.progression.*;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.presentation.actionbar.ActionBarCompositor;
import com.magmaguy.magmacore.instance.InstanceProtector;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Bukkit;
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
