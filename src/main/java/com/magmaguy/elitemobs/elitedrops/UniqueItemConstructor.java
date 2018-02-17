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
import com.magmaguy.elitemobs.config.ItemsUniqueConfig;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class UniqueItemConstructor {

    CustomItemConstructor customItemConstructor = new CustomItemConstructor();

    ArrayList<ItemStack> uniqueItems = new ArrayList<>();

    public static final String HUNTING_SET_IDENTIFIER = " Hunting set";

    public void intializeUniqueItems() {

        if (!ConfigValues.itemsUniqueConfig.getBoolean(ItemsUniqueConfig.ENABLE_UNIQUE_ITEMS)) return;

        if (ConfigValues.itemsUniqueConfig.getBoolean(ItemsUniqueConfig.ENABLE_HUNTING_SET)) {

            //add helmet
            ItemStack helmet = itemStackConstructor(ConfigValues.itemsUniqueConfig.getString(ItemsUniqueConfig.HUNTING_SET_HELMET));
            helmet = tempWorthLoreAdder(helmet);

            if (dropWeight(ConfigValues.itemsUniqueConfig.getString(ItemsUniqueConfig.HUNTING_SET_HELMET)) > 0) {

                CustomItemConstructor.staticCustomItemHashMap.put(helmet, dropWeight(ConfigValues.itemsUniqueConfig.getString(ItemsUniqueConfig.HUNTING_SET_HELMET)));

            } else {

                //todo: add in case of dynamic

            }

            //add helmet
            CustomItemConstructor.customItemList.add(helmet);

            ItemStack chestplate = itemStackConstructor(ConfigValues.itemsUniqueConfig.getString(ItemsUniqueConfig.HUNTING_SET_CHESTPLATE));
            chestplate = tempWorthLoreAdder(chestplate);

            if (dropWeight(ConfigValues.itemsUniqueConfig.getString(ItemsUniqueConfig.HUNTING_SET_CHESTPLATE)) > 0) {

                CustomItemConstructor.staticCustomItemHashMap.put(chestplate, dropWeight(ConfigValues.itemsUniqueConfig.getString(ItemsUniqueConfig.HUNTING_SET_CHESTPLATE)));

            } else {

                //todo: add in case of dynamic

            }

            CustomItemConstructor.customItemList.add(chestplate);

            //add leggings
            ItemStack leggings = itemStackConstructor(ConfigValues.itemsUniqueConfig.getString(ItemsUniqueConfig.HUNTING_SET_LEGGINGS));
            leggings = tempWorthLoreAdder(leggings);

            if (dropWeight(ConfigValues.itemsUniqueConfig.getString(ItemsUniqueConfig.HUNTING_SET_LEGGINGS)) > 0) {

                CustomItemConstructor.staticCustomItemHashMap.put(leggings, dropWeight(ConfigValues.itemsUniqueConfig.getString(ItemsUniqueConfig.HUNTING_SET_LEGGINGS)));

            } else {

                //todo: add in case of dynamic

            }

            CustomItemConstructor.customItemList.add(leggings);

            //add boots
            ItemStack boots = itemStackConstructor(ConfigValues.itemsUniqueConfig.getString(ItemsUniqueConfig.HUNTING_SET_BOOTS));
            boots = tempWorthLoreAdder(boots);

            if (dropWeight(ConfigValues.itemsUniqueConfig.getString(ItemsUniqueConfig.HUNTING_SET_BOOTS)) > 0) {

                CustomItemConstructor.staticCustomItemHashMap.put(boots, dropWeight(ConfigValues.itemsUniqueConfig.getString(ItemsUniqueConfig.HUNTING_SET_BOOTS)));

            } else {

                //todo: add in case of dynamic

            }


            CustomItemConstructor.customItemList.add(boots);

            //add bow
            ItemStack bow = itemStackConstructor(ConfigValues.itemsUniqueConfig.getString(ItemsUniqueConfig.HUNTING_SET_BOW));
            bow = tempWorthLoreAdder(bow);

            if (dropWeight(ConfigValues.itemsUniqueConfig.getString(ItemsUniqueConfig.HUNTING_SET_BOW)) > 0) {

                CustomItemConstructor.staticCustomItemHashMap.put(bow, dropWeight(ConfigValues.itemsUniqueConfig.getString(ItemsUniqueConfig.HUNTING_SET_BOW)));

            } else {

                //todo: add in case of dynamic

            }

            CustomItemConstructor.customItemList.add(bow);

        }

    }

    private ItemStack tempWorthLoreAdder(ItemStack itemStack) {

        List<String> newLore = new ArrayList<>();
        newLore.addAll(itemStack.getItemMeta().getLore());
        newLore.add("Worth " + ItemWorthCalculator.determineItemWorth(itemStack) + " " + ConfigValues.economyConfig.getString(EconomySettingsConfig.CURRENCY_NAME));

        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setLore(newLore);
        itemStack.setItemMeta(itemMeta);

        return itemStack;

    }

    private static Material itemMaterial(String string) {

        for (String line : string.split("\n")) {


            if (line.contains("Item Type:")) {

                String materialString = line.substring(line.indexOf(":") + 2);
                Material material = Material.getMaterial(materialString);
                return material;

            }


        }

        return null;

    }

    private static String itemName(String string) {

        for (String line : string.split("\n")) {

            if (line.contains("Item Name:")) {

                String itemName = line.substring(line.indexOf(":") + 2);
                return itemName;

            }


        }

        return null;

    }

    private static List<String> itemLore(String string) {

        List<String> stringList = new ArrayList<>();

        boolean inLore = false;

        for (String line : string.split("\n")) {

            if (line.contains("Item Lore:")) {

                inLore = true;

            }

            if (line.contains("-") && inLore) {

                stringList.add(line.substring(line.indexOf("-") + 2));

            }

            if (line.contains("Enchantments:")) {

                return stringList;

            }

        }

        return null;


    }

    private static List<String> enchantments(String string) {

        List<String> stringList = new ArrayList<>();

        boolean inEnchant = false;

        for (String line : string.split("\n")) {

            if (line.contains("Enchantments:")) {

                inEnchant = true;

            }

            if (line.contains("-") && inEnchant) {

                stringList.add(line.substring(line.indexOf("-") + 2));

            }

            if (line.contains("Potion Effects:")) {

                break;

            }

        }

        return stringList;

    }

    private static List<String> potionEffect(String string) {

        List<String> stringList = new ArrayList<>();

        boolean inPotionEffect = false;

        for (String line : string.split("\n")) {

            if (line.contains("Potion Effects:")) {

                inPotionEffect = true;

            }

            if (line.contains("-") && inPotionEffect) {

                stringList.add(line.substring(line.indexOf("-") + 2));

            }

            if (line.contains("Drop Weight:")) {

                break;

            }

        }

        return stringList;

    }

    private static double dropWeight(String string) {

        for (String line : string.split("\n")) {

            if (line.contains("Drop Weight:")) {

                if (line.substring(line.indexOf(":") + 2).equalsIgnoreCase("dynamic")) {

                    return 0;

                }

                return Integer.parseInt(line.substring(line.indexOf(":") + 2));

            }

        }

        return 0;

    }

    private ItemStack itemStackConstructor(String string) {

        ItemStack itemStack = new ItemStack(itemMaterial(string), 1);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName(itemName(ChatColorConverter.chatColorConverter(string)));
        itemMeta.setLore(itemLore(ChatColorConverter.chatColorConverter(string)));

        if (enchantments(string).size() > 0) {


            for (String line : enchantments(string)) {

                String[] splitLine = line.split(",");
                Enchantment enchantmentType = Enchantment.getByName(splitLine[0]);
                int enchantmentLevel = Integer.parseInt(splitLine[1]);
                itemMeta.addEnchant(enchantmentType, enchantmentLevel, true);

            }

        }

        itemStack.setItemMeta(itemMeta);
        List<String> tempLore = itemMeta.getLore();

        String obfuscatedPotionEffects = "";

        if (potionEffect(string).size() > 0) {

            for (String line : potionEffect(string)) {

                String[] parsedString = line.split(",");

                String lore = parsedString[0].toLowerCase().substring(0, 1).toUpperCase() +
                        parsedString[0].toLowerCase().substring(1, (parsedString[0].length())).replace("_", " ") +
                        " " + parsedString[1];

                tempLore.add(0, lore);

            }

            itemMeta.setLore(tempLore);
            itemStack.setItemMeta(itemMeta);

            for (String line : potionEffect(string)) {

                obfuscatedPotionEffects += customItemConstructor.loreObfuscator(" " + line);

            }

            List<String> obfuscatedLore = new ArrayList<>();

            for (String line : itemMeta.getLore()) {

                if (line.equals(itemMeta.getLore().get(0))) {

                    obfuscatedLore.add(line + obfuscatedPotionEffects + customItemConstructor.loreObfuscator(HUNTING_SET_IDENTIFIER));

                } else {

                    obfuscatedLore.add(line);

                }

            }

            itemMeta.setLore(obfuscatedLore);

        }

        itemStack.setItemMeta(itemMeta);
        ObfuscatedSignatureLoreData.obfuscateSignatureData(itemStack);

        return itemStack;

    }

    public boolean huntingSetItemDetector(ItemStack itemStack) {

        if (itemStack == null) return false;
        if (itemStack.getType() == Material.AIR) return false;
        if (!itemStack.hasItemMeta()) return false;
        if (!itemStack.getItemMeta().hasLore()) return false;

        for (String string : itemStack.getItemMeta().getLore()) {

            if (string.contains(customItemConstructor.loreObfuscator(HUNTING_SET_IDENTIFIER))) {

                return true;

            }

        }

        return false;

    }

}
