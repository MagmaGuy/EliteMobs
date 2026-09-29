package com.magmaguy.elitemobs.mobconstructor.custombosses;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfig;
import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import com.magmaguy.elitemobs.config.ClassLootSettingsConfig;
import com.magmaguy.elitemobs.config.powers.PowersConfig;
import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import com.magmaguy.elitemobs.instanced.dungeons.DifficultyResolver;
import com.magmaguy.elitemobs.instanced.dungeons.DungeonInstance;
import com.magmaguy.elitemobs.items.ClassLootCoverage;
import com.magmaguy.elitemobs.mobconstructor.PersistentObjectHandler;
import com.magmaguy.elitemobs.powers.meta.ElitePower;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockbukkit.mockbukkit.MockBukkit;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class ExternalInstancedBossApiTest {
    private Location location;
    private CustomBossesConfigFields config;

    @BeforeEach
    void open() {
        var server = MockBukkit.mock();
        MetadataHandler.PLUGIN = MockBukkit.createMockPlugin("EliteMobs");
        location = server.addSimpleWorld("external-instance").getSpawnLocation();
        config = new CustomBossesConfigFields("external_guard.yml", EntityType.ZOMBIE,
                true, "External guard", "dynamic");
    }

    @AfterEach
    void close() {
        PersistentObjectHandler.shutdown();
        MockBukkit.unmock();
    }

    @Test
    void externalConstructorRetainsExplicitLevelAndInstancePersistenceWithoutAMatchObject() {
        var boss = new InstancedBossEntity(config, location, 37);

        assertEquals(37, boss.getLevel());
        assertEquals(location, boss.getSpawnLocation());
        assertTrue(boss.getIsPersistent());
        assertNull(boss.getDungeonInstance());
        assertEquals("0", boss.getDifficultyID(), "Existing callers retain Normal difficulty");
        assertNull(boss.getLivingEntity(), "Construction must leave spawning under the caller's control");
        assertTrue(RegionalBossEntity.getRegionalBossEntities(config).isEmpty(),
                "External instances must not become permanent regional spawn definitions");

        boss.setCustomBossesConfigFields(config);
        assertEquals(37, boss.getLevel(), "A dynamic phase definition must preserve the external instance level");
    }

    @Test
    void namedFactoryReturnsAnInstancedBossWithTheRequestedLevel() {
        try (var bosses = mockStatic(CustomBossesConfig.class)) {
            bosses.when(() -> CustomBossesConfig.getCustomBoss(config.getFilename())).thenReturn(config);

            var boss = assertInstanceOf(InstancedBossEntity.class,
                    InstancedBossEntity.createInstancedBossEntity(config.getFilename(), location, 19));

            assertEquals(19, boss.getLevel());
            assertSame(config, boss.getCustomBossesConfigFields());
            assertEquals(location, boss.getSpawnLocation());
            assertTrue(boss.getIsPersistent());
            assertNull(boss.getLivingEntity());
            assertEquals("0", boss.getDifficultyID());
        }
    }

    @ParameterizedTest(name = "{displayName} [{index}] {arguments}")
    @ValueSource(strings = {"0", "1", "2"})
    void explicitDifficultySurvivesDeferredSpawningAndPhaseRefresh(String difficulty) {
        config.setPowers(List.of(
                Map.of("filename", "normal.yml", "difficultyID", 0),
                Map.of("filename", "hard.yml", "difficultyID", List.of("Hard", "Mythic")),
                Map.of("filename", "mythic.yml", "difficultyID", "2")));
        var authoredPowers = List.copyOf(config.getPowers());
        try (var powers = mockStatic(PowersConfig.class)) {
            powers.when(() -> PowersConfig.getPower("normal.yml")).thenReturn(power("normal.yml", NormalPower.class));
            powers.when(() -> PowersConfig.getPower("hard.yml")).thenReturn(power("hard.yml", HardPower.class));
            powers.when(() -> PowersConfig.getPower("mythic.yml")).thenReturn(power("mythic.yml", MythicPower.class));
            Location unloaded = new Location(location.getWorld(), 1600, 64, 1600);
            assertFalse(unloaded.getWorld().isChunkLoaded(100, 100));
            var boss = new InstancedBossEntity(config, unloaded, 37, difficulty);

            boss.spawn(true);

            assertNull(boss.getLivingEntity(), "Unloaded chunks defer materialization without losing instance context");
            assertEquals(difficulty, boss.getDifficultyID());
            assertEquals(difficulty, boss.getResolvedDifficultyID());
            Set<String> expected = switch (difficulty) {
                case "0" -> Set.of("normal.yml");
                case "1" -> Set.of("hard.yml");
                default -> Set.of("hard.yml", "mythic.yml");
            };
            assertEquals(expected, boss.getElitePowers().stream().map(ElitePower::getFileName).collect(Collectors.toSet()));
            boss.setCustomBossesConfigFields(config);
            assertEquals(expected, boss.getElitePowers().stream().map(ElitePower::getFileName).collect(Collectors.toSet()));
            assertEquals(37, boss.getLevel());
            assertEquals(authoredPowers, config.getPowers(), "The shared authored definition must not be filtered in place");
        }
    }

    @ParameterizedTest(name = "{displayName} [{index}] {arguments}")
    @ValueSource(strings = {"0", "1", "2"})
    void namedFactoryPassesItsDifficultyToClassLoot(String difficulty) {
        try (var bosses = mockStatic(CustomBossesConfig.class);
             var settings = mockStatic(ClassLootSettingsConfig.class)) {
            bosses.when(() -> CustomBossesConfig.getCustomBoss(config.getFilename())).thenReturn(config);
            var expected = ClassLootSettingsConfig.Difficulty.values()[Integer.parseInt(difficulty)];
            settings.when(() -> ClassLootSettingsConfig.forDifficultyId(difficulty, difficulty)).thenReturn(expected);
            var boss = assertInstanceOf(InstancedBossEntity.class,
                    InstancedBossEntity.createInstancedBossEntity(config.getFilename(), location, 19, difficulty));

            assertEquals(expected, ClassLootCoverage.difficulty(boss));
            settings.verify(() -> ClassLootSettingsConfig.forDifficultyId(difficulty, difficulty));
            assertEquals(19, boss.getLevel());
            assertNull(boss.getDungeonInstance());
        }
    }

    @Test
    void dungeonOwnedBossesRetainTheirPackageLocalDifficultyResolver() {
        var resolver = new DifficultyResolver("legacy_dungeon.yml", List.of(
                Map.of("id", "easy", "name", "Easy"), Map.of("id", "medium", "name", "Medium"),
                Map.of("id", "hard", "name", "Hard")));
        var dungeon = mock(DungeonInstance.class);
        when(dungeon.getDifficultyID()).thenReturn("hard");
        when(dungeon.getResolvedDifficultyID()).thenReturn("2");
        when(dungeon.getPlayers()).thenReturn(new java.util.HashSet<>());
        when(dungeon.matchesDifficulty(anyList(), anyString())).thenAnswer(call ->
                resolver.matches(call.getArgument(0), "hard", call.getArgument(1)));
        var boss = new InstancedBossEntity(config, location, dungeon);

        assertEquals("hard", boss.getDifficultyID());
        assertEquals("2", boss.getResolvedDifficultyID());
        assertTrue(boss.matchesDifficulty(List.of("Hard", "2"), "power.yml"));
        assertFalse(boss.matchesDifficulty(List.of("Medium"), "power.yml"));
    }

    @ParameterizedTest(name = "{displayName} [{index}] {arguments}")
    @ValueSource(strings = {"", "3", "mythic"})
    void externalContextRejectsNonCanonicalIds(String difficulty) {
        assertThrows(IllegalArgumentException.class, () -> new InstancedBossEntity(config, location, 10, difficulty));
    }

    private static PowersConfigFields power(String filename, Class<? extends ElitePower> type) {
        return new PowersConfigFields(filename, true, null, type, PowersConfigFields.PowerType.OFFENSIVE);
    }

    public static class NormalPower extends ElitePower {
        public NormalPower() { super(new PowersConfigFields("normal.yml", true)); }
    }
    public static class HardPower extends ElitePower {
        public HardPower() { super(new PowersConfigFields("hard.yml", true)); }
    }
    public static class MythicPower extends ElitePower {
        public MythicPower() { super(new PowersConfigFields("mythic.yml", true)); }
    }
}
