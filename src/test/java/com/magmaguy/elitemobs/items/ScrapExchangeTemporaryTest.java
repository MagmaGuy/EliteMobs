package com.magmaguy.elitemobs.items;

import com.magmaguy.elitemobs.MetadataHandler;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import static org.junit.jupiter.api.Assertions.*;

/** temp.elitemobs.scrap-exchange: scrap players still hold is worth its tier value in coins. */
@EnabledIfEnvironmentVariable(named = "NIGHTBREAK_TEMP_TICKET_TESTS", matches = "true")
class ScrapExchangeTemporaryTest extends RepairMenuTemporaryFixture {

    @Test void scrapWithACustomItemIdPaysItsTierValueForTheWholeStack() {
        player.getInventory().setItem(0, scrap(Material.WHITE_DYE, 3, "elite_scrap_medium.yml", false));
        openMenu();
        clickPlayerSlot(0);
        assertEquals(240, balance, 0.0001, "3 medium scrap at 80 coins each");
        assertNull(player.getInventory().getItem(0), "The exchanged stack is gone");
        String message = player.nextMessage();
        assertNotNull(message);
        assertTrue(message.contains("240") && message.contains("3"), message);
    }

    @Test void olderScrapWithOnlyTheFilenameTagIsRecognized() {
        ItemStack older = scrap(Material.PURPLE_DYE, 2, "elite_scrap_huge.yml", true);
        assertNull(ItemTagger.getCustomItemId(older));
        assertEquals(5, ItemConsumables.scrapTier(older));
        player.getInventory().setItem(0, older);
        openMenu();
        clickPlayerSlot(0);
        assertEquals(450, balance, 0.0001, "2 huge scrap at 225 coins each");
        assertNull(player.getInventory().getItem(0));
    }

    @Test void sellMenusValueScrapAtItsTierValue() {
        assertEquals(5, ItemWorthCalculator.determineResaleWorth(scrap(Material.WHITE_DYE, 1, "elite_scrap_tiny.yml", false), player), 0.0001);
        assertEquals(30, ItemWorthCalculator.determineResaleWorth(scrap(Material.WHITE_DYE, 64, "elite_scrap_small.yml", false), player), 0.0001);
        assertEquals(150, ItemWorthCalculator.determineResaleWorth(scrap(Material.PURPLE_DYE, 1, "elite_scrap_large.yml", true), player), 0.0001);
    }

    @Test void ordinaryDyeIsNotScrap() {
        ItemStack otherCustomItem = scrap(Material.WHITE_DYE, 1, "my_dye.yml", false);
        assertEquals(0, ItemConsumables.scrapTier(otherCustomItem));
        assertEquals(0, ItemConsumables.scrapCoinValue(otherCustomItem));
        ItemStack plain = new ItemStack(Material.WHITE_DYE, 5);
        assertEquals(0, ItemConsumables.scrapTier(plain));
        player.getInventory().setItem(0, plain.clone());
        openMenu();
        clickPlayerSlot(0);
        assertEquals(0, balance, 0.0001);
        assertEquals(plain, player.getInventory().getItem(0), "A plain dye stays where it was");
    }

    /** Elite scrap as shipped: tagged by custom item ID, or by the older filename-as-key tag only. */
    private ItemStack scrap(Material material, int amount, String filename, boolean filenameTagOnly) {
        ItemStack item = new ItemStack(material, amount);
        var meta = item.getItemMeta();
        ItemTagger.registerEliteItem(meta);
        if (filenameTagOnly)
            meta.getPersistentDataContainer().set(new NamespacedKey(MetadataHandler.PLUGIN, filename), PersistentDataType.STRING, filename);
        else ItemTagger.registerCustomItemId(meta, filename);
        item.setItemMeta(meta);
        return item;
    }
}
