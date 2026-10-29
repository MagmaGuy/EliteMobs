package com.magmaguy.elitemobs.powers.lua;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.DefaultConfig;
import com.magmaguy.elitemobs.config.powers.LuaPowerConfigFields;
import com.magmaguy.elitemobs.config.powers.PowersConfigFields.PowerType;
import com.magmaguy.elitemobs.config.translations.TranslationCsvParser;
import com.magmaguy.elitemobs.config.translations.TranslationsConfig;
import com.magmaguy.elitemobs.powers.scripts.caching.ScriptActionBlueprint;
import com.magmaguy.magmacore.util.ChatColorConverter;
import com.magmaguy.shaded.luaj.vm2.LuaTable;
import com.magmaguy.shaded.luaj.vm2.Varargs;
import com.magmaguy.shaded.luaj.vm2.lib.VarArgFunction;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockbukkit.mockbukkit.MockBukkit;

import java.io.File;
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
 * Script text in content packages is translated by its English through translations/&lt;language&gt;.csv. Each case
 * boots French so the lines register, fills the fr column of the generated CSV as a translator would, boots again as
 * /em reload does, and checks the text a player receives.
 */
@EnabledIfEnvironmentVariable(named = "NIGHTBREAK_TEMP_TICKET_TESTS", matches = "true")
class ScriptTextTranslationTemporaryTest {

    private static final Path WORKSPACE = Path.of("..").toAbsolutePath().normalize();
    private static final String BOILER_LINE = "&6[Living Boiler] &fLet my steam boil your skin!";
    private static final String GRADIENT_TITLE = "<gradient:#FCA5A5:#DC2626>Phase 2</gradient>";
    // A text call whose message starts with a string literal; such a file must register at least one row.
    private static final Pattern LITERAL_TEXT_CALL =
            Pattern.compile(":(send_message|show_action_bar|show_boss_bar|show_title)\\s*\\(\\s*['\"\\[]");

    private JavaPlugin previousPlugin;
    private String previousLanguage;
    private Path translations;

    @BeforeEach
    void open() throws Exception {
        MockBukkit.mock();
        previousPlugin = MetadataHandler.PLUGIN;
        MetadataHandler.PLUGIN = MockBukkit.createMockPlugin("EliteMobs");
        translations = MetadataHandler.PLUGIN.getDataFolder().toPath().resolve("translations");
        previousLanguage = setLanguage("english");
        new TranslationsConfig();
    }

    @AfterEach
    void close() throws Exception {
        TranslationsConfig.shutdown();
        LuaPowerManager.shutdown();
        setLanguage(previousLanguage);
        MetadataHandler.PLUGIN = previousPlugin;
        MockBukkit.unmock();
    }

    @Test
    void luaTextReachesThePlayerTranslated(@TempDir Path folder) throws Exception {
        File power = writePower(folder);
        Map<String, String> french = Map.of(
                "&8[&2Alchemist Goblin&8] ", "&8[&2Gobelin alchimiste&8] ",
                "&fMe had that fart in for THREE WEEKS!", "&fMoi garder ça TROIS SEMAINES !",
                "It's \"boiling\"", "C'est \"bouillant\" \\ vraiment",
                "Hot", "Chaud",
                "Steam rises", "La vapeur monte",
                "The boiler wakes", "La chaudière\ns'éveille",
                "Long\nline", "Longue\nligne",
                "Wave ", "Vague ");
        translateTo(() -> load(power), french::get);

        assertEquals(List.of(
                "&8[&2Gobelin alchimiste&8] &fMoi garder ça TROIS SEMAINES !",
                "C'est \"bouillant\" \\ vraiment",
                "Chaud",
                "La vapeur monte", "La chaudière\ns'éveille",
                "Longue\nligne",
                "Untranslated line",
                "Vague 3",
                "BLOCK_NOTE_BLOCK_HARP"), shown(load(power)));
        List<String> rows = scriptTextEnglish();
        assertTrue(rows.containsAll(french.keySet()), "every display literal registered: " + rows);
        assertTrue(rows.contains("Untranslated line"));
        assertFalse(rows.contains("BLOCK_NOTE_BLOCK_HARP"), "a sound name is not text");
        assertEquals(french.size() + 1, rows.size(), "one row per distinct line: " + rows);
    }

    @Test
    void englishServerLeavesScriptsAndFilesAlone(@TempDir Path folder) throws Exception {
        File power = writePower(folder);
        assertEquals(List.of(
                "&8[&2Alchemist Goblin&8] &fMe had that fart in for THREE WEEKS!",
                "It's \"boiling\"",
                "Hot",
                "Steam rises", "The boiler wakes",
                "Long\nline",
                "Untranslated line",
                "Wave 3",
                "BLOCK_NOTE_BLOCK_HARP"), shown(load(power)));
        assertEquals(ChatColorConverter.convert(BOILER_LINE), airship().get(0).getSValue());
        TranslationsConfig.shutdown();
        assertFalse(Files.exists(translations), "an English server writes no translation files");
    }

    @Test
    void eliteScriptTextActionsAreTranslatedOncePerLine() throws Exception {
        Map<String, String> french = Map.of(
                BOILER_LINE, "&6[Chaudière vivante] &fQue ma vapeur te brûle la peau !",
                GRADIENT_TITLE, "<gradient:#FCA5A5:#DC2626>Phase deux</gradient>",
                "&eThe steam rises!", "&eLa vapeur monte !",
                "$player is boiling", "$player bout",
                "Boiler pressure", "Pression de $boss");
        translateTo(this::airship, french::get);

        List<ScriptActionBlueprint> actions = airship();
        String boiler = ChatColorConverter.convert("&6[Chaudière vivante] &fQue ma vapeur te brûle la peau !");
        assertEquals(boiler, actions.get(0).getSValue());
        assertEquals(boiler, actions.get(1).getSValue());
        assertEquals(ChatColorConverter.convert("<gradient:#FCA5A5:#DC2626>Phase deux</gradient>"), actions.get(2).getTitle());
        assertEquals(ChatColorConverter.convert("&eLa vapeur monte !"), actions.get(2).getSubtitle());
        assertEquals("$player bout", actions.get(3).getSValue());
        assertEquals("Boiler pressure", actions.get(4).getSValue(), "a translation naming an unknown placeholder is refused");
        assertEquals("minecraft:block.smoker.smoke", actions.get(5).getSValue());

        List<String> rows = scriptTextEnglish();
        assertTrue(rows.contains(GRADIENT_TITLE), "translators see the line as authored: " + rows);
        assertEquals(1, Collections.frequency(rows, BOILER_LINE), "the same line in two files is one row");
        assertFalse(rows.contains("minecraft:block.smoker.smoke"), "a sound name is not text");
        assertEquals(french.size(), rows.size(), "one row per distinct line: " + rows);
    }

    /** Translation must never change whether a shipped package's Lua power loads, and must reach every one it can. */
    @ParameterizedTest(name = "{displayName} [{index}] {0}")
    @MethodSource("packageLuaPowers")
    void everyPackageLuaPowerLoadsTranslated(String relative) throws Exception {
        File file = dlcGeneration().resolve(relative).toFile();
        LuaPowerConfigFields english = loadOrNull(file);
        translateTo(() -> loadOrNull(file), line -> "«" + line + " \"\\ é»");
        LuaPowerConfigFields translated = loadOrNull(file);

        assertEquals(english != null, translated != null, "translation changed whether the power loads");
        if (english == null) return;
        if (LITERAL_TEXT_CALL.matcher(Files.readString(file.toPath())).find())
            assertFalse(scriptTextEnglish().isEmpty(), "a literal passed to a text call registered no row");
        if (!scriptTextEnglish().isEmpty())
            assertNotEquals(english.getLuaPowerDefinition().getSource(), translated.getLuaPowerDefinition().getSource(),
                    "registered text was not placed in the source (the loader fell back to English)");
    }

    static Stream<String> packageLuaPowers() throws Exception {
        Path root = dlcGeneration();
        try (Stream<Path> files = Files.walk(root)) {
            return files.map(path -> root.relativize(path).toString().replace('\\', '/'))
                    .filter(path -> path.endsWith(".lua") && path.matches("[^/]+/(elitemobs/)?powers/.+"))
                    .sorted().toList().stream();
        }
    }

    private static Path dlcGeneration() throws Exception {
        Path state = WORKSPACE.resolve("dlc/private-state");
        Matcher generation = Pattern.compile("\"generation\"\\s*:\\s*\"([^\"]+)\"")
                .matcher(Files.readString(state.resolve("current.json")));
        if (!generation.find()) throw new IllegalStateException("dlc/private-state/current.json names no generation");
        return state.resolve("generations").resolve(generation.group(1));
    }

    /** Boots French, registers, fills the fr column of the generated CSV, and boots French again. */
    private void translateTo(Runnable register, Function<String, String> translator) throws Exception {
        setLanguage("french");
        new TranslationsConfig();
        register.run();
        TranslationsConfig.shutdown();
        Path csv = translations.resolve("french.csv");
        if (Files.exists(csv)) {
            TranslationCsvParser.TranslationData data = TranslationCsvParser.parse(csv);
            for (String key : List.copyOf(data.getKeys())) {
                if (!key.startsWith("script_text.")) continue;
                String translated = translator.apply(data.getString(key, "en"));
                if (translated != null) data.set(key, "fr", translated);
            }
            TranslationCsvParser.write(data, csv);
        }
        new TranslationsConfig();
    }

    private List<String> scriptTextEnglish() throws Exception {
        Path csv = translations.resolve("french.csv");
        if (!Files.exists(csv)) return List.of();
        TranslationCsvParser.TranslationData data = TranslationCsvParser.parse(csv);
        return data.getKeys().stream().filter(key -> key.startsWith("script_text."))
                .map(key -> data.getString(key, "en")).toList();
    }

    private List<ScriptActionBlueprint> airship() {
        return List.of(
                action("the_airship_boiler_steam_cloud.yml", Map.of("action", "MESSAGE", "sValue", BOILER_LINE)),
                action("another_boiler.yml", Map.of("action", "MESSAGE", "sValue", BOILER_LINE)),
                action("the_airship_boiler_steam_cloud.yml",
                        Map.of("action", "TITLE_MESSAGE", "title", GRADIENT_TITLE, "subtitle", "&eThe steam rises!")),
                action("the_airship_boiler_steam_cloud.yml", Map.of("action", "ACTION_BAR_MESSAGE", "sValue", "$player is boiling")),
                action("the_airship_boiler_steam_cloud.yml", Map.of("action", "BOSS_BAR_MESSAGE", "sValue", "Boiler pressure")),
                action("the_airship_boiler_steam_cloud.yml",
                        Map.of("action", "PLAY_SOUND", "sValue", "minecraft:block.smoker.smoke")));
    }

    private static ScriptActionBlueprint action(String file, Map<String, Object> entry) {
        Map<String, Object> withTarget = new HashMap<>(entry);
        withTarget.put("Target", Map.of("targetType", "SELF"));
        return new ScriptActionBlueprint(withTarget, "BossInvulnerable", file);
    }

    private static File writePower(Path folder) throws Exception {
        Files.writeString(folder.resolve("prefix.inc"), "local PREFIX = \"&8[&2Alchemist Goblin&8] \"\n");
        Path power = folder.resolve("translated_power.lua");
        Files.writeString(power, """
                -- @include prefix.inc
                local MODE = 'Hot'
                return {
                 api_version = 1,
                 on_spawn = function(c)
                \tc.player:send_message(PREFIX .. "&fMe had that fart in for THREE WEEKS!")
                \tc.player:send_message("It's \\"boiling\\"")
                \tif MODE == 'Hot' then c.player:send_message('Hot') end
                \tc.player:show_title("Steam rises", 'The boiler wakes', 10, 40, 10)
                \tc.player:send_message([[Long
                line]])
                \tc.player:send_message('Untranslated line')
                \tc.player:send_message('Wave ' .. c.wave)
                \tc.player:play_sound('BLOCK_NOTE_BLOCK_HARP')
                 end
                }
                """);
        return power.toFile();
    }

    private static LuaPowerConfigFields load(File file) {
        try {
            return LuaPowerManager.loadLuaPower(file.getName(), file, null, PowerType.MISCELLANEOUS);
        } catch (Exception failure) {
            throw new AssertionError("Could not load " + file, failure);
        }
    }

    private static LuaPowerConfigFields loadOrNull(File file) {
        try {
            return LuaPowerManager.loadLuaPower(file.getName(), file, null, PowerType.MISCELLANEOUS);
        } catch (Exception unloadable) {
            return null;
        }
    }

    /** Runs on_spawn against a player table that records every text and sound it is handed. */
    private static List<String> shown(LuaPowerConfigFields power) {
        List<String> shown = new ArrayList<>();
        LuaTable player = new LuaTable();
        player.set("send_message", recorder(shown, 1));
        player.set("show_title", recorder(shown, 2));
        player.set("play_sound", recorder(shown, 1));
        LuaTable context = new LuaTable();
        context.set("player", player);
        context.set("wave", 3);
        power.getLuaPowerDefinition().instantiate().get("on_spawn").call(context);
        return shown;
    }

    private static VarArgFunction recorder(List<String> shown, int texts) {
        return new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                for (int i = 2; i < 2 + texts; i++) shown.add(args.checkjstring(i));
                return NONE;
            }
        };
    }

    private static String setLanguage(String language) throws Exception {
        Field field = DefaultConfig.class.getDeclaredField("language");
        field.setAccessible(true);
        String previous = (String) field.get(null);
        field.set(null, language);
        return previous;
    }
}
