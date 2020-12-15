package com.magmaguy.elitemobs.commands.shops;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.api.EliteMobsItemDetector;
import com.magmaguy.elitemobs.config.ConfigValues;
import com.magmaguy.elitemobs.config.EconomySettingsConfig;
import com.magmaguy.elitemobs.config.TranslationConfig;
import com.magmaguy.elitemobs.config.menus.premade.SellMenuConfig;
import com.magmaguy.elitemobs.economy.EconomyHandler;
import com.magmaguy.elitemobs.items.ItemWorthCalculator;
import com.magmaguy.elitemobs.items.customenchantments.SoulbindEnchantment;
import com.magmaguy.elitemobs.utils.ItemStackGenerator;
import com.magmaguy.elitemobs.utils.ObfuscatedStringHandler;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class SellMenu implements Listener {

    private static final String SHOP_KEY = ObfuscatedStringHandler.obfuscateString("///");
    public static final String SHOP_NAME = SellMenuConfig.shopName + SHOP_KEY;

    private static final List<Integer> validSlots = SellMenuConfig.storeSlots;

    /**
     * Creates a menu for selling elitemobs items. Only special Elite Mob items can be sold here.
     *
     * @param player Player for whom the inventory will be created
     */
    public void constructSellMenu(Player player) {

        Inventory sellInventory = Bukkit.createInventory(player, 54, SHOP_NAME);

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

            if (validSlots.contains(i)) {
                continue;
            }

            sellInventory.setItem(i, ItemStackGenerator.generateItemStack(Material.GLASS_PANE));

        }

        player.openInventory(sellInventory);

    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {

        if (!event.getView().getTitle().equals(SHOP_NAME)) return;
        event.setCancelled(true);
        if (!SharedShopElements.sellMenuNullPointPreventer(event)) return;

        if (!event.getClickedInventory().getType().equals(InventoryType.PLAYER)) {

            //Signature item
            if (event.getCurrentItem().equals(SellMenuConfig.infoButton))
                return;

            //sell
            if (event.getSlot() == SellMenuConfig.confirmSlot) {
                int positionCounter = 0;
                for (ItemStack itemStack : event.getClickedInventory()) {
                    if (validSlots.contains(positionCounter))
                        if (itemStack != null) {
                            double amountDeduced = ItemWorthCalculator.determineResaleWorth(itemStack, Bukkit.getPlayer(event.getWhoClicked().getUniqueId())) * itemStack.getAmount();
                            EconomyHandler.addCurrency(event.getWhoClicked().getUniqueId(), amountDeduced);
                            event.getWhoClicked().sendMessage(
                                    ChatColorConverter.convert(
                                            ConfigValues.translationConfig.getString(TranslationConfig.SHOP_SELL_MESSAGE)
                                                    .replace("$item_name", itemStack.getItemMeta().getDisplayName())
                                                    .replace("$currency_amount", amountDeduced + "")
                                                    .replace("$currency_name", EconomySettingsConfig.currencyName)));
                            event.getClickedInventory().setItem(positionCounter, new ItemStack(Material.AIR));
                        }
                    positionCounter++;
                }
                event.getWhoClicked().sendMessage(
                        ChatColorConverter.convert(
                                ConfigValues.translationConfig.getString(TranslationConfig.SHOP_CURRENT_BALANCE)
                                        .replace("$currency_amount", EconomyHandler.checkCurrency(event.getWhoClicked().getUniqueId()) + "")
                                        .replace("$currency_name", EconomySettingsConfig.currencyName)));
                return;
            }

            //cancel
            if (event.getSlot() == SellMenuConfig.cancelSlot) {
                int positionCounter = 0;
                for (ItemStack itemStack : event.getClickedInventory()) {
                    if (validSlots.contains(positionCounter))
                        if (itemStack != null) {
                            event.getWhoClicked().getInventory().addItem(itemStack);
                            event.getClickedInventory().setItem(positionCounter, new ItemStack(Material.AIR));
                        }
                    positionCounter++;
                }
                event.getWhoClicked().closeInventory();
                return;
            }

            if (!validSlots.contains(event.getSlot())) return;


            if (validSlots.contains(event.getSlot())) {
                event.getWhoClicked().getInventory().addItem(event.getCurrentItem());
                event.getCurrentItem().setAmount(0);
                //Update worth of things to be sold
                double itemWorth = 0;
                int positionCounter = 0;
                for (ItemStack itemStack : event.getInventory()) {
                    if (validSlots.contains(positionCounter))
                        if (itemStack != null)
                            itemWorth += ItemWorthCalculator.determineResaleWorth(itemStack, Bukkit.getPlayer(event.getWhoClicked().getUniqueId())) * itemStack.getAmount();
                    positionCounter++;
                }

                ItemStack clonedConfirmButton = SellMenuConfig.confirmButton.clone();

                List<String> lore = new ArrayList<>();
                for (String string : clonedConfirmButton.getItemMeta().getLore())
                    lore.add(string
                            .replace("$currency_amount", itemWorth + "")
                            .replace("$currency_name", EconomySettingsConfig.currencyName));


                event.getInventory().setItem(SellMenuConfig.confirmSlot, clonedConfirmButton);
                return;
            }

        }

        if (!EliteMobsItemDetector.isEliteMobsItem(event.getCurrentItem())) {
            event.getWhoClicked().sendMessage(ChatColorConverter.convert(ConfigValues.translationConfig.getString(TranslationConfig.SHOP_SALE_INSTRUCTIONS)));
            return;
        }

        if (!SoulbindEnchantment.isValidSoulbindUser(event.getCurrentItem().getItemMeta(), (Player) event.getWhoClicked())) {
            event.getWhoClicked().sendMessage(ChatColorConverter.convert(ConfigValues.translationConfig.getString(TranslationConfig.SHOP_SALE_OTHERS_ITEMS)));
            return;
        }

        boolean inventoryIsFull = true;
        for (int i : validSlots)
            if (event.getInventory().getItem(i) == null) {
                inventoryIsFull = false;
                break;
            }
        if (inventoryIsFull) return;
        event.getInventory().addItem(event.getCurrentItem());
        event.getCurrentItem().setAmount(0);

        //Update worth of things to be sold
        double itemWorth = 0;
        int positionCounter = 0;
        for (ItemStack itemStack : event.getInventory()) {
            if (validSlots.contains(positionCounter))
                if (itemStack != null)
                    itemWorth += ItemWorthCalculator.determineResaleWorth(itemStack, Bukkit.getPlayer(event.getWhoClicked().getUniqueId())) * itemStack.getAmount();
            positionCounter++;
        }

        ItemStack clonedConfirmButton = SellMenuConfig.confirmButton.clone();

        List<String> lore = new ArrayList<>();
        for (String string : clonedConfirmButton.getItemMeta().getLore())
            lore.add(string
                    .replace("$currency_amount", itemWorth + "")
                    .replace("$currency_name", EconomySettingsConfig.currencyName));

        ItemMeta clonedMeta = clonedConfirmButton.getItemMeta();
        clonedMeta.setLore(lore);
        clonedConfirmButton.setItemMeta(clonedMeta);

        event.getInventory().setItem(SellMenuConfig.confirmSlot, clonedConfirmButton);

    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {

        if (!event.getView().getTitle().equals(SHOP_NAME)) return;

        int positionCounter = 0;
        for (ItemStack itemStack : event.getInventory()) {
            if (validSlots.contains(positionCounter))
                if (itemStack != null) {
                    event.getPlayer().getInventory().addItem(itemStack);
                }
            positionCounter++;
        }

    }

}
