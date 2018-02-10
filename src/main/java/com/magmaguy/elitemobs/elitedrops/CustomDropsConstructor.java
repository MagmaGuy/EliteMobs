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
public class CustomDropsConstructor implements Listener {

    public static List<ItemStack> lootList = new ArrayList();
    public static HashMap<Integer, List<ItemStack>> rankedItemStacks = new HashMap<>();

    private LootCustomConfig lootCustomConfig = new LootCustomConfig();

    public void superDropParser() {

        //TODO: use ItemRankHandler.guessItemRank(material, enchantmentTotal)
        //TODO: split class up

        List<String> lootCount = lootCounter();

        for (String lootEntry : lootCount) {

            StringBuilder path = new StringBuilder();
            path.append(lootEntry);

            String previousPath = path.toString();


            String itemType = itemTypeHandler(previousPath);
            Bukkit.getLogger().info("Adding: " + previousPath);
            String itemName = itemNameHandler(previousPath);

            List itemEnchantments = itemEnchantmentHandler(previousPath);
            List potionEffects = itemPotionEffectHandler(previousPath);

            ItemStack itemStack = new ItemStack(Material.getMaterial(itemType), 1);
            ItemMeta itemMeta = itemStack.getItemMeta();
            itemMeta.setDisplayName(itemName);

            int enchantmentCount = 0;

            //Add enchantments
            if (itemEnchantments != null) {

                for (Object object : itemEnchantments) {

                    String string = object.toString();

                    String[] parsedString = string.split(",");

                    String enchantmentName = parsedString[0];
                    Enchantment enchantmentType = Enchantment.getByName(enchantmentName);

                    int enchantmentLevel = Integer.parseInt(parsedString[1]);

                    enchantmentCount += enchantmentLevel;

                    itemMeta.addEnchant(enchantmentType, enchantmentLevel, true);

                }

            }


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

                        int potionEffectAmplifier = Integer.parseInt(parsedString[1]);
                        enchantmentCount += potionEffectAmplifier * 15;

                    }

                    String lore = "";

                    //Add hidden lore to identify powers
                    //Legacy support: potion effects used to only require two tags
                    if (ConfigValues.customLootSettingsConfig.getBoolean(CustomLootSettingsConfig.SHOW_POTION_EFFECTS)) {

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
                    lore += obfuscatedString;

                    potionEffectLore.add(lore);

                }

            }

            int itemRank = ItemRankHandler.guessItemRank(itemStack.getType(), enchantmentCount);

            List<String> structuredLore = Lists.newArrayList(ConfigValues.customLootSettingsConfig.getString(CustomLootSettingsConfig.LORE_STRUCTURE).split("\n"));
            List<String> parsedStructuredLore = new ArrayList<>();

            for (String string : structuredLore) {

                if (string.contains("$potionEffect")) {

                    if (potionEffectLore.size() > 0) {

                        for (String string1 : potionEffectLore) {

                            parsedStructuredLore.add(ChatColorConverter.chatColorConverter(string1));

                        }

                    }

                } else if (string.contains("$customLore")) {

                    if (customLoreParser(previousPath) != null && customLoreParser(previousPath).size() > 0) {

                        for (String string1 : customLoreParser(previousPath)) {


                            parsedStructuredLore.add(ChatColorConverter.chatColorConverter(string1));

                        }

                    }

                } else if (string.contains("$itemValue")) {

                    if (loreWorthParser(itemRank) != null) {

                        parsedStructuredLore.add(ChatColorConverter.chatColorConverter(loreWorthParser(itemRank)));

                    }

                } else {

                    parsedStructuredLore.add(ChatColorConverter.chatColorConverter(string));

                }

            }

            //Add config lore
            itemMeta.setLore(parsedStructuredLore);
            itemStack.setItemMeta(itemMeta);

            if (ConfigValues.defaultConfig.getBoolean("Use MMORPG colors for custom items") && ChatColor.stripColor(itemName).equals(itemName)) {

                DropQuality.dropQualityColorizer(itemStack);

            }

            //Add custom item to lootList
            lootList.add(itemStack);

            //Add item to ranked item list for drop math
            rankedItemMapCreator(itemRank, itemStack);

        }

    }

    private String loreObfuscator(String textToObfuscate) {

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

        for (String configIterator : lootCustomConfig.getLootConfig().getKeys(true)) {

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

    private String itemTypeHandler(String previousPath) {

        String path = automatedStringBuilder(previousPath, "Item Type");

        String itemType = lootCustomConfig.getLootConfig().getString(path);

        return itemType;

    }

    private String itemNameHandler(String previousPath) {

        String path = automatedStringBuilder(previousPath, "Item Name");

        String itemName = chatColorConverter(lootCustomConfig.getLootConfig().getString(path));

        return itemName;

    }

    private List<String> customLoreParser(String previousPath) {

        String path = automatedStringBuilder(previousPath, "Item Lore");

        List<String> itemLore = (List<String>) lootCustomConfig.getLootConfig().getList(path);

        List<String> newList = new ArrayList<>();

        if (itemLore != null && !itemLore.isEmpty()) {

            for (String string : itemLore) {

                if (string != null && !string.isEmpty()) {

                    newList.add(chatColorConverter(string));

                }

            }

        }

        if (newList == null || newList.isEmpty()) {

            return itemLore;

        }

        return newList;

    }

    private String loreWorthParser(int itemRank) {

        if (ConfigValues.economyConfig.getBoolean("Enable economy")) {

            String valueLore;

            String value = itemRank * ConfigValues.economyConfig.getDouble(EconomySettingsConfig.TIER_PRICE_PROGRESSION) + "";

            valueLore = ConfigValues.randomItemsConfig.getString(RandomItemsSettingsConfig.LORE_WORTH);
            valueLore = valueLore.replace("$worth", value);
            valueLore = valueLore.replace("$currencyName", ConfigValues.economyConfig.getString(EconomySettingsConfig.CURRENCY_NAME));

            return valueLore;

        }

        return null;

    }

    private List itemEnchantmentHandler(String previousPath) {

        String path = automatedStringBuilder(previousPath, "Enchantments");

        List enchantments = lootCustomConfig.getLootConfig().getList(path);

        return enchantments;

    }

    private List<String> itemPotionEffectHandler(String previousPath) {

        String path = automatedStringBuilder(previousPath, "Potion Effects");

        List potionEffects = lootCustomConfig.getLootConfig().getList(path);

        return potionEffects;

    }

    private void rankedItemMapCreator(int itemPower, ItemStack itemStack) {

        if (rankedItemStacks.get(itemPower) == null) {

            List<ItemStack> list = new ArrayList<>();

            list.add(itemStack);

            rankedItemStacks.put(itemPower, list);

        } else {

            List<ItemStack> list = rankedItemStacks.get(itemPower);

            list.add(itemStack);

            rankedItemStacks.put(itemPower, list);

        }

    }

}
