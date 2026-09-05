package com.magmaguy.elitemobs.experimentalcombat;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.api.PlayerDamagedByEliteMobEvent;
import com.magmaguy.elitemobs.combatsystem.combattag.DungeonCombatRuntime;
import com.magmaguy.elitemobs.experimentalcombat.classes.AbilitySlot;
import com.magmaguy.elitemobs.experimentalcombat.content.BuiltInClassContent;
import com.magmaguy.elitemobs.experimentalcombat.progression.*;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.presentation.actionbar.ActionBarCompositor;
import com.magmaguy.magmacore.instance.InstanceProtector;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffectType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class ExperimentalCombatBehaviorTest {
    private JavaPlugin previousPlugin;
    private PlayerMock player;
    private ExperimentalCombatModule module;

    @BeforeEach
    void openCombat() {
        var server = MockBukkit.mock();
        previousPlugin = MetadataHandler.PLUGIN;
        MetadataHandler.PLUGIN = MockBukkit.createMockPlugin();
        player = server.addPlayer();
        InstanceProtector.addProtectedWorld(player.getWorld());
        var levels = new EnumMap<SkillType, Integer>(SkillType.class);
        for (var skill : SkillType.values()) levels.put(skill, 100);
        var progression = new ClassProgressionModule(BuiltInClassContent.catalog(),
                id -> Optional.of(new FoundationLevelSnapshot(id, levels)),
                new MemoryStore(player.getUniqueId()), Runnable::run);
        progression.load(player.getUniqueId()).join();
        module = new ExperimentalCombatModule(new DungeonCombatRuntime(200, 20), availability -> progression);
        ClassAbilityEligibility.install(module::controlsAlwaysAvailable, module::mechanicsActive);
        module.onControlModeChanged(player);
    }

    @AfterEach
    void closeCombat() {
        try {
            if (module != null) module.close();
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

    @SuppressWarnings("removal")
    private double incomingDamage() {
        var attacker = player.getWorld().spawnEntity(player.getLocation(), EntityType.ZOMBIE);
        var hit = new EntityDamageByEntityEvent(attacker, player,
                EntityDamageEvent.DamageCause.ENTITY_ATTACK, 10D);
        // Protection is recipient-owned, including after the attacking elite loses its live body.
        var event = new PlayerDamagedByEliteMobEvent(new EliteEntity(), player, hit, null, 10D);
        Bukkit.getPluginManager().callEvent(event);
        attacker.remove();
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
