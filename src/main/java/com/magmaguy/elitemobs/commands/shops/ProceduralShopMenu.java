package com.magmaguy.elitemobs.commands.shops;

import com.magmaguy.elitemobs.config.ConfigValues;
import com.magmaguy.elitemobs.config.EconomySettingsConfig;
import com.magmaguy.elitemobs.config.menus.premade.BuyOrSellMenuConfig;
import com.magmaguy.elitemobs.config.menus.premade.ProceduralShopMenuConfig;
import com.magmaguy.elitemobs.economy.EconomyHandler;
import com.magmaguy.elitemobs.economy.UUIDFilter;
import com.magmaguy.elitemobs.items.ItemWorthCalculator;
import com.magmaguy.elitemobs.items.ObfuscatedSignatureLoreData;
import com.magmaguy.elitemobs.items.itemconstructor.ItemConstructor;
import com.magmaguy.elitemobs.utils.ObfuscatedStringHandler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Random;

/**
 * Created by MagmaGuy on 17/06/2017.
 */
public class ProceduralShopMenu implements Listener {

    private static final String SHOP_KEY = ObfuscatedStringHandler.obfuscateString("/");
    private static final String SHOP_NAME = ProceduralShopMenuConfig.SHOP_NAME + SHOP_KEY;

    public static void shopInitializer(Player player) {

        if (!ConfigValues.economyConfig.getBoolean(EconomySettingsConfig.ENABLE_ECONOMY)) return;
        BuyOrSellMenu.constructBuyOrSellMenu(player, BuyOrSellMenuConfig.BUY_PROCEDURAL_ITEM);

    }

    public static void shopConstructor(Player player) {

        Inventory shopInventory = Bukkit.createInventory(player, 54, SHOP_NAME);
        populateShop(shopInventory);
        player.openInventory(shopInventory);

    }

    private static void populateShop(Inventory shopInventory) {

        shopInventory.setItem(ProceduralShopMenuConfig.REROLL_SLOT, ProceduralShopMenuConfig.REROLL_ITEM);
        shopContents(shopInventory);

    }

    private static List<Integer> validSlots = ProceduralShopMenuConfig.STORE_SLOTS;

    private static void shopContents(Inventory shopInventory) {

        Random random = new Random();

        for (int i : validSlots) {

            int balancedMax = ProceduralShopMenuConfig.MAX_TIER - ProceduralShopMenuConfig.MIN_TIER;
            int balancedMin = ProceduralShopMenuConfig.MIN_TIER;

            int randomTier = random.nextInt(balancedMax) + balancedMin;

            shopInventory.setItem(i, ItemConstructor.constructItem(randomTier, null));

        }

    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {

        if (!event.getView().getTitle().equals(SHOP_NAME)) return;
        if (event.getClickedInventory() == null || !event.getClickedInventory().getType().equals(InventoryType.CHEST)) {
            event.setCancelled(true);
            return;
        }
        event.setCancelled(true);
        if (!SharedShopElements.inventoryNullPointerPreventer(event)) return;

        //reroll loot button
        if (event.getCurrentItem().equals(ProceduralShopMenuConfig.REROLL_ITEM)) {
            populateShop(event.getInventory());
            event.setCancelled(true);
            return;
        }

        if (!ObfuscatedSignatureLoreData.obfuscatedSignatureDetector(event.getCurrentItem())) {
            event.setCancelled(true);
            return;
        }

        Player player = (Player) event.getWhoClicked();
        ItemStack itemStack = event.getCurrentItem();
        String itemDisplayName = itemStack.getItemMeta().getDisplayName();

        double itemValue = ItemWorthCalculator.determineItemWorth(itemStack);

        boolean inventoryHasFreeSlots = false;
        for (ItemStack iteratedStack : player.getInventory().getStorageContents())
            if (iteratedStack == null) {
                inventoryHasFreeSlots = true;
                break;
            }

        //These slots are for buying items
        if (event.getView().getTitle().equalsIgnoreCase(SHOP_NAME) && validSlots.contains(event.getSlot())) {

            if (!inventoryHasFreeSlots) {

                player.sendMessage(ProceduralShopMenuConfig.MESSAGE_FULL_INVENTORY);
                player.closeInventory();

            } else if (EconomyHandler.checkCurrency(UUIDFilter.guessUUI(player.getName())) >= itemValue) {
                //player has enough money
                EconomyHandler.subtractCurrency(UUIDFilter.guessUUI(player.getName()), itemValue);
                player.getInventory().addItem(itemStack);
                populateShop(event.getInventory());

                SharedShopElements.buyMessage(player, itemDisplayName, itemValue);

            } else {

                player.closeInventory();
                SharedShopElements.insufficientFundsMessage(player, itemValue);

            }

        }

    }


}
