package com.magmaguy.elitemobs.menus;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.api.utils.EliteItemManager;
import com.magmaguy.elitemobs.config.DefaultConfig;
import com.magmaguy.elitemobs.config.EconomySettingsConfig;
import com.magmaguy.elitemobs.config.ResourcePackDataConfig;
import com.magmaguy.elitemobs.config.TranslationConfig;
import com.magmaguy.elitemobs.config.menus.premade.SellMenuConfig;
import com.magmaguy.elitemobs.economy.EconomyHandler;
import com.magmaguy.elitemobs.items.ItemWorthCalculator;
import com.magmaguy.elitemobs.items.customenchantments.SoulbindEnchantment;
import com.magmaguy.elitemobs.utils.ItemStackGenerator;
import com.magmaguy.elitemobs.utils.Round;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SellMenu extends EliteMenu implements Listener {

    private static final List<Integer> validSlots = SellMenuConfig.storeSlots;

    public static Set<Inventory> inventories = new HashSet<>();

    private static double calculateShopValue(Inventory shopInventory, Player player) {
        double itemWorth = 0;
        for (Integer validSlot : validSlots) {
            ItemStack itemStack = shopInventory.getItem(validSlot);
            if (itemStack == null) continue;
            itemWorth += (ItemWorthCalculator.determineResaleWorth(itemStack, player) * itemStack.getAmount());
        }
        return itemWorth;
    }

    private static ItemStack updateConfirmButton(double itemWorth) {
        ItemStack clonedConfirmButton = SellMenuConfig.confirmButton.clone();

        List<String> lore = new ArrayList<>();
        for (String string : clonedConfirmButton.getItemMeta().getLore())
            lore.add(string
                    .replace("$currency_amount", itemWorth + "")
                    .replace("$currency_name", EconomySettingsConfig.getCurrencyName()));

        ItemMeta clonedMeta = clonedConfirmButton.getItemMeta();
        clonedMeta.setLore(lore);
        clonedConfirmButton.setItemMeta(clonedMeta);
        return clonedConfirmButton;
    }

    /**
     * Creates a menu for selling elitemobs items. Only special Elite Mob items can be sold here.
     *
     * @param player Player for whom the inventory will be created
     */
    public void constructSellMenu(Player player) {

        String menuName = SellMenuConfig.shopName;
        if (ResourcePackDataConfig.isDisplayCustomMenuUnicodes())
            menuName = ChatColor.WHITE + "\uF801\uDB80\uDC5B\uF805          " + menuName;

        Inventory sellInventory = Bukkit.createInventory(player, 54, menuName);

        for (int i = 0; i < 54; i++) {

            if (i == SellMenuConfig.infoSlot) {
                sellInventory.setItem(i, SellMenuConfig.infoButton);
                continue;
            }

            if (i == SellMenuConfig.cancelSlot) {
                sellInventory.setItem(i, SellMenuConfig.cancelButton);
                continue;
            }

            if (i == SellMenuConfig.confirmSlot) {

                ItemStack clonedConfirmButton = SellMenuConfig.confirmButton.clone();

                List<String> lore = new ArrayList<>();
                for (String string : SellMenuConfig.confirmButton.getItemMeta().getLore())
                    lore.add(string
                            .replace("$currency_amount", 0 + "")
                            .replace("$currency_name", EconomySettingsConfig.getCurrencyName()));
                SellMenuConfig.confirmButton.getItemMeta().setLore(lore);
                ItemMeta clonedMeta = clonedConfirmButton.getItemMeta();
                clonedMeta.setLore(lore);
                clonedConfirmButton.setItemMeta(clonedMeta);
                sellInventory.setItem(i, clonedConfirmButton);
                continue;

            }

            if (validSlots.contains(i))
                continue;

            if (DefaultConfig.isUseGlassToFillMenuEmptySpace())
                sellInventory.setItem(i, ItemStackGenerator.generateItemStack(Material.GLASS_PANE));

        }

        player.openInventory(sellInventory);
        createEliteMenu(sellInventory, inventories);

    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {

        if (!isEliteMenu(event, inventories)) return;
        event.setCancelled(true);

        Player player = (Player) event.getWhoClicked();
        ItemStack currentItem = event.getCurrentItem();
        Inventory shopInventory = event.getView().getTopInventory();
        Inventory playerInventory = event.getView().getBottomInventory();

        if (isBottomMenu(event)) {
            //CASE: If the player clicked on something in their inventory to put it on the shop

            //Check if it's an elitemobs item. The soulbind check only says if the player would be able to pick it up, and vanilla items can be picked up
            if (!EliteItemManager.isEliteMobsItem(event.getCurrentItem())) {
                event.getWhoClicked().sendMessage(ChatColorConverter.convert(TranslationConfig.getShopSaleInstructions()));
                return;
            }

            //If the item isn't soulbound to the player, it can't be sold by that player
            if (!SoulbindEnchantment.isValidSoulbindUser(currentItem.getItemMeta(), player)) {
                player.sendMessage(ChatColorConverter.convert(TranslationConfig.getShopSaleOthersItems()));
                return;
            }

            //If the shop is full, don't let the player put stuff in it
            int firstEmptySlot = -1;
            for (int i : validSlots)
                if (shopInventory.getItem(i) == null) {
                    firstEmptySlot = i;
                    break;
                }
            if (firstEmptySlot == -1) return;

            //Do transfer
            shopInventory.setItem(firstEmptySlot, currentItem);
            playerInventory.clear(event.getSlot());

            //Update worth of things to be sold, now using cached prices
            event.getInventory().setItem(SellMenuConfig.confirmSlot, updateConfirmButton(calculateShopValue(shopInventory, player)));

        } else {
            //CASE: Player clicked on the shop

            //Signature item, does nothing
            if (currentItem.equals(SellMenuConfig.infoButton))
                return;

            //sell items in shop
            if (event.getSlot() == SellMenuConfig.confirmSlot) {

                int amount = (int) validSlots.stream().filter(validSlot -> shopInventory.getItem(validSlot) != null).count();
                double totalItemValue = 0;

                for (Integer validSlot : validSlots) {
                    ItemStack itemStack = shopInventory.getItem(validSlot);
                    if (itemStack == null)
                        continue;
                    double itemValue = ItemWorthCalculator.determineResaleWorth(itemStack, player) * itemStack.getAmount();
                    EconomyHandler.addCurrency(player.getUniqueId(), itemValue);
                    totalItemValue += itemValue;

                    if (amount < 4)
                        player.sendMessage(ChatColorConverter.convert(
                                TranslationConfig.getShopSellMessage()
                                        .replace("$item_name", itemStack.getItemMeta().getDisplayName())
                                        .replace("$currency_amount", Round.twoDecimalPlaces(itemValue) + "")
                                        .replace("$currency_name", EconomySettingsConfig.getCurrencyName())));
                    shopInventory.clear(validSlot);
                }

                if (amount >= 3)
                    player.sendMessage(ChatColorConverter.convert(
                            TranslationConfig.getShopBatchSellMessage()
                                    .replace("$currency_amount", totalItemValue + "")
                                    .replace("$currency_name", EconomySettingsConfig.getCurrencyName())));

                player.sendMessage(ChatColorConverter.convert(
                        TranslationConfig.getShopCurrentBalance()
                                .replace("$currency_amount", EconomyHandler.checkCurrency(player.getUniqueId()) + "")
                                .replace("$currency_name", EconomySettingsConfig.getCurrencyName())));
                updateConfirmButton(0);
                return;
            }

            //cancel, transfer items back to player inv and exit
            if (event.getSlot() == SellMenuConfig.cancelSlot) {
                event.getWhoClicked().closeInventory();
                return;
            }

            //If player clicks on a border glass pane, do nothing
            if (!validSlots.contains(event.getSlot())) return;


            //If player clicks on one of the items already in the shop, return to their inventory
            playerInventory.addItem(event.getCurrentItem());
            shopInventory.clear(event.getSlot());

            event.getInventory().setItem(SellMenuConfig.confirmSlot, updateConfirmButton(calculateShopValue(shopInventory, player)));

        }
    }


    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (inventories.contains(event.getInventory())) {
            inventories.remove(event.getInventory());
            EliteMenu.cancel(event.getPlayer(), event.getView().getTopInventory(), event.getView().getBottomInventory(), SellMenuConfig.storeSlots);
        }
    }

}
