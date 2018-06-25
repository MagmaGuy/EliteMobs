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

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.ConfigValues;
import com.magmaguy.elitemobs.config.DefaultConfig;
import com.magmaguy.elitemobs.config.EconomySettingsConfig;
import com.magmaguy.elitemobs.config.ItemsProceduralSettingsConfig;
import com.magmaguy.elitemobs.mobcustomizer.DamageAdjuster;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import static org.bukkit.Material.*;

/**
 * Created by MagmaGuy on 04/06/2017.
 */
public class ProceduralItemGenerator {

    private static ArrayList<String> nouns = (ArrayList<String>) ConfigValues.itemsProceduralSettingsConfig.getList("Valid nouns");
    private static ArrayList<String> adjectives = (ArrayList<String>) ConfigValues.itemsProceduralSettingsConfig.getList("Valid adjectives");
    private static ArrayList<String> verbs = (ArrayList<String>) ConfigValues.itemsProceduralSettingsConfig.getList("Valid verbs");
    private static ArrayList<String> verbers = (ArrayList<String>) ConfigValues.itemsProceduralSettingsConfig.getList("Valid verb-er (noun)");
    HashMap<Enchantment, Integer> validEnchantments = new HashMap();
    private Random random = new Random();

    /*
    In order to create an item, check the level of the mob and add just slightly more value to an item than would be necessary
    to defeat it assuming that every enchantment is combat-oriented.
     */

    public ItemStack proceduralItemGenerator(int mobLevel, Entity entity) {

        double itemTier = (int) MobTierFinder.findMobTier (mobLevel);

        return tieredProceduralItemGenerator(itemTier, entity);

    }

    public ItemStack tieredProceduralItemGenerator(double itemTier, Entity entity) {

        //Create itemstack, generate material
        ItemStack proceduralItem = new ItemStack(randomMaterialConstructor(itemTier), 1);
        ItemMeta itemMeta = proceduralItem.getItemMeta();

        //Apply item name
        itemMeta.setDisplayName(ChatColorConverter.chatColorConverter(randomItemNameConstructor(proceduralItem.getType())));

        //Apply enchantments
        itemMeta = randomItemEnchantmentConstructor(proceduralItem.getType(), itemMeta, (int) itemTier);

        proceduralItem.setItemMeta(itemMeta);

        //Apply lore
        itemMeta.setLore(proceduralItemLoreConstructor(proceduralItem, entity, itemTier));

        proceduralItem.setItemMeta(itemMeta);

        ItemQuality.dropQualityColorizer(proceduralItem);

        if (ConfigValues.itemsProceduralSettingsConfig.getBoolean(ItemsProceduralSettingsConfig.MONITOR_ITEMS_ON_CONSOLE)) {

            consolePrintItem(proceduralItem);

        }

        //Add hidden lore for shops to validate
        ObfuscatedSignatureLoreData.obfuscateSignatureData(proceduralItem);

        if (ConfigValues.defaultConfig.getBoolean(DefaultConfig.HIDE_ENCHANTMENTS_ATTRIBUTE))
            EnchantmentHider.hideEnchantments(proceduralItem);

        return proceduralItem;

    }

    public ItemStack randomItemGeneratorCommand(int mobLevel) {

        Entity entity = null;

        return proceduralItemGenerator(mobLevel, entity);

    }

    private Material randomMaterialConstructor(double itemTier) {

        List<Material> validMaterials = new ArrayList<>();

        for (Object object : ConfigValues.itemsProceduralSettingsConfig.getList(ItemsProceduralSettingsConfig.PROCEDURAL_ITEM_VALID_MATERIALS)) {

            try {

                Material parsedMaterial = getMaterial(object.toString());
                validMaterials.add(parsedMaterial);

            } catch (Exception e) {

                Bukkit.getLogger().info("Invalid material type detected: " + object.toString());

            }


        }

        if (itemTier < DamageAdjuster.DIAMOND_TIER_LEVEL) {

            validMaterials.remove(DIAMOND);
            validMaterials.remove(DIAMOND_AXE);
            validMaterials.remove(DIAMOND_BARDING);
            validMaterials.remove(DIAMOND_BLOCK);
            validMaterials.remove(DIAMOND_CHESTPLATE);
            validMaterials.remove(DIAMOND_HELMET);
            validMaterials.remove(DIAMOND_HOE);
            validMaterials.remove(DIAMOND_LEGGINGS);
            validMaterials.remove(DIAMOND_ORE);
            validMaterials.remove(DIAMOND_PICKAXE);
            validMaterials.remove(DIAMOND_SPADE);
            validMaterials.remove(DIAMOND_SWORD);

        }

        if (itemTier < DamageAdjuster.IRON_TIER_LEVEL) {

            validMaterials.remove(IRON_AXE);
            validMaterials.remove(IRON_BARDING);
            validMaterials.remove(IRON_BLOCK);
            validMaterials.remove(IRON_BOOTS);
            validMaterials.remove(IRON_CHESTPLATE);
            validMaterials.remove(IRON_HELMET);
            validMaterials.remove(IRON_HOE);
            validMaterials.remove(IRON_INGOT);
            validMaterials.remove(IRON_LEGGINGS);
            validMaterials.remove(IRON_NUGGET);
            validMaterials.remove(IRON_ORE);
            validMaterials.remove(IRON_PICKAXE);
            validMaterials.remove(IRON_SPADE);
            validMaterials.remove(IRON_SWORD);
            validMaterials.remove(IRON_BOOTS);

        }

        if (itemTier < DamageAdjuster.STONE_CHAIN_TIER_LEVEL) {

            validMaterials.remove(CHAINMAIL_BOOTS);
            validMaterials.remove(CHAINMAIL_CHESTPLATE);
            validMaterials.remove(CHAINMAIL_HELMET);
            validMaterials.remove(CHAINMAIL_LEGGINGS);
            validMaterials.remove(STONE_SWORD);
            validMaterials.remove(STONE_HOE);
            validMaterials.remove(STONE_SPADE);
            validMaterials.remove(STONE_PICKAXE);
            validMaterials.remove(STONE_AXE);

        }

        int index = random.nextInt(validMaterials.size());

        Material material = validMaterials.get(index);

        return material;

    }

    private String randomItemNameConstructor(Material material) {

        String finalName = "";

        int nounConstructorSelector = random.nextInt(7) + 1;

        if (nounConstructorSelector == 1) {

            finalName = verbTypeAdjectiveNoun(material);

        } else if (nounConstructorSelector == 2) {

            finalName = typeAdjectiveNoun(material);

        } else if (nounConstructorSelector == 3) {

            finalName = nounVerbType(material);

        } else if (nounConstructorSelector == 4) {

            finalName = verbType(material);

        } else if (nounConstructorSelector == 5) {

            finalName = adjectiveVerbType(material);

        } else if (nounConstructorSelector == 6) {

            finalName = articleVerber();

        } else if (nounConstructorSelector == 7) {

            finalName = articleAdjectiveVerber();

        }

        return finalName;

    }

    private String verbTypeAdjectiveNoun(Material material) {

        String randomVerb = verbs.get(random.nextInt(verbs.size()));
        String itemType = itemTypeStringParser(material);
        String randomAdjective = adjectives.get(random.nextInt(adjectives.size()));
        String randomNoun = nouns.get(random.nextInt(nouns.size()));

        String finalName = randomVerb + " " + itemType + " " + "of the" + " " + randomAdjective + " " + randomNoun;

        return finalName;

    }

    private String typeAdjectiveNoun(Material material) {

        String itemType = itemTypeStringParser(material);
        String randomAdjective = adjectives.get(random.nextInt(adjectives.size()));
        String randomNoun = nouns.get(random.nextInt(nouns.size()));

        String finalName = itemType + " " + "of the" + " " + randomAdjective + " " + randomNoun;

        return finalName;

    }

    private String nounVerbType(Material material) {

        String randomNoun = nouns.get(random.nextInt(nouns.size()));
        String randomAdjective = adjectives.get(random.nextInt(adjectives.size()));
        String randomVerb = verbs.get(random.nextInt(verbs.size()));
        String itemType = itemTypeStringParser(material);

        String finalName = randomNoun + "'s" + " " + randomAdjective + " " + randomVerb + " " + itemType;

        return finalName;

    }

    private String verbType(Material material) {

        String randomVerb = verbs.get(random.nextInt(verbs.size()));
        String itemType = itemTypeStringParser(material);

        String finalName = randomVerb + " " + itemType;

        return finalName;

    }

    private String adjectiveVerbType(Material material) {

        String randomAdjective = adjectives.get(random.nextInt(adjectives.size()));
        String randomVerb = verbs.get(random.nextInt(verbs.size()));
        String itemType = itemTypeStringParser(material);

        String finalName = randomAdjective + " " + randomVerb + " " + itemType;

        return finalName;

    }

    private String articleVerber() {

        String article = "The";
        String randomVerber = verbers.get(random.nextInt(verbers.size()));

        String finalName = article + " " + randomVerber;

        return finalName;

    }

    private String articleAdjectiveVerber() {

        String article = "The";
        String randomAdjective = adjectives.get(random.nextInt(adjectives.size()));
        String randomVerber = verbers.get(random.nextInt(verbers.size()));

        String finalName = article + " " + randomAdjective + " " + randomVerber;

        return finalName;

    }

    private String itemTypeStringParser(Material material) {

        if (material.equals(DIAMOND_SWORD) || material.equals(GOLD_SWORD) ||
                material.equals(IRON_SWORD) || material.equals(STONE_SWORD) ||
                material.equals(WOOD_SWORD)) {

            return ConfigValues.itemsProceduralSettingsConfig.getString("Material name.Sword");

        } else if (material.equals(BOW)) {

            return ConfigValues.itemsProceduralSettingsConfig.getString("Material name.Bow");

        } else if (material.equals(DIAMOND_PICKAXE) || material.equals(GOLD_PICKAXE) ||
                material.equals(IRON_PICKAXE) || material.equals(STONE_PICKAXE) ||
                material.equals(WOOD_PICKAXE)) {

            return ConfigValues.itemsProceduralSettingsConfig.getString("Material name.Pickaxe");

        } else if (material.equals(DIAMOND_SPADE) || material.equals(GOLD_SPADE) ||
                material.equals(IRON_SPADE) || material.equals(STONE_SPADE) ||
                material.equals(WOOD_SPADE)) {

            return ConfigValues.itemsProceduralSettingsConfig.getString("Material name.Spade");

        } else if (material.equals(DIAMOND_HOE) || material.equals(GOLD_HOE) ||
                material.equals(IRON_HOE) || material.equals(STONE_HOE) ||
                material.equals(WOOD_HOE)) {

            return ConfigValues.itemsProceduralSettingsConfig.getString("Material name.Hoe");

        } else if (material.equals(DIAMOND_AXE) || material.equals(GOLD_AXE) ||
                material.equals(IRON_AXE) || material.equals(STONE_AXE) ||
                material.equals(WOOD_AXE)) {

            return ConfigValues.itemsProceduralSettingsConfig.getString("Material name.Axe");

        } else if (material.equals(CHAINMAIL_HELMET) || material.equals(DIAMOND_HELMET) ||
                material.equals(GOLD_HELMET) || material.equals(IRON_HELMET) ||
                material.equals(LEATHER_HELMET)) {

            return ConfigValues.itemsProceduralSettingsConfig.getString("Material name.Helmet");

        } else if (material.equals(CHAINMAIL_CHESTPLATE) || material.equals(DIAMOND_CHESTPLATE) ||
                material.equals(GOLD_CHESTPLATE) || material.equals(IRON_CHESTPLATE) ||
                material.equals(LEATHER_CHESTPLATE)) {

            return ConfigValues.itemsProceduralSettingsConfig.getString("Material name.Chestplate");

        } else if (material.equals(CHAINMAIL_LEGGINGS) || material.equals(DIAMOND_LEGGINGS) ||
                material.equals(GOLD_LEGGINGS) || material.equals(IRON_LEGGINGS) ||
                material.equals(LEATHER_LEGGINGS)) {

            return ConfigValues.itemsProceduralSettingsConfig.getString("Material name.Leggings");

        } else if (material.equals(CHAINMAIL_BOOTS) || material.equals(DIAMOND_BOOTS) ||
                material.equals(GOLD_BOOTS) || material.equals(IRON_BOOTS) ||
                material.equals(LEATHER_BOOTS)) {

            return ConfigValues.itemsProceduralSettingsConfig.getString("Material name.Boots");

        } else if (material.equals(SHEARS)) {

            return ConfigValues.itemsProceduralSettingsConfig.getString("Material name.Shears");

        } else if (material.equals(FISHING_ROD)) {

            return ConfigValues.itemsProceduralSettingsConfig.getString("Material name.Fishing Rod");

        } else if (material.equals(SHIELD)) {

            return ConfigValues.itemsProceduralSettingsConfig.getString("Material name.Shield");

        }

        Bukkit.getLogger().info("EliteMobs - found unexpected material type in procedurally generated loot. Can't generate item type name.");
        return "";

    }

    private List<String> proceduralItemLoreConstructor(ItemStack itemStack, Entity entity, double itemTier) {

        String line1 = "";

        if (entity != null) {

            line1 = ConfigValues.itemsProceduralSettingsConfig.getString(ItemsProceduralSettingsConfig.LORE_MOB_LEVEL_SOURCE).replace("$level",
                    entity.getMetadata(MetadataHandler.ELITE_MOB_MD).get(0).asInt() + "");

            String newName = "";

            if (entity.getType().name().contains("_")) {

                List<String> tempSubList = Arrays.asList(entity.getType().name().split("_"));

                for (String string : tempSubList) {

                    string = string.toLowerCase().substring(0, 1).toUpperCase() + " ";
                    newName += string;

                }

            } else {

                newName = entity.getType().name().substring(0, 1) + entity.getType().name().substring(1).toLowerCase();

            }

            line1 = line1.replace("$mob", newName);


        } else {

            line1 = ConfigValues.itemsProceduralSettingsConfig.getString(ItemsProceduralSettingsConfig.LORE_SHOP_SOURCE);

        }

        String line2 = "";

        if (ConfigValues.economyConfig.getBoolean(EconomySettingsConfig.ENABLE_ECONOMY)) {

            String itemWorth = ItemWorthCalculator.determineItemWorth(itemStack) + "";
            line2 = ConfigValues.itemsProceduralSettingsConfig.getString(ItemsProceduralSettingsConfig.LORE_WORTH).replace("$currencyName",
                    ConfigValues.economyConfig.getString(EconomySettingsConfig.CURRENCY_NAME));
            line2 = line2.replace("$worth", itemWorth);

        }

        String line3 = ConfigValues.itemsProceduralSettingsConfig.getString(ItemsProceduralSettingsConfig.LORE_SIGNATURE);

        String line4 = ConfigValues.itemsProceduralSettingsConfig.getString(ItemsProceduralSettingsConfig.LORE_TIER).replace("$tier", String.valueOf(itemTier));

        List loreStructure = ConfigValues.itemsProceduralSettingsConfig.getList(ItemsProceduralSettingsConfig.LORE_STRUCTURE);
        List<String> lore = new ArrayList<>();

        for (Object object: loreStructure) {

            String string = object.toString();

            if (string.contains("$line1"))
                if (line1.length() > 0)
                    string = string.replace("$line1", line1);
            if (string.contains("$line2"))
                if (line2.length() > 0)
                    string = string.replace("$line2", line2);
            if (string.contains("$line3"))
                if (line3.length() > 0)
                    string = string.replace("$line3", line3);
            if (string.contains("$line4"))
                if (line4.length() > 0)
                    string = string.replace("$line4", line4);

            lore.add(string);

        }

        return lore;

    }

    private ItemMeta randomItemEnchantmentConstructor(Material material, ItemMeta itemMeta, int itemTier) {

        if (itemTier < 1) return itemMeta;

        switch (material) {

            case DIAMOND_SWORD:
            case GOLD_SWORD:
            case IRON_SWORD:
            case STONE_SWORD:
            case WOOD_SWORD:
                itemMeta = validatePrimaryEnchantment("DAMAGE_ALL", itemMeta, itemTier);
                validateSecondaryEnchantments("DAMAGE_ARTHROPODS");
                validateSecondaryEnchantments("DAMAGE_UNDEAD");
                validateSecondaryEnchantments("DURABILITY");
                validateSecondaryEnchantments("FIRE_ASPECT");
                validateSecondaryEnchantments("KNOCKBACK");
                validateSecondaryEnchantments("LOOT_BONUS_MOBS");
                validateSecondaryEnchantments("MENDING");
                validateSecondaryEnchantments("SWEEPING_EDGE");
                validateSecondaryEnchantments("VANISHING_CURSE");
                break;
            case BOW:
                itemMeta = validatePrimaryEnchantment("ARROW_DAMAGE", itemMeta, itemTier);
                validateSecondaryEnchantments("ARROW_FIRE");
                validateSecondaryEnchantments("ARROW_INFINITE");
                validateSecondaryEnchantments("ARROW_KNOCKBACK");
                validateSecondaryEnchantments("DURABILITY");
                validateSecondaryEnchantments("MENDING");
                validateSecondaryEnchantments("VANISHING_CURSE");
                break;
            case DIAMOND_PICKAXE:
            case GOLD_PICKAXE:
            case IRON_PICKAXE:
            case STONE_PICKAXE:
            case WOOD_PICKAXE:
                itemMeta = validatePrimaryEnchantment("DIG_SPEED", itemMeta, itemTier);
                validateSecondaryEnchantments("DURABILITY");
                validateSecondaryEnchantments("MENDING");
                validateSecondaryEnchantments("VANISHING_CURSE");
                //TODO: this doesn't take config into account
                if (random.nextDouble() < 0.5) {
                    validateSecondaryEnchantments("LOOT_BONUS_BLOCKS");
                } else {
                    validateSecondaryEnchantments("SILK_TOUCH");
                }
                break;
            case DIAMOND_SPADE:
            case GOLD_SPADE:
            case IRON_SPADE:
            case STONE_SPADE:
            case WOOD_SPADE:
                itemMeta = validatePrimaryEnchantment("DIG_SPEED", itemMeta, itemTier);
                validateSecondaryEnchantments("DURABILITY");
                validateSecondaryEnchantments("MENDING");
                validateSecondaryEnchantments("VANISHING_CURSE");
                if (random.nextDouble() < 0.5) {
                    validateSecondaryEnchantments("LOOT_BONUS_BLOCKS");
                } else {
                    validateSecondaryEnchantments("SILK_TOUCH");
                }
                break;
            case DIAMOND_HOE:
            case GOLD_HOE:
            case IRON_HOE:
            case STONE_HOE:
            case WOOD_HOE:
            case SHIELD:
                itemMeta = validatePrimaryEnchantment("DURABILITY", itemMeta, itemTier);
                validateSecondaryEnchantments("MENDING");
                validateSecondaryEnchantments("VANISHING_CURSE");
                break;
            case DIAMOND_AXE:
            case GOLD_AXE:
            case IRON_AXE:
            case STONE_AXE:
            case WOOD_AXE:
                itemMeta = validatePrimaryEnchantment("DAMAGE_ALL", itemMeta, itemTier);
                validateSecondaryEnchantments("DAMAGE_ARTHROPODS");
                validateSecondaryEnchantments("DAMAGE_UNDEAD");
                validateSecondaryEnchantments("DURABILITY");
                validateSecondaryEnchantments("MENDING");
                validateSecondaryEnchantments("VANISHING_CURSE");
                validateSecondaryEnchantments("DIG_SPEED");
                validateSecondaryEnchantments("LOOT_BONUS_BLOCKS");
                break;
            case CHAINMAIL_HELMET:
            case DIAMOND_HELMET:
            case GOLD_HELMET:
            case IRON_HELMET:
            case LEATHER_HELMET:
                itemMeta = validatePrimaryEnchantment("PROTECTION_ENVIRONMENTAL", itemMeta, itemTier);
                validateSecondaryEnchantments("BINDING_CURSE");
                validateSecondaryEnchantments("DURABILITY");
                validateSecondaryEnchantments("MENDING");
                validateSecondaryEnchantments("OXYGEN");
                validateSecondaryEnchantments("PROTECTION_EXPLOSIONS");
                validateSecondaryEnchantments("PROTECTION_FIRE");
                validateSecondaryEnchantments("PROTECTION_PROJECTILE");
                validateSecondaryEnchantments("THORNS");
                validateSecondaryEnchantments("VANISHING_CURSE");
                validateSecondaryEnchantments("WATER_WORKER");
                break;
            case CHAINMAIL_CHESTPLATE:
            case DIAMOND_CHESTPLATE:
            case GOLD_CHESTPLATE:
            case IRON_CHESTPLATE:
            case LEATHER_CHESTPLATE:
                itemMeta = validatePrimaryEnchantment("PROTECTION_ENVIRONMENTAL", itemMeta, itemTier);
                validateSecondaryEnchantments("DURABILITY");
                validateSecondaryEnchantments("MENDING");
                validateSecondaryEnchantments("PROTECTION_EXPLOSIONS");
                validateSecondaryEnchantments("PROTECTION_FIRE");
                validateSecondaryEnchantments("PROTECTION_PROJECTILE");
                validateSecondaryEnchantments("THORNS");
                validateSecondaryEnchantments("VANISHING_CURSE");
                break;
            case CHAINMAIL_LEGGINGS:
            case DIAMOND_LEGGINGS:
            case GOLD_LEGGINGS:
            case IRON_LEGGINGS:
            case LEATHER_LEGGINGS:
                itemMeta = validatePrimaryEnchantment("PROTECTION_ENVIRONMENTAL", itemMeta, itemTier);
                validateSecondaryEnchantments("BINDING_CURSE");
                validateSecondaryEnchantments("DURABILITY");
                validateSecondaryEnchantments("MENDING");
                validateSecondaryEnchantments("PROTECTION_EXPLOSIONS");
                validateSecondaryEnchantments("PROTECTION_FIRE");
                validateSecondaryEnchantments("PROTECTION_PROJECTILE");
                validateSecondaryEnchantments("THORNS");
                validateSecondaryEnchantments("VANISHING_CURSE");
                break;
            case CHAINMAIL_BOOTS:
            case DIAMOND_BOOTS:
            case GOLD_BOOTS:
            case IRON_BOOTS:
            case LEATHER_BOOTS:
                itemMeta = validatePrimaryEnchantment("PROTECTION_ENVIRONMENTAL", itemMeta, itemTier);
                validateSecondaryEnchantments("BINDING_CURSE");
                validateSecondaryEnchantments("DURABILITY");
                validateSecondaryEnchantments("MENDING");
                validateSecondaryEnchantments("PROTECTION_EXPLOSIONS");
                validateSecondaryEnchantments("PROTECTION_FALL");
                validateSecondaryEnchantments("PROTECTION_FIRE");
                validateSecondaryEnchantments("PROTECTION_PROJECTILE");
                validateSecondaryEnchantments("THORNS");
                validateSecondaryEnchantments("VANISHING_CURSE");
                validateSecondaryEnchantments("DEPTH_STRIDER");
                validateSecondaryEnchantments("FROST_WALKER");
                break;
            case FISHING_ROD:
                itemMeta = validatePrimaryEnchantment("DURABILITY", itemMeta, itemTier);
                validateSecondaryEnchantments("VANISHING_CURSE");
                validateSecondaryEnchantments("MENDING");
                validateSecondaryEnchantments("LUCK");
                validateSecondaryEnchantments("LURE");
                break;
            case SHEARS:
                itemMeta = validatePrimaryEnchantment("DIG_SPEED", itemMeta, itemTier);
                validateSecondaryEnchantments("VANISHING_CURSE");
                validateSecondaryEnchantments("MENDING");
                validateSecondaryEnchantments("DURABILITY");
                break;

        }

        int maxSecondaryEnchantmentLevel = (secondaryEnchantmentTotalParsedLevel < itemTier - 2) ? secondaryEnchantmentTotalParsedLevel : itemTier;
        int secondaryEnchantmentCount = ThreadLocalRandom.current().nextInt(maxSecondaryEnchantmentLevel + 1);

        if (itemTier < 2 || secondaryEnchantmentCount < 1 || validEnchantments.size() == 0) return itemMeta;

        HashMap<Enchantment, Integer> validEnchantmentsClone = (HashMap<Enchantment, Integer>) validEnchantments.clone();

        while (secondaryEnchantmentCount != 0) {

            if (validEnchantments.size() < 1) {

                break;

            }

            int randomIndex = random.nextInt(validEnchantments.size());

            List<Enchantment> enchantmentList = new ArrayList();

            for (Enchantment enchantment : validEnchantments.keySet()) {

                enchantmentList.add(enchantment);

            }

            Enchantment enchantment = enchantmentList.get(randomIndex);

            validEnchantments.put(enchantment, validEnchantments.get(enchantment) - 1);

            int finalEnchantLevel = validEnchantmentsClone.get(enchantment) - validEnchantments.get(enchantment);
            itemMeta.addEnchant(enchantment, finalEnchantLevel, true);

            int newEnchantInt = validEnchantments.get(enchantment);

            if (newEnchantInt == 0) validEnchantments.remove(enchantment);

            secondaryEnchantmentCount--;

        }

        return itemMeta;

    }

    private ItemMeta validatePrimaryEnchantment(String string, ItemMeta itemMeta, int itemTier) {

        String mainString = "Valid Enchantments." + string;

        if (ConfigValues.itemsProceduralSettingsConfig.getBoolean(mainString + ".Allow")) {

            int maxEnchantmentLevel;

            if (ConfigValues.itemsProceduralSettingsConfig.contains(mainString + ".Max Level")) {

                maxEnchantmentLevel = ConfigValues.itemsProceduralSettingsConfig.getInt(mainString + ".Max Level");

            } else {

                maxEnchantmentLevel = 1;

            }

            int enchantmentLevel = (maxEnchantmentLevel < itemTier) ? maxEnchantmentLevel : itemTier;

            //index for random getting

            itemMeta.addEnchant(Enchantment.getByName(string), enchantmentLevel, true);

        }

        return itemMeta;

    }

    private int secondaryEnchantmentTotalParsedLevel = 0;

    private void validateSecondaryEnchantments(String string) {

        if (string.equalsIgnoreCase("SWEEP") && Integer.getInteger(Bukkit.getVersion().split(".")[1]) < 11 ||
                string.equalsIgnoreCase("SWEEP") && Integer.getInteger(Bukkit.getVersion().split(".")[1]) == 11 &&
                        Bukkit.getVersion().split(".").length < 2) {
            return;
        }

        String mainString = "Valid Enchantments." + string;

        if (ConfigValues.itemsProceduralSettingsConfig.getBoolean(mainString + ".Allow")) {

            int enchantmentLevel;

            if (ConfigValues.itemsProceduralSettingsConfig.contains(mainString + ".Max Level")) {

                enchantmentLevel = ConfigValues.itemsProceduralSettingsConfig.getInt(mainString + ".Max Level");

            } else {

                enchantmentLevel = 1;

            }

            //index for random getting

            secondaryEnchantmentTotalParsedLevel += enchantmentLevel;
            validEnchantments.put(Enchantment.getByName(string), enchantmentLevel);

        }

    }

    private void consolePrintItem(ItemStack proceduralItem) {

        Bukkit.getLogger().info("[EliteMobs] Procedurally generated item with the following attributes:");
        Bukkit.getLogger().info("[EliteMobs] Item type: " + proceduralItem.getType());
        Bukkit.getLogger().info("[EliteMobs] Item name: " + proceduralItem.getItemMeta().getDisplayName());
        Bukkit.getLogger().info("[EliteMobs] Item lore: " + proceduralItem.getItemMeta().getLore().get(0));
        Bukkit.getLogger().info("[EliteMobs] Item enchantments:");

        for (Map.Entry<Enchantment, Integer> entry : proceduralItem.getItemMeta().getEnchants().entrySet()) {

            Bukkit.getLogger().info(entry.getKey() + " level " + entry.getValue());

        }

    }

}
