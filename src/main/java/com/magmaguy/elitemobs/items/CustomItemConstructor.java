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

package com.magmaguy.elitemobs.items;

import com.magmaguy.elitemobs.config.ConfigValues;
import com.magmaguy.elitemobs.items.itemconstructor.ItemConstructor;
import com.magmaguy.elitemobs.items.parserutil.CustomEnchantmentConfigParser;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.Configuration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.magmaguy.elitemobs.ChatColorConverter.chatColorConverter;
import static com.magmaguy.elitemobs.items.parserutil.ConfigPathBuilder.automatedStringBuilder;
import static com.magmaguy.elitemobs.items.parserutil.EnchantmentConfigParser.parseEnchantments;

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

            Material itemType = getMaterial(previousPath);
            Bukkit.getLogger().info("Adding: " + previousPath);
            String itemName = getName(previousPath);

            if (itemType == null)
                continue;

            HashMap<Enchantment, Integer> itemEnchantments = parseEnchantments(ConfigValues.itemsCustomLootListConfig, previousPath);
            HashMap<String, Integer> customEnchantments = CustomEnchantmentConfigParser.parseCustomEnchantments();
            List potionList = itemPotionEffectHandler(ConfigValues.itemsCustomLootListConfig, previousPath);
            List<String> loreList = getCustomLore(previousPath);

            ItemStack itemStack = ItemConstructor.constructItem(itemName, itemType, itemEnchantments, potionList, loreList);

            if (!loreAddDropFrequency(previousPath, itemStack)) {

                //Add item to ranked item list for drop math
                rankedItemMapCreator(itemStack);

            }

            customItemList.add(itemStack);

        }

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

    private Material getMaterial(String previousPath) {

        String path = automatedStringBuilder(previousPath, "Item Type");

        Material material;

        try {

            material = Material.getMaterial(ConfigValues.itemsCustomLootListConfig.getString(path));

        } catch (Error error) {

            Bukkit.getLogger().warning("[EliteMobs] Invalid material!");
            return null;

        }


        return material;

    }

    private String getName(String previousPath) {

        String name = automatedStringBuilder(previousPath, "Item Name");

        if (name.isEmpty())
            Bukkit.getLogger().warning("[EliteMobs] Invalid name!");

        return chatColorConverter(ConfigValues.itemsCustomLootListConfig.getString(name));

    }

    private List<String> getCustomLore(String previousPath) {

        String path = automatedStringBuilder(previousPath, "Item Lore");

        List<String> itemLore = (List<String>) ConfigValues.itemsCustomLootListConfig.getList(path);

        List<String> newList = new ArrayList<>();

        if (itemLore != null && !itemLore.isEmpty())
            for (String string : itemLore)
                if (string != null && !string.isEmpty())
                    newList.add(chatColorConverter(string));

        if (newList.isEmpty())
            return itemLore;

        return newList;

    }

    public static List<String> itemPotionEffectHandler(Configuration configuration, String previousPath) {

        String path = automatedStringBuilder(previousPath, "Potion Effects");

        List potionEffects = configuration.getList(path);

        return potionEffects;

    }

    private void rankedItemMapCreator(ItemStack itemStack) {

        int itemTier = (int) ItemTierFinder.findBattleTier(itemStack);

        if (dynamicRankedItemStacks.get(itemTier) == null) {

            List<ItemStack> list = new ArrayList<>();

            list.add(itemStack);

            dynamicRankedItemStacks.put(itemTier, list);

        } else {

            List<ItemStack> list = dynamicRankedItemStacks.get(itemTier);

            list.add(itemStack);

            dynamicRankedItemStacks.put(itemTier, list);

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
