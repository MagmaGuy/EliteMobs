package com.magmaguy.elitemobs.items.itemconstructor;

import com.magmaguy.elitemobs.combatsystem.CombatSystem;
import com.magmaguy.elitemobs.config.ItemSettingsConfig;
import com.magmaguy.elitemobs.config.ProceduralItemGenerationSettingsConfig;
import com.magmaguy.elitemobs.config.enchantments.EnchantmentsConfig;
import com.magmaguy.elitemobs.config.enchantments.EnchantmentsConfigFields;
import com.magmaguy.elitemobs.items.EliteEnchantments;
import com.magmaguy.elitemobs.items.customenchantments.CriticalStrikesEnchantment;
import com.magmaguy.elitemobs.items.customenchantments.HunterEnchantment;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class EnchantmentGenerator {

    public static ItemMeta generateEnchantments(ItemMeta itemMeta, HashMap<Enchantment, Integer> enchantmentMap) {
        for (Map.Entry<Enchantment, Integer> entry : enchantmentMap.entrySet()) {
            if (entry == null) continue;
            EnchantmentsConfigFields enchantmentsConfigFields = EnchantmentsConfig.getEnchantment(entry.getKey().getName().toLowerCase() + ".yml");
            if (enchantmentsConfigFields == null || !enchantmentsConfigFields.isEnabled()) continue;
            if (enchantmentMap.get(entry.getKey()) > entry.getKey().getMaxLevel()) {
                if (EliteEnchantments.isPotentialEliteEnchantment(entry.getKey())) {
                    if (enchantmentMap.get(entry.getKey()) > entry.getKey().getMaxLevel()) {
                        itemMeta.addEnchant(entry.getKey(), entry.getKey().getMaxLevel(), true);
                    } else
                        itemMeta.addEnchant(entry.getKey(), enchantmentMap.get(entry.getKey()), true);
                } else
                    itemMeta.addEnchant(entry.getKey(), entry.getValue(), true);
            } else {
                itemMeta.addEnchant(entry.getKey(), entry.getValue(), true);
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

        itemTier -= CombatSystem.getMaterialTier(material);

        /*
        No enchantments for items too low tier to have one
         */
        if (itemTier < 1) return enchantmentMap;

        HashMap<Enchantment, Integer> validEnchantments = new HashMap();

        /*
        Primary enchantments get instantly validated and applies since there is only one per item type
        Secondary enchantments get added to a common pool to be randomized later
         */
        switch (material) {
            case TRIDENT:
                if (ThreadLocalRandom.current().nextDouble() < 0.5)
                    validEnchantments.putAll(validateEnchantments("LOYALTY"));
                else
                    validEnchantments.putAll(validateEnchantments("CHANNELING"));
                validEnchantments.putAll(validateEnchantments("RIPTIDE"));
                validEnchantments.putAll(validateEnchantments("IMPALING"));
            case DIAMOND_SWORD:
            case GOLDEN_SWORD:
            case IRON_SWORD:
            case STONE_SWORD:
            case WOODEN_SWORD:
                validEnchantments.putAll(validateEnchantments("DAMAGE_ALL"));
                validEnchantments.putAll(validateEnchantments("DAMAGE_ARTHROPODS"));
                validEnchantments.putAll(validateEnchantments("DAMAGE_UNDEAD"));
                validEnchantments.putAll(validateEnchantments("DURABILITY"));
                validEnchantments.putAll(validateEnchantments("FIRE_ASPECT"));
                validEnchantments.putAll(validateEnchantments("KNOCKBACK"));
                validEnchantments.putAll(validateEnchantments("LOOT_BONUS_MOBS"));
                validEnchantments.putAll(validateEnchantments("MENDING"));
                validEnchantments.putAll(validateEnchantments("SWEEPING_EDGE"));
                validEnchantments.putAll(validateEnchantments("VANISHING_CURSE"));
                break;
            case BOW:
                validEnchantments.putAll(validateEnchantments("ARROW_DAMAGE"));
                validEnchantments.putAll(validateEnchantments("ARROW_FIRE"));
                validEnchantments.putAll(validateEnchantments("ARROW_INFINITE"));
                validEnchantments.putAll(validateEnchantments("ARROW_KNOCKBACK"));
                validEnchantments.putAll(validateEnchantments("DURABILITY"));
                validEnchantments.putAll(validateEnchantments("MENDING"));
                validEnchantments.putAll(validateEnchantments("VANISHING_CURSE"));
                break;
            case CROSSBOW:
                validEnchantments.putAll(validateEnchantments("ARROW_DAMAGE"));
                validEnchantments.putAll(validateEnchantments("QUICK_CHARGE"));
                validEnchantments.putAll(validateEnchantments("MULTISHOT"));
                validEnchantments.putAll(validateEnchantments("PIERCING"));
                validEnchantments.putAll(validateEnchantments("DURABILITY"));
                validEnchantments.putAll(validateEnchantments("MENDING"));
                validEnchantments.putAll(validateEnchantments("VANISHING_CURSE"));
                break;
            case DIAMOND_PICKAXE:
            case GOLDEN_PICKAXE:
            case IRON_PICKAXE:
            case STONE_PICKAXE:
            case WOODEN_PICKAXE:
                validEnchantments.putAll(validateEnchantments("DIG_SPEED"));
                validEnchantments.putAll(validateEnchantments("DURABILITY"));
                validEnchantments.putAll(validateEnchantments("MENDING"));
                validEnchantments.putAll(validateEnchantments("VANISHING_CURSE"));
                //TODO: this doesn't take config into account
                if (ThreadLocalRandom.current().nextDouble() < 0.5) {
                    validEnchantments.putAll(validateEnchantments("LOOT_BONUS_BLOCKS"));
                } else {
                    validEnchantments.putAll(validateEnchantments("SILK_TOUCH"));
                }
                break;
            case DIAMOND_SHOVEL:
            case GOLDEN_SHOVEL:
            case IRON_SHOVEL:
            case STONE_SHOVEL:
            case WOODEN_SHOVEL:
                validEnchantments.putAll(validateEnchantments("DIG_SPEED"));
                validEnchantments.putAll(validateEnchantments("DURABILITY"));
                validEnchantments.putAll(validateEnchantments("MENDING"));
                validEnchantments.putAll(validateEnchantments("VANISHING_CURSE"));
                if (ThreadLocalRandom.current().nextDouble() < 0.5) {
                    validEnchantments.putAll(validateEnchantments("LOOT_BONUS_BLOCKS"));
                } else {
                    validEnchantments.putAll(validateEnchantments("SILK_TOUCH"));
                }
                break;
            case DIAMOND_HOE:
            case GOLDEN_HOE:
            case IRON_HOE:
            case STONE_HOE:
            case WOODEN_HOE:
                if (ItemSettingsConfig.isUseHoesAsWeapons())
                    validEnchantments.putAll(validateEnchantments("DAMAGE_ALL"));
                validEnchantments.putAll(validateEnchantments("DIG_SPEED"));
                validEnchantments.putAll(validateEnchantments("DURABILITY"));
                validEnchantments.putAll(validateEnchantments("MENDING"));
                validEnchantments.putAll(validateEnchantments("VANISHING_CURSE"));
                if (ThreadLocalRandom.current().nextDouble() < 0.5) {
                    validEnchantments.putAll(validateEnchantments("LOOT_BONUS_BLOCKS"));
                } else {
                    validEnchantments.putAll(validateEnchantments("SILK_TOUCH"));
                }
                break;
            case SHIELD:
                validEnchantments.putAll(validateEnchantments("DURABILITY"));
                validEnchantments.putAll(validateEnchantments("MENDING"));
                validEnchantments.putAll(validateEnchantments("VANISHING_CURSE"));
                break;
            case DIAMOND_AXE:
            case GOLDEN_AXE:
            case IRON_AXE:
            case STONE_AXE:
            case WOODEN_AXE:
                validEnchantments.putAll(validateEnchantments("DAMAGE_ALL"));
                validEnchantments.putAll(validateEnchantments("DAMAGE_ARTHROPODS"));
                validEnchantments.putAll(validateEnchantments("DAMAGE_UNDEAD"));
                validEnchantments.putAll(validateEnchantments("DURABILITY"));
                validEnchantments.putAll(validateEnchantments("MENDING"));
                validEnchantments.putAll(validateEnchantments("VANISHING_CURSE"));
                validEnchantments.putAll(validateEnchantments("DIG_SPEED"));
                validEnchantments.putAll(validateEnchantments("LOOT_BONUS_BLOCKS"));
                break;
            case CHAINMAIL_HELMET:
            case DIAMOND_HELMET:
            case GOLDEN_HELMET:
            case IRON_HELMET:
            case LEATHER_HELMET:
            case TURTLE_HELMET:
                validEnchantments.putAll(validateEnchantments("PROTECTION_ENVIRONMENTAL"));
                validEnchantments.putAll(validateEnchantments("BINDING_CURSE"));
                validEnchantments.putAll(validateEnchantments("DURABILITY"));
                validEnchantments.putAll(validateEnchantments("MENDING"));
                validEnchantments.putAll(validateEnchantments("OXYGEN"));
                validEnchantments.putAll(validateEnchantments("PROTECTION_EXPLOSIONS"));
                validEnchantments.putAll(validateEnchantments("PROTECTION_FIRE"));
                validEnchantments.putAll(validateEnchantments("PROTECTION_PROJECTILE"));
                validEnchantments.putAll(validateEnchantments("THORNS"));
                validEnchantments.putAll(validateEnchantments("VANISHING_CURSE"));
                validEnchantments.putAll(validateEnchantments("WATER_WORKER"));
                break;
            case CHAINMAIL_CHESTPLATE:
            case DIAMOND_CHESTPLATE:
            case GOLDEN_CHESTPLATE:
            case IRON_CHESTPLATE:
            case LEATHER_CHESTPLATE:
                validEnchantments.putAll(validateEnchantments("PROTECTION_ENVIRONMENTAL"));
                validEnchantments.putAll(validateEnchantments("DURABILITY"));
                validEnchantments.putAll(validateEnchantments("MENDING"));
                validEnchantments.putAll(validateEnchantments("PROTECTION_EXPLOSIONS"));
                validEnchantments.putAll(validateEnchantments("PROTECTION_FIRE"));
                validEnchantments.putAll(validateEnchantments("PROTECTION_PROJECTILE"));
                validEnchantments.putAll(validateEnchantments("THORNS"));
                validEnchantments.putAll(validateEnchantments("VANISHING_CURSE"));
                break;
            case CHAINMAIL_LEGGINGS:
            case DIAMOND_LEGGINGS:
            case GOLDEN_LEGGINGS:
            case IRON_LEGGINGS:
            case LEATHER_LEGGINGS:
                validEnchantments.putAll(validateEnchantments("PROTECTION_ENVIRONMENTAL"));
                validEnchantments.putAll(validateEnchantments("BINDING_CURSE"));
                validEnchantments.putAll(validateEnchantments("DURABILITY"));
                validEnchantments.putAll(validateEnchantments("MENDING"));
                validEnchantments.putAll(validateEnchantments("PROTECTION_EXPLOSIONS"));
                validEnchantments.putAll(validateEnchantments("PROTECTION_FIRE"));
                validEnchantments.putAll(validateEnchantments("PROTECTION_PROJECTILE"));
                validEnchantments.putAll(validateEnchantments("THORNS"));
                validEnchantments.putAll(validateEnchantments("VANISHING_CURSE"));
                break;
            case CHAINMAIL_BOOTS:
            case DIAMOND_BOOTS:
            case GOLDEN_BOOTS:
            case IRON_BOOTS:
            case LEATHER_BOOTS:
                validEnchantments.putAll(validateEnchantments("PROTECTION_ENVIRONMENTAL"));
                validEnchantments.putAll(validateEnchantments("BINDING_CURSE"));
                validEnchantments.putAll(validateEnchantments("DURABILITY"));
                validEnchantments.putAll(validateEnchantments("MENDING"));
                validEnchantments.putAll(validateEnchantments("PROTECTION_EXPLOSIONS"));
                validEnchantments.putAll(validateEnchantments("PROTECTION_FALL"));
                validEnchantments.putAll(validateEnchantments("PROTECTION_FIRE"));
                validEnchantments.putAll(validateEnchantments("PROTECTION_PROJECTILE"));
                validEnchantments.putAll(validateEnchantments("THORNS"));
                validEnchantments.putAll(validateEnchantments("VANISHING_CURSE"));
                validEnchantments.putAll(validateEnchantments("DEPTH_STRIDER"));
                validEnchantments.putAll(validateEnchantments("FROST_WALKER"));
                validEnchantments.putAll(validateEnchantments("SOUL_SPEED"));
                break;
            case FISHING_ROD:
                validEnchantments.putAll(validateEnchantments("DURABILITY"));
                validEnchantments.putAll(validateEnchantments("VANISHING_CURSE"));
                validEnchantments.putAll(validateEnchantments("MENDING"));
                validEnchantments.putAll(validateEnchantments("LUCK"));
                validEnchantments.putAll(validateEnchantments("LURE"));
                break;
            case SHEARS:
                validEnchantments.putAll(validateEnchantments("DIG_SPEED"));
                validEnchantments.putAll(validateEnchantments("VANISHING_CURSE"));
                validEnchantments.putAll(validateEnchantments("MENDING"));
                validEnchantments.putAll(validateEnchantments("DURABILITY"));
                break;
        }

        /*
        Now that the primary enchantments are applied and the secondary enchantments are parsed, apply secondary enchants
         */
        int secondaryEnchantmentTotalParsedLevel = totalSecondaryEnchantmentCount(validEnchantments);
        int maxSecondaryEnchantmentLevel = (secondaryEnchantmentTotalParsedLevel < itemTier - 2) ? secondaryEnchantmentTotalParsedLevel : (int) itemTier;
        int secondaryEnchantmentCount = ThreadLocalRandom.current().nextInt(maxSecondaryEnchantmentLevel + 1);

        if (itemTier < 2 || secondaryEnchantmentCount < 1 || validEnchantments.size() == 0) {
            generateEnchantments(itemMeta, enchantmentMap);
            return enchantmentMap;
        }

        HashMap<Enchantment, Integer> validEnchantmentsClone = (HashMap<Enchantment, Integer>) validEnchantments.clone();

        while (ThreadLocalRandom.current().nextBoolean()) {

            if (validEnchantments.size() < 1)
                break;

            int randomIndex = ThreadLocalRandom.current().nextInt(validEnchantments.size());

            List<Enchantment> enchantmentList = new ArrayList();

            for (Enchantment enchantment : validEnchantments.keySet())
                enchantmentList.add(enchantment);

            Enchantment enchantment = enchantmentList.get(randomIndex);

            validEnchantments.put(enchantment, validEnchantments.get(enchantment) - 1);

            int finalEnchantLevel = validEnchantmentsClone.get(enchantment) - validEnchantments.get(enchantment);
            enchantmentMap.put(enchantment, finalEnchantLevel);

            int newEnchantInt = validEnchantments.get(enchantment);

            if (newEnchantInt == 0) validEnchantments.remove(enchantment);

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
        int secondaryEnchantmentTotalParsedLevel = totalEnchantmentCount(validSecondaryEnchantments);

        if (itemTier < 2 || secondaryEnchantmentTotalParsedLevel < 1 || validSecondaryEnchantments.size() == 0)
            return enchantmentMap;

        if (ThreadLocalRandom.current().nextDouble() > ProceduralItemGenerationSettingsConfig.getCustomEnchantmentChance())
            return enchantmentMap;

        /*
        Right now there is only one enchantment per material, so it doesn't further randomize picking enchants
         */
        return validSecondaryEnchantments;

    }

    private static HashMap<Enchantment, Integer> validateEnchantments(String string) {

        EnchantmentsConfigFields enchantmentsConfigFields = EnchantmentsConfig.getEnchantment(string.toLowerCase() + ".yml");

        if (enchantmentsConfigFields == null ||
                !enchantmentsConfigFields.isEnabled() ||
                !enchantmentsConfigFields.isEnabledForProcedurallyGeneratedItems())
            return new HashMap<>();

        HashMap<Enchantment, Integer> enchantmentMap = new HashMap<>();

        //Make sure vanishing and binding curses aren't always there
        if (string.equalsIgnoreCase("VANISHING_CURSE") || string.equalsIgnoreCase("BINDING_CURSE"))
            if (ThreadLocalRandom.current().nextDouble() < 0.5)
                return enchantmentMap;

        if (enchantmentsConfigFields != null && enchantmentsConfigFields.isEnabled()) {
            Enchantment enchantment = enchantmentsConfigFields.getEnchantment();
            enchantmentMap.put(enchantment, enchantmentsConfigFields.getMaxLevel());
        }

        return enchantmentMap;

    }

    private static HashMap<String, Integer> validateSecondaryCustomEnchantments(String string) {

        EnchantmentsConfigFields enchantmentsConfigFields = EnchantmentsConfig.getEnchantment(string.toLowerCase() + ".yml");

        if (enchantmentsConfigFields == null ||
                !enchantmentsConfigFields.isEnabled() ||
                !enchantmentsConfigFields.isEnabledForProcedurallyGeneratedItems())
            return new HashMap<>();

        HashMap<String, Integer> enchantmentMap = new HashMap<>();

        if (enchantmentsConfigFields != null && enchantmentsConfigFields.isEnabled())
            enchantmentMap.put(string, ThreadLocalRandom.current().nextInt(enchantmentsConfigFields.getMaxLevel()) + 1);

        return enchantmentMap;

    }

    private static int totalSecondaryEnchantmentCount(HashMap<Enchantment, Integer> validEnchantments) {

        int totalCount = 0;

        for (Enchantment enchantment : validEnchantments.keySet())
            totalCount += validEnchantments.get(enchantment);

        return totalCount;

    }

    private static int totalEnchantmentCount(HashMap<String, Integer> validEnchantments) {

        int totalCount = 0;

        for (String enchantment : validEnchantments.keySet())
            totalCount += validEnchantments.get(enchantment);

        return totalCount;

    }

}
