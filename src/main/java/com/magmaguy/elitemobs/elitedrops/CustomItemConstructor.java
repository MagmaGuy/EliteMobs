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

import com.google.common.collect.Lists;
import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.config.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.magmaguy.elitemobs.ChatColorConverter.chatColorConverter;

/**
 * Created by MagmaGuy on 29/11/2016.
 */
public class CustomItemConstructor implements Listener {

    public static ArrayList<ItemStack> customItemList = new ArrayList();
    public static HashMap<ItemStack, Double> staticCustomItemHashMap = new HashMap<>();
    public static HashMap<Integer, List<ItemStack>> dynamicRankedItemStacks = new HashMap<>();

    public void superDropParser() {

        List<String> lootCount = lootCounter();

        for (String lootEntry : lootCount) {

            StringBuilder path = new StringBuilder();
            path.append(lootEntry);

            String previousPath = path.toString();


            Material itemType = itemTypeHandler(previousPath);
            Bukkit.getLogger().info("Adding: " + previousPath);
            String itemName = itemNameHandler(previousPath);

            if (itemType == null) {

                Bukkit.getLogger().info(ChatColorConverter.chatColorConverter("&4[EliteMobs] Material type used for " + itemName + " is not valid."));
                break;

            }

            List itemEnchantments = itemEnchantmentHandler(previousPath);
            List potionEffects = itemPotionEffectHandler(previousPath);

            ItemStack itemStack = new ItemStack(itemType, 1);

            ItemMeta itemMeta = itemStack.getItemMeta();
            itemMeta.setDisplayName(itemName);

            //Add enchantments
            if (itemEnchantments != null) {

                for (Object object : itemEnchantments) {

                    String string = object.toString();

                    String[] parsedString = string.split(",");

                    String enchantmentName = parsedString[0];
                    Enchantment enchantmentType = Enchantment.getByName(enchantmentName);

                    int enchantmentLevel = Integer.parseInt(parsedString[1]);

                    itemMeta.addEnchant(enchantmentType, enchantmentLevel, true);

                }

            }

            //All obfuscated potion effect lore is stored in the first line of lore, so it gets temporarily stored in a single string
            String allObfuscatedPotionEffects = "";

            List<String> potionEffectLore = new ArrayList<>();
            //Add potion effects
            if (potionEffects != null) {

                for (Object object : potionEffects) {

                    //Add potion effects to item rank
                    String string = object.toString();

                    String[] parsedString = string.split(",");

                    //check if the input is correct
                    if (parsedString.length == 1) {

                        Bukkit.getLogger().info("[EliteMobs] Your item " + itemName + " does not have a level for its potion effect.");
                        Bukkit.getLogger().info("[EliteMobs] You need to add a number (probably 1) after the comma for the potion effect.");
                        Bukkit.getLogger().info("[EliteMobs] THIS MIGHT BREAK THE PLUGIN!");
                        break;

                    } else if (parsedString.length > 1) {

                        if (PotionEffectType.getByName(parsedString[0]) == null) {

                            break;

                        }

                        try {

                            Integer.parseInt(parsedString[1]);

                        } catch (NumberFormatException e) {

                            Bukkit.getLogger().info("[EliteMobs] Your item " + itemName + " does not have a valid level for its potion effect.");
                            Bukkit.getLogger().info("[EliteMobs] The item is therefore broken. Fix it!");

                            break;
                        }

                    }

                    String lore = "";

                    //Add hidden lore to identify powers
                    //Legacy support: potion effects used to only require two tags
                    if (ConfigValues.itemsCustomLootSettingsConfig.getBoolean(ItemsCustomLootSettingsConfig.SHOW_POTION_EFFECTS)) {

                        lore = parsedString[0].toLowerCase().substring(0, 1).toUpperCase() +
                                parsedString[0].toLowerCase().substring(1, (parsedString[0].length())).replace("_", " ") +
                                " " + parsedString[1];

//                        if (parsedString.length > 2) {
//
//                            lore += "," + parsedString[2];
//
//                        }
//
//                        if (parsedString.length > 3) {
//
//                            lore +=  "," + parsedString[3];
//
//                        }

                    }

                    String obfuscatedString = loreObfuscator(" " + string);
                    allObfuscatedPotionEffects += obfuscatedString;
//                    lore += obfuscatedString;

                    potionEffectLore.add(lore);

                }

            }

            //temporarily add enchantments ahead of time so the worth parser knows what potion effects are in play
            List<String> tempLore = new ArrayList<>();
            tempLore.add(allObfuscatedPotionEffects);
            itemMeta.setLore(tempLore);
            itemStack.setItemMeta(itemMeta);

            List<String> structuredLore = Lists.newArrayList(ConfigValues.itemsCustomLootSettingsConfig.getString(ItemsCustomLootSettingsConfig.LORE_STRUCTURE).split("\n"));
            List<String> parsedStructuredLore = new ArrayList<>();

            for (String string : structuredLore) {

                if (string.contains("$potionEffect")) {

                    if (potionEffectLore.size() > 0) {

                        for (String string1 : potionEffectLore) {

                            String potionEffectEntryString = string.replace("$potionEffect", "");
                            potionEffectEntryString += ChatColorConverter.chatColorConverter(string1);

                            parsedStructuredLore.add(ChatColorConverter.chatColorConverter(potionEffectEntryString));

                        }

                    }

                } else if (string.contains("$customLore")) {

                    if (customLoreParser(previousPath) != null && customLoreParser(previousPath).size() > 0) {

                        for (String string1 : customLoreParser(previousPath)) {


                            parsedStructuredLore.add(ChatColorConverter.chatColorConverter(string1));

                        }

                    }

                } else if (string.contains("$itemValue")) {

                    if (loreWorthParser(itemStack) != null) {

                        parsedStructuredLore.add(ChatColorConverter.chatColorConverter(loreWorthParser(itemStack)));

                    }

                } else {

                    parsedStructuredLore.add(ChatColorConverter.chatColorConverter(string));

                }

            }

            //Add config lore
            itemMeta.setLore(parsedStructuredLore);
            itemStack.setItemMeta(itemMeta);

            if (ConfigValues.defaultConfig.getBoolean(DefaultConfig.MMORPG_COLORS_FOR_CUSTOM_ITEMS) && ChatColor.stripColor(itemName).equals(itemName)) {

                ItemQuality.dropQualityColorizer(itemStack);

            }

            itemMeta = itemStack.getItemMeta();

            List<String> obfuscatedLore = new ArrayList<>();
            //Add the identifying line of lore
            for (String string : itemMeta.getLore()) {

                if (string.equals(itemMeta.getLore().get(0))) {

                    obfuscatedLore.add(string + allObfuscatedPotionEffects);

                } else {

                    obfuscatedLore.add(string);

                }

            }

            itemMeta.setLore(obfuscatedLore);
            itemStack.setItemMeta(itemMeta);

            //Add hidden lore for shops to validate
            ObfuscatedSignatureLoreData.obfuscateSignatureData(itemStack);

            if (ConfigValues.defaultConfig.getBoolean(DefaultConfig.HIDE_ENCHANTMENTS_ATTRIBUTE))
                EnchantmentHider.hideEnchantments(itemStack);

            //Add custom item to customItemList
            if (!loreAddDropFrequency(previousPath, itemStack)) {

                //Add item to ranked item list for drop math
                rankedItemMapCreator(itemStack);

            }

            customItemList.add(itemStack);

        }

    }

    public String loreObfuscator(String textToObfuscate) {

        String insert = "§";
        int period = 1;

        StringBuilder stringBuilder = new StringBuilder(textToObfuscate.length() + insert.length() * (textToObfuscate.length() / period) + 0);

        int index = 0;
        String prefix = "";

        while (index < textToObfuscate.length()) {

            stringBuilder.append(prefix);
            prefix = insert;
            stringBuilder.append(textToObfuscate.substring(index, Math.min(index + period, textToObfuscate.length())));
            index += period;

        }

        return stringBuilder.toString();

    }

    private List<String> lootCounter() {

        List<String> lootCount = new ArrayList();

        for (String configIterator : ConfigValues.itemsCustomLootListConfig.getKeys(true)) {

            int dotCount = 0;

            if (configIterator.contains("Loot.")) {

                for (int i = 0; i < configIterator.length(); i++) {

                    if (configIterator.charAt(i) == '.') {

                        dotCount++;

                    }

                }

                if (dotCount == 1) {

                    lootCount.add(configIterator);


                }

            }

        }

        return lootCount;

    }

    private String automatedStringBuilder(String previousPath, String append) {

        StringBuilder automatedStringBuilder = new StringBuilder();

        automatedStringBuilder.append(previousPath);
        automatedStringBuilder.append(".");
        automatedStringBuilder.append(append);

        String path = automatedStringBuilder.toString();

        return path;

    }

    private Material itemTypeHandler(String previousPath) {

        String path = automatedStringBuilder(previousPath, "Item Type");

        Material itemType;

        try {

            itemType = Material.getMaterial(ConfigValues.itemsCustomLootListConfig.getString(path));

        } catch (Error error) {

            return null;

        }


        return itemType;

    }

    private String itemNameHandler(String previousPath) {

        String path = automatedStringBuilder(previousPath, "Item Name");

        return chatColorConverter(ConfigValues.itemsCustomLootListConfig.getString(path));

    }

    private List<String> customLoreParser(String previousPath) {

        String path = automatedStringBuilder(previousPath, "Item Lore");

        List<String> itemLore = (List<String>) ConfigValues.itemsCustomLootListConfig.getList(path);

        List<String> newList = new ArrayList<>();

        if (itemLore != null && !itemLore.isEmpty()) {

            for (String string : itemLore) {

                if (string != null && !string.isEmpty()) {

                    newList.add(chatColorConverter(string));

                }

            }

        }

        if (newList.isEmpty()) {

            return itemLore;

        }

        return newList;

    }

    private String loreWorthParser(ItemStack itemStack) {

        if (ConfigValues.economyConfig.getBoolean(EconomySettingsConfig.ENABLE_ECONOMY)) {

            String valueLore;

            String value = ItemWorthCalculator.determineItemWorth(itemStack) + "";

            valueLore = ConfigValues.itemsProceduralSettingsConfig.getString(ItemsProceduralSettingsConfig.LORE_WORTH);
            valueLore = valueLore.replace("$worth", value);
            valueLore = valueLore.replace("$currencyName", ConfigValues.economyConfig.getString(EconomySettingsConfig.CURRENCY_NAME));

            return valueLore;

        }

        return null;

    }

    private List itemEnchantmentHandler(String previousPath) {

        String path = automatedStringBuilder(previousPath, "Enchantments");

        List enchantments = ConfigValues.itemsCustomLootListConfig.getList(path);

        return enchantments;

    }

    private List<String> itemPotionEffectHandler(String previousPath) {

        String path = automatedStringBuilder(previousPath, "Potion Effects");

        List potionEffects = ConfigValues.itemsCustomLootListConfig.getList(path);

        return potionEffects;

    }

    private void rankedItemMapCreator(ItemStack itemStack) {

        double itemWorth = ItemWorthCalculator.determineItemWorth(itemStack);
        int itemRank = (int) (itemWorth / 10);

        if (dynamicRankedItemStacks.get(itemRank) == null) {

            List<ItemStack> list = new ArrayList<>();

            list.add(itemStack);

            dynamicRankedItemStacks.put(itemRank, list);

        } else {

            List<ItemStack> list = dynamicRankedItemStacks.get(itemRank);

            list.add(itemStack);

            dynamicRankedItemStacks.put(itemRank, list);

        }

    }

    private boolean loreAddDropFrequency(String previousPath, ItemStack itemStack) {

        String path = automatedStringBuilder(previousPath, "Drop Weight");

        //Only tag the item if it contains a value other than dynamic
        if (ConfigValues.itemsCustomLootListConfig.contains(path)) {

            if (!ConfigValues.itemsCustomLootListConfig.getString(path).equalsIgnoreCase("dynamic")) {

                try {

                    Double dropWeight = Double.valueOf(ConfigValues.itemsCustomLootListConfig.getString(path));
                    staticCustomItemHashMap.put(itemStack, dropWeight);
                    return true;

                } catch (NumberFormatException e) {

                    Bukkit.getLogger().info("[EliteMobs] Your item " + path + " contains an invalid drop weight value ("
                            + ConfigValues.itemsCustomLootListConfig.getString(path) + ")");

                }


            }

        }

        return false;

    }

}
