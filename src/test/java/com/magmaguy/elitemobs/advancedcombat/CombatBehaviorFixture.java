package com.magmaguy.elitemobs.advancedcombat;

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
import com.magmaguy.elitemobs.advancedcombat.content.BuiltInClassContent;
import com.magmaguy.elitemobs.advancedcombat.classes.AbilitySlot;
import com.magmaguy.elitemobs.advancedcombat.progression.*;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.presentation.actionbar.ActionBarCompositor;
import com.magmaguy.elitemobs.dungeons.EliteMobsWorld;
import com.magmaguy.elitemobs.config.contentpackages.ContentPackagesConfigFields;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Bukkit;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import org.mockbukkit.mockbukkit.plugin.PluginMock;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

abstract class CombatBehaviorFixture {
    @TempDir java.nio.file.Path configurationDirectory;
    private JavaPlugin previousPlugin;
    PlayerMock player;
    AdvancedCombatModule module;
    private EliteEntity target;
    boolean fullCombatActive;
    private boolean partyInitialized;

    @BeforeEach
    final void openCombat() {
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
        EliteMobsWorld.create(player.getWorld().getUID(), new ContentPackagesConfigFields("combat-test.yml", true));
        fullCombatActive = false;
        var levels = new EnumMap<SkillType, Integer>(SkillType.class);
        for (var skill : SkillType.values()) levels.put(skill, 100);
        var progression = new ClassProgressionModule(BuiltInClassContent.catalog(),
                id -> Optional.of(new FoundationLevelSnapshot(id, levels)),
                new MemoryStore(), Runnable::run);
        progression.load(player.getUniqueId()).join();
        module = new AdvancedCombatModule(new DungeonCombatRuntime(200, 20), availability -> progression,
                ignored -> fullCombatActive, org.bukkit.entity.LivingEntity::damage);
        module.startGameplay();
        ClassAbilityEligibility.install(module::controlsAlwaysAvailable, module::mechanicsActive);
        module.onControlModeChanged(player);
    }

    @AfterEach
    final void closeCombat() {
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
                if (player != null) EliteMobsWorld.destroy(player.getWorld().getUID());
                MockBukkit.unmock();
                MetadataHandler.PLUGIN = previousPlugin;
            }
        }
    }

    final void openParty(PlayerMock... members) throws Exception {
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
        player.addAttachment(MetadataHandler.PLUGIN, "elitemobs.party", true);
        assertEquals(com.magmaguy.elitemobs.parties.PartyOperationResult.SUCCESS, PartyManager.create(player, false));
        assertNotNull(PartyManager.getParty(player.getUniqueId()));
        for (var member : members) {
            assertTrue(PlayerData.isDataLoaded(member.getUniqueId()));
            member.addAttachment(MetadataHandler.PLUGIN, "elitemobs.party", true);
            PartyManager.invite(player, member.getName());
            PartyManager.accept(member);
            assertSame(PartyManager.getParty(player.getUniqueId()), PartyManager.getParty(member.getUniqueId()));
        }
    }

    final EliteEntity target() {
        if (target == null) {
            target = CombatTestEntities.spawnElite(player.getLocation().add(0, 0, 3));
        }
        return target;
    }

    final double outgoingDamage() {
        var event = outgoingEvent();
        Bukkit.getPluginManager().callEvent(event);
        return event.getDamage();
    }

    final void recoverAndCast(AbilitySlot slot) {
        var scheduler = MockBukkit.getMock().getScheduler();
        for (int update = 0; update < 120; update++) {
            scheduler.performTicks(20);
            if (module.useAbility(player, slot).successful()) return;
        }
        fail("Scheduled recovery did not fund the production cast within two simulated minutes");
    }

    final EliteMobDamagedByPlayerEvent outgoingEvent() {
        return outgoingEvent(player, target());
    }

    @SuppressWarnings("removal")
    final EliteMobDamagedByPlayerEvent outgoingEvent(PlayerMock attacker, EliteEntity victim) {
        var hit = new EntityDamageByEntityEvent(attacker, victim.getLivingEntity(),
                EntityDamageEvent.DamageCause.ENTITY_ATTACK, 10D);
        return new EliteMobDamagedByPlayerEvent(victim, attacker, hit, 10D, false, false, 1D);
    }

    final double incomingDamage() {
        return incomingDamage(player);
    }

    final double incomingDamage(PlayerMock victim) {
        return incomingDamage(victim, target());
    }

    @SuppressWarnings("removal")
    final double incomingDamage(PlayerMock victim, EliteEntity attacker) {
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
            return profiles.computeIfAbsent(id, ignored -> {
                // Skill behavior starts after root trials; unlocking classes is
                // a separate feature. New party members need the same baseline.
                for (var root : BuiltInClassContent.catalog().roots()) {
                    long xp = root.band() == com.magmaguy.elitemobs.advancedcombat.classes.ClassBand.STARTER
                            ? com.magmaguy.elitemobs.skills.SkillXPCalculator.totalXPForLevel(root.band().effectiveEnd()) : 0;
                    rows(id).put(root.id(), new StoredClassProgress(id, root.id(), xp, version, true));
                }
                return new StoredClassProfile(id, "spellcaster", InputProfile.DEFAULT.storedId(),
                        version);
            });
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
