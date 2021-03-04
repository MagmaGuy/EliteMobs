package com.magmaguy.elitemobs.menus;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.api.EliteMobsItemDetector;
import com.magmaguy.elitemobs.items.ItemWorthCalculator;
import com.magmaguy.elitemobs.config.ConfigValues;
import com.magmaguy.elitemobs.config.EconomySettingsConfig;
import com.magmaguy.elitemobs.config.TranslationConfig;
import com.magmaguy.elitemobs.config.menus.premade.SellMenuConfig;
import com.magmaguy.elitemobs.economy.EconomyHandler;
import com.magmaguy.elitemobs.items.customenchantments.SoulbindEnchantment;
import com.magmaguy.elitemobs.utils.ItemStackGenerator;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SellMenu extends EliteMenu implements Listener {

    private static final List<Integer> validSlots = SellMenuConfig.storeSlots;

    public static HashMap<Player, Inventory> inventories = new HashMap<>();

    /**
     * Creates a menu for selling elitemobs items. Only special Elite Mob items can be sold here.
     *
     * @param player Player for whom the inventory will be created
     */
    public void constructSellMenu(Player player) {

        Inventory sellInventory = Bukkit.createInventory(player, 54, SellMenuConfig.shopName);

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
                            .replace("$currency_name", EconomySettingsConfig.currencyName));
                SellMenuConfig.confirmButton.getItemMeta().setLore(lore);
                ItemMeta clonedMeta = clonedConfirmButton.getItemMeta();
                clonedMeta.setLore(lore);
                clonedConfirmButton.setItemMeta(clonedMeta);
                sellInventory.setItem(i, clonedConfirmButton);
                continue;

            }

            if (validSlots.contains(i))
                continue;

            sellInventory.setItem(i, ItemStackGenerator.generateItemStack(Material.GLASS_PANE));

        }

        player.openInventory(sellInventory);
        createEliteMenu(player, sellInventory, inventories);

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
            if (!EliteMobsItemDetector.isEliteMobsItem(event.getCurrentItem())) {
                event.getWhoClicked().sendMessage(ChatColorConverter.convert(ConfigValues.translationConfig.getString(TranslationConfig.SHOP_SALE_INSTRUCTIONS)));
                return;
            }

            //If the item isn't soulbound to the player, it can't be sold by that player
            if (!SoulbindEnchantment.isValidSoulbindUser(currentItem.getItemMeta(), player)) {
                player.sendMessage(ChatColorConverter.convert(ConfigValues.translationConfig.getString(TranslationConfig.SHOP_SALE_OTHERS_ITEMS)));
                return;
            }

            //If the shop is full, don't let the player put stuff in it
            if (shopInventory.firstEmpty() == -1) return;

            //Do transfer
            shopInventory.addItem(currentItem);
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

                for (Integer validSlot : validSlots) {
                    ItemStack itemStack = shopInventory.getItem(validSlot);
                    if (itemStack == null)
                        continue;
                    double itemValue = ItemWorthCalculator.determineResaleWorth(itemStack, player) * itemStack.getAmount();
                    EconomyHandler.addCurrency(player.getUniqueId(), itemValue);

                    player.sendMessage(ChatColorConverter.convert(
                            ConfigValues.translationConfig.getString(TranslationConfig.SHOP_SELL_MESSAGE)
                                    .replace("$item_name", itemStack.getItemMeta().getDisplayName())
                                    .replace("$currency_amount", itemValue + "")
                                    .replace("$currency_name", EconomySettingsConfig.currencyName)));
                    shopInventory.clear(validSlot);
                }

                player.sendMessage(ChatColorConverter.convert(
                        ConfigValues.translationConfig.getString(TranslationConfig.SHOP_CURRENT_BALANCE)
                                .replace("$currency_amount", EconomyHandler.checkCurrency(player.getUniqueId()) + "")
                                .replace("$currency_name", EconomySettingsConfig.currencyName)));
                updateConfirmButton(0);
                return;
            }

            //cancel, transfer items back to player inv and exit
            if (event.getSlot() == SellMenuConfig.cancelSlot) {
                cancel(playerInventory, shopInventory);
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
                    .replace("$currency_name", EconomySettingsConfig.currencyName));

        ItemMeta clonedMeta = clonedConfirmButton.getItemMeta();
        clonedMeta.setLore(lore);
        clonedConfirmButton.setItemMeta(clonedMeta);
        return clonedConfirmButton;
    }

    private static void cancel(Inventory playerInventory, Inventory shopInventory) {
        for (Integer validSlot : validSlots) {
            ItemStack itemStack = shopInventory.getItem(validSlot);
            if (itemStack != null) {
                playerInventory.addItem(itemStack);
                shopInventory.remove(itemStack);
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!onInventoryClose(event, inventories)) return;
        cancel(event.getView().getBottomInventory(), event.getView().getTopInventory());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        onPlayerQuit(event, inventories);
    }

}
