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
import com.magmaguy.elitemobs.config.EconomySettingsConfig;
import com.magmaguy.elitemobs.config.ItemsUniqueConfig;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class UniqueItemConstructor {

    static CustomItemConstructor customItemConstructor = new CustomItemConstructor();

    public static HashMap<String, ItemStack> uniqueItems = new HashMap();
    public static List<ItemStack> uniqueItemsList = new ArrayList();
    public static final String HUNTING_HELMET = "HuntingHelmet";
    public static final String HUNTING_CHESTPLATE = "HuntingChestplate";
    public static final String HUNTING_LEGGINGS = "HuntingLeggings";
    public static final String HUNTING_BOOTS = "HuntingBoots";
    public static final String HUNTING_BOW = "HuntingBow";
    public static final String ZOMBIE_KING_AXE = "ZombieKingAxe";
    public static final String DEPTHS_SEEKER = "DepthsSeeker";
    public static final String GREED = "DwarvenGreed";
    public static final String THE_FELLER = "TheFeller";

    public static final String HUNTING_SET_IDENTIFIER = " Hunting set";
    public static final String ZOMBIE_KING_AXE_IDENTIFIER = " King Axe";


//    public void intializeUniqueItems() {
//
//        if (VersionChecker.currentVersionIsUnder(12, 2)) return;
//        if (!ConfigValues.itemsUniqueConfig.getBoolean(ItemsUniqueConfig.ENABLE_UNIQUE_ITEMS)) return;
//
////        for (String string : ConfigValues.itemsUniqueConfig.getKeys(true))
////            if (string.contains(ItemsUniqueConfig.ITEM_KEY)) {
////
////            }
//
//        if (ConfigValues.itemsUniqueConfig.getBoolean(ItemsUniqueConfig.ENABLE_HUNTING_SET)) {
//
//            //add helmet
//            ItemStack helmet = uniqueItemConstructor(ConfigValues.itemsUniqueConfig.getString(ItemsUniqueConfig.HUNTING_SET_HELMET), UniqueItemType.ELITE_MOB_HUNTING_SET);
//            helmet = tempWorthLoreAdder(helmet);
//
//            if (dropWeight(ConfigValues.itemsUniqueConfig.getString(ItemsUniqueConfig.HUNTING_SET_HELMET)) > 0) {
//
//                CustomItemConstructor.staticCustomItemHashMap.put(helmet, dropWeight(ConfigValues.itemsUniqueConfig.getString(ItemsUniqueConfig.HUNTING_SET_HELMET)));
//
//            } else {
//
//                //todo: add in case of dynamic
//
//            }
//
//            //add helmet
//            CustomItemConstructor.customItemList.add(helmet);
//            uniqueItemsList.add(helmet);
//            uniqueItems.put(HUNTING_HELMET, helmet);
//
//            ItemStack chestplate = uniqueItemConstructor(ConfigValues.itemsUniqueConfig.getString(ItemsUniqueConfig.HUNTING_SET_CHESTPLATE), UniqueItemType.ELITE_MOB_HUNTING_SET);
//            chestplate = tempWorthLoreAdder(chestplate);
//
//            if (dropWeight(ConfigValues.itemsUniqueConfig.getString(ItemsUniqueConfig.HUNTING_SET_CHESTPLATE)) > 0) {
//
//                CustomItemConstructor.staticCustomItemHashMap.put(chestplate, dropWeight(ConfigValues.itemsUniqueConfig.getString(ItemsUniqueConfig.HUNTING_SET_CHESTPLATE)));
//
//            } else {
//
//                //todo: add in case of dynamic
//
//            }
//
//            CustomItemConstructor.customItemList.add(chestplate);
//            uniqueItemsList.add(chestplate);
//            uniqueItems.put(HUNTING_CHESTPLATE, chestplate);
//
//            //add leggings
//            ItemStack leggings = uniqueItemConstructor(ConfigValues.itemsUniqueConfig.getString(ItemsUniqueConfig.HUNTING_SET_LEGGINGS), UniqueItemType.ELITE_MOB_HUNTING_SET);
//            leggings = tempWorthLoreAdder(leggings);
//
//            if (dropWeight(ConfigValues.itemsUniqueConfig.getString(ItemsUniqueConfig.HUNTING_SET_LEGGINGS)) > 0) {
//
//                CustomItemConstructor.staticCustomItemHashMap.put(leggings, dropWeight(ConfigValues.itemsUniqueConfig.getString(ItemsUniqueConfig.HUNTING_SET_LEGGINGS)));
//
//            } else {
//
//                //todo: add in case of dynamic
//
//            }
//
//            CustomItemConstructor.customItemList.add(leggings);
//            uniqueItemsList.add(leggings);
//            uniqueItems.put(HUNTING_LEGGINGS, leggings);
//
//            //add boots
//            ItemStack boots = uniqueItemConstructor(ConfigValues.itemsUniqueConfig.getString(ItemsUniqueConfig.HUNTING_SET_BOOTS), UniqueItemType.ELITE_MOB_HUNTING_SET);
//            boots = tempWorthLoreAdder(boots);
//
//            if (dropWeight(ConfigValues.itemsUniqueConfig.getString(ItemsUniqueConfig.HUNTING_SET_BOOTS)) > 0) {
//
//                CustomItemConstructor.staticCustomItemHashMap.put(boots, dropWeight(ConfigValues.itemsUniqueConfig.getString(ItemsUniqueConfig.HUNTING_SET_BOOTS)));
//
//            } else {
//
//                //todo: add in case of dynamic
//
//            }
//
//            CustomItemConstructor.customItemList.add(boots);
//            uniqueItemsList.add(boots);
//            uniqueItems.put(HUNTING_BOOTS, boots);
//
//            //add bow
//            ItemStack bow = uniqueItemConstructor(ConfigValues.itemsUniqueConfig.getString(ItemsUniqueConfig.HUNTING_SET_BOW), UniqueItemType.ELITE_MOB_HUNTING_SET);
//            bow = tempWorthLoreAdder(bow);
//
//            if (dropWeight(ConfigValues.itemsUniqueConfig.getString(ItemsUniqueConfig.HUNTING_SET_BOW)) > 0) {
//
//                CustomItemConstructor.staticCustomItemHashMap.put(bow, dropWeight(ConfigValues.itemsUniqueConfig.getString(ItemsUniqueConfig.HUNTING_SET_BOW)));
//
//            } else {
//
//                //todo: add in case of dynamic
//
//            }
//
//            CustomItemConstructor.customItemList.add(bow);
//            uniqueItemsList.add(bow);
//            uniqueItems.put(HUNTING_BOW, bow);
//
//        }
//
//        if (ConfigValues.itemsUniqueConfig.getBoolean(ItemsUniqueConfig.ENABLE_ZOMBIE_KING_AXE)) {
//
//            //add axe
//            ItemStack zombieKingAxe = uniqueItemConstructor(ConfigValues.itemsUniqueConfig.getString(ItemsUniqueConfig.ZOMBIE_KING_AXE), UniqueItemType.ZOMBIE_KING_AXE);
//            zombieKingAxe = tempWorthLoreAdder(zombieKingAxe);
//
//            //This item only ever drops when the Zombie King gets slain
//            uniqueItemsList.add(zombieKingAxe);
//            uniqueItems.put(ZOMBIE_KING_AXE, zombieKingAxe);
//
//        }
//
//        if (ConfigValues.itemsUniqueConfig.getBoolean(ItemsUniqueConfig.ENABLE_KRAKEN_FISHING_ROD)) {
//
//            //add axe
//            ItemStack depthsSeeker = uniqueItemConstructor(ConfigValues.itemsUniqueConfig.getString(ItemsUniqueConfig.DEPTHS_SEEKER), UniqueItemType.DEPTHS_SEEKER);
//            depthsSeeker = tempWorthLoreAdder(depthsSeeker);
//
//            //This item only ever drops when the Zombie King gets slain
//            uniqueItemsList.add(depthsSeeker);
//            uniqueItems.put(DEPTHS_SEEKER, depthsSeeker);
//
//        }
//
//        if (ConfigValues.itemsUniqueConfig.getBoolean(ItemsUniqueConfig.ENABLE_GREED)) {
//
//            //add axe
//            ItemStack greed = uniqueItemConstructor(ConfigValues.itemsUniqueConfig.getString(ItemsUniqueConfig.GREED), UniqueItemType.GREED);
//            greed = tempWorthLoreAdder(greed);
//
//            //This item only ever drops when the Zombie King gets slain
//            uniqueItemsList.add(greed);
//            uniqueItems.put(GREED, greed);
//
//        }
//
//        if (ConfigValues.itemsUniqueConfig.getBoolean(ItemsUniqueConfig.ENABLE_THE_FELLER)) {
//
//            //add axe
//            ItemStack theFeller = uniqueItemConstructor(ConfigValues.itemsUniqueConfig.getString(ItemsUniqueConfig.THE_FELLER), UniqueItemType.THE_FELLER);
//            theFeller = tempWorthLoreAdder(theFeller);
//
//            //This item only ever drops when the Zombie King gets slain
//            uniqueItemsList.add(theFeller);
//            uniqueItems.put(THE_FELLER, theFeller);
//
//        }
//
//    }

    private ItemStack tempWorthLoreAdder(ItemStack itemStack) {

        if (!ConfigValues.itemsUniqueConfig.getBoolean(ItemsUniqueConfig.SHOW_ITEM_WORTH)) return itemStack;

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

//    private ItemStack uniqueItemConstructor(String string, UniqueItemType uniqueItemType) {
//
//        ItemStack itemStack = new ItemStack(itemMaterial(string), 1);
//        ItemMeta itemMeta = itemStack.getItemMeta();
//        itemMeta.setDisplayName(itemName(ChatColorConverter.chatColorConverter(string)));
//        itemMeta.setLore(itemLore(ChatColorConverter.chatColorConverter(string)));
//
//        if (enchantments(string).size() > 0) {
//
//
//            for (String line : enchantments(string)) {
//
//                String[] splitLine = line.split(",");
//                Enchantment enchantmentType = Enchantment.getByName(splitLine[0]);
//                int enchantmentLevel = Integer.parseInt(splitLine[1]);
//                itemMeta.addEnchant(enchantmentType, enchantmentLevel, true);
//
//            }
//
//        }
//
//        itemStack.setItemMeta(itemMeta);
//        List<String> tempLore = itemMeta.getLore();
//
//        String obfuscatedPotionEffects = "";
//
//        if (potionEffect(string).size() > 0) {
//
//            for (String line : potionEffect(string)) {
//
//                String[] parsedString = line.split(",");
//
//                String lore = parsedString[0].toLowerCase().substring(0, 1).toUpperCase() +
//                        parsedString[0].toLowerCase().substring(1, (parsedString[0].length())).replace("_", " ") +
//                        " " + parsedString[1];
//
//                tempLore.add(0, lore);
//
//            }
//
//            itemMeta.setLore(tempLore);
//            itemStack.setItemMeta(itemMeta);
//
//            for (String line : potionEffect(string)) {
//
////                obfuscatedPotionEffects += customItemConstructor.loreObfuscator(" " + line);
//
//            }
//
//            List<String> obfuscatedLore = new ArrayList<>();
//
//            /*
//            Hide unique lore
//             */
//            String uniqueLore = "";
//            switch (uniqueItemType) {
//
//                case ELITE_MOB_HUNTING_SET:
//                    uniqueLore = HUNTING_SET_IDENTIFIER;
//                    break;
//                case ZOMBIE_KING_AXE:
//                    uniqueLore = ZOMBIE_KING_AXE_IDENTIFIER;
//                    break;
//
//            }
//
//            for (String line : itemMeta.getLore()) {
//
//                if (line.equals(itemMeta.getLore().get(0))) {
//
////                    obfuscatedLore.add(line + obfuscatedPotionEffects + customItemConstructor.loreObfuscator(uniqueLore));
//
//                } else {
//
//                    obfuscatedLore.add(line);
//
//                }
//
//            }
//
//            itemMeta.setLore(obfuscatedLore);
//
//        }
//
//        itemStack.setItemMeta(itemMeta);
////        ObfuscatedSignatureLoreData.obfuscateSignatureData(itemStack);
//
//        if (ConfigValues.defaultConfig.getBoolean(DefaultConfig.HIDE_ENCHANTMENTS_ATTRIBUTE))
//            EnchantmentHider.hideEnchantments(itemStack);
//
//        return itemStack;
//
//    }

    private enum UniqueItemType {

        ELITE_MOB_HUNTING_SET,
        ZOMBIE_KING_AXE,
        DEPTHS_SEEKER,
        GREED,
        THE_FELLER

    }

    public boolean huntingSetItemDetector(ItemStack itemStack) {

        if (itemStack == null) return false;
        if (itemStack.getType() == Material.AIR) return false;
        if (!itemStack.hasItemMeta()) return false;
        if (!itemStack.getItemMeta().hasLore()) return false;

        for (String string : itemStack.getItemMeta().getLore()) {

//            if (string.contains(customItemConstructor.loreObfuscator(HUNTING_SET_IDENTIFIER))) {
//
//                return true;
//
//            }

        }

        return false;

    }

    public static boolean zombieKingAxeDetector(ItemStack itemStack) {

        if (itemStack == null) return false;
        if (itemStack.getType() == Material.AIR) return false;
        if (!itemStack.hasItemMeta()) return false;
        if (!itemStack.getItemMeta().hasLore()) return false;

        for (String string : itemStack.getItemMeta().getLore()) {

//            if (string.contains(customItemConstructor.loreObfuscator(ZOMBIE_KING_AXE_IDENTIFIER))) {
//
//                return true;
//
//            }

        }

        return false;

    }

}
