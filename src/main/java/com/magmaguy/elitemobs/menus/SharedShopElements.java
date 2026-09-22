package com.magmaguy.elitemobs.menus;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.EconomySettingsConfig;
import com.magmaguy.elitemobs.economy.EconomyHandler;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class SharedShopElements {

    private static final java.util.Set<java.util.UUID> purchases = new java.util.HashSet<>();

    /** Prepare first, consume the provider's debit result, then deliver once. */
    public static boolean purchase(Player player, org.bukkit.inventory.ItemStack template, double price) {
        if (!purchases.add(player.getUniqueId())) return false;
        boolean paymentAttempted = false;
        boolean paid = false;
        org.bukkit.inventory.ItemStack delivery = null;
        try {
            delivery = template.clone();
            if (com.magmaguy.elitemobs.items.ItemTagger.isEliteItem(delivery))
                new com.magmaguy.elitemobs.items.EliteItemLore(delivery, false);
            paymentAttempted = true;
            if (!EconomyHandler.tryWithdraw(player.getUniqueId(), price)) {
                insufficientFundsMessage(player, price);
                return false;
            }
            paid = true;
            for (org.bukkit.inventory.ItemStack overflow : player.getInventory().addItem(delivery).values()) {
                var dropped = player.getWorld().dropItem(player.getLocation(), overflow);
                dropped.setOwner(player.getUniqueId());
            }
            return true;
        } catch (RuntimeException failure) {
            com.magmaguy.magmacore.util.Logger.warn("Shop purchase needs inspection for " + player.getUniqueId()
                    + "; price=" + price + "; debit attempted=" + paymentAttempted + "; debit accepted=" + paid
                    + "; item=" + delivery + "; failure=" + failure);
            player.sendMessage(EconomySettingsConfig.getShopTransactionFailedMessage());
            player.closeInventory();
            return false;
        } finally {
            purchases.remove(player.getUniqueId());
        }
    }

    public static boolean itemNullPointerPrevention(InventoryClickEvent event) {
        //Check if current item is valid
        if (event.getCurrentItem() == null) return false;
        if (event.getCurrentItem().getType().equals(Material.AIR)) return false;
        return event.getCurrentItem().getItemMeta() != null;
    }

    public static void buyMessage(Player player, String itemDisplayName, double itemValue) {

        new BukkitRunnable() {

            @Override
            public void run() {

                player.sendMessage(
                                EconomySettingsConfig.getShopBuyMessage()
                                        .replace("$item_name", itemDisplayName)
                                        .replace("$item_value", EconomyHandler.formatCurrency(itemValue))
                                        .replace("$currency_name", EconomySettingsConfig.getCurrencyName()));

                player.sendMessage(
                                EconomySettingsConfig.getShopCurrentBalance()
                                        .replace("$currency_amount", EconomyHandler.formatCurrency(EconomyHandler.checkCurrency(player.getUniqueId())))
                                        .replace("$currency_name", EconomySettingsConfig.getCurrencyName()));


            }


        }.runTaskLater(Bukkit.getPluginManager().getPlugin(MetadataHandler.ELITE_MOBS), 2);

    }

    public static void insufficientFundsMessage(Player player, double itemValue) {

        new BukkitRunnable() {

            @Override
            public void run() {

                player.sendMessage(
                                EconomySettingsConfig.getShopInsufficientFundsMessage()
                                        .replace("$currency_name", EconomySettingsConfig.getCurrencyName()));

                player.sendMessage(
                                EconomySettingsConfig.getShopCurrentBalance()
                                        .replace("$currency_amount", EconomyHandler.formatCurrency(EconomyHandler.checkCurrency(player.getUniqueId())))
                                        .replace("$currency_name", EconomySettingsConfig.getCurrencyName()));

                player.sendMessage(
                                EconomySettingsConfig.getShopItemPrice()
                                        .replace("$item_value", EconomyHandler.formatCurrency(itemValue))
                                        .replace("$currency_name", EconomySettingsConfig.getCurrencyName()));

            }


        }.runTaskLater(Bukkit.getPluginManager().getPlugin(MetadataHandler.ELITE_MOBS), 2);

        player.closeInventory();

    }


}
