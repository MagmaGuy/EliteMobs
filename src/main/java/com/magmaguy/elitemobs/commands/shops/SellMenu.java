package com.magmaguy.elitemobs.commands.shops;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.api.EliteMobsItemDetector;
import com.magmaguy.elitemobs.commands.guiconfig.SignatureItem;
import com.magmaguy.elitemobs.config.ConfigValues;
import com.magmaguy.elitemobs.config.DefaultConfig;
import com.magmaguy.elitemobs.config.EconomySettingsConfig;
import com.magmaguy.elitemobs.config.TranslationConfig;
import com.magmaguy.elitemobs.economy.EconomyHandler;
import com.magmaguy.elitemobs.economy.UUIDFilter;
import com.magmaguy.elitemobs.items.ItemWorthCalculator;
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
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;

public class SellMenu implements Listener {

    private static final String SHOP_KEY = ObfuscatedStringHandler.obfuscateString("///");
    public static final String SHOP_NAME = ConfigValues.economyConfig.getString(EconomySettingsConfig.SELL_SHOP_NAME) + SHOP_KEY;

    private static List<Integer> validSlots = (List<Integer>) ConfigValues.economyConfig.getList(EconomySettingsConfig.SELL_SHOP_VALID_SLOTS);

    /**
     * Creates a menu for selling elitemobs items. Only special Elite Mob items can be sold here.
     *
     * @param player Player for whom the inventory will be created
     */
    public void constructSellMenu(Player player) {

        Inventory sellInventory = Bukkit.createInventory(player, 54, SHOP_NAME);

        ItemStack advice;
        if (ConfigValues.defaultConfig.getBoolean(DefaultConfig.SKULL_SIGNATURE_ITEM)) {
            advice = new ItemStack(Material.LEGACY_SKULL_ITEM, 1, (short) 3);
            SkullMeta arrowLeftSkullMeta = (SkullMeta) advice.getItemMeta();
            arrowLeftSkullMeta.setOwner("MHF_Exclamation");
            arrowLeftSkullMeta.setDisplayName(ChatColorConverter.convert(ConfigValues.economyConfig.getString(EconomySettingsConfig.SELL_SHOP_INFO_NAME)));
            arrowLeftSkullMeta.setLore(ChatColorConverter.convert(ConfigValues.economyConfig.getStringList(EconomySettingsConfig.SELL_SHOP_INFO_LORE)));
            advice.setItemMeta(arrowLeftSkullMeta);
        } else {
            advice = new ItemStack(Material.PAPER);
            ItemMeta itemMeta = advice.getItemMeta();
            itemMeta.setDisplayName(ChatColorConverter.convert(ConfigValues.economyConfig.getString(EconomySettingsConfig.SELL_SHOP_INFO_NAME)));
            itemMeta.setLore(ChatColorConverter.convert(ConfigValues.economyConfig.getStringList(EconomySettingsConfig.SELL_SHOP_INFO_LORE)));
            advice.setItemMeta(itemMeta);
        }


        for (int i = 0; i < 54; i++) {

            if (i == 4) {
                sellInventory.setItem(i, SignatureItem.SIGNATURE_ITEMSTACK);
                continue;
            }

            if (i == ConfigValues.economyConfig.getInt(EconomySettingsConfig.SELL_SHOP_INFO_SLOT)) {

                sellInventory.setItem(i, advice);
                continue;

            }

            if (i == ConfigValues.economyConfig.getInt(EconomySettingsConfig.SELL_SHOP_CANCEL_SLOT)) {

                sellInventory.setItem(i, ItemStackGenerator.generateItemStack(Material.BARRIER,
                        ChatColorConverter.convert(ConfigValues.economyConfig.getString(EconomySettingsConfig.SELL_SHOP_CANCEL_NAME)),
                        ChatColorConverter.convert(ConfigValues.economyConfig.getStringList(EconomySettingsConfig.SELL_SHOP_CANCEL_LORE))));
                continue;

            }

            if (i == ConfigValues.economyConfig.getInt(EconomySettingsConfig.SELL_SHOP_CONFIRM_SLOT)) {

                List<String> lore = new ArrayList<>();
                for (String string : ConfigValues.economyConfig.getStringList(EconomySettingsConfig.SELL_SHOP_CONFIRM_LORE))
                    lore.add(string
                            .replace("$currency_amount", 0 + "")
                            .replace("$currency_name", ConfigValues.economyConfig.getString(EconomySettingsConfig.CURRENCY_NAME)));

                sellInventory.setItem(i, ItemStackGenerator.generateItemStack(Material.EMERALD,
                        ChatColorConverter.convert(ConfigValues.economyConfig.getString(EconomySettingsConfig.SELL_SHOP_CONFIRM_NAME)),
                        ChatColorConverter.convert(lore)));
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
            if (!event.getCurrentItem().getItemMeta().hasDisplayName()
                    || event.getCurrentItem().getItemMeta().getDisplayName().equals(SignatureItem.SIGNATURE_ITEMSTACK.getItemMeta().getDisplayName()))
                return;


            //sell
            if (event.getSlot() == ConfigValues.economyConfig.getInt(EconomySettingsConfig.SELL_SHOP_CONFIRM_SLOT)) {
                int positionCounter = 0;
                for (ItemStack itemStack : event.getClickedInventory()) {
                    if (validSlots.contains(positionCounter))
                        if (itemStack != null) {
                            double amountDeduced = ItemWorthCalculator.determineResaleWorth(itemStack) * itemStack.getAmount();
                            EconomyHandler.addCurrency(UUIDFilter.guessUUI(event.getWhoClicked().getName()), amountDeduced);
                            event.getWhoClicked().sendMessage(
                                    ChatColorConverter.convert(
                                            ConfigValues.translationConfig.getString(TranslationConfig.SHOP_SELL_MESSAGE)
                                                    .replace("$item_name", itemStack.getItemMeta().getDisplayName())
                                                    .replace("$currency_amount", amountDeduced + "")
                                                    .replace("$currency_name", ConfigValues.economyConfig.getString(EconomySettingsConfig.CURRENCY_NAME))));
                            event.getClickedInventory().setItem(positionCounter, new ItemStack(Material.AIR));
                        }
                    positionCounter++;
                }
                event.getWhoClicked().sendMessage(
                        ChatColorConverter.convert(
                                ConfigValues.translationConfig.getString(TranslationConfig.SHOP_CURRENT_BALANCE)
                                        .replace("$currency_amount", EconomyHandler.checkCurrency(UUIDFilter.guessUUI(event.getWhoClicked().getName())) + "")
                                        .replace("$currency_name", ConfigValues.economyConfig.getString(EconomySettingsConfig.CURRENCY_NAME))));
                return;
            }

            //cancel
            if (event.getSlot() == ConfigValues.economyConfig.getInt(EconomySettingsConfig.SELL_SHOP_CANCEL_SLOT)) {
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
                            itemWorth += ItemWorthCalculator.determineResaleWorth(itemStack) * itemStack.getAmount();
                    positionCounter++;
                }

                List<String> lore = new ArrayList<>();
                for (String string : ConfigValues.economyConfig.getStringList(EconomySettingsConfig.SELL_SHOP_CONFIRM_LORE))
                    lore.add(string
                            .replace("$currency_amount", itemWorth + "")
                            .replace("$currency_name", ConfigValues.economyConfig.getString(EconomySettingsConfig.CURRENCY_NAME)));


                event.getInventory().setItem(
                        ConfigValues.economyConfig.getInt(EconomySettingsConfig.SELL_SHOP_CONFIRM_SLOT),
                        ItemStackGenerator.generateItemStack(Material.EMERALD,
                                ChatColorConverter.convert(ConfigValues.economyConfig.getString(EconomySettingsConfig.SELL_SHOP_CONFIRM_NAME)),
                                ChatColorConverter.convert(lore)));
                return;
            }

        }

        if (!EliteMobsItemDetector.isEliteMobsItem(event.getCurrentItem())) {
            event.getWhoClicked().sendMessage(ChatColorConverter.convert(ConfigValues.translationConfig.getString(TranslationConfig.SHOP_SALE_INSTRUCTIONS)));
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
                    itemWorth += ItemWorthCalculator.determineResaleWorth(itemStack) * itemStack.getAmount();
            positionCounter++;
        }

        List<String> lore = new ArrayList<>();
        for (String string : ConfigValues.economyConfig.getStringList(EconomySettingsConfig.SELL_SHOP_CONFIRM_LORE))
            lore.add(string
                    .replace("$currency_amount", itemWorth + "")
                    .replace("$currency_name", ConfigValues.economyConfig.getString(EconomySettingsConfig.CURRENCY_NAME)));


        event.getInventory().setItem(
                ConfigValues.economyConfig.getInt(EconomySettingsConfig.SELL_SHOP_CONFIRM_SLOT),
                ItemStackGenerator.generateItemStack(Material.EMERALD,
                        ChatColorConverter.convert(ConfigValues.economyConfig.getString(EconomySettingsConfig.SELL_SHOP_CONFIRM_NAME)),
                        ChatColorConverter.convert(lore)));

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
