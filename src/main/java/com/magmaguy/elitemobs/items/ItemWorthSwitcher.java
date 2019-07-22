package com.magmaguy.elitemobs.items;

import com.magmaguy.elitemobs.config.EconomySettingsConfig;
import com.magmaguy.elitemobs.config.ItemSettingsConfig;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ItemWorthSwitcher {

    public static ItemStack switchToWorth(ItemStack itemStack) {

        ItemMeta itemMeta = itemStack.getItemMeta();

        List<String> newLore = new ArrayList<>();

        for (String string : itemMeta.getLore()) {
            if (string.contains(EconomySettingsConfig.currencyName) && string.contains(ItemWorthCalculator.determineResaleWorth(itemStack) + "")) {
                newLore.add(ItemSettingsConfig.loreWorth.
                        replace("$worth", ItemWorthCalculator.determineItemWorth(itemStack) + "")
                        .replace("$currencyName", EconomySettingsConfig.currencyName));
                continue;
            }
            newLore.add(string);
        }

        itemMeta.setLore(newLore);
        itemStack.setItemMeta(itemMeta);

        return itemStack;

    }

    public static ItemStack switchToResaleValue(ItemStack itemStack) {

        ItemMeta itemMeta = itemStack.getItemMeta();

        List<String> newLore = new ArrayList<>();

        for (String string : itemMeta.getLore()) {
            if (string.contains(EconomySettingsConfig.currencyName) && string.contains(ItemWorthCalculator.determineItemWorth(itemStack) + "")) {
                newLore.add(ItemSettingsConfig.loreResale.
                        replace("$resale", ItemWorthCalculator.determineResaleWorth(itemStack) + "")
                        .replace("$currencyName", EconomySettingsConfig.currencyName));
                continue;
            }
            newLore.add(string);
        }

        itemMeta.setLore(newLore);
        itemStack.setItemMeta(itemMeta);

        return itemStack;

    }

}
