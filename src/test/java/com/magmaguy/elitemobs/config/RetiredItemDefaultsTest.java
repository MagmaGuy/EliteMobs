package com.magmaguy.elitemobs.config;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.customitems.CustomItemsConfig;
import com.magmaguy.elitemobs.items.ItemConsumables;
import com.magmaguy.magmacore.MagmaCore;
import com.magmaguy.magmacore.config.OutdatedConfigurationArchive;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.*;
import org.mockbukkit.mockbukkit.MockBukkit;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class RetiredItemDefaultsTest {
    private JavaPlugin plugin;
    private MagmaCore previousCore;

    public static class ResourcePlugin extends JavaPlugin {
        @Override public InputStream getResource(String name) {
            return RetiredItemDefaultsTest.class.getClassLoader().getResourceAsStream(name);
        }
    }

    @BeforeEach void open() throws Exception {
        MockBukkit.mock();
        plugin = MockBukkit.loadSimple(ResourcePlugin.class);
        MetadataHandler.PLUGIN = plugin;
        var instance = MagmaCore.class.getDeclaredField("instance");
        instance.setAccessible(true);
        previousCore = MagmaCore.getInstance();
        instance.set(null, null);
        MagmaCore.createInstance(plugin);
    }

    @AfterEach void close() throws Exception {
        var instance = MagmaCore.class.getDeclaredField("instance");
        instance.setAccessible(true);
        instance.set(null, previousCore);
        CustomItemsConfig.getCustomItems().clear();
        OutdatedConfigurationArchive.unregister(plugin);
        MockBukkit.unmock();
    }

    @Test void ticketDefaultsRegenerateWhileCustomCopiesAndCurrentDefaultsArePreserved() throws Exception {
        new CustomItemsConfig();
        var defaults = new HashMap<>(CustomItemsConfig.getCustomItems());
        var oldItems = YamlConfiguration.loadConfiguration(new InputStreamReader(
                Objects.requireNonNull(getClass().getResourceAsStream("/tickets/esquivel-retired-items.yml")),
                StandardCharsets.UTF_8));
        Path directory = plugin.getDataFolder().toPath().resolve("customitems");
        var expectedEnchantments = new HashMap<String, List<String>>();
        var expectedConsumables = new HashMap<String, ItemConsumables.Definition>();
        var oldFiles = new HashMap<String, String>();
        assertEquals(63, oldItems.getMapList("items").size());
        for (var oldItem : oldItems.getMapList("items")) {
            String filename = (String) oldItem.get("file");
            var fields = defaults.get(filename);
            if (fields != null) {
                expectedEnchantments.put(filename, fields.getEnchantments() == null
                        ? List.of() : List.copyOf(fields.getEnchantments()));
                expectedConsumables.put(filename, fields.getConsumable());
            }
            String old = "# operator customization\nisEnabled: true\nmaterial: PAPER\nname: Old customized name\n"
                    + "enchantments: ['" + oldItem.get("retired") + ",1']\n";
            oldFiles.put(filename, old);
            Files.writeString(directory.resolve(filename), old);
        }
        // A current default with custom text is still current and must not be archived.
        Path current = directory.resolve("enchanted_book_mending.yml");
        String currentYaml = "# keep my edits\nisEnabled: true\nmaterial: ENCHANTED_BOOK\n"
                + "name: My mending book\nenchantments: ['MENDING,1']\n";
        Files.writeString(current, currentYaml);
        expectedEnchantments.remove(current.getFileName().toString());
        expectedConsumables.remove(current.getFileName().toString());
        oldFiles.remove(current.getFileName().toString());

        assertTrue(expectedEnchantments.size() > 45, "fixture must exercise the shipped retired defaults");

        OutdatedConfigurationArchive.archiveFor(plugin);
        assertAll(expectedEnchantments.keySet().stream().map(filename ->
                () -> assertFalse(Files.exists(directory.resolve(filename)), "obsolete default retained: " + filename)));
        assertEquals(currentYaml, Files.readString(current));
        for (String filename : oldFiles.keySet()) {
            if (!defaults.containsKey(filename))
                assertEquals(oldFiles.get(filename), Files.readString(directory.resolve(filename)),
                        "non-default content must not be archived: " + filename);
        }
        List<String> archived = archiveContents();
        assertEquals(expectedEnchantments.size(), archived.size());
        for (String filename : expectedEnchantments.keySet()) assertTrue(archived.contains(oldFiles.get(filename)));

        new CustomItemsConfig();
        for (String filename : expectedEnchantments.keySet()) {
            var replacement = CustomItemsConfig.getCustomItems().get(filename);
            assertNotNull(replacement, filename);
            assertTrue(replacement.isEnabled(), filename);
            assertEquals(expectedEnchantments.get(filename), replacement.getEnchantments(), filename);
            assertEquals(expectedConsumables.get(filename), replacement.getConsumable(), filename);
        }
        OutdatedConfigurationArchive.archiveFor(plugin);
        assertEquals(archived.size(), archiveContents().size(), "second startup must not archive replacements");
    }

    private List<String> archiveContents() throws Exception {
        Path archive = plugin.getDataFolder().toPath().getParent().resolve("MagmaCore/outdated files");
        if (!Files.exists(archive)) return List.of();
        try (var files = Files.walk(archive)) {
            var contents = new ArrayList<String>();
            for (Path path : files.filter(Files::isRegularFile).toList()) contents.add(Files.readString(path));
            return contents;
        }
    }
}
