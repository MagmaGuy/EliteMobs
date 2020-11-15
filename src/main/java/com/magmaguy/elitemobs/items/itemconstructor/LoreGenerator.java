package com.magmaguy.elitemobs.items.itemconstructor;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.config.EconomySettingsConfig;
import com.magmaguy.elitemobs.config.ItemSettingsConfig;
import com.magmaguy.elitemobs.config.enchantments.EnchantmentsConfig;
import com.magmaguy.elitemobs.config.potioneffects.PotionEffectsConfig;
import com.magmaguy.elitemobs.items.EliteEnchantments;
import com.magmaguy.elitemobs.items.ItemTagger;
import com.magmaguy.elitemobs.items.ItemTierFinder;
import com.magmaguy.elitemobs.items.ItemWorthCalculator;
import com.magmaguy.elitemobs.items.potioneffects.ElitePotionEffectContainer;
import com.magmaguy.elitemobs.mobconstructor.EliteMobEntity;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class LoreGenerator {

    public static ItemMeta generateLore(ItemMeta itemMeta, Material material,
                                        HashMap<Enchantment, Integer> enchantmentMap,
                                        HashMap<String, Integer> customEnchantments,
                                        List<String> potionList, List<String> loreList,
                                        EliteMobEntity eliteMobEntity) {

        List<String> lore = new ArrayList<>();

        for (Object object : ItemSettingsConfig.loreStructure) {

            String string = (String) object;

            if (string.contains("$enchantments")) {

                if (ItemSettingsConfig.useEliteEnchantments) {
                    if (!enchantmentMap.isEmpty()) {
                        lore.addAll(enchantmentLore(enchantmentMap));
                        lore.addAll(customEnchantmentLore(customEnchantments));
                    }
                }
            } else if (string.equals("$itemSource")) {
                if (eliteMobEntity != null)
                    lore.add(itemSource(eliteMobEntity));
            } else if (string.contains("$potionEffect"))
                lore.addAll(setPotionsLore(potionList));
            else if (string.contains("$loreResaleValue"))
                lore.add(itemResaleWorth(material, enchantmentMap, customEnchantments, potionList));
            else if (string.contains("$tier"))
                lore.add(string.replace("$tier", ItemTierFinder.findGenericTier(material, enchantmentMap) + ""));
            else if (string.contains("$customLore")) {
                if (loreList != null)
                    lore.addAll(colorizedLore(loreList));
            } else
                lore.add(string);

        }

        //Tag the item
        ItemTagger.registerEnchantments(itemMeta, enchantmentMap);
        ItemTagger.registerCustomEnchantments(itemMeta, customEnchantments);
        //Tag the potion effects
        new ElitePotionEffectContainer(itemMeta, potionList);
        itemMeta.setLore(lore);

        return itemMeta;

    }

    public static ItemMeta generateLore(ItemMeta itemMeta,
                                        Material material,
                                        HashMap<Enchantment, Integer> enchantmentMap,
                                        HashMap<String, Integer> customEnchantments,
                                        EliteMobEntity eliteMobEntity) {

        List<String> lore = new ArrayList<>();

        itemMeta = applyVanillaEnchantments(enchantmentMap, itemMeta);

        for (Object object : ItemSettingsConfig.loreStructure) {

            String string = (String) object;

            if (string.contains("$potionEffect")) {
                //TODO: Add potion effects to dynamic items
            } else if (string.contains("$enchantments")) {
                if (ItemSettingsConfig.useEliteEnchantments) {
                    lore.addAll(enchantmentLore(enchantmentMap));
                    lore.addAll(customEnchantmentLore(customEnchantments));
                }
            } else if (string.contains("$loreResaleValue"))
                lore.add(itemResaleWorth(material, enchantmentMap, customEnchantments, new ArrayList<>()));
            else if (string.contains("$tier"))
                lore.add(string.replace("$tier", ItemTierFinder.findGenericTier(material, enchantmentMap) + ""));
            else if (string.equals("$itemSource")) {
                if (eliteMobEntity != null) {
                    lore.add(itemSource(eliteMobEntity));
                }
            } else if (string.equalsIgnoreCase("$customLore")) {
                //has none
            } else
                lore.add(string);

        }

        itemMeta.setLore(lore);

        ItemTagger.registerEnchantments(itemMeta, enchantmentMap);
        ItemTagger.registerCustomEnchantments(itemMeta, customEnchantments);

        return itemMeta;

    }

    private static List<String> enchantmentLore(HashMap<Enchantment, Integer> enchantmentMap) {

        List<String> enchantmentsLore = new ArrayList<>();

        for (Enchantment enchantment : enchantmentMap.keySet()) {

            if (enchantment.getName().contains("CURSE")) {
                String loreLine = ChatColorConverter.convert("&c" + getEnchantmentName(enchantment) + " " + enchantmentMap.get(enchantment));
                enchantmentsLore.add(loreLine);
                continue;
            }

            if (enchantmentMap.get(enchantment) > enchantment.getMaxLevel() &&
                    EliteEnchantments.isPotentialEliteEnchantment(enchantment)) {

                String loreLine1 = ChatColorConverter.convert("&7" + getEnchantmentName(enchantment) + " " + enchantment.getMaxLevel());
                String loreLine2 = ChatColorConverter.convert("&7" + ItemSettingsConfig.eliteEnchantLoreString + " " + getEnchantmentName(enchantment) + " " + (enchantmentMap.get(enchantment) - enchantment.getMaxLevel()));
                enchantmentsLore.add(loreLine1);
                enchantmentsLore.add(loreLine2);

            } else {
                String loreLine = ChatColorConverter.convert("&7" + getEnchantmentName(enchantment) + " " + enchantmentMap.get(enchantment));
                enchantmentsLore.add(loreLine);
            }

        }

        return enchantmentsLore;

    }

    private static String getEnchantmentName(Enchantment enchantment) {

        return EnchantmentsConfig.getEnchantment(enchantment).getName();

    }

    private static List<String> customEnchantmentLore(HashMap<String, Integer> customEnchantments) {

        List<String> customEnchantmentLore = new ArrayList<>();

        for (String string : customEnchantments.keySet()) {

            String loreLine;
            loreLine = ChatColorConverter.convert("&6" + getCustomEnchantmentName(string) + " " + customEnchantments.get(string));
            customEnchantmentLore.add(loreLine);

        }

        return customEnchantmentLore;

    }

    private static String getCustomEnchantmentName(String customEnchantmentKey) {

        if (EnchantmentsConfig.getEnchantment(customEnchantmentKey.toLowerCase() + ".yml") == null) {

            Bukkit.getLogger().warning("[EliteMobs] Missing enchantment name " + customEnchantmentKey);
            Bukkit.getLogger().warning("[EliteMobs] Report this to the dev!");
            return customEnchantmentKey;

        }

        return EnchantmentsConfig.getEnchantment(customEnchantmentKey.toLowerCase() + ".yml").getName();

    }

    private static ItemMeta applyVanillaEnchantments(HashMap<Enchantment, Integer> enchantmentMap, ItemMeta itemMeta) {

        for (Enchantment enchantment : enchantmentMap.keySet())
            if (EliteEnchantments.isPotentialEliteEnchantment(enchantment) && enchantmentMap.get(enchantment) > enchantment.getMaxLevel())
                itemMeta.addEnchant(enchantment, enchantment.getMaxLevel(), true);
            else
                itemMeta.addEnchant(enchantment, enchantmentMap.get(enchantment), true);

        return itemMeta;

    }

    private static List<String> setPotionsLore(List<String> potionList) {

        List<String> potionsLore = new ArrayList<>();

        if (potionList == null || potionList.isEmpty() || potionList.get(0) == null) return potionsLore;

        for (String string : potionList) {
            String loreLine = ChatColorConverter.convert("&2" + getPotionName(string.split(",")[0]) + " " + (Integer.valueOf(string.split(",")[1]) + 1));
            potionsLore.add(loreLine);
        }

        return potionsLore;

    }

    private static String getPotionName(String string) {

        if (PotionEffectsConfig.getPotionEffect(string + ".yml") == null) {
            Bukkit.getLogger().warning("[EliteMobs] Missing potion name " + string);
            Bukkit.getLogger().warning("[EliteMobs] Report this to the dev!");
            return string;
        }

        return PotionEffectsConfig.getPotionEffect(string + ".yml").getName();

    }

    private static String itemResaleWorth(Material material,
                                          HashMap<Enchantment, Integer> enchantmentMap,
                                          HashMap<String, Integer> customEnchantments,
                                          List<String> potionList) {

        return ItemSettingsConfig.loreResale
                .replace("$resale", ItemWorthCalculator.determineResaleWorth(material, enchantmentMap, potionList, customEnchantments) + "")
                .replace("$currencyName", EconomySettingsConfig.currencyName);

    }

    private static String itemSource(EliteMobEntity eliteMobEntity) {

        String itemSource;

        if (eliteMobEntity.getLivingEntity() != null) {
            String uncoloredString = ItemSettingsConfig.mobItemSource.replace("$mob", eliteMobEntity.getName());
            itemSource = ChatColorConverter.convert(uncoloredString);
        } else
            itemSource = ItemSettingsConfig.shopItemSource;

        return itemSource;

    }

    private static List<String> colorizedLore(List<String> rawLore) {

        List<String> colorizedLore = new ArrayList<>();

        for (String string : rawLore)
            colorizedLore.add(ChatColorConverter.convert(string));

        return colorizedLore;

    }

}
