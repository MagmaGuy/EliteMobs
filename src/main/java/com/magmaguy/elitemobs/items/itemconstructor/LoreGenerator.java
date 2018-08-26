package com.magmaguy.elitemobs.items.itemconstructor;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.*;
import com.magmaguy.elitemobs.items.ItemTierFinder;
import com.magmaguy.elitemobs.items.ItemWorthCalculator;
import com.magmaguy.elitemobs.items.ObfuscatedSignatureLoreData;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class LoreGenerator {

    public static ItemMeta generateLore(ItemMeta itemMeta, Material material, HashMap<Enchantment, Integer> enchantmentMap,
                                        List<String> potionList, List<String> loreList) {

        List<String> lore = new ArrayList<>();

        for (Object object : ConfigValues.itemsCustomLootSettingsConfig.getList(ItemsCustomLootSettingsConfig.LORE_STRUCTURE)) {

            String string = (String) object;

            if (string.contains("$enchantments")) {
                if (ConfigValues.itemsDropSettingsConfig.getBoolean(ItemsDropSettingsConfig.ENABLE_CUSTOM_ENCHANTMENT_SYSTEM)) {
                    lore.addAll(enchantmentLore(enchantmentMap));
                }
            } else if (string.contains("$potionEffect"))
                lore.addAll(potionsLore(potionList));
            else if (string.contains("$itemValue"))
                lore.add(itemWorth(material, enchantmentMap, potionList));
            else if (string.contains("$tier"))
                lore.add(string.replace("$tier", ItemTierFinder.findGenericTier(material, enchantmentMap) + ""));
            else if (string.contains("$customLore")) {
                if (loreList != null)
                    lore.addAll(loreList);
            } else
                lore.add(string);

        }

        /*
        Obfuscate potion and enchantment info
         */
        lore = generateObfuscatedLore(lore, getObfuscatedLoreString(enchantmentMap, potionList));
        itemMeta.setLore(lore);

        return itemMeta;

    }

    public static ItemMeta generateLore(ItemMeta itemMeta, Material material, HashMap<Enchantment, Integer> enchantmentMap, LivingEntity livingEntity) {

        List<String> lore = new ArrayList<>();

        itemMeta = applyVanillaEnchantments(enchantmentMap, itemMeta);

        for (Object object : ConfigValues.itemsProceduralSettingsConfig.getList(ItemsProceduralSettingsConfig.LORE_STRUCTURE)) {

            String string = (String) object;

            if (string.contains("$potionEffect")) {
                //TODO: Add potion effects to dynamic items
            } else if (string.contains("$enchantments")) {
                if (ConfigValues.itemsDropSettingsConfig.getBoolean(ItemsDropSettingsConfig.ENABLE_CUSTOM_ENCHANTMENT_SYSTEM)) {
                    lore.addAll(enchantmentLore(enchantmentMap));
                }
            } else if (string.contains("$itemValue"))
                lore.add(itemWorth(material, enchantmentMap));
            else if (string.contains("$tier"))
                lore.add(string.replace("$tier", ItemTierFinder.findGenericTier(material, enchantmentMap) + ""));
            else if (string.equals("$itemSource"))
                lore.add(itemSource(livingEntity));
            else
                lore.add(string);

        }

        /*
        Obfuscate potion and enchantment info
         */
        lore = generateObfuscatedLore(lore, getObfuscatedLoreString(enchantmentMap));
        itemMeta.setLore(lore);

        return itemMeta;

    }


    private static List<String> generateObfuscatedLore(List<String> loreList, String obfuscatedLoreString) {

        List<String> newList = new ArrayList<>();

        int counter = 0;

        for (String string : loreList) {

            if (counter == 0)
                newList.add(string + obfuscatedLoreString);
            else
                newList.add(string);

            counter++;

        }

        return newList;

    }

    public static final String OBFUSCATED_ENCHANTMENTS = "EliteEnchantments";
    public static final String OBFUSCATED_POTIONS = "ElitePotions";

    private static String getObfuscatedLoreString(HashMap<Enchantment, Integer> enchantmentMap, List<String> potionList) {

        StringBuilder obfuscatedString = new StringBuilder();

        obfuscatedString.append(ObfuscatedSignatureLoreData.ITEM_SIGNATURE).append(",");

        if (!enchantmentMap.isEmpty()) {
            obfuscatedString.append(OBFUSCATED_ENCHANTMENTS).append(",");
            for (Enchantment enchantment : enchantmentMap.keySet())
                obfuscatedString.append(enchantment.getName()).append(":").append(enchantmentMap.get(enchantment)).append(",");
        }

        if (potionList != null && !potionList.isEmpty()) {
            obfuscatedString.append(OBFUSCATED_POTIONS).append(",");
            for (String string : potionList)
                obfuscatedString.append(string.replace(",", ":")).append(",");
        }

        String finalString = stringObfuscator(obfuscatedString);

        return finalString;

    }

    private static String getObfuscatedLoreString(HashMap<Enchantment, Integer> enchantmentMap) {

        StringBuilder obfuscatedString = new StringBuilder();

        obfuscatedString.append(ObfuscatedSignatureLoreData.ITEM_SIGNATURE);

        if (!enchantmentMap.isEmpty()) {
            obfuscatedString.append(OBFUSCATED_ENCHANTMENTS).append(",");
            for (Enchantment enchantment : enchantmentMap.keySet())
                obfuscatedString.append(enchantment.getName()).append(":").append(enchantmentMap.get(enchantment)).append(",");
        }

        String finalString = stringObfuscator(obfuscatedString);

        return finalString;

    }

    private static String stringObfuscator(StringBuilder originalString) {

        String insert = "§";
        int period = 1;

        StringBuilder stringBuilder = new StringBuilder(originalString.length() + insert.length() * (originalString.length() / period));

        int index = 0;
        String prefix = "";

        while (index < originalString.length()) {

            stringBuilder.append(prefix);
            prefix = insert;
            stringBuilder.append(originalString, index, Math.min(index + period, originalString.length()));
            index += period;

        }

        String finalString = stringBuilder.toString();

        finalString = "§" + finalString;

        return finalString;

    }

    private static List<String> enchantmentLore(HashMap<Enchantment, Integer> enchantmentMap) {

        List<String> enchantmentsLore = new ArrayList<>();

        for (Enchantment enchantment : enchantmentMap.keySet()) {
            String loreLine;
            if (enchantment.getName().contains("CURSE"))
                loreLine = ChatColorConverter.chatColorConverter("&c" + getEnchantmentName(enchantment) + " " + enchantmentMap.get(enchantment));
            else
                loreLine = ChatColorConverter.chatColorConverter("&7" + getEnchantmentName(enchantment) + " " + enchantmentMap.get(enchantment));
            enchantmentsLore.add(loreLine);
        }

        return enchantmentsLore;

    }

    private static String getEnchantmentName(Enchantment enchantment) {

        if (!ConfigValues.itemsDropSettingsConfig.getKeys(true).contains(ItemsDropSettingsConfig.ENCHANTMENT_NAME + enchantment.getName())) {
            Bukkit.getLogger().warning("[EliteMobs] Missing enchantment name " + enchantment.getName());
            Bukkit.getLogger().warning("[EliteMobs] Report this to the dev!");
            return enchantment.getName();
        }

        return ConfigValues.itemsDropSettingsConfig.getString(ItemsDropSettingsConfig.ENCHANTMENT_NAME + enchantment.getName());

    }

    private static ItemMeta applyVanillaEnchantments(HashMap<Enchantment, Integer> enchantmentMap, ItemMeta itemMeta) {

        for (Enchantment enchantment : enchantmentMap.keySet()) {
            if (enchantmentMap.get(enchantment) > 5)
                itemMeta.addEnchant(enchantment, 5, true);
            else
                itemMeta.addEnchant(enchantment, enchantmentMap.get(enchantment), true);
        }

        return itemMeta;

    }

    private static List<String> potionsLore(List<String> potionList) {

        List<String> potionsLore = new ArrayList<>();


        if (potionList == null || potionList.isEmpty()) return potionsLore;

        for (String string : potionList) {
            String loreLine = ChatColorConverter.chatColorConverter("&7" + getPotionName(string.split(",")[0]) + " " + string.split(",")[1]);
            potionsLore.add(loreLine);
        }

        return potionsLore;

    }

    private static String getPotionName(String string) {

        if (!ConfigValues.itemsDropSettingsConfig.getKeys(true).contains(ItemsDropSettingsConfig.POTION_EFFECT_NAME + string)) {
            Bukkit.getLogger().warning("[EliteMobs] Missing potion name " + string);
            Bukkit.getLogger().warning("[EliteMobs] Report this to the dev!");
            return string;
        }

        return ConfigValues.itemsDropSettingsConfig.getString(ItemsDropSettingsConfig.POTION_EFFECT_NAME + string);

    }

    private static String itemWorth(Material material, HashMap<Enchantment, Integer> enchantmentMap, List<String> potionList) {

        return ConfigValues.itemsDropSettingsConfig.getString(ItemsDropSettingsConfig.LORE_WORTH)
                .replace("$worth", ItemWorthCalculator.determineItemWorth(material, enchantmentMap, potionList) + "")
                .replace("$currencyName", ConfigValues.economyConfig.getString(EconomySettingsConfig.CURRENCY_NAME));

    }

    private static String itemWorth(Material material, HashMap<Enchantment, Integer> enchantmentMap) {

        return ConfigValues.itemsDropSettingsConfig.getString(ItemsDropSettingsConfig.LORE_WORTH)
                .replace("$worth", ItemWorthCalculator.determineItemWorth(material, enchantmentMap) + "")
                .replace("$currencyName", ConfigValues.economyConfig.getString(EconomySettingsConfig.CURRENCY_NAME));

    }

    private static String itemSource(LivingEntity livingEntity) {

        String itemSource;

        if (livingEntity != null) {

            itemSource = ConfigValues.itemsProceduralSettingsConfig.getString(ItemsProceduralSettingsConfig.LORE_MOB_LEVEL_SOURCE).replace("$level",
                    livingEntity.getMetadata(MetadataHandler.ELITE_MOB_MD).get(0).asInt() + "");

            String newName = "";

            if (livingEntity.getType().name().contains("_")) {

                String[] tempSubList = livingEntity.getType().name().split("_");

                for (String string : tempSubList) {

                    string = string.toLowerCase().substring(0, 1).toUpperCase() + " ";
                    newName += string;

                }

            } else
                newName = livingEntity.getType().name().substring(0, 1) + livingEntity.getType().name().substring(1).toLowerCase();


            itemSource = itemSource.replace("$mob", newName);


        } else
            itemSource = ConfigValues.itemsProceduralSettingsConfig.getString(ItemsProceduralSettingsConfig.LORE_SHOP_SOURCE);


        return itemSource;

    }

}
