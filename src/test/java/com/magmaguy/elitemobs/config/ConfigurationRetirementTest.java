package com.magmaguy.elitemobs.config;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.enchantments.EnchantmentsConfigFields;
import com.magmaguy.elitemobs.config.customitems.CustomItemsConfig;
import com.magmaguy.elitemobs.config.customquests.CustomQuestsConfig;
import com.magmaguy.elitemobs.items.ItemConsumables;
import com.magmaguy.magmacore.MagmaCore;
import com.magmaguy.magmacore.config.OutdatedConfigurationArchive;
import com.magmaguy.magmacore.enchantments.EnchantmentCatalog;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ConfigurationRetirementTest {
    private ResourcePlugin plugin;

    public static class ResourcePlugin extends JavaPlugin {
        @Override public InputStream getResource(String name) {
            return ConfigurationRetirementTest.class.getClassLoader().getResourceAsStream(name);
        }
    }

    @BeforeEach void open() throws Exception {
        resetMagmaCoreInstance();
        MockBukkit.mock();
        plugin = MockBukkit.loadSimple(ResourcePlugin.class);
        MetadataHandler.PLUGIN = plugin;
        OutdatedConfigurationArchive.register(plugin);
    }
    @AfterEach void close() throws Exception {
        OutdatedConfigurationArchive.unregister(plugin);
        MockBukkit.unmock();
        resetMagmaCoreInstance();
    }

    private static void resetMagmaCoreInstance() throws Exception {
        // Each MockBukkit server owns a different plugin data directory.
        var instance = MagmaCore.class.getDeclaredField("instance");
        instance.setAccessible(true);
        instance.set(null, null);
    }

    @Test void retiredSharedDefaultIsArchivedBeforeItsRealBundledReplacementIsGenerated() throws Exception {
        Path directory = plugin.getDataFolder().toPath().resolve("enchantments");
        Files.createDirectories(directory);
        Path old = directory.resolve("loud_strikes.yml");
        String contents = "# customized\r\nisEnabled: true\r\nname: Custom old name\r\nmaxLevelV2: 99\r\n";
        Files.writeString(old, contents);
        OutdatedConfigurationArchive.archiveFor(plugin);
        assertFalse(Files.exists(old));
        EnchantmentCatalog.initializeDefaults(plugin, directory, List.of("loud_strikes"));
        var replacement = YamlConfiguration.loadConfiguration(old.toFile());
        assertEquals("loud_strikes.lua", replacement.getString("script"));
        assertTrue(Files.isRegularFile(directory.resolve("loud_strikes.lua")));
        assertFalse(replacement.contains("maxLevelV2"));
        Path archive = plugin.getDataFolder().toPath().getParent().resolve("MagmaCore/outdated files");
        try (var files = Files.walk(archive)) {
            var originals = files.filter(Files::isRegularFile).toList();
            assertEquals(1, originals.size());
            assertEquals(contents, Files.readString(originals.getFirst()));
        }
        OutdatedConfigurationArchive.archiveFor(plugin);
        assertTrue(Files.exists(old));
    }

    @Test void welcomeQuestWithScottyIsArchivedAndRegeneratedWithoutHim() throws Exception {
        Path directory = plugin.getDataFolder().toPath().resolve("customquests");
        Files.createDirectories(directory);
        Path quest = directory.resolve("ag_welcome_quest_1.yml");
        String contents = "# customized welcome quest\r\nname: My welcome quest\r\ncustomObjectives:\r\n"
                + "  Objective13:\r\n    objectiveType: DIALOG\r\n    filename: scroll_applier_config.yml\r\n    npcName: Scotty\r\n";
        Files.writeString(quest, contents);
        OutdatedConfigurationArchive.archiveFor(plugin);
        assertFalse(Files.exists(quest));
        MagmaCore.createInstance(plugin);
        new CustomQuestsConfig();
        var fields = CustomQuestsConfig.getCustomQuests().get("ag_welcome_quest_1.yml");
        assertNotNull(fields);
        assertTrue(fields.isEnabled());
        var replacement = YamlConfiguration.loadConfiguration(quest.toFile());
        var objectives = replacement.getConfigurationSection("customObjectives");
        assertNotNull(objectives);
        assertEquals(15, objectives.getKeys(false).size());
        assertFalse(objectives.contains("Objective13"));
        for (String objective : objectives.getKeys(false))
            assertNotEquals("scroll_applier_config.yml", objectives.getString(objective + ".filename"));
        assertEquals("wood_league_arena_master.yml", objectives.getString("Objective14.filename"));
        assertEquals("story_dungeons_quest_giver.yml", objectives.getString("Objective15.filename"));
        assertEquals("training_dummy_lv1.yml", objectives.getString("Objective16.filename"));
        String regenerated = Files.readString(quest);
        OutdatedConfigurationArchive.archiveFor(plugin);
        assertEquals(regenerated, Files.readString(quest));
        Path archive = plugin.getDataFolder().toPath().getParent().resolve("MagmaCore/outdated files");
        try (var files = Files.walk(archive)) {
            var originals = files.filter(Files::isRegularFile).toList();
            assertEquals(1, originals.size());
            assertEquals(contents, Files.readString(originals.getFirst()));
        }
    }

    @Test void releasedGrapplingScriptIsReplacedAndCustomizedOnesStay() throws Exception {
        Path directory = plugin.getDataFolder().toPath().resolve("enchantments");
        Files.createDirectories(directory);
        Path script = directory.resolve("grappling_hook.lua");
        byte[] released;
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("retired/grappling_hook_10.9.7.lua")) {
            released = input.readAllBytes();
        }
        byte[] bundled;
        try (InputStream input = plugin.getResource("enchantments/grappling_hook.lua")) {
            bundled = input.readAllBytes();
        }
        Files.write(script, released);
        OutdatedConfigurationArchive.archiveFor(plugin);
        assertFalse(Files.exists(script));
        EnchantmentCatalog.initializeDefaults(plugin, directory, List.of("grappling_hook"));
        assertArrayEquals(bundled, Files.readAllBytes(script));
        // The regenerated default never matches, so later boots keep it.
        OutdatedConfigurationArchive.archiveFor(plugin);
        assertArrayEquals(bundled, Files.readAllBytes(script));
        Path archive = plugin.getDataFolder().toPath().getParent().resolve("MagmaCore/outdated files");
        try (var files = Files.walk(archive)) {
            var originals = files.filter(Files::isRegularFile).toList();
            assertEquals(1, originals.size());
            assertArrayEquals(released, Files.readAllBytes(originals.getFirst()));
        }
        byte[] customized = ("-- tuned pull speed\n" + new String(released, java.nio.charset.StandardCharsets.UTF_8))
                .getBytes(java.nio.charset.StandardCharsets.UTF_8);
        Files.write(script, customized);
        OutdatedConfigurationArchive.archiveFor(plugin);
        assertArrayEquals(customized, Files.readAllBytes(script));
    }

    @Test void retirementRulesNeverListACurrentBundledDefault() throws Exception {
        // A listed current default would be archived and regenerated on every boot.
        java.util.Map<String, java.util.Map<String, java.util.Set<String>>> retired = new java.util.HashMap<>();
        try (InputStream input = plugin.getResource(OutdatedConfigurationArchive.RULES_RESOURCE)) {
            java.util.Map<?, ?> rules = new org.yaml.snakeyaml.Yaml().load(input);
            for (var category : rules.entrySet())
                for (Object rule : (List<?>) category.getValue())
                    if (rule instanceof java.util.Map<?, ?> fields && fields.containsKey("sha256"))
                        for (Object file : (List<?>) fields.get("files"))
                            retired.computeIfAbsent((String) category.getKey(), ignored -> new java.util.HashMap<>())
                                    .computeIfAbsent((String) file, ignored -> new java.util.HashSet<>())
                                    .addAll(((List<?>) fields.get("sha256")).stream().map(String::valueOf).toList());
        }
        int powers = 0;
        for (Class<? extends com.magmaguy.elitemobs.config.luapowers.LuaPowersConfigFields> type :
                new com.magmaguy.shaded.reflections.Reflections("com.magmaguy.elitemobs.config.luapowers.premade")
                        .getSubTypesOf(com.magmaguy.elitemobs.config.luapowers.LuaPowersConfigFields.class)) {
            if (java.lang.reflect.Modifier.isAbstract(type.getModifiers())) continue;
            var premade = type.getDeclaredConstructor().newInstance();
            assertFalse(retired.getOrDefault("powers", java.util.Map.of()).getOrDefault(premade.getFilename(), java.util.Set.of())
                    .contains(sourceHash(premade.getSource().getBytes(java.nio.charset.StandardCharsets.UTF_8))), premade.getFilename());
            powers++;
        }
        assertTrue(powers > 20, "premade Lua powers were found: " + powers);
        Path enchantments = Path.of(getClass().getClassLoader().getResource("enchantments/grappling_hook.lua").toURI()).getParent();
        try (var scripts = Files.list(enchantments)) {
            for (Path script : scripts.filter(path -> path.toString().endsWith(".lua")).toList())
                assertFalse(retired.getOrDefault("enchantments", java.util.Map.of())
                        .getOrDefault(script.getFileName().toString(), java.util.Set.of())
                        .contains(sourceHash(Files.readAllBytes(script))), script.getFileName().toString());
        }
    }

    private static String sourceHash(byte[] source) throws Exception {
        byte[] normalized = new String(source, java.nio.charset.StandardCharsets.UTF_8).replace("\r", "")
                .getBytes(java.nio.charset.StandardCharsets.UTF_8);
        return java.util.HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256").digest(normalized));
    }

    @Test void nativeDefaultsNoLongerRegenerateTheRetiredKey() {
        var fields = new EnchantmentsConfigFields("sharpness.yml", true, "Sharpness", 5, 1, true, 10);
        var yaml = new YamlConfiguration();
        fields.setFileConfiguration(yaml);
        fields.processConfigFields();
        assertEquals(5, yaml.getInt("maxLevel"));
        assertFalse(yaml.contains("maxLevelV2"));
    }

    @Test void archivedScrapDefaultsRegenerateWithoutRetiredEnchantments() throws Exception {
        Path directory = plugin.getDataFolder().toPath().resolve("customitems");
        Files.createDirectories(directory);
        List<String> sizes = List.of("tiny", "small", "medium", "large", "huge");
        for (int index = 0; index < sizes.size(); index++) {
            Files.writeString(directory.resolve("elite_scrap_" + sizes.get(index) + ".yml"),
                    "# customized old scrap\nisEnabled: true\nname: My scrap\nenchantments: ['repair," + (index + 1) + "']\n");
        }
        Path current = directory.resolve("my_item.yml");
        String currentYaml = "isEnabled: true\nmaterial: DIAMOND_SWORD\nname: My weapon\nlore: []\nenchantments: ['elitemobs:hunter,3']\n";
        Files.writeString(current, currentYaml);
        OutdatedConfigurationArchive.archiveFor(plugin);
        for (String size : sizes) assertFalse(Files.exists(directory.resolve("elite_scrap_" + size + ".yml")));
        assertEquals(currentYaml, Files.readString(current));
        MagmaCore.createInstance(plugin);
        new CustomItemsConfig();
        for (int index = 0; index < sizes.size(); index++) {
            String name = "elite_scrap_" + sizes.get(index) + ".yml";
            var fields = CustomItemsConfig.getCustomItems().get(name);
            assertTrue(fields.isEnabled());
            assertEquals(new ItemConsumables.Definition(ItemConsumables.Type.REPAIR_SCRAP, index + 1), fields.getConsumable());
            assertTrue(fields.getEnchantments().isEmpty());
            var yaml = YamlConfiguration.loadConfiguration(directory.resolve(name).toFile());
            assertEquals("repair_scrap", yaml.getString("consumable.type"));
            assertEquals(index + 1, yaml.getInt("consumable.tier"));
        }
        OutdatedConfigurationArchive.archiveFor(plugin);
        for (String size : sizes) assertTrue(Files.exists(directory.resolve("elite_scrap_" + size + ".yml")));
        Path archive = plugin.getDataFolder().toPath().getParent().resolve("MagmaCore/outdated files");
        try (var files = Files.walk(archive)) {
            var originals = files.filter(Files::isRegularFile).toList();
            assertEquals(5, originals.size());
            for (Path original : originals) assertTrue(Files.readString(original).startsWith("# customized old scrap\n"));
        }
    }
}
