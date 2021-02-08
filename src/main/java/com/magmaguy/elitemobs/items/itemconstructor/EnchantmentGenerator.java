package com.magmaguy.elitemobs.items.itemconstructor;

import com.magmaguy.elitemobs.config.ItemSettingsConfig;
import com.magmaguy.elitemobs.config.ProceduralItemGenerationSettingsConfig;
import com.magmaguy.elitemobs.config.enchantments.EnchantmentsConfig;
import com.magmaguy.elitemobs.config.enchantments.EnchantmentsConfigFields;
import com.magmaguy.elitemobs.items.MaterialTier;
import com.magmaguy.elitemobs.items.customenchantments.CriticalStrikesEnchantment;
import com.magmaguy.elitemobs.items.customenchantments.HunterEnchantment;
import com.magmaguy.elitemobs.utils.VersionChecker;
import com.magmaguy.elitemobs.items.EliteEnchantments;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class EnchantmentGenerator {

    public static ItemMeta generateEnchantments(ItemMeta itemMeta, HashMap<Enchantment, Integer> enchantmentMap) {

        for (Enchantment enchantment : enchantmentMap.keySet()) {
            if (enchantmentMap.get(enchantment) > enchantment.getMaxLevel() && ItemSettingsConfig.useEliteEnchantments) {
                if (EliteEnchantments.isPotentialEliteEnchantment(enchantment)) {
                    if (enchantmentMap.get(enchantment) > enchantment.getMaxLevel()) {
                        itemMeta.addEnchant(enchantment, enchantment.getMaxLevel(), true);
                    } else
                        itemMeta.addEnchant(enchantment, enchantmentMap.get(enchantment), true);
                } else
                    itemMeta.addEnchant(enchantment, enchantmentMap.get(enchantment), true);

            } else {
                itemMeta.addEnchant(enchantment, enchantmentMap.get(enchantment), true);
            }


        }
        return itemMeta;

    }

    /*
    Note: For procedurally generated items the enchantments only get applied to the item at the lore phase
    This only gathers the list of enchantments to be applied
     */
    public static HashMap<Enchantment, Integer> generateEnchantments(double itemTier, Material material, ItemMeta itemMeta) {

        HashMap<Enchantment, Integer> enchantmentMap = new HashMap<>();

        itemTier -= MaterialTier.getMaterialTier(material);

        /*
        No enchantments for items too low tier to have one
         */
        if (itemTier < 1) return enchantmentMap;

        HashMap<Enchantment, Integer> validSecondaryEnchantments = new HashMap();

        /*
        Primary enchantments get instantly validated and applies since there is only one per item type
        Secondary enchantments get added to a common pool to be randomized later
         */
        switch (material) {
            case TRIDENT:
                validSecondaryEnchantments.putAll(validateSecondaryEnchantments("LOYALTY"));
                validSecondaryEnchantments.putAll(validateSecondaryEnchantments("RIPTIDE"));
                validSecondaryEnchantments.putAll(validateSecondaryEnchantments("CHANNELING"));
                validSecondaryEnchantments.putAll(validateSecondaryEnchantments("IMPALING"));
            case DIAMOND_SWORD:
            case GOLDEN_SWORD:
            case IRON_SWORD:
            case STONE_SWORD:
            case WOODEN_SWORD:
                enchantmentMap.putAll(validateAndApplyPrimaryEnchantment("DAMAGE_ALL", itemTier));
                validSecondaryEnchantments.putAll(validateSecondaryEnchantments("DAMAGE_ARTHROPODS"));
                validSecondaryEnchantments.putAll(validateSecondaryEnchantments("DAMAGE_UNDEAD"));
                validSecondaryEnchantments.putAll(validateSecondaryEnchantments("DURABILITY"));
                validSecondaryEnchantments.putAll(validateSecondaryEnchantments("FIRE_ASPECT"));
                validSecondaryEnchantments.putAll(validateSecondaryEnchantments("KNOCKBACK"));
                validSecondaryEnchantments.putAll(validateSecondaryEnchantments("LOOT_BONUS_MOBS"));
                validSecondaryEnchantments.putAll(validateSecondaryEnchantments("MENDING"));
                validSecondaryEnchantments.putAll(validateSecondaryEnchantments("SWEEPING_EDGE"));
                validSecondaryEnchantments.putAll(validateSecondaryEnchantments("VANISHING_CURSE"));
                break;
            case BOW:
                enchantmentMap.putAll(validateAndApplyPrimaryEnchantment("ARROW_DAMAGE", itemTier));
                validSecondaryEnchantments.putAll(validateSecondaryEnchantments("ARROW_FIRE"));
                validSecondaryEnchantments.putAll(validateSecondaryEnchantments("ARROW_INFINITE"));
                validSecondaryEnchantments.putAll(validateSecondaryEnchantments("ARROW_KNOCKBACK"));
                validSecondaryEnchantments.putAll(validateSecondaryEnchantments("DURABILITY"));
                validSecondaryEnchantments.putAll(validateSecondaryEnchantments("MENDING"));
                validSecondaryEnchantments.putAll(validateSecondaryEnchantments("VANISHING_CURSE"));
                break;
            case CROSSBOW:
                enchantmentMap.putAll(validateAndApplyPrimaryEnchantment("ARROW_DAMAGE", itemTier));
                validSecondaryEnchantments.putAll(validateSecondaryEnchantments("QUICK_CHARGE"));
                validSecondaryEnchantments.putAll(validateSecondaryEnchantments("MULTISHOT"));
                validSecondaryEnchantments.putAll(validateSecondaryEnchantments("PIERCING"));
                validSecondaryEnchantments.putAll(validateSecondaryEnchantments("DURABILITY"));
                validSecondaryEnchantments.putAll(validateSecondaryEnchantments("MENDING"));
                validSecondaryEnchantments.putAll(validateSecondaryEnchantments("VANISHING_CURSE"));
                break;
            case DIAMOND_PICKAXE:
            case GOLDEN_PICKAXE:
            case IRON_PICKAXE:
            case STONE_PICKAXE:
            case WOODEN_PICKAXE:
                enchantmentMap.putAll(validateAndApplyPrimaryEnchantment("DIG_SPEED", itemTier));
                validSecondaryEnchantments.putAll(validateSecondaryEnchantments("DURABILITY"));
                validSecondaryEnchantments.putAll(validateSecondaryEnchantments("MENDING"));
                validSecondaryEnchantments.putAll(validateSecondaryEnchantments("VANISHING_CURSE"));
                //TODO: this doesn't take config into account
                if (ThreadLocalRandom.current().nextDouble() < 0.5) {
                    validSecondaryEnchantments.putAll(validateSecondaryEnchantments("LOOT_BONUS_BLOCKS"));
                } else {
                    validSecondaryEnchantments.putAll(validateSecondaryEnchantments("SILK_TOUCH"));
                }
                break;
            case DIAMOND_SHOVEL:
            case GOLDEN_SHOVEL:
            case IRON_SHOVEL:
            case STONE_SHOVEL:
            case WOODEN_SHOVEL:
                enchantmentMap.putAll(validateAndApplyPrimaryEnchantment("DIG_SPEED", itemTier));
                validSecondaryEnchantments.putAll(validateSecondaryEnchantments("DURABILITY"));
                validSecondaryEnchantments.putAll(validateSecondaryEnchantments("MENDING"));
                validSecondaryEnchantments.putAll(validateSecondaryEnchantments("VANISHING_CURSE"));
                if (ThreadLocalRandom.current().nextDouble() < 0.5) {
                    validSecondaryEnchantments.putAll(validateSecondaryEnchantments("LOOT_BONUS_BLOCKS"));
                } else {
                    validSecondaryEnchantments.putAll(validateSecondaryEnchantments("SILK_TOUCH"));
                }
                break;
            case DIAMOND_HOE:
            case GOLDEN_HOE:
            case IRON_HOE:
            case STONE_HOE:
            case WOODEN_HOE:
                if (ItemSettingsConfig.useHoesAsWeapons)
                    enchantmentMap.putAll(validateAndApplyPrimaryEnchantment("DAMAGE_ALL", itemTier));
                enchantmentMap.putAll(validateAndApplyPrimaryEnchantment("DIG_SPEED", itemTier));
                validSecondaryEnchantments.putAll(validateSecondaryEnchantments("DURABILITY"));
                validSecondaryEnchantments.putAll(validateSecondaryEnchantments("MENDING"));
                validSecondaryEnchantments.putAll(validateSecondaryEnchantments("VANISHING_CURSE"));
                if (ThreadLocalRandom.current().nextDouble() < 0.5) {
                    validSecondaryEnchantments.putAll(validateSecondaryEnchantments("LOOT_BONUS_BLOCKS"));
                } else {
                    validSecondaryEnchantments.putAll(validateSecondaryEnchantments("SILK_TOUCH"));
                }
                break;
            case SHIELD:
                enchantmentMap.putAll(validateAndApplyPrimaryEnchantment("DURABILITY", itemTier));
                validSecondaryEnchantments.putAll(validateSecondaryEnchantments("MENDING"));
                validSecondaryEnchantments.putAll(validateSecondaryEnchantments("VANISHING_CURSE"));
                break;
            case DIAMOND_AXE:
            case GOLDEN_AXE:
            case IRON_AXE:
            case STONE_AXE:
            case WOODEN_AXE:
                enchantmentMap.putAll(validateAndApplyPrimaryEnchantment("DAMAGE_ALL", itemTier));
                validSecondaryEnchantments.putAll(validateSecondaryEnchantments("DAMAGE_ARTHROPODS"));
                validSecondaryEnchantments.putAll(validateSecondaryEnchantments("DAMAGE_UNDEAD"));
                validSecondaryEnchantments.putAll(validateSecondaryEnchantments("DURABILITY"));
                validSecondaryEnchantments.putAll(validateSecondaryEnchantments("MENDING"));
                validSecondaryEnchantments.putAll(validateSecondaryEnchantments("VANISHING_CURSE"));
                validSecondaryEnchantments.putAll(validateSecondaryEnchantments("DIG_SPEED"));
                validSecondaryEnchantments.putAll(validateSecondaryEnchantments("LOOT_BONUS_BLOCKS"));
                break;
            case CHAINMAIL_HELMET:
            case DIAMOND_HELMET:
            case GOLDEN_HELMET:
            case IRON_HELMET:
            case LEATHER_HELMET:
            case TURTLE_HELMET:
                enchantmentMap.putAll(validateAndApplyPrimaryEnchantment("PROTECTION_ENVIRONMENTAL", itemTier));
                validSecondaryEnchantments.putAll(validateSecondaryEnchantments("BINDING_CURSE"));
                validSecondaryEnchantments.putAll(validateSecondaryEnchantments("DURABILITY"));
                validSecondaryEnchantments.putAll(validateSecondaryEnchantments("MENDING"));
                validSecondaryEnchantments.putAll(validateSecondaryEnchantments("OXYGEN"));
                validSecondaryEnchantments.putAll(validateSecondaryEnchantments("PROTECTION_EXPLOSIONS"));
                validSecondaryEnchantments.putAll(validateSecondaryEnchantments("PROTECTION_FIRE"));
                validSecondaryEnchantments.putAll(validateSecondaryEnchantments("PROTECTION_PROJECTILE"));
                validSecondaryEnchantments.putAll(validateSecondaryEnchantments("THORNS"));
                validSecondaryEnchantments.putAll(validateSecondaryEnchantments("VANISHING_CURSE"));
                validSecondaryEnchantments.putAll(validateSecondaryEnchantments("WATER_WORKER"));
                break;
            case CHAINMAIL_CHESTPLATE:
            case DIAMOND_CHESTPLATE:
            case GOLDEN_CHESTPLATE:
            case IRON_CHESTPLATE:
            case LEATHER_CHESTPLATE:
                enchantmentMap.putAll(validateAndApplyPrimaryEnchantment("PROTECTION_ENVIRONMENTAL", itemTier));
                validSecondaryEnchantments.putAll(validateSecondaryEnchantments("DURABILITY"));
                validSecondaryEnchantments.putAll(validateSecondaryEnchantments("MENDING"));
                validSecondaryEnchantments.putAll(validateSecondaryEnchantments("PROTECTION_EXPLOSIONS"));
                validSecondaryEnchantments.putAll(validateSecondaryEnchantments("PROTECTION_FIRE"));
                validSecondaryEnchantments.putAll(validateSecondaryEnchantments("PROTECTION_PROJECTILE"));
                validSecondaryEnchantments.putAll(validateSecondaryEnchantments("THORNS"));
                validSecondaryEnchantments.putAll(validateSecondaryEnchantments("VANISHING_CURSE"));
                break;
            case CHAINMAIL_LEGGINGS:
            case DIAMOND_LEGGINGS:
            case GOLDEN_LEGGINGS:
            case IRON_LEGGINGS:
            case LEATHER_LEGGINGS:
                enchantmentMap.putAll(validateAndApplyPrimaryEnchantment("PROTECTION_ENVIRONMENTAL", itemTier));
                validSecondaryEnchantments.putAll(validateSecondaryEnchantments("BINDING_CURSE"));
                validSecondaryEnchantments.putAll(validateSecondaryEnchantments("DURABILITY"));
                validSecondaryEnchantments.putAll(validateSecondaryEnchantments("MENDING"));
                validSecondaryEnchantments.putAll(validateSecondaryEnchantments("PROTECTION_EXPLOSIONS"));
                validSecondaryEnchantments.putAll(validateSecondaryEnchantments("PROTECTION_FIRE"));
                validSecondaryEnchantments.putAll(validateSecondaryEnchantments("PROTECTION_PROJECTILE"));
                validSecondaryEnchantments.putAll(validateSecondaryEnchantments("THORNS"));
                validSecondaryEnchantments.putAll(validateSecondaryEnchantments("VANISHING_CURSE"));
                break;
            case CHAINMAIL_BOOTS:
            case DIAMOND_BOOTS:
            case GOLDEN_BOOTS:
            case IRON_BOOTS:
            case LEATHER_BOOTS:
                enchantmentMap.putAll(validateAndApplyPrimaryEnchantment("PROTECTION_ENVIRONMENTAL", itemTier));
                validSecondaryEnchantments.putAll(validateSecondaryEnchantments("BINDING_CURSE"));
                validSecondaryEnchantments.putAll(validateSecondaryEnchantments("DURABILITY"));
                validSecondaryEnchantments.putAll(validateSecondaryEnchantments("MENDING"));
                validSecondaryEnchantments.putAll(validateSecondaryEnchantments("PROTECTION_EXPLOSIONS"));
                validSecondaryEnchantments.putAll(validateSecondaryEnchantments("PROTECTION_FALL"));
                validSecondaryEnchantments.putAll(validateSecondaryEnchantments("PROTECTION_FIRE"));
                validSecondaryEnchantments.putAll(validateSecondaryEnchantments("PROTECTION_PROJECTILE"));
                validSecondaryEnchantments.putAll(validateSecondaryEnchantments("THORNS"));
                validSecondaryEnchantments.putAll(validateSecondaryEnchantments("VANISHING_CURSE"));
                validSecondaryEnchantments.putAll(validateSecondaryEnchantments("DEPTH_STRIDER"));
                validSecondaryEnchantments.putAll(validateSecondaryEnchantments("FROST_WALKER"));
                break;
            case FISHING_ROD:
                enchantmentMap.putAll(validateAndApplyPrimaryEnchantment("DURABILITY", itemTier));
                validSecondaryEnchantments.putAll(validateSecondaryEnchantments("VANISHING_CURSE"));
                validSecondaryEnchantments.putAll(validateSecondaryEnchantments("MENDING"));
                validSecondaryEnchantments.putAll(validateSecondaryEnchantments("LUCK"));
                validSecondaryEnchantments.putAll(validateSecondaryEnchantments("LURE"));
                break;
            case SHEARS:
                enchantmentMap.putAll(validateAndApplyPrimaryEnchantment("DIG_SPEED", itemTier));
                validSecondaryEnchantments.putAll(validateSecondaryEnchantments("VANISHING_CURSE"));
                validSecondaryEnchantments.putAll(validateSecondaryEnchantments("MENDING"));
                validSecondaryEnchantments.putAll(validateSecondaryEnchantments("DURABILITY"));
                break;
        }

        /*
        Now that the primary enchantments are applied and the secondary enchantments are parsed, apply secondary enchants
         */
        int secondaryEnchantmentTotalParsedLevel = totalSecondaryEnchantmentCount(validSecondaryEnchantments);
        int maxSecondaryEnchantmentLevel = (secondaryEnchantmentTotalParsedLevel < itemTier - 2) ? secondaryEnchantmentTotalParsedLevel : (int) itemTier;
        int secondaryEnchantmentCount = ThreadLocalRandom.current().nextInt(maxSecondaryEnchantmentLevel + 1);

        if (itemTier < 2 || secondaryEnchantmentCount < 1 || validSecondaryEnchantments.size() == 0)
            return enchantmentMap;

        HashMap<Enchantment, Integer> validEnchantmentsClone = (HashMap<Enchantment, Integer>) validSecondaryEnchantments.clone();

        while (secondaryEnchantmentCount != 0) {

            if (validSecondaryEnchantments.size() < 1)
                break;

            int randomIndex = ThreadLocalRandom.current().nextInt(validSecondaryEnchantments.size());

            List<Enchantment> enchantmentList = new ArrayList();

            for (Enchantment enchantment : validSecondaryEnchantments.keySet())
                enchantmentList.add(enchantment);

            Enchantment enchantment = enchantmentList.get(randomIndex);

            validSecondaryEnchantments.put(enchantment, validSecondaryEnchantments.get(enchantment) - 1);

            int finalEnchantLevel = validEnchantmentsClone.get(enchantment) - validSecondaryEnchantments.get(enchantment);
            enchantmentMap.put(enchantment, finalEnchantLevel);

            int newEnchantInt = validSecondaryEnchantments.get(enchantment);

            if (newEnchantInt == 0) validSecondaryEnchantments.remove(enchantment);

            secondaryEnchantmentCount--;

        }

        //this applies the vanilla enchants
        generateEnchantments(itemMeta, enchantmentMap);

        return enchantmentMap;

    }

    public static HashMap<String, Integer> generateCustomEnchantments(double itemTier, Material material) {

        HashMap<String, Integer> enchantmentMap = new HashMap<>();

        /*
        No enchantments for items too low tier to have one
         */
        if (itemTier < 1) return enchantmentMap;

        HashMap<String, Integer> validSecondaryEnchantments = new HashMap();

        /*
        Primary enchantments get instantly validated and applies since there is only one per item type
        Secondary enchantments get added to a common pool to be randomized later
         */
        switch (material) {
            case DIAMOND_SWORD:
            case GOLDEN_SWORD:
            case IRON_SWORD:
            case STONE_SWORD:
            case WOODEN_SWORD:
            case TRIDENT:
                validSecondaryEnchantments.putAll(validateSecondaryCustomEnchantments(CriticalStrikesEnchantment.key));
                break;
            case BOW:
            case CROSSBOW:
                validSecondaryEnchantments.putAll(validateSecondaryCustomEnchantments(CriticalStrikesEnchantment.key));
                break;
            case DIAMOND_PICKAXE:
            case GOLDEN_PICKAXE:
            case IRON_PICKAXE:
            case STONE_PICKAXE:
            case WOODEN_PICKAXE:
                break;
            case DIAMOND_SHOVEL:
            case GOLDEN_SHOVEL:
            case IRON_SHOVEL:
            case STONE_SHOVEL:
            case WOODEN_SHOVEL:
                break;
            case DIAMOND_HOE:
            case GOLDEN_HOE:
            case IRON_HOE:
            case STONE_HOE:
            case WOODEN_HOE:
            case SHIELD:
                break;
            case DIAMOND_AXE:
            case GOLDEN_AXE:
            case IRON_AXE:
            case STONE_AXE:
            case WOODEN_AXE:
                validSecondaryEnchantments.putAll(validateSecondaryCustomEnchantments(CriticalStrikesEnchantment.key));
                break;
            case CHAINMAIL_HELMET:
            case DIAMOND_HELMET:
            case GOLDEN_HELMET:
            case IRON_HELMET:
            case LEATHER_HELMET:
                validSecondaryEnchantments.putAll(validateSecondaryCustomEnchantments(HunterEnchantment.key));
                break;
            case CHAINMAIL_CHESTPLATE:
            case DIAMOND_CHESTPLATE:
            case GOLDEN_CHESTPLATE:
            case IRON_CHESTPLATE:
            case LEATHER_CHESTPLATE:
                validSecondaryEnchantments.putAll(validateSecondaryCustomEnchantments(HunterEnchantment.key));
                break;
            case CHAINMAIL_LEGGINGS:
            case DIAMOND_LEGGINGS:
            case GOLDEN_LEGGINGS:
            case IRON_LEGGINGS:
            case LEATHER_LEGGINGS:
                validSecondaryEnchantments.putAll(validateSecondaryCustomEnchantments(HunterEnchantment.key));
                break;
            case CHAINMAIL_BOOTS:
            case DIAMOND_BOOTS:
            case GOLDEN_BOOTS:
            case IRON_BOOTS:
            case LEATHER_BOOTS:
                validSecondaryEnchantments.putAll(validateSecondaryCustomEnchantments(HunterEnchantment.key));
                break;
            case FISHING_ROD:
                break;
            case SHEARS:
                break;
        }

        /*
        Now that the primary enchantments are applied and the secondary enchantments are parsed, apply secondary enchants
         */
        int secondaryEnchantmentTotalParsedLevel = totalSecondaryCustomEnchantmentCount(validSecondaryEnchantments);

        if (itemTier < 2 || secondaryEnchantmentTotalParsedLevel < 1 || validSecondaryEnchantments.size() == 0)
            return enchantmentMap;

        if (ThreadLocalRandom.current().nextDouble() > ProceduralItemGenerationSettingsConfig.customEnchantmentChance)
            return enchantmentMap;

        /*
        Right now there is only one enchantment per material, so it doesn't further randomize picking enchants
         */
        return validSecondaryEnchantments;

    }

    private static HashMap<Enchantment, Integer> validateAndApplyPrimaryEnchantment(String string, double itemTier) {

        EnchantmentsConfigFields enchantmentsConfigFields = EnchantmentsConfig.getEnchantment(string.toLowerCase() + ".yml");

        HashMap<Enchantment, Integer> enchantmentMap = new HashMap<>();

        if (enchantmentsConfigFields.isEnabled()) {
            Enchantment enchantment = enchantmentsConfigFields.getEnchantment();
            int enchantmentLevel = (enchantmentsConfigFields.getMaxLevel() < itemTier) ? enchantmentsConfigFields.getMaxLevel() : (int) itemTier;
            enchantmentMap.put(enchantment, enchantmentLevel);
        }

        return enchantmentMap;

    }

    private static HashMap<Enchantment, Integer> validateSecondaryEnchantments(String string) {

        EnchantmentsConfigFields enchantmentsConfigFields = EnchantmentsConfig.getEnchantment(string.toLowerCase() + ".yml");

        HashMap<Enchantment, Integer> enchantmentMap = new HashMap<>();

        //Make sure vanishing and binding curses aren't always there
        if (string.equalsIgnoreCase("VANISHING_CURSE") || string.equalsIgnoreCase("BINDING_CURSE"))
            if (ThreadLocalRandom.current().nextDouble() < 0.5)
                return enchantmentMap;

        if (enchantmentsConfigFields.isEnabled()) {
            Enchantment enchantment = enchantmentsConfigFields.getEnchantment();
            enchantmentMap.put(enchantment, enchantmentsConfigFields.getMaxLevel());
        }

        return enchantmentMap;

    }

    private static HashMap<String, Integer> validateSecondaryCustomEnchantments(String string) {

        EnchantmentsConfigFields enchantmentsConfigFields = EnchantmentsConfig.getEnchantment(string.toLowerCase() + ".yml");

        HashMap<String, Integer> enchantmentMap = new HashMap<>();

        if (enchantmentsConfigFields.isEnabled())
            enchantmentMap.put(string, ThreadLocalRandom.current().nextInt(enchantmentsConfigFields.getMaxLevel()) + 1);

        return enchantmentMap;

    }

    private static int totalSecondaryEnchantmentCount(HashMap<Enchantment, Integer> validSecondaryEnchantments) {

        int totalCount = 0;

        for (Enchantment enchantment : validSecondaryEnchantments.keySet())
            totalCount += validSecondaryEnchantments.get(enchantment);

        return totalCount;

    }

    private static int totalSecondaryCustomEnchantmentCount(HashMap<String, Integer> validSecondaryEnchantments) {

        int totalCount = 0;

        for (String enchantment : validSecondaryEnchantments.keySet())
            totalCount += validSecondaryEnchantments.get(enchantment);

        return totalCount;

    }

    //Version and subVersion should be set to the update at which the enchantment was introduced
    private static boolean enchantmentBackwardsCompatibility(int version, int subVersion, String
            actualEnchantement, String enchantmentToAvoid) {

        return !VersionChecker.currentVersionIsUnder(version, subVersion) && actualEnchantement.equalsIgnoreCase(enchantmentToAvoid);

    }

}
