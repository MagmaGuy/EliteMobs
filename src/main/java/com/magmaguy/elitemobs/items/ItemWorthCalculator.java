package com.magmaguy.elitemobs.items;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.EconomySettingsConfig;
import com.magmaguy.elitemobs.config.enchantments.EnchantmentsConfig;
import com.magmaguy.elitemobs.items.potioneffects.ElitePotionEffect;
import com.magmaguy.elitemobs.items.potioneffects.ElitePotionEffectContainer;
import com.magmaguy.elitemobs.utils.Round;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.List;

import static com.magmaguy.elitemobs.config.EconomySettingsConfig.resaleValue;

public class ItemWorthCalculator {

    public static double determineItemWorth(ItemStack itemStack) {

        double itemWorth = 1 + Round.twoDecimalPlaces(
                EconomySettingsConfig.getMaterialWorth(itemStack.getType()) +
                        getAllEnchantmentValues(itemStack) +
                        getAllPotionEffectValues(itemStack.getItemMeta()));

        return itemWorth;

    }

    public static double determineItemWorth(Material material, HashMap<Enchantment, Integer> enchantmentsMap, List<String> potionsMap, HashMap<String, Integer> customEnchantments) {

        double itemWorth = 1 + Round.twoDecimalPlaces(
                EconomySettingsConfig.getMaterialWorth(material) +
                        getAllEnchantmentValues(enchantmentsMap, customEnchantments) +
                        getAllPotionEffectValues(potionsMap));

        return itemWorth;

    }

    public static double determineResaleWorth(ItemStack itemStack) {

        double resaleWorth = Round.twoDecimalPlaces(determineItemWorth(itemStack) * (resaleValue / 100));

        return resaleWorth;

    }

    public static double determineResaleWorth(Material material, HashMap<Enchantment, Integer> enchantmentsMap, List<String> potionsMap, HashMap<String, Integer> customEnchantments) {

        double resaleWorth = Math.round((determineItemWorth(material, enchantmentsMap, potionsMap, customEnchantments) * (resaleValue / 100)) * 100.0) / 100.0;

        return resaleWorth;

    }

    private static double getAllEnchantmentValues(ItemStack itemStack) {
        double value = 0;
        for (Enchantment enchantment : itemStack.getEnchantments().keySet()) {
            if (EliteEnchantments.isPotentialEliteEnchantment(enchantment))
                if (ItemTagger.getEnchantment(itemStack.getItemMeta(), enchantment.getKey()) != 0) {
                    value += EnchantmentsConfig.getEnchantment(enchantment).getValue() * ItemTagger.getEnchantment(itemStack.getItemMeta(), enchantment.getKey());
                    continue;
                }
            value += EnchantmentsConfig.getEnchantment(enchantment).getValue() * itemStack.getEnchantments().get(enchantment);
        }
        value += EnchantmentsConfig.getEnchantment("hunter.yml").getValue() * ItemTagger.getEnchantment(itemStack.getItemMeta(), new NamespacedKey(MetadataHandler.PLUGIN, "hunter"));
        value += EnchantmentsConfig.getEnchantment("flamethrower.yml").getValue() * ItemTagger.getEnchantment(itemStack.getItemMeta(), new NamespacedKey(MetadataHandler.PLUGIN, "flamethrower"));
        return value;
    }


    private static double getAllEnchantmentValues(HashMap<Enchantment, Integer> enchantmentsMap, HashMap<String, Integer> customEnchantments) {
        double value = 0;
        for (Enchantment enchantment : enchantmentsMap.keySet())
            value += EnchantmentsConfig.getEnchantment(enchantment).getValue() * enchantmentsMap.get(enchantment);
        for (String customEnchantment : customEnchantments.keySet())
            value += EnchantmentsConfig.getEnchantment(customEnchantment.toLowerCase() + ".yml").getValue() * customEnchantments.get(customEnchantment);
        return value;
    }

    private static double getAllPotionEffectValues(ItemMeta itemMeta) {
        double value = 0;
        for (ElitePotionEffect elitePotionEffect : ElitePotionEffectContainer.getElitePotionEffects(itemMeta, ElitePotionEffect.ApplicationMethod.CONTINUOUS))
            value += (elitePotionEffect.getValue() * (elitePotionEffect.getPotionEffect().getAmplifier() + 1));
        for (ElitePotionEffect elitePotionEffect : ElitePotionEffectContainer.getElitePotionEffects(itemMeta, ElitePotionEffect.ApplicationMethod.ONHIT))
            value += (elitePotionEffect.getValue() * (elitePotionEffect.getPotionEffect().getAmplifier() + 1));
        return value;
    }

    private static double getAllPotionEffectValues(List<String> potionsMap) {
        double value = 0;
        for (String string : potionsMap) {
            ElitePotionEffect elitePotionEffect = new ElitePotionEffect(string);
            value += (elitePotionEffect.getValue() * (elitePotionEffect.getPotionEffect().getAmplifier() + 1));
        }
        return value;
    }


}
