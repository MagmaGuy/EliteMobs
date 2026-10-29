package com.magmaguy.elitemobs.config.translations;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.CustomConfigFields;
import com.magmaguy.elitemobs.config.DefaultConfig;
import com.magmaguy.elitemobs.config.contentpackages.ContentPackagesConfigFields;
import com.magmaguy.elitemobs.config.customarenas.CustomArenasConfigFields;
import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import com.magmaguy.elitemobs.config.customevents.CustomEventsConfigFields;
import com.magmaguy.elitemobs.config.customitems.CustomItemsConfigFields;
import com.magmaguy.elitemobs.config.customquests.CustomQuestsConfigFields;
import com.magmaguy.elitemobs.config.npcs.NPCsConfigFields;
import com.magmaguy.elitemobs.config.wormholes.WormholeConfigFields;
import com.magmaguy.elitemobs.mobconstructor.mobdata.aggressivemobs.EliteMobProperties;
import com.magmaguy.magmacore.util.ChatColorConverter;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Content text moved from {@code <file>.<field>} keys to {@code text.<hash of the English>} keys. These cases seed a
 * language file the way an existing server has it, load content through the real config classes, and check what
 * players are shown and what the CSV holds afterwards.
 */
@EnabledIfEnvironmentVariable(named = "NIGHTBREAK_TEMP_TICKET_TESTS", matches = "true")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ContentTextKeysTemporaryTest {

    private static final Path WORKSPACE = Path.of("..").toAbsolutePath().normalize();
    private static final Map<String, Function<String, CustomConfigFields>> CONTENT = Map.of(
            "custombosses", name -> new CustomBossesConfigFields(name, true),
            "customitems", name -> new CustomItemsConfigFields(name, true),
            "npcs", name -> new NPCsConfigFields(name, true),
            "customquests", name -> new CustomQuestsConfigFields(name, true),
            "customarenas", name -> new CustomArenasConfigFields(name, true),
            "customevents", name -> new CustomEventsConfigFields(name, true),
            "wormholes", name -> new WormholeConfigFields(name, true),
            "content_packages", name -> new ContentPackagesConfigFields(name, true));

    private static final String BOSS = """
            isEnabled: true
            entityType: ZOMBIE
            name: '&cGoblin King'
            level: '10'
            deathMessages:
            - '&aThe king falls!'
            - '&a$players won.'
            spawnMessage: '&4The king rises!'
            escapeMessage: 'New text'
            onKillMessage: '&eYou killed the king, $player!'
            """;
    private static final String OTHER_BOSS = """
            isEnabled: true
            entityType: ZOMBIE
            name: '&cOther King'
            spawnMessage: '&4The king rises!'
            """;
    private static final String ITEM = """
            isEnabled: true
            material: DIAMOND_HELMET
            name: '&6Crown'
            lore:
            - '&7Heavy.'
            - '&7Golden.'
            """;
    private static final String NPC = """
            isEnabled: true
            name: 'Bob'
            greetings:
            - 'Hello!'
            """;

    private JavaPlugin previousPlugin;
    private String previousLanguage;
    private Path translations;

    private MockedStatic<EliteMobProperties> mobProperties;

    @BeforeEach
    void open() throws Exception {
        MockBukkit.mock();
        // Bosses stop loading before their text without the mob data that plugin startup builds. Stub-only, because a
        // recording mock keeps every call (with its stack trace) from thousands of bosses and exhausts the heap.
        mobProperties = Mockito.mockStatic(EliteMobProperties.class,
                Mockito.withSettings().stubOnly().defaultAnswer(Mockito.CALLS_REAL_METHODS));
        mobProperties.when(() -> EliteMobProperties.getPluginData(Mockito.any(EntityType.class)))
                .thenReturn(Mockito.mock(EliteMobProperties.class, Mockito.withSettings().stubOnly()));
        previousPlugin = MetadataHandler.PLUGIN;
        MetadataHandler.PLUGIN = MockBukkit.createMockPlugin("EliteMobs");
        translations = MetadataHandler.PLUGIN.getDataFolder().toPath().resolve("translations");
        Files.createDirectories(translations);
        previousLanguage = setLanguage("english");
    }

    @AfterEach
    void close() throws Exception {
        TranslationsConfig.shutdown();
        mobProperties.close();
        setLanguage(previousLanguage);
        MetadataHandler.PLUGIN = previousPlugin;
        MockBukkit.unmock();
    }

    @Test
    @Order(1)
    void fieldTranslationsMoveToTextKeys() throws Exception {
        Path csv = translations.resolve("french.csv");
        TranslationCsvParser.TranslationData seeded = new TranslationCsvParser.TranslationData(List.of("en", "fr"));
        row(seeded, "fr", "test_boss.name", "&cGoblin King", "&cRoi Gobelin");
        row(seeded, "fr", "test_boss.deathMessages", List.of("&aThe king falls!", "&a$players won."),
                List.of("&aLe roi tombe !", "&a$players a gagné."));
        row(seeded, "fr", "test_boss.spawnMessage", "&4The king rises!", "&4Le roi se lève !");
        row(seeded, "fr", "other_boss.spawnMessage", "&4The king rises!", "&4Le roi arrive !");
        row(seeded, "fr", "test_boss.escapeMessage", "Old text", "Vieux texte");
        row(seeded, "fr", "test_boss.onKillMessage", "&eYou killed the king, $player!", "&eTu as tué le roi, $player !");
        row(seeded, "fr", "test_item.name", "&6Crown", "&6Couronne");
        row(seeded, "fr", "test_item.lore", List.of("&7Heavy.", "&7Golden."), List.of("&7Lourde,", "&7dorée", "&7et brillante."));
        row(seeded, "fr", "test_npc.name", "Bob", "");
        row(seeded, "fr", "test_npc.greetings", List.of("Hello!"), List.of("Bonjour !"));
        row(seeded, "fr", "SomePluginConfig.someKey", "Plugin text", "Texte du plugin");
        TranslationCsvParser.write(seeded, csv);

        setLanguage("french");
        new TranslationsConfig();
        CustomBossesConfigFields boss = load(new CustomBossesConfigFields("test_boss.yml", true), BOSS);
        CustomBossesConfigFields other = load(new CustomBossesConfigFields("other_boss.yml", true), OTHER_BOSS);
        CustomItemsConfigFields item = load(new CustomItemsConfigFields("test_item.yml", true), ITEM);
        NPCsConfigFields npc = load(new NPCsConfigFields("test_npc.yml", true), NPC);

        assertEquals(ChatColorConverter.convert("&cRoi Gobelin"), boss.getName());
        assertEquals(ChatColorConverter.convert(List.of("&aLe roi tombe !", "&a$players a gagné.")), boss.getDeathMessages());
        assertEquals(ChatColorConverter.convert("&4Le roi se lève !"), boss.getSpawnMessage());
        assertEquals(ChatColorConverter.convert("&4Le roi se lève !"), other.getSpawnMessage(),
                "the same English shows one translation everywhere");
        assertEquals(ChatColorConverter.convert("&cOther King"), other.getName());
        assertEquals(ChatColorConverter.convert("&eTu as tué le roi, $player !"), boss.getOnKillMessage());
        assertEquals(ChatColorConverter.convert("New text"), boss.getEscapeMessage(),
                "a translation written for other English is not shown");
        assertEquals(ChatColorConverter.convert("&6Couronne"), item.getName());
        assertEquals(ChatColorConverter.convert(List.of("&7Lourde,", "&7dorée", "&7et brillante.")), item.getLore(),
                "a list translation keeps its own number of lines");
        assertEquals("Bob", npc.getName());
        assertEquals(ChatColorConverter.convert(List.of("Bonjour !")), npc.getGreetings());

        TranslationsConfig.shutdown();
        TranslationCsvParser.TranslationData result = TranslationCsvParser.parse(csv);
        for (String moved : List.of("test_boss.name", "test_boss.deathMessages", "test_boss.spawnMessage",
                "test_boss.onKillMessage", "test_item.name",
                "test_item.lore", "test_npc.name", "test_npc.greetings"))
            assertFalse(result.hasKey(moved), moved + " should have moved to its text key");
        assertEquals("&4Le roi arrive !", result.getString("other_boss.spawnMessage", "fr"),
                "a different translation of the same English is kept for comparison");
        assertEquals("Vieux texte", result.getString("test_boss.escapeMessage", "fr"), "a stale row is kept");
        assertEquals("Texte du plugin", result.getString("SomePluginConfig.someKey", "fr"), "plugin keys are untouched");

        assertEquals("&cRoi Gobelin", textTranslation(result, "fr", "&cGoblin King"));
        assertEquals("&4Le roi se lève !", textTranslation(result, "fr", "&4The king rises!"));
        assertEquals(List.of("&aLe roi tombe !", "&a$players a gagné."),
                textTranslation(result, "fr", List.of("&aThe king falls!", "&a$players won.")));
        assertEquals(List.of("&7Lourde,", "&7dorée", "&7et brillante."),
                textTranslation(result, "fr", List.of("&7Heavy.", "&7Golden.")));
        assertEquals("New text", textTranslation(result, "fr", "New text"), "new text starts as an English placeholder");
        assertEquals("Bob", textTranslation(result, "fr", "Bob"));
        assertEquals(List.of("Bonjour !"), textTranslation(result, "fr", List.of("Hello!")));

        byte[] afterFirstBoot = Files.readAllBytes(csv);
        new TranslationsConfig();
        assertEquals(ChatColorConverter.convert("&cRoi Gobelin"),
                load(new CustomBossesConfigFields("test_boss.yml", true), BOSS).getName());
        load(new CustomBossesConfigFields("other_boss.yml", true), OTHER_BOSS);
        load(new CustomItemsConfigFields("test_item.yml", true), ITEM);
        load(new NPCsConfigFields("test_npc.yml", true), NPC);
        TranslationsConfig.shutdown();
        assertArrayEquals(afterFirstBoot, Files.readAllBytes(csv), "a second boot changes nothing");
    }

    @Test
    @Order(2)
    void customLanguageKeepsEditedTextAndDropsPlaceholders() throws Exception {
        Path csv = translations.resolve("custom.csv");
        TranslationCsvParser.TranslationData seeded = new TranslationCsvParser.TranslationData(List.of("en", "custom"));
        row(seeded, "custom", "test_item.name", "&6Crown", "&6Kingly Crown");
        row(seeded, "custom", "test_item.lore", List.of("&7Heavy.", "&7Golden."), List.of("&7Heavy.", "&7Golden."));
        TranslationCsvParser.write(seeded, csv);

        setLanguage("custom");
        new TranslationsConfig();
        CustomItemsConfigFields item = load(new CustomItemsConfigFields("test_item.yml", true), ITEM);
        assertEquals(ChatColorConverter.convert("&6Kingly Crown"), item.getName());
        assertEquals(ChatColorConverter.convert(List.of("&7Heavy.", "&7Golden.")), item.getLore());

        TranslationsConfig.shutdown();
        TranslationCsvParser.TranslationData result = TranslationCsvParser.parse(csv);
        assertFalse(result.hasKey("test_item.name"));
        assertFalse(result.hasKey("test_item.lore"));
        assertEquals("&6Kingly Crown", textTranslation(result, "custom", "&6Crown"));
        assertEquals(List.of("&7Heavy.", "&7Golden."), textTranslation(result, "custom", List.of("&7Heavy.", "&7Golden.")));
    }

    /**
     * A real language file from before the move (the 26.2 testbed's russian.csv) against every content file in the
     * current package copy: no translation may be lost, only content rows may move, and a second boot changes nothing.
     */
    @Test
    @Order(3)
    void realLanguageFileMovesWithoutLosingTranslations() throws Exception {
        Path source = WORKSPACE.resolve("TestBeds/26.2/plugins/EliteMobs/translations/russian.csv");
        assertTrue(Files.isRegularFile(source), "fixture missing: " + source);
        Path csv = translations.resolve("russian.csv");
        Files.copy(source, csv);
        TranslationCsvParser.TranslationData original = TranslationCsvParser.parse(csv);
        List<ContentFile> files = contentFiles();

        setLanguage("russian");
        new TranslationsConfig();
        Map<String, Integer> loaded = new TreeMap<>();
        Map<String, Integer> failed = new TreeMap<>();
        Map<String, String> stems = new HashMap<>();
        loadAll(files, loaded, failed, stems);
        TranslationsConfig.shutdown();
        TranslationCsvParser.TranslationData result = TranslationCsvParser.parse(csv);

        List<String> lost = new ArrayList<>();
        List<String> foreign = new ArrayList<>();
        int removed = 0;
        int removedTranslated = 0;
        Map<String, Integer> movedByType = new TreeMap<>();
        for (String key : original.getKeys()) {
            if (result.hasKey(key)) continue;
            removed++;
            String type = stems.get(key.substring(0, Math.max(0, key.indexOf('.'))));
            if (type == null) foreign.add(key);
            Object english = original.get(key, "en");
            Object translation = original.get(key, "ru");
            if (blank(translation) || trimmed(translation).equals(trimmed(english))) continue;
            removedTranslated++;
            movedByType.merge(String.valueOf(type), 1, Integer::sum);
            Object kept = textTranslation(result, "ru", english);
            if (kept == null || !trimmed(kept).equals(trimmed(translation))) lost.add(key);
        }
        long textRows = result.getKeys().stream().filter(key -> key.startsWith("text.")).count();
        System.out.println("loaded=" + loaded + " failed=" + failed + " originalRows=" + original.getKeys().size()
                + " removed=" + removed + " removedTranslated=" + removedTranslated + " movedByType=" + movedByType + " textRows=" + textRows
                + " resultRows=" + result.getKeys().size());

        assertTrue(foreign.isEmpty(), "rows outside the loaded content files were removed: "
                + foreign.subList(0, Math.min(10, foreign.size())));
        assertTrue(lost.isEmpty(), lost.size() + " translations were lost: " + lost.subList(0, Math.min(10, lost.size())));
        assertTrue(removedTranslated > 500, "only " + removedTranslated + " translations moved");
        for (String type : List.of("custombosses", "customitems", "npcs", "customquests"))
            assertTrue(movedByType.getOrDefault(type, 0) > 0, "no " + type + " translation moved: " + movedByType);

        byte[] afterFirstBoot = Files.readAllBytes(csv);
        System.out.println("heap after first boot: " + usedMegabytes() + " MB");
        new TranslationsConfig();
        // Every 50th file; each file was checked in full on the first boot. The first boot's YAML trees stay reachable
        // in the test JVM (about 476 MB of its heap), so reloading more does not fit.
        List<ContentFile> sample = new ArrayList<>();
        for (int i = 0; i < files.size(); i += 50) sample.add(files.get(i));
        loadAll(sample, new TreeMap<>(), new TreeMap<>(), new HashMap<>());
        TranslationsConfig.shutdown();
        System.out.println("heap after second boot: " + usedMegabytes() + " MB");
        assertArrayEquals(afterFirstBoot, Files.readAllBytes(csv), "a second boot changes nothing");
    }

    private record ContentFile(Path path, String type) {
    }

    private static List<ContentFile> contentFiles() throws Exception {
        Path state = WORKSPACE.resolve("dlc/private-state");
        Matcher generation = Pattern.compile("\"generation\"\\s*:\\s*\"([^\"]+)\"")
                .matcher(Files.readString(state.resolve("current.json")));
        if (!generation.find()) throw new IllegalStateException("dlc/private-state/current.json names no generation");
        Path root = state.resolve("generations").resolve(generation.group(1));
        try (Stream<Path> walk = Files.walk(root)) {
            return walk.filter(path -> path.toString().endsWith(".yml")).sorted()
                    .map(path -> new ContentFile(path, type(root, path))).filter(file -> file.type() != null).toList();
        }
    }

    /** The content folder a package file belongs to: {@code <pack>/<type>/...} or {@code <pack>/elitemobs/<type>/...}. */
    private static String type(Path root, Path file) {
        String[] parts = root.relativize(file).toString().replace('\\', '/').split("/");
        int index = parts.length > 3 && parts[1].equals("elitemobs") ? 2 : 1;
        return parts.length > index + 1 && CONTENT.containsKey(parts[index]) ? parts[index] : null;
    }

    private static void loadAll(List<ContentFile> files, Map<String, Integer> loaded, Map<String, Integer> failed,
                                Map<String, String> stems) throws Exception {
        for (ContentFile file : files) {
            String name = file.path().getFileName().toString();
            // Text registered before a failure has still moved, so the file's rows count as content either way.
            stems.put(name.replace(".yml", ""), file.type());
            try {
                load(CONTENT.get(file.type()).apply(name), Files.readString(file.path()));
                loaded.merge(file.type(), 1, Integer::sum);
            } catch (Exception | LinkageError failure) {
                if (failed.merge(file.type(), 1, Integer::sum) == 1)
                    System.out.println("first " + file.type() + " failure (" + name + "): " + failure);
            }
        }
    }

    private static <T extends CustomConfigFields> T load(T fields, String yaml) throws Exception {
        YamlConfiguration configuration = new YamlConfiguration();
        configuration.loadFromString(yaml);
        // A scratch path, never the package file: the package copy is read-only.
        fields.setFile(MetadataHandler.PLUGIN.getDataFolder().toPath().resolve("content").resolve(fields.getFilename()).toFile());
        fields.setFileConfiguration(configuration);
        fields.processConfigFields();
        return fields;
    }

    private static void row(TranslationCsvParser.TranslationData data, String language, String key, Object english,
                            Object translation) {
        data.set(key, "en", english);
        data.set(key, language, translation);
    }

    /** The translation on the text row whose English is this, comparing lists without the CSV's blank padding. */
    private static Object textTranslation(TranslationCsvParser.TranslationData data, String language, Object english) {
        Object wanted = trimmed(english);
        for (String key : data.getKeys())
            if (key.startsWith("text.") && trimmed(data.get(key, "en")).equals(wanted))
                return trimmed(data.get(key, language));
        return null;
    }

    private static Object trimmed(Object value) {
        if (!(value instanceof List<?> list)) return value == null ? "" : value;
        List<Object> lines = new ArrayList<>(list);
        while (!lines.isEmpty() && String.valueOf(lines.get(lines.size() - 1)).isEmpty()) lines.remove(lines.size() - 1);
        return lines;
    }

    private static boolean blank(Object value) {
        Object trimmed = trimmed(value);
        return trimmed instanceof List<?> list ? list.stream().allMatch(line -> String.valueOf(line).isBlank())
                : String.valueOf(trimmed).isBlank();
    }

    private static long usedMegabytes() {
        System.gc();
        Runtime runtime = Runtime.getRuntime();
        return (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024);
    }

    private static String setLanguage(String language) throws Exception {
        Field field = DefaultConfig.class.getDeclaredField("language");
        field.setAccessible(true);
        String previous = (String) field.get(null);
        field.set(null, language);
        return previous;
    }
}
