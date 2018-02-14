/*
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.magmaguy.elitemobs.commands.shops;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.commands.guiconfig.SignatureItem;
import com.magmaguy.elitemobs.config.ConfigValues;
import com.magmaguy.elitemobs.config.EconomySettingsConfig;
import com.magmaguy.elitemobs.economy.EconomyHandler;
import com.magmaguy.elitemobs.economy.UUIDFilter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class SharedShopElements {

    public static void shopHeader(Inventory shopInventory) {

        ItemStack signature = SignatureItem.signatureItem();

        shopInventory.setItem(ConfigValues.economyConfig.getInt(EconomySettingsConfig.SIGNATURE_ITEM_LOCATION_SHOPS), signature);

    }

    public static boolean inventoryNullPointerPreventer(InventoryClickEvent event) {

        //Check if economy is enabled
        if (!ConfigValues.economyConfig.getBoolean(EconomySettingsConfig.ENABLE_ECONOMY)) return false;
        //Check if current item is valid
        if (event.getCurrentItem() == null) return false;
        if (event.getCurrentItem().getType().equals(Material.AIR)) return false;
        if (event.getCurrentItem().getItemMeta() == null) return false;
        return event.getCurrentItem().getItemMeta().hasLore();

    }

    public static void buyMessage(Player player, String itemDisplayName, double itemValue) {

        new BukkitRunnable() {

            @Override
            public void run() {

                player.sendMessage(ChatColor.GREEN + "You have bought " + itemDisplayName + " for " + ChatColor.DARK_GREEN + itemValue + " " + ChatColor.GREEN + ConfigValues.economyConfig.getString(EconomySettingsConfig.CURRENCY_NAME));
                player.sendMessage(ChatColor.GREEN + "You have " + ChatColor.DARK_GREEN + EconomyHandler.checkCurrency(UUIDFilter.guessUUI(player.getName())) + " " + ChatColor.GREEN + ConfigValues.economyConfig.getString(EconomySettingsConfig.CURRENCY_NAME));

            }


        }.runTaskLater(Bukkit.getPluginManager().getPlugin(MetadataHandler.ELITE_MOBS), 2);

    }

    public static void insufficientFundsMessage(Player player, double itemValue) {

        new BukkitRunnable() {

            @Override
            public void run() {

                player.sendMessage(ChatColor.RED + "You don't have enough " + ConfigValues.economyConfig.getString(EconomySettingsConfig.CURRENCY_NAME) + "!");
                player.sendMessage(ChatColor.GREEN + "You have " + ChatColor.DARK_GREEN + EconomyHandler.checkCurrency(UUIDFilter.guessUUI(player.getName())) + " " + ChatColor.GREEN + ConfigValues.economyConfig.getString(EconomySettingsConfig.CURRENCY_NAME));
                player.sendMessage(ChatColor.GREEN + "That item cost " + ChatColor.DARK_GREEN + itemValue + " " + ChatColor.GREEN + ConfigValues.economyConfig.getString(EconomySettingsConfig.CURRENCY_NAME) + ".");

            }


        }.runTaskLater(Bukkit.getPluginManager().getPlugin(MetadataHandler.ELITE_MOBS), 2);

        player.closeInventory();

    }

    public static void sellMessage(Player player, String itemDisplayName, double amountDeduced) {

        new BukkitRunnable() {

            @Override
            public void run() {

                player.sendMessage("You have sold " + itemDisplayName + " for " + amountDeduced + " " + ConfigValues.economyConfig.getString(EconomySettingsConfig.CURRENCY_NAME));
                player.sendMessage("You have " + EconomyHandler.checkCurrency(UUIDFilter.guessUUI(player.getName())) + " " + ConfigValues.economyConfig.getString(EconomySettingsConfig.CURRENCY_NAME));

            }


        }.runTaskLater(Bukkit.getPluginManager().getPlugin(MetadataHandler.ELITE_MOBS), 2);

    }


}
