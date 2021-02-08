package com.magmaguy.elitemobs.menus;

import com.magmaguy.elitemobs.items.EliteItemLore;
import com.magmaguy.elitemobs.items.ItemTagger;
import com.magmaguy.elitemobs.items.ItemWorthCalculator;
import com.magmaguy.elitemobs.config.EconomySettingsConfig;
import com.magmaguy.elitemobs.config.menus.premade.BuyOrSellMenuConfig;
import com.magmaguy.elitemobs.config.menus.premade.CustomShopMenuConfig;
import com.magmaguy.elitemobs.economy.EconomyHandler;
import com.magmaguy.elitemobs.items.customenchantments.SoulbindEnchantment;
import com.magmaguy.elitemobs.items.customitems.CustomItem;
import com.magmaguy.elitemobs.utils.ObfuscatedStringHandler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

/**
 * Created by MagmaGuy on 20/06/2017.
 */
public class CustomShopMenu implements Listener {

    /**
     * Fixed shop name, used to track if an event is happening within it
     */
    private static final String SHOP_KEY = ObfuscatedStringHandler.obfuscateString("//");
    public static final String SHOP_NAME = CustomShopMenuConfig.shopName + SHOP_KEY;

    /**
     * Creates a new instance of a BuyOrSellMenu
     *
     * @param player Player for whom the new menu will show up
     */
    public static void customShopInitializer(Player player) {

        if (!EconomySettingsConfig.enableEconomy) return;
        BuyOrSellMenu.constructBuyOrSellMenu(player, BuyOrSellMenuConfig.BUY_CUSTOM_ITEM);

    }

    /**
     * Creates a new instance of a custom shop
     *
     * @param player Player for whom the new menu will show up
     */
    public static void customShopConstructor(Player player) {

        Inventory shopInventory = Bukkit.createInventory(player, 54, SHOP_NAME);
        populateShop(shopInventory, player);
        player.openInventory(shopInventory);

    }

    /**
     * Preliminary shop fill
     *
     * @param shopInventory Inventory to be filled
     */
    private static void populateShop(Inventory shopInventory, Player player) {

        shopInventory.setItem(CustomShopMenuConfig.rerollSlot, CustomShopMenuConfig.rerollItem);
        shopContents(shopInventory, player);

    }

    /**
     * Fills with the items to be sold in the shop
     *
     * @param shopInventory Inventory to be filled
     */
    private static void shopContents(Inventory shopInventory, Player player) {

        //Anything after 8 is populated
        Random random = new Random();

        for (int i : CustomShopMenuConfig.storeSlots) {

            int itemEntryIndex = random.nextInt(CustomItem.getCustomItemStackShopList().size());

            ItemStack itemStack = CustomItem.getCustomItemStackShopList().get(itemEntryIndex).clone();
            SoulbindEnchantment.addEnchantment(itemStack, player);
            new EliteItemLore(itemStack, true);

            shopInventory.setItem(i, itemStack);

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
        if (event.getCurrentItem().getItemMeta().getDisplayName().equals(CustomShopMenuConfig.rerollItem.getItemMeta().getDisplayName())) {
            populateShop(event.getInventory(), Bukkit.getPlayer(event.getWhoClicked().getUniqueId()));
            event.setCancelled(true);
            return;
        }

        if (!ItemTagger.isEliteItem(event.getCurrentItem())) {
            event.setCancelled(true);
            return;
        }

        Player player = (Player) event.getWhoClicked();
        ItemStack itemStack = event.getCurrentItem();
        String itemDisplayName = event.getCurrentItem().getItemMeta().getDisplayName();

        double itemValue = ItemWorthCalculator.determineItemWorth(itemStack, player);

        //These slots are for buying items
        if (CustomShopMenuConfig.storeSlots.contains(event.getSlot()) && event.getView().getTitle().equalsIgnoreCase(SHOP_NAME)) {

            boolean inventoryHasFreeSlots = false;
            for (ItemStack iteratedStack : player.getInventory())
                if (iteratedStack == null) {
                    inventoryHasFreeSlots = true;
                    break;
                }

            if (!inventoryHasFreeSlots) {

                player.sendMessage(CustomShopMenuConfig.messageFullInventory);
                player.closeInventory();

            } else if (EconomyHandler.checkCurrency(player.getUniqueId()) >= itemValue) {
                //player has enough money
                EconomyHandler.subtractCurrency(player.getUniqueId(), itemValue);
                new EliteItemLore(itemStack, false);
                player.getInventory().addItem(itemStack);
                populateShop(event.getInventory(), Bukkit.getPlayer(event.getWhoClicked().getUniqueId()));

                SharedShopElements.buyMessage(player, itemDisplayName, itemValue);

            } else {

                player.closeInventory();
                SharedShopElements.insufficientFundsMessage(player, itemValue);

            }

        }

    }

}
