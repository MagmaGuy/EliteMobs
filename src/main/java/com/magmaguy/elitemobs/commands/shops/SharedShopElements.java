package com.magmaguy.elitemobs.commands.shops;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.commands.guiconfig.SignatureItem;
import com.magmaguy.elitemobs.config.ConfigValues;
import com.magmaguy.elitemobs.config.EconomySettingsConfig;
import com.magmaguy.elitemobs.config.TranslationConfig;
import com.magmaguy.elitemobs.economy.EconomyHandler;
import com.magmaguy.elitemobs.economy.UUIDFilter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class SharedShopElements {

    public static void shopHeader(Inventory shopInventory) {

        ItemStack signature = SignatureItem.SIGNATURE_ITEMSTACK;

        shopInventory.setItem(ConfigValues.economyConfig.getInt(EconomySettingsConfig.SIGNATURE_ITEM_LOCATION_SHOPS), signature);

    }

    public static boolean inventoryNullPointerPreventer(InventoryClickEvent event) {

        if (!sellMenuNullPointPreventer(event)) return false;
        return event.getCurrentItem().getItemMeta().hasLore();

    }

    public static boolean sellMenuNullPointPreventer(InventoryClickEvent event) {

        //Check if current item is valid
        if (event.getCurrentItem() == null) return false;
        if (event.getCurrentItem().getType().equals(Material.AIR)) return false;
        return event.getCurrentItem().getItemMeta() != null;

    }

    public static boolean sellInventoryNullPointerPreventer(InventoryClickEvent event) {

        //Check if economy is enabled
        if (!ConfigValues.economyConfig.getBoolean(EconomySettingsConfig.ENABLE_ECONOMY)) return false;
        //Check if current item is valid
        if (event.getCurrentItem() == null) return false;
        return !event.getCurrentItem().getType().equals(Material.AIR);
    }

    public static void buyMessage(Player player, String itemDisplayName, double itemValue) {

        new BukkitRunnable() {

            @Override
            public void run() {

                player.sendMessage(
                        ChatColorConverter.convert(
                                ConfigValues.translationConfig.getString(TranslationConfig.SHOP_BUY_MESSAGE)
                                        .replace("$item_name", itemDisplayName)
                                        .replace("$item_value", itemValue + "")
                                        .replace("$currency_name", ConfigValues.economyConfig.getString(EconomySettingsConfig.CURRENCY_NAME))));

                player.sendMessage(
                        ChatColorConverter.convert(
                                ConfigValues.translationConfig.getString(TranslationConfig.SHOP_CURRENT_BALANCE)
                                        .replace("$currency_amount", EconomyHandler.checkCurrency(UUIDFilter.guessUUI(player.getName())) + "")
                                        .replace("$currency_name", ConfigValues.economyConfig.getString(EconomySettingsConfig.CURRENCY_NAME))));


            }


        }.runTaskLater(Bukkit.getPluginManager().getPlugin(MetadataHandler.ELITE_MOBS), 2);

    }

    public static void insufficientFundsMessage(Player player, double itemValue) {

        new BukkitRunnable() {

            @Override
            public void run() {

                player.sendMessage(
                        ChatColorConverter.convert(
                                ConfigValues.translationConfig.getString(TranslationConfig.SHOP_INSUFFICIENT_FUNDS_MESSAGE)
                                        .replace("$currency_name", ConfigValues.economyConfig.getString(EconomySettingsConfig.CURRENCY_NAME))));

                player.sendMessage(
                        ChatColorConverter.convert(
                                ConfigValues.translationConfig.getString(TranslationConfig.SHOP_CURRENT_BALANCE)
                                        .replace("$currency_amount", EconomyHandler.checkCurrency(UUIDFilter.guessUUI(player.getName())) + "")
                                        .replace("$currency_name", ConfigValues.economyConfig.getString(EconomySettingsConfig.CURRENCY_NAME))));

                player.sendMessage(
                        ChatColorConverter.convert(
                                ConfigValues.translationConfig.getString(TranslationConfig.SHOP_ITEM_PRICE)
                                        .replace("$item_value", itemValue + "")
                                        .replace("$currency_name", ConfigValues.economyConfig.getString(EconomySettingsConfig.CURRENCY_NAME))));

            }


        }.runTaskLater(Bukkit.getPluginManager().getPlugin(MetadataHandler.ELITE_MOBS), 2);

        player.closeInventory();

    }


}
