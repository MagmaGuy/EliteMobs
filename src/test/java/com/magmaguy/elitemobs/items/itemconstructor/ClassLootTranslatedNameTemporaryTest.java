package com.magmaguy.elitemobs.items.itemconstructor;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.ClassLootSettingsConfig;
import com.magmaguy.elitemobs.config.ClassLootSettingsConfig.Difficulty;
import com.magmaguy.elitemobs.config.ClassLootSettingsConfig.Rank;
import com.magmaguy.elitemobs.config.DefaultConfig;
import com.magmaguy.elitemobs.config.ItemSettingsConfig;
import com.magmaguy.elitemobs.config.customitems.CustomItemsConfigFields;
import com.magmaguy.elitemobs.config.translations.TranslationsConfig;
import com.magmaguy.elitemobs.items.ClassLootFamily;
import com.magmaguy.elitemobs.items.ClassLootProfile;
import com.magmaguy.elitemobs.items.customitems.CustomItem;
import com.magmaguy.magmacore.MagmaCore;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockbukkit.mockbukkit.MockBukkit;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/** Temporary ticket check (crqyyds): class loot names take $difficulty and $item from the language file. */
class ClassLootTranslatedNameTemporaryTest {
    private MagmaCore previousCore;
    private MockedStatic<DefaultConfig> language;

    @BeforeEach void open() throws Exception {
        MockBukkit.mock();
        var plugin = MockBukkit.createMockPlugin();
        MetadataHandler.PLUGIN = plugin;
        var coreInstance = MagmaCore.class.getDeclaredField("instance");
        coreInstance.setAccessible(true);
        previousCore = MagmaCore.getInstance();
        coreInstance.set(null, null);
        MagmaCore.createInstance(plugin);
        Path translations = plugin.getDataFolder().toPath().resolve("translations");
        Files.createDirectories(translations);
        // Only two of the new keys are translated, as a server would have after translating some of them.
        Files.writeString(translations.resolve("chineseSimplified.csv"), """
                "key","en","zh_cn"
                "ItemSettings.classLootDifficultyNames.MYTHIC","MYTHIC","神话"
                "ItemSettings.classLootFamilyNames.DPS_CHESTPLATES","DPS Chestplate","输出胸甲"
                """, StandardCharsets.UTF_8);
        language = mockStatic(DefaultConfig.class, CALLS_REAL_METHODS);
        language.when(DefaultConfig::getLanguage).thenReturn("chineseSimplified");
        new TranslationsConfig();
        new ItemSettingsConfig();
    }

    @AfterEach void close() throws Exception {
        TranslationsConfig.shutdown();
        language.close();
        var coreInstance = MagmaCore.class.getDeclaredField("instance");
        coreInstance.setAccessible(true);
        coreInstance.set(null, previousCore);
        MockBukkit.unmock();
        MetadataHandler.PLUGIN = null;
    }

    @Test void translatedDifficultyAndFamilyReachTheConstructedName() {
        String mythic = constructedName(Difficulty.MYTHIC);
        String hard = constructedName(Difficulty.HARD);
        System.out.println("constructed names: " + mythic + " | " + hard);
        assertEquals("[神话] 虚空锻造胸甲 · 输出胸甲", mythic);
        // HARD is not translated in the file yet, so it keeps the English default it always had.
        assertEquals("[HARD] 虚空锻造胸甲 · 输出胸甲", hard);
    }

    /** Runs the production class loot constructor and returns the name it hands to the item builder. */
    private static String constructedName(Difficulty difficulty) {
        var item = mock(CustomItem.class);
        var fields = mock(CustomItemsConfigFields.class);
        when(item.getCustomItemsConfigFields()).thenReturn(fields);
        when(fields.isEnabled()).thenReturn(true);
        when(fields.getClassLootFamily()).thenReturn(ClassLootFamily.DPS_CHESTPLATES);
        when(fields.getMaterial()).thenReturn(Material.DIAMOND_CHESTPLATE);
        when(fields.getName()).thenReturn("[$difficulty] 虚空锻造胸甲 · $item");
        var profile = mock(ClassLootProfile.class);
        when(profile.roll(anyDouble(), anyInt())).thenReturn(new ClassLootProfile.Roll(new HashMap<>(), new HashMap<>()));
        AtomicReference<String> name = new AtomicReference<>();
        try (var profiles = mockStatic(ClassLootSettingsConfig.class);
             var builder = mockStatic(ItemConstructor.class, call -> {
                 if (call.getMethod().getName().equals("constructItem")) name.set(call.getArgument(1));
                 return new ItemStack(Material.DIAMOND_CHESTPLATE);
             })) {
            profiles.when(() -> ClassLootSettingsConfig.profile(any(), any(), any())).thenReturn(profile);
            assertNotNull(ClassLootItemConstructor.construct(item, 30, difficulty, Rank.BOSS, null, null, "Nether Bell"));
        }
        return name.get();
    }
}
