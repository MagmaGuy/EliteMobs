package com.magmaguy.elitemobs.menus;

import com.magmaguy.elitemobs.items.EliteItemLore;
import com.magmaguy.elitemobs.items.ItemTagger;
import com.magmaguy.elitemobs.items.ItemWorthCalculator;
import com.magmaguy.elitemobs.config.EconomySettingsConfig;
import com.magmaguy.elitemobs.config.menus.premade.BuyOrSellMenuConfig;
import com.magmaguy.elitemobs.config.menus.premade.ProceduralShopMenuConfig;
import com.magmaguy.elitemobs.economy.EconomyHandler;
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
    private static final String SHOP_NAME = ProceduralShopMenuConfig.shopName + SHOP_KEY;

    public static void shopInitializer(Player player) {

        if (!EconomySettingsConfig.enableEconomy) return;
        BuyOrSellMenu.constructBuyOrSellMenu(player, BuyOrSellMenuConfig.BUY_PROCEDURAL_ITEM);

    }

    public static void shopConstructor(Player player) {

        Inventory shopInventory = Bukkit.createInventory(player, 54, SHOP_NAME);
        populateShop(shopInventory, player);
        player.openInventory(shopInventory);

    }

    private static void populateShop(Inventory shopInventory, Player player) {

        shopInventory.setItem(ProceduralShopMenuConfig.rerollSlot, ProceduralShopMenuConfig.rerollItem);
        shopContents(shopInventory, player);

    }

    private static final List<Integer> validSlots = ProceduralShopMenuConfig.storeSlots;

    private static void shopContents(Inventory shopInventory, Player player) {

        Random random = new Random();

        for (int i : validSlots) {

            int balancedMax = ProceduralShopMenuConfig.maxTier - ProceduralShopMenuConfig.minTier;
            int balancedMin = ProceduralShopMenuConfig.minTier;

            int randomTier = random.nextInt(balancedMax) + balancedMin;

            ItemStack itemStack = ItemConstructor.constructItem(randomTier, null, player, true);
            //SoulbindEnchantment.addEnchantment(itemStack, player);
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
        if (event.getCurrentItem().getItemMeta().getDisplayName().equals(ProceduralShopMenuConfig.rerollItem.getItemMeta().getDisplayName())) {
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
        String itemDisplayName = itemStack.getItemMeta().getDisplayName();

        double itemValue = ItemWorthCalculator.determineItemWorth(itemStack, player);

        boolean inventoryHasFreeSlots = false;
        for (ItemStack iteratedStack : player.getInventory().getStorageContents())
            if (iteratedStack == null) {
                inventoryHasFreeSlots = true;
                break;
            }

        //These slots are for buying items
        if (event.getView().getTitle().equalsIgnoreCase(SHOP_NAME) && validSlots.contains(event.getSlot())) {

            if (!inventoryHasFreeSlots) {

                player.sendMessage(ProceduralShopMenuConfig.messageFullInventory);
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
