package com.magmaguy.elitemobs.config.translations;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.DefaultConfig;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockbukkit.mockbukkit.MockBukkit;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Merging a downloaded translation update into french.csv through the production load and save path.
 */
@EnabledIfEnvironmentVariable(named = "NIGHTBREAK_TEMP_TICKET_TESTS", matches = "true")
class TranslationUpdateMergeTemporaryTest {
    private static final String LIVE = """
            "key","en","fr"
            "text.aaa","Hello","Bonjour"
            "text.bbb","Bye","Au revoir mec"
            "ui.unregistered","Click","Clic"
            "menu.lore[0]","A","a"
            "menu.lore[1]","B","b"
            """;
    private static final String BASELINE = """
            "key","en","fr"
            "text.aaa","Hello","Bonjour"
            "text.bbb","Bye","Au revoir"
            "menu.lore[0]","A","a"
            "menu.lore[1]","B","b"
            """;
    private static final String PUBLISHED = """
            "key","en","fr"
            "text.aaa","Hello","Salut"
            "text.bbb","Bye","Adieu"
            "ui.unregistered","Click","Cliquez"
            "menu.lore[0]","A","x"
            "menu.lore[1]","B","y"
            "text.ccc","New line","Nouvelle ligne"
            "ui.fresh","Open","Ouvrir"
            "somefile.name","Hello","Salut"
            """;

    private JavaPlugin previousPlugin;
    private String previousLanguage;
    private boolean previousAutoUpdate;
    private Path folder;
    private TranslationsConfigFields fields;

    @BeforeEach void open() throws Exception {
        MockBukkit.mock();
        previousPlugin = MetadataHandler.PLUGIN;
        MetadataHandler.PLUGIN = MockBukkit.createMockPlugin();
        previousLanguage = DefaultConfig.getLanguage();
        previousAutoUpdate = DefaultConfig.isAutoUpdateTranslations();
        setDefault("language", "french");
        setDefault("autoUpdateTranslations", true);
        folder = MetadataHandler.PLUGIN.getDataFolder().toPath().resolve("translations");
        Files.createDirectories(folder);
        write("french.csv", LIVE);
        write("french_data.csv", BASELINE);
    }

    @AfterEach void close() throws Exception {
        if (fields != null) fields.shutdown();
        setDefault("language", previousLanguage);
        setDefault("autoUpdateTranslations", previousAutoUpdate);
        MetadataHandler.PLUGIN = previousPlugin;
        MockBukkit.unmock();
    }

    @Test void shippedRowsTakeTheUpdateAndEditedRowsAreKept() throws Exception {
        write("french.csv.update", PUBLISHED);
        fields = new TranslationsConfigFields();

        assertEquals("Salut", fields.getRaw("text", "aaa"));
        assertEquals("Au revoir mec", fields.getRaw("text", "bbb"));
        assertEquals("Cliquez", fields.getRaw("ui", "unregistered"));
        assertEquals(List.of("x", "y"), fields.getRaw("menu", "lore"));
        assertEquals("Nouvelle ligne", fields.getRaw("text", "ccc"));
        assertEquals("Ouvrir", fields.getRaw("ui", "fresh"));
        assertNull(fields.getRaw("somefile", "name"), "a field-key copy of a content line is not added");
        assertFalse(Files.exists(folder.resolve("french.csv.update")));
        assertTrue(Files.exists(folder.resolve("french.csv.applying")), "kept until the merge is saved");
        assertEquals(LIVE, read("french.csv.before-update"));

        fields.shutdown();
        fields = null;
        assertFalse(Files.exists(folder.resolve("french.csv.applying")));
        var saved = TranslationCsvParser.parse(folder.resolve("french.csv"));
        assertEquals("Salut", saved.get("text.aaa", "fr"));
        assertEquals("Au revoir mec", saved.get("text.bbb", "fr"));
        assertEquals(List.of("x", "y"), saved.get("menu.lore", "fr"));
        var baseline = TranslationCsvParser.parse(folder.resolve("french_data.csv"));
        assertEquals("Salut", baseline.get("text.aaa", "fr"));
        assertEquals("Au revoir", baseline.get("text.bbb", "fr"), "an edited row keeps its recorded translation");
        assertEquals(List.of("x", "y"), baseline.get("menu.lore", "fr"));
    }

    @Test void newerDownloadSupersedesAnUnsavedMerge() throws Exception {
        write("french.csv.applying", PUBLISHED);
        write("french.csv.update", PUBLISHED.replace("\"Salut\"", "\"Coucou\""));
        fields = new TranslationsConfigFields();

        assertEquals("Coucou", fields.getRaw("text", "aaa"));
        assertFalse(Files.exists(folder.resolve("french.csv.update")));
    }

    @Test void interruptedMergeIsAppliedAgain() throws Exception {
        write("french.csv.applying", PUBLISHED);
        fields = new TranslationsConfigFields();

        assertEquals("Salut", fields.getRaw("text", "aaa"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"\"key\",\"en\",\"fr\"\n\"text.aaa\",\"Hello\",\"Sal", "\"key\",\"en\",\"de\"\n\"text.aaa\",\"Hello\",\"Hallo\"\n"})
    void unusableUpdatesAreDiscarded(String update) throws Exception {
        write("french.csv.update", update);
        fields = new TranslationsConfigFields();

        assertEquals("Bonjour", fields.getRaw("text", "aaa"));
        assertFalse(Files.exists(folder.resolve("french.csv.update")));
        assertFalse(Files.exists(folder.resolve("french.csv.applying")));
        assertFalse(Files.exists(folder.resolve("french.csv.before-update")));
    }

    @Test void disabledUpdatesStayPending() throws Exception {
        setDefault("autoUpdateTranslations", false);
        write("french.csv.update", PUBLISHED);
        fields = new TranslationsConfigFields();

        assertEquals("Bonjour", fields.getRaw("text", "aaa"));
        assertTrue(Files.exists(folder.resolve("french.csv.update")));
    }

    @Test void freshCopyRecordsItsVersionAndDropsPendingUpdates() throws Exception {
        write("french.csv.update", PUBLISHED);
        write("french.csv.applying", PUBLISHED);
        TranslationUpdater.freshCopyInstalled(folder, "french", "\"604b41-65f0ed499ebe5\"");

        assertEquals("\"604b41-65f0ed499ebe5\"", read("french.csv.etag"));
        assertFalse(Files.exists(folder.resolve("french.csv.update")));
        assertFalse(Files.exists(folder.resolve("french.csv.applying")));

        TranslationUpdater.freshCopyInstalled(folder, "french", "");
        assertFalse(Files.exists(folder.resolve("french.csv.etag")), "a copy without an ETag is always downloaded again");
    }

    private void write(String name, String contents) throws Exception {
        Files.writeString(folder.resolve(name), contents, StandardCharsets.UTF_8);
    }

    private String read(String name) throws Exception {
        return Files.readString(folder.resolve(name), StandardCharsets.UTF_8);
    }

    private static void setDefault(String field, Object value) throws Exception {
        var declared = DefaultConfig.class.getDeclaredField(field);
        declared.setAccessible(true);
        declared.set(null, value);
    }
}
