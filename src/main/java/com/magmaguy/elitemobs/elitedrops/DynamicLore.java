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

package com.magmaguy.elitemobs.elitedrops;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.config.ConfigValues;
import com.magmaguy.elitemobs.config.EconomySettingsConfig;
import com.magmaguy.elitemobs.config.ItemsProceduralSettingsConfig;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class DynamicLore {

    public static boolean displayWorth = true;

    /*
    This method runs inside of a repeating task that runs on a set interval
     */
    public static void refreshDynamicLore() {

        for (Player player : Bukkit.getOnlinePlayers()) {

            for (ItemStack itemStack : player.getInventory().getContents()) {

                if (itemStack != null && itemStack.hasItemMeta()
                        && itemStack.getItemMeta().hasLore()) {

                    boolean eliteItem = false;

                    for (String string : itemStack.getItemMeta().getLore()) {

                        if (string.contains(ObfuscatedSignatureLoreData.loreObfuscator(ObfuscatedSignatureLoreData.ITEM_SIGNATURE))) {

                            eliteItem = true;

                        }

                    }

                    if (eliteItem && !player.getInventory().getItemInMainHand().equals(itemStack)
                            && !player.getInventory().getItemInOffHand().equals(itemStack)) {

                        if (displayWorth) {

                            setLoreToWorth(itemStack);

                        } else {

                            setLoreToResale(itemStack);

                        }

                    }

                }

            }


        }

        displayWorth = !displayWorth;

    }

    private static void setLoreToWorth(ItemStack itemStack) {

        double itemWorth = ItemWorthCalculator.determineItemWorth(itemStack);
        double itemResale = ItemWorthCalculator.determineResaleWorth(itemStack);

        ItemMeta itemMeta = itemStack.getItemMeta();

        List<String> newLore = new ArrayList<>();

        for (String loreLine : itemMeta.getLore()) {

            if (findEconomyLoreLine(loreLine, itemWorth, itemResale)) {

                String lorePrefix = lorePrefix(loreLine, itemWorth, itemResale);

                newLore.add(lorePrefix + targetValueLine(itemWorth));

            } else {

                newLore.add(loreLine);

            }

        }

        itemMeta.setLore(newLore);
        itemStack.setItemMeta(itemMeta);

    }

    private static void setLoreToResale(ItemStack itemStack) {

        double itemWorth = ItemWorthCalculator.determineItemWorth(itemStack);
        double itemResale = ItemWorthCalculator.determineResaleWorth(itemStack);

        ItemMeta itemMeta = itemStack.getItemMeta();

        List<String> newLore = new ArrayList<>();

        for (String loreLine : itemMeta.getLore()) {

            if (findEconomyLoreLine(loreLine, itemWorth, itemResale)) {

                String lorePrefix = lorePrefix(loreLine, itemWorth, itemResale);

                newLore.add(lorePrefix + targetResaleLine(itemResale));

            } else {

                newLore.add(loreLine);

            }

        }

        itemMeta.setLore(newLore);
        itemStack.setItemMeta(itemMeta);

    }

    private static boolean findEconomyLoreLine(String loreLine, double worth, double resale) {

        String targetWorthLine = targetValueLine(worth);
        String targetResaleLine = targetResaleLine(resale);

        return loreLine.contains(targetResaleLine) || loreLine.contains(targetWorthLine);

    }

    private static String lorePrefix(String loreLine, double worth, double resale) {

        String targetWorthLine = targetValueLine(worth);
        String targetResaleLine = targetResaleLine(resale);
        String lorePrefix = "";

        if (loreLine.contains(targetWorthLine)) {

            lorePrefix = loreLine.substring(0, loreLine.indexOf(targetWorthLine));

        }

        if (loreLine.contains(targetResaleLine)) {

            lorePrefix = loreLine.substring(0, loreLine.indexOf(targetResaleLine));

        }

        return lorePrefix;

    }

    private static String targetValueLine(double worthOrResale) {

        String worthLore = ConfigValues.itemsProceduralSettingsConfig.getString(ItemsProceduralSettingsConfig.LORE_WORTH);
        worthLore = worthLore.replace("$worth", worthOrResale + "");
        worthLore = worthLore.replace("$currencyName", ConfigValues.economyConfig.getString(EconomySettingsConfig.CURRENCY_NAME));
        worthLore = ChatColorConverter.chatColorConverter(worthLore);

        return worthLore;

    }

    private static String targetResaleLine(double resale) {

        String resaleLore = ConfigValues.itemsProceduralSettingsConfig.getString(ItemsProceduralSettingsConfig.LORE_RESALE_WORTH);
        resaleLore = resaleLore.replace("$resale", resale + "");
        resaleLore = resaleLore.replace("$currencyName", ConfigValues.economyConfig.getString(EconomySettingsConfig.CURRENCY_NAME));
        resaleLore = ChatColorConverter.chatColorConverter(resaleLore);

        return resaleLore;

    }


}
