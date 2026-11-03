package com.magmaguy.elitemobs.powers.lua;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.DefaultConfig;
import com.magmaguy.elitemobs.config.powers.LuaPowerConfigFields;
import com.magmaguy.elitemobs.config.powers.PowersConfigFields.PowerType;
import com.magmaguy.elitemobs.config.translations.TranslationCsvParser;
import com.magmaguy.elitemobs.config.translations.TranslationsConfig;
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
 * Lua power text that reaches a text call through a helper, a table, a loop or pick() is translated (EliteMobs
 * 5883b6cb7), its English is read as UTF-8 (398ac135d), and text whose value the script also compares or keys on
 * stays English. Each case boots French so the lines register, fills the fr column of the generated CSV as a
 * translator would, boots again as /em reload does, and checks what the power hands the player.
 */
@EnabledIfEnvironmentVariable(named = "NIGHTBREAK_TEMP_TICKET_TESTS", matches = "true")
class LuaTextFlowTemporaryTest {

    private static final Path WORKSPACE = Path.of("..").toAbsolutePath().normalize();
    private static final Path AG_V28 = WORKSPACE.resolve("dlc/work/the-adventurers-guild_v28");
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
    void textThroughHelpersTablesAndLoopsIsTranslated(@TempDir Path folder) throws Exception {
        File power = writePower(folder);
        List<String> english = shown(load(power));
        assertEquals(List.of(
                "block:STONE",
                "&6[Instructor] &fLeap explain one",
                "&6[Instructor] &fLeap explain two",
                "sound:ENTITY_HORSE_GALLOP",
                "&6[Instructor] &fCalm first line",
                "&6[Instructor] &fCalm again line",
                "&6[Instructor] &fTaunt line",
                "&6[Instructor] &fReady to fight",
                "fact:matched",
                "fact:key intact",
                "&6[Instructor] &fFallback line",
                "&6[Instructor] &fDash — line"), english, "the fixture runs as written in English");

        translateTo(() -> load(power), line -> "FR " + line);

        assertEquals(List.of(
                "block:STONE",
                "FR &6[Instructor] &fFR Leap explain one",
                "FR &6[Instructor] &fFR Leap explain two",
                "sound:ENTITY_HORSE_GALLOP",
                "FR &6[Instructor] &fFR Calm first line",
                "FR &6[Instructor] &fFR Calm again line",
                "FR &6[Instructor] &fFR Taunt line",
                "FR &6[Instructor] &fReady to fight",
                "fact:matched",
                "fact:key intact",
                "FR &6[Instructor] &fFR Fallback line",
                "FR &6[Instructor] &fFR Dash — line"), shown(load(power)));
        assertEquals(Set.of("&6[Instructor] &f", "Leap explain one", "Leap explain two", "Calm first line",
                "Calm again line", "Taunt line", "Fallback line", "Dash — line"), new HashSet<>(textEnglish()),
                "only shown lines register: no block, sound, compared or run-time keyed value");
        String csv = Files.readString(translations.resolve("french.csv"));
        assertFalse(csv.contains("\u0014"), "the em dash reached the CSV as itself, not as U+0014");
    }

    /** Translation must never change whether a store or AG v28 Lua power loads, and must reach every one it can. */
    @ParameterizedTest(name = "{displayName} [{index}] {0}")
    @MethodSource("packageLuaPowers")
    void everyPackageLuaPowerLoadsTranslated(String labelled) throws Exception {
        File file = resolve(labelled).toFile();
        LuaPowerConfigFields english = loadOrNull(file);
        translateTo(() -> loadOrNull(file), line -> "«" + line + " \"\\ é»");
        LuaPowerConfigFields translated = loadOrNull(file);

        assertEquals(english != null, translated != null, "translation changed whether the power loads");
        if (english == null) return;
        if (LITERAL_TEXT_CALL.matcher(Files.readString(file.toPath())).find())
            assertFalse(textEnglish().isEmpty(), "a literal passed to a text call registered no row");
        if (!textEnglish().isEmpty())
            assertNotEquals(english.getLuaPowerDefinition().getSource(), translated.getLuaPowerDefinition().getSource(),
                    "registered text was not placed in the source (the loader fell back to English)");
        for (String line : textEnglish())
            assertFalse(line.contains("\u0014"), "a registered line lost a character outside Latin-1: " + line);
    }

    static Stream<String> packageLuaPowers() throws Exception {
        List<String> all = new ArrayList<>(powersUnder("store:", dlcGeneration()));
        all.addAll(powersUnder("work:", WORKSPACE.resolve("dlc/work")).stream()
                .filter(path -> path.startsWith("work:the-adventurers-guild_v28/")).toList());
        if (all.stream().noneMatch(path -> path.startsWith("work:")))
            throw new IllegalStateException("The AG v28 work copy has no Lua powers: " + AG_V28);
        return all.stream();
    }

    private static List<String> powersUnder(String label, Path root) throws Exception {
        try (Stream<Path> files = Files.walk(root, 8)) {
            return files.map(path -> root.relativize(path).toString().replace('\\', '/'))
                    .filter(path -> path.endsWith(".lua") && path.matches("[^/]+/(elitemobs/)?powers/.+"))
                    .sorted().map(path -> label + path).toList();
        }
    }

    private static Path resolve(String labelled) throws Exception {
        if (labelled.startsWith("store:")) return dlcGeneration().resolve(labelled.substring("store:".length()));
        return WORKSPACE.resolve("dlc/work").resolve(labelled.substring("work:".length()));
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
                if (!key.startsWith("text.")) continue;
                String translated = translator.apply(data.getString(key, "en"));
                if (translated != null) data.set(key, "fr", translated);
            }
            TranslationCsvParser.write(data, csv);
        }
        new TranslationsConfig();
    }

    private List<String> textEnglish() throws Exception {
        Path csv = translations.resolve("french.csv");
        if (!Files.exists(csv)) return List.of();
        TranslationCsvParser.TranslationData data = TranslationCsvParser.parse(csv);
        return data.getKeys().stream().filter(key -> key.startsWith("text."))
                .map(key -> data.getString(key, "en")).toList();
    }

    private static File writePower(Path folder) throws Exception {
        Path power = folder.resolve("text_flow_power.lua");
        Files.writeString(power, """
                local MATS = { "STONE" }
                local LINES = { calm = { first = "Calm first line", again = { "Calm again line" } } }
                local POWERS = { leap = { explain = { "Leap explain one", "Leap explain two" }, sound = "ENTITY_HORSE_GALLOP" } }
                local TAUNTS = { "Taunt line" }
                local KEYED = {}
                local function pick(list) return list[math.random(#list)] end
                local function say(c, line) c.player:send_message("&6[Instructor] &f" .. line) end
                return {
                 api_version = 1,
                 on_spawn = function(c)
                  c.player:place_block(pick(MATS))
                  local p = POWERS[c.id]
                  for _, line in ipairs(p.explain) do say(c, line) end
                  c.player:play_sound(p.sound)
                  local lines = LINES[c.mode]
                  say(c, lines.first)
                  if lines.again then say(c, pick(lines.again)) end
                  say(c, pick(TAUNTS))
                  local status = c.ready and "Ready to fight" or "Not ready"
                  say(c, status)
                  c.player:record(status == "Ready to fight" and "matched" or "unmatched")
                  KEYED[c.mode] = "Written at run time"
                  c.player:record(KEYED.calm == "Written at run time" and "key intact" or "key broken")
                  say(c, KEYED.other or "Fallback line")
                  say(c, "Dash — line")
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

    /** Runs on_spawn against a player table that records every text, block, sound and fact it is handed. */
    private static List<String> shown(LuaPowerConfigFields power) {
        List<String> shown = new ArrayList<>();
        LuaTable player = new LuaTable();
        player.set("send_message", recorder(shown, ""));
        player.set("place_block", recorder(shown, "block:"));
        player.set("play_sound", recorder(shown, "sound:"));
        player.set("record", recorder(shown, "fact:"));
        LuaTable context = new LuaTable();
        context.set("player", player);
        context.set("id", "leap");
        context.set("mode", "calm");
        context.set("ready", com.magmaguy.shaded.luaj.vm2.LuaValue.TRUE);
        power.getLuaPowerDefinition().instantiate().get("on_spawn").call(context);
        return shown;
    }

    private static VarArgFunction recorder(List<String> shown, String prefix) {
        return new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                shown.add(prefix + args.checkjstring(2));
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
