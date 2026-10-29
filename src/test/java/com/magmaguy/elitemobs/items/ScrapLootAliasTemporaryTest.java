package com.magmaguy.elitemobs.items;

import com.magmaguy.elitemobs.config.customtreasurechests.CustomTreasureChestConfigFields;
import com.magmaguy.elitemobs.items.customloottable.CurrencyCustomLootEntry;
import com.magmaguy.elitemobs.items.customloottable.CustomLootEntry;
import com.magmaguy.elitemobs.items.customloottable.CustomLootTable;
import com.magmaguy.elitemobs.items.customloottable.EliteCustomLootEntry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** temp.elitemobs.scrap-loot-alias: content that still rewards elite scrap pays its coin value instead. */
@EnabledIfEnvironmentVariable(named = "NIGHTBREAK_TEMP_TICKET_TESTS", matches = "true")
class ScrapLootAliasTemporaryTest {
    private final FreshCustomEnchantmentTest itemFixture = new FreshCustomEnchantmentTest();

    @BeforeEach void open() throws Exception { itemFixture.open(); }

    @AfterEach void close() throws Exception { itemFixture.close(); }

    @Test void everyLootFormatNamingScrapPaysCoinsAndKeepsItsConditions() {
        var chest = mock(CustomTreasureChestConfigFields.class);
        when(chest.getFilename()).thenReturn("scrap_alias_chest.yml");
        // The three shapes DLC uses: filename:chance:permission, key=value pairs and YAML maps.
        when(chest.getLootList()).thenReturn(List.of(
                "elite_scrap_tiny.yml:0.3:my.permission",
                "filename=elite_scrap_small.yml:amount=2:chance=0.2:wave=10",
                Map.of("filename", "elite_scrap_huge.yml", "amount", 3, "chance", 0.05, "itemLevel", 7),
                "filename=elite_scrap_medium.yml",
                "some_other_item.yml:0.5"));
        List<CustomLootEntry> entries = new CustomLootTable(chest).getEntries();
        assertEquals(5, entries.size(), "Every entry parses");

        var tiny = assertInstanceOf(CurrencyCustomLootEntry.class, entries.get(0));
        assertEquals(5, tiny.getCurrencyAmount());
        assertEquals(0.3, tiny.getChance(), 0.0001);
        assertEquals("my.permission", tiny.getPermission());

        var small = assertInstanceOf(CurrencyCustomLootEntry.class, entries.get(1));
        assertEquals(60, small.getCurrencyAmount(), "2 small scrap at 30 coins each");
        assertEquals(0.2, small.getChance(), 0.0001);
        assertEquals(10, small.getWave());
        assertEquals(1, small.getAmount(), "The amount is folded into the coins");

        var huge = assertInstanceOf(CurrencyCustomLootEntry.class, entries.get(2));
        assertEquals(675, huge.getCurrencyAmount(), "3 huge scrap at 225 coins each");
        assertEquals(0.05, huge.getChance(), 0.0001);
        assertEquals(7, huge.getItemLevel());

        var medium = assertInstanceOf(CurrencyCustomLootEntry.class, entries.get(3));
        assertEquals(80, medium.getCurrencyAmount());
        assertEquals(1, medium.getChance(), 0.0001);

        var other = assertInstanceOf(EliteCustomLootEntry.class, entries.get(4), "Other files still give items");
        assertEquals(0.5, other.getChance(), 0.0001);
    }
}
