package com.magmaguy.elitemobs.commands.shops;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.commands.guiconfig.SignatureItem;
import com.magmaguy.elitemobs.config.ConfigValues;
import com.magmaguy.elitemobs.config.EconomySettingsConfig;
import com.magmaguy.elitemobs.config.TranslationConfig;
import com.magmaguy.elitemobs.economy.EconomyHandler;
import com.magmaguy.elitemobs.economy.UUIDFilter;
import com.magmaguy.elitemobs.items.ItemWorthCalculator;
import com.magmaguy.elitemobs.items.ObfuscatedSignatureLoreData;
import com.magmaguy.elitemobs.items.customitems.CustomItem;
import com.magmaguy.elitemobs.utils.ObfuscatedStringHandler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Random;

/**
 * Created by MagmaGuy on 20/06/2017.
 */
public class CustomShopHandler implements Listener {

    /**
     * Fixed shop name, used to track if an event is happening within it
     */
    private static final String SHOP_KEY = ObfuscatedStringHandler.obfuscateString("//");
    public static final String SHOP_NAME = ConfigValues.economyConfig.getString(EconomySettingsConfig.CUSTOM_SHOP_NAME) + SHOP_KEY;

    /**
     * Generates a new custom item shop menu for a player
     *
     * @param player Player for whom the shop menu will pop up
     */
    public static void customShopInitializer(Player player) {

        if (!ConfigValues.economyConfig.getBoolean(EconomySettingsConfig.ENABLE_ECONOMY)) return;
        BuyOrSellMenu.constructBuyOrSellMenu(player, ChatColorConverter.convert(ConfigValues.translationConfig.getString(TranslationConfig.BUY_OR_SELL_CUSTOM_ITEMS)));

    }

    public static void customShopConstructor(Player player) {

        Inventory shopInventory = Bukkit.createInventory(player, 54, SHOP_NAME);
        populateShop(shopInventory);
        player.openInventory(shopInventory);

    }

    private static void populateShop(Inventory shopInventory) {

        SharedShopElements.shopHeader(shopInventory);
        shopContents(shopInventory);

    }

    private static List<Integer> validSlots = (List<Integer>) ConfigValues.economyConfig.getList(EconomySettingsConfig.CUSTOM_SHOP_VALID_SLOTS);

    private static void shopContents(Inventory shopInventory) {

        //Anything after 8 is populated
        Random random = new Random();

        for (int i : validSlots) {

            int itemEntryIndex = random.nextInt(CustomItem.getCustomItemStackList().size());

            shopInventory.setItem(i, CustomItem.getCustomItemStackList().get(itemEntryIndex));

        }

    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {

        if (!event.getView().getTitle().equals(SHOP_NAME)) return;
        event.setCancelled(true);
        if (!SharedShopElements.inventoryNullPointerPreventer(event)) return;

        //reroll loot button
        if (event.getCurrentItem().getItemMeta().getDisplayName().equals(SignatureItem.SIGNATURE_ITEMSTACK.getItemMeta().getDisplayName())) {

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
        String itemDisplayName = event.getCurrentItem().getItemMeta().getDisplayName();

        double itemValue = ItemWorthCalculator.determineItemWorth(itemStack);

        //These slots are for buying items
        if (validSlots.contains(event.getSlot()) && event.getView().getTitle().equalsIgnoreCase(SHOP_NAME)) {

            boolean inventoryHasFreeSlots = false;
            for (ItemStack iteratedStack : player.getInventory()) {

                if (iteratedStack == null) {

                    inventoryHasFreeSlots = true;
                    break;

                }

            }

            if (!inventoryHasFreeSlots) {

                player.sendMessage(ChatColorConverter.convert(ConfigValues.translationConfig.getString(TranslationConfig.INVENTORY_FULL_MESSAGE)));
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
