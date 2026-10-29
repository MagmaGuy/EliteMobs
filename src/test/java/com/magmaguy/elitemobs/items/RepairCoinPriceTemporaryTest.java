package com.magmaguy.elitemobs.items;

import com.magmaguy.elitemobs.config.EconomySettingsConfig;
import com.magmaguy.elitemobs.config.menus.premade.RepairMenuConfig;
import com.magmaguy.elitemobs.economy.EconomyHandler;
import com.magmaguy.elitemobs.menus.RepairMenu;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/** temp.elitemobs.repair-coin-price: repairs cost coins per item level and missing durability. */
@EnabledIfEnvironmentVariable(named = "NIGHTBREAK_TEMP_TICKET_TESTS", matches = "true")
class RepairCoinPriceTemporaryTest extends RepairMenuTemporaryFixture {

    // Read after MockBukkit starts: Material cannot initialize without a server.
    private int halfBroken;

    @BeforeEach void readDurability() {
        halfBroken = Material.DIAMOND_CHESTPLATE.getMaxDurability() / 2;
    }

    @Test void halfBrokenLevel100ItemCosts50AndRepairChargesExactlyThat() {
        balance = 120;
        ItemStack damaged = chestplate(halfBroken);
        assertEquals(50, RepairMenu.repairCost(damaged));
        player.getInventory().setItem(0, damaged);
        openMenu();
        clickPlayerSlot(0);

        ItemStack price = menu.getItem(RepairMenuConfig.repairCostSlot);
        assertNotNull(price, "The middle box shows the price");
        assertEquals("Cost 50 Elite Coins", price.getItemMeta().getDisplayName());
        assertEquals("Have " + EconomyHandler.formatCurrency(120), price.getItemMeta().getLore().get(0));
        assertEquals(0, damage(menu.getItem(RepairMenuConfig.outputSlot)));

        clickRaw(RepairMenuConfig.confirmSlot);
        assertEquals(70, balance, 0.0001);
        ItemStack repaired = Arrays.stream(player.getInventory().getStorageContents())
                .filter(item -> item != null && item.getType() == Material.DIAMOND_CHESTPLATE).findFirst().orElseThrow();
        assertEquals(0, damage(repaired));
        assertTrue(ItemTagger.isEliteItem(repaired));
        assertNull(menu.getItem(RepairMenuConfig.eliteItemInputSlot));
        assertNull(menu.getItem(RepairMenuConfig.repairCostSlot));
        assertNull(menu.getItem(RepairMenuConfig.outputSlot));
    }

    @Test void tooFewCoinsChargesNothingAndKeepsTheItemInTheMenu() {
        balance = 10;
        ItemStack damaged = chestplate(halfBroken);
        player.getInventory().setItem(0, damaged);
        openMenu();
        clickPlayerSlot(0);
        clickRaw(RepairMenuConfig.confirmSlot);

        assertEquals(10, balance, 0.0001);
        ItemStack held = menu.getItem(RepairMenuConfig.eliteItemInputSlot);
        assertNotNull(held, "An unaffordable repair leaves the item in the menu");
        assertEquals(halfBroken, damage(held));
        assertNotNull(menu.getItem(RepairMenuConfig.repairCostSlot));
        String message = player.nextMessage();
        assertNotNull(message);
        assertTrue(message.contains("50"), message);
        player.closeInventory();
        assertEquals(1, Arrays.stream(player.getInventory().getStorageContents())
                .filter(item -> item != null && item.getType() == Material.DIAMOND_CHESTPLATE
                        && damage(item) == halfBroken).count(), "Closing returns the unrepaired item");
    }

    @Test void undamagedItemHasNoPriceAndNothingHappens() {
        balance = 120;
        ItemStack intact = chestplate(0);
        assertEquals(-1, RepairMenu.repairCost(intact));
        player.getInventory().setItem(0, intact);
        openMenu();
        clickPlayerSlot(0);
        assertNull(menu.getItem(RepairMenuConfig.repairCostSlot));
        assertNull(menu.getItem(RepairMenuConfig.outputSlot));
        clickRaw(RepairMenuConfig.confirmSlot);
        assertEquals(120, balance, 0.0001);
        assertNotNull(menu.getItem(RepairMenuConfig.eliteItemInputSlot));
    }

    @Test void repairsAreFreeWhenTheEconomyIsDisabled() throws Exception {
        var field = EconomySettingsConfig.class.getDeclaredField("enableEconomy");
        field.setAccessible(true);
        boolean previous = field.getBoolean(null);
        field.setBoolean(null, false);
        try {
            balance = 120;
            ItemStack damaged = chestplate(halfBroken);
            assertEquals(0, RepairMenu.repairCost(damaged));
            player.getInventory().setItem(0, damaged);
            openMenu();
            clickPlayerSlot(0);
            clickRaw(RepairMenuConfig.confirmSlot);
            assertEquals(120, balance, 0.0001);
            assertTrue(Arrays.stream(player.getInventory().getStorageContents())
                    .anyMatch(item -> item != null && item.getType() == Material.DIAMOND_CHESTPLATE && damage(item) == 0));
        } finally {
            field.setBoolean(null, previous);
        }
    }
}
