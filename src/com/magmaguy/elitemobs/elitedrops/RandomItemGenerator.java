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
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.ConfigValues;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

/**
 * Created by MagmaGuy on 04/06/2017.
 */
public class RandomItemGenerator {

    private Random random = new Random();

    public ItemStack randomItemGenerator(int itemRank, Entity entity) {

        //Create itemstack, generate material
        ItemStack randomItem = new ItemStack(randomMaterialConstructor(itemRank), 1);
        ItemMeta itemMeta = randomItem.getItemMeta();

        //Apply item name
        itemMeta.setDisplayName(ChatColorConverter.chatColorConverter(randomItemNameConstructor(randomItem.getType())));

        //Apply enchantments
        itemMeta = randomItemEnchantmentConstructor(randomItem.getType(), itemMeta, itemRank);

        randomItem.setItemMeta(itemMeta);

        //Apply lore
        itemMeta.setLore(randomItemLoreConstructor(itemMeta, randomItem.getType(), entity));

        randomItem.setItemMeta(itemMeta);

        if (ConfigValues.defaultConfig.getBoolean("Use MMORPG colors for item ranks")) {

            double rankProgression = ConfigValues.defaultConfig.getDouble("Increase MMORPG color rank every X levels X=");

                if (Math.floor(itemRank /rankProgression) >= 5) {

                    itemMeta.setDisplayName(ChatColor.GOLD + itemMeta.getDisplayName());

                    List list = new ArrayList();

                    for (String string : itemMeta.getLore()) {

                        String coloredString = ChatColor.GOLD + "" + ChatColor.ITALIC + string;
                        list.add(coloredString);

                    }

                    itemMeta.setLore(list);

                } else if (Math.floor(itemRank / rankProgression) == 4) {

                    itemMeta.setDisplayName(ChatColor.DARK_PURPLE + itemMeta.getDisplayName());

                    List list = new ArrayList();

                    for (String string : itemMeta.getLore()) {

                        String coloredString = ChatColor.DARK_PURPLE + "" + ChatColor.ITALIC + string;
                        list.add(coloredString);

                    }

                    itemMeta.setLore(list);

                } else if (Math.floor(itemRank / rankProgression) == 3) {

                    itemMeta.setDisplayName(ChatColor.BLUE + itemMeta.getDisplayName());

                    List list = new ArrayList();

                    for (String string : itemMeta.getLore()) {

                        String coloredString = ChatColor.BLUE + "" + ChatColor.ITALIC + string;
                        list.add(coloredString);

                    }

                    itemMeta.setLore(list);

                } else if (Math.floor(itemRank / rankProgression) == 2) {

                    itemMeta.setDisplayName(ChatColor.GREEN + itemMeta.getDisplayName());

                    List list = new ArrayList();

                    for (String string : itemMeta.getLore()) {

                        String coloredString = ChatColor.GREEN + "" + ChatColor.ITALIC + string;
                        list.add(coloredString);

                    }

                    itemMeta.setLore(list);

                } else if (Math.floor(itemRank / rankProgression) == 1) {

                    itemMeta.setDisplayName(ChatColor.WHITE + itemMeta.getDisplayName());

                    List list = new ArrayList();

                    for (String string : itemMeta.getLore()) {

                        String coloredString = ChatColor.WHITE + "" + ChatColor.ITALIC + string;
                        list.add(coloredString);

                    }

                    itemMeta.setLore(list);

                } else if (Math.floor(itemRank / rankProgression) == 0) {

                    itemMeta.setDisplayName(ChatColor.GRAY + itemMeta.getDisplayName());

                    List list = new ArrayList();

                    for (String string : itemMeta.getLore()) {

                        String coloredString = ChatColor.GRAY + "" + ChatColor.ITALIC + string;
                        list.add(coloredString);

                    }

                    itemMeta.setLore(list);

                }



        }

        randomItem.setItemMeta(itemMeta);

        if (ConfigValues.randomItemsConfig.getBoolean("Monitor randomly generated drops on console")) {

            Bukkit.getLogger().info("[EliteMobs] Procedurally generated item with the following attributes:");
            Bukkit.getLogger().info("[EliteMobs] Item type: " + randomItem.getType());
            Bukkit.getLogger().info("[EliteMobs] Item name: " + randomItem.getItemMeta().getDisplayName());
            Bukkit.getLogger().info("[EliteMobs] Item lore: " + randomItem.getItemMeta().getLore().get(0));
            Bukkit.getLogger().info("[EliteMobs] Item enchantments:");

            for (Map.Entry<Enchantment, Integer> entry : randomItem.getItemMeta().getEnchants().entrySet()){

                Bukkit.getLogger().info(entry.getKey() + " level " + entry.getValue());

            }

        }

        return randomItem;

    }

    public ItemStack randomItemGeneratorCommand(int itemRank) {

        Entity entity = null;

        return randomItemGenerator(itemRank, entity);

    }

    private Material randomMaterialConstructor(int mobLevel){

        List<Material> validMaterials = new ArrayList<>();

        for (Object object : ConfigValues.randomItemsConfig.getList("Valid material list for random items")) {

            try {

                Material parsedMaterial = Material.getMaterial(object.toString());
                validMaterials.add(parsedMaterial);

            } catch (Exception e) {

                Bukkit.getLogger().info("Invalid material type detected: " + object.toString());

            }


        }

        if (mobLevel < 5) {

            validMaterials.remove(Material.DIAMOND);
            validMaterials.remove(Material.DIAMOND_AXE);
            validMaterials.remove(Material.DIAMOND_BARDING);
            validMaterials.remove(Material.DIAMOND_BLOCK);
            validMaterials.remove(Material.DIAMOND_CHESTPLATE);
            validMaterials.remove(Material.DIAMOND_HELMET);
            validMaterials.remove(Material.DIAMOND_HOE);
            validMaterials.remove(Material.DIAMOND_LEGGINGS);
            validMaterials.remove(Material.DIAMOND_ORE);
            validMaterials.remove(Material.DIAMOND_PICKAXE);
            validMaterials.remove(Material.DIAMOND_SPADE);
            validMaterials.remove(Material.DIAMOND_SWORD);

        }

        if (mobLevel < 4) {

            validMaterials.remove(Material.IRON_AXE);
            validMaterials.remove(Material.IRON_BARDING);
            validMaterials.remove(Material.IRON_BLOCK);
            validMaterials.remove(Material.IRON_BOOTS);
            validMaterials.remove(Material.IRON_CHESTPLATE);
            validMaterials.remove(Material.IRON_HELMET);
            validMaterials.remove(Material.IRON_HOE);
            validMaterials.remove(Material.IRON_INGOT);
            validMaterials.remove(Material.IRON_LEGGINGS);
            validMaterials.remove(Material.IRON_NUGGET);
            validMaterials.remove(Material.IRON_ORE);
            validMaterials.remove(Material.IRON_PICKAXE);
            validMaterials.remove(Material.IRON_SPADE);
            validMaterials.remove(Material.IRON_SWORD);
            validMaterials.remove(Material.IRON_BOOTS);

        }

        if (mobLevel < 3) {

            validMaterials.remove(Material.CHAINMAIL_BOOTS);
            validMaterials.remove(Material.CHAINMAIL_CHESTPLATE);
            validMaterials.remove(Material.CHAINMAIL_HELMET);
            validMaterials.remove(Material.CHAINMAIL_LEGGINGS);

        }

        if (mobLevel < 2) {

            validMaterials.remove(Material.GOLD_AXE);
            validMaterials.remove(Material.GOLD_BARDING);
            validMaterials.remove(Material.GOLD_BLOCK);
            validMaterials.remove(Material.GOLD_BOOTS);
            validMaterials.remove(Material.GOLD_CHESTPLATE);
            validMaterials.remove(Material.GOLD_HELMET);
            validMaterials.remove(Material.GOLD_HOE);
            validMaterials.remove(Material.GOLD_INGOT);
            validMaterials.remove(Material.GOLD_NUGGET);
            validMaterials.remove(Material.GOLD_ORE);
            validMaterials.remove(Material.GOLD_PICKAXE);
            validMaterials.remove(Material.GOLD_SPADE);
            validMaterials.remove(Material.GOLD_SWORD);
            validMaterials.remove(Material.GOLDEN_APPLE);
            validMaterials.remove(Material.GOLDEN_CARROT);

        }

        if (mobLevel < 1) {

            validMaterials.remove(Material.LEATHER_BOOTS);
            validMaterials.remove(Material.LEATHER_CHESTPLATE);
            validMaterials.remove(Material.LEATHER_HELMET);
            validMaterials.remove(Material.LEATHER_LEGGINGS);

        }

        if (!validMaterials.isEmpty()) {

            int index = random.nextInt(validMaterials.size());

            Material material = validMaterials.get(index);

            return material;

        }

        return Material.AIR;

    }

    private static ArrayList<String> nouns = (ArrayList<String>) ConfigValues.randomItemsConfig.getList("Valid nouns");
    private static ArrayList<String> adjectives = (ArrayList<String>) ConfigValues.randomItemsConfig.getList("Valid adjectives");
    private static ArrayList<String> verbs = (ArrayList<String>) ConfigValues.randomItemsConfig.getList("Valid verbs");
    private static ArrayList<String> verbers = (ArrayList<String>) ConfigValues.randomItemsConfig.getList("Valid verb-er (noun)");

    private String randomItemNameConstructor(Material material){

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

    private String verbTypeAdjectiveNoun (Material material) {

        String randomVerb = verbs.get(random.nextInt(verbs.size()));
        String itemType = itemTypeStringParser(material);
        String randomAdjective = adjectives.get(random.nextInt(adjectives.size()));
        String randomNoun = nouns.get(random.nextInt(nouns.size()));

        String finalName = randomVerb + " " + itemType + " " + "of the" + " " + randomAdjective + " " + randomNoun;

        return finalName;

    }

    private String typeAdjectiveNoun (Material material) {

        String itemType = itemTypeStringParser(material);
        String randomAdjective = adjectives.get(random.nextInt(adjectives.size()));
        String randomNoun = nouns.get(random.nextInt(nouns.size()));

        String finalName = itemType + " " + "of the" + " " + randomAdjective + " " + randomNoun;

        return finalName;

    }

    private String nounVerbType (Material material) {

        String randomNoun = nouns.get(random.nextInt(nouns.size()));
        String randomAdjective = adjectives.get(random.nextInt(adjectives.size()));
        String randomVerb = verbs.get(random.nextInt(verbs.size()));
        String itemType = itemTypeStringParser(material);

        String finalName = randomNoun + "'s" + " " + randomAdjective + " " + randomVerb + " " + itemType;

        return finalName;

    }

    private String verbType (Material material) {

        String randomVerb = verbs.get(random.nextInt(verbs.size()));
        String itemType = itemTypeStringParser(material);

        String finalName = randomVerb + " " + itemType;

        return finalName;

    }

    private String adjectiveVerbType (Material material) {

        String randomAdjective = adjectives.get(random.nextInt(adjectives.size()));
        String randomVerb = verbs.get(random.nextInt(verbs.size()));
        String itemType = itemTypeStringParser(material);

        String finalName = randomAdjective + " " + randomVerb + " " + itemType;

        return finalName;

    }

    private String articleVerber () {

        String article = "The";
        String randomVerber = verbers.get(random.nextInt(verbers.size()));

        String finalName = article + " " + randomVerber;

        return finalName;

    }

    private String articleAdjectiveVerber () {

        String article = "The";
        String randomAdjective = adjectives.get(random.nextInt(adjectives.size()));
        String randomVerber = verbers.get(random.nextInt(verbers.size()));

        String finalName = article + " " + randomAdjective + " " + randomVerber;

        return finalName;

    }

    private String itemTypeStringParser (Material material) {

        if (material.equals(Material.DIAMOND_SWORD) || material.equals(Material.GOLD_SWORD) ||
                material.equals(Material.IRON_SWORD) || material.equals(Material.STONE_SWORD) ||
                material.equals(Material.WOOD_SWORD)) {

            return ConfigValues.randomItemsConfig.getString("Material name.Sword");

        } else if (material.equals(Material.BOW)) {

            return ConfigValues.randomItemsConfig.getString("Material name.Bow");

        } else if (material.equals(Material.DIAMOND_PICKAXE) || material.equals(Material.GOLD_PICKAXE) ||
                material.equals(Material.IRON_PICKAXE) || material.equals(Material.STONE_PICKAXE) ||
                material.equals(Material.WOOD_PICKAXE)) {

            return ConfigValues.randomItemsConfig.getString("Material name.Pickaxe");

        } else if (material.equals(Material.DIAMOND_SPADE) || material.equals(Material.GOLD_SPADE) ||
                material.equals(Material.IRON_SPADE) || material.equals(Material.STONE_SPADE) ||
                material.equals(Material.WOOD_SPADE)){

            return ConfigValues.randomItemsConfig.getString("Material name.Spade");

        } else if (material.equals(Material.DIAMOND_HOE) || material.equals(Material.GOLD_HOE) ||
                material.equals(Material.IRON_HOE) || material.equals(Material.STONE_HOE) ||
                material.equals(Material.WOOD_HOE)) {

            return ConfigValues.randomItemsConfig.getString("Material name.Hoe");

        } else if (material.equals(Material.DIAMOND_AXE) || material.equals(Material.GOLD_AXE) ||
                material.equals(Material.IRON_AXE) || material.equals(Material.STONE_AXE) ||
                material.equals(Material.WOOD_AXE)) {

            return ConfigValues.randomItemsConfig.getString("Material name.Axe");

        } else if (material.equals(Material.CHAINMAIL_HELMET) || material.equals(Material.DIAMOND_HELMET) ||
                material.equals(Material.GOLD_HELMET) || material.equals(Material.IRON_HELMET) ||
                material.equals(Material.LEATHER_HELMET)) {

            return ConfigValues.randomItemsConfig.getString("Material name.Helmet");

        } else if (material.equals(Material.CHAINMAIL_CHESTPLATE) || material.equals(Material.DIAMOND_CHESTPLATE) ||
                material.equals(Material.GOLD_CHESTPLATE) || material.equals(Material.IRON_CHESTPLATE) ||
                material.equals(Material.LEATHER_CHESTPLATE)) {

            return ConfigValues.randomItemsConfig.getString("Material name.Chestplate");

        } else if (material.equals(Material.CHAINMAIL_LEGGINGS) || material.equals(Material.DIAMOND_LEGGINGS) ||
                material.equals(Material.GOLD_LEGGINGS) || material.equals(Material.IRON_LEGGINGS) ||
                material.equals(Material.LEATHER_LEGGINGS)) {

            return ConfigValues.randomItemsConfig.getString("Material name.Leggings");

        } else if (material.equals(Material.CHAINMAIL_BOOTS) || material.equals(Material.DIAMOND_BOOTS) ||
                material.equals(Material.GOLD_BOOTS) || material.equals(Material.IRON_BOOTS) ||
                material.equals(Material.LEATHER_BOOTS)) {

            return ConfigValues.randomItemsConfig.getString("Material name.Boots");

        } else if (material.equals(Material.SHEARS)) {

            return ConfigValues.randomItemsConfig.getString("Material name.Shears");

        } else if (material.equals(Material.FISHING_ROD)) {

            return ConfigValues.randomItemsConfig.getString("Material name.Fishing Rod");

        } else if (material.equals(Material.SHIELD)) {

            return ConfigValues.randomItemsConfig.getString("Material name.Shield");

        }

        Bukkit.getLogger().info("EliteMobs - found unexpected material type in procedurally generated loot. Can't generate item type name.");
        return "";

    }


    private List<String> randomItemLoreConstructor(ItemMeta itemMeta, Material material, Entity entity){

        int enchantmentCount = 0;

        if (!itemMeta.getEnchants().isEmpty()) {

            for (Enchantment enchantment : itemMeta.getEnchants().keySet()) {

                enchantmentCount += itemMeta.getEnchantLevel(enchantment);

            }

        }

        int rankLevel = ItemRankHandler.guessItemRank(material, enchantmentCount);

        String lore1 = "Rank " + rankLevel + " Elite Mob Drop";

        String lore2;

        if (entity != null) {

            lore2 = "Looted from a level " + entity.getMetadata(MetadataHandler.ELITE_MOB_MD).get(0).asInt() + " Elite " + entity.getType().toString();

        } else {

            lore2 = "Obtained via command";
        }


        List<String> loreList = new ArrayList<>();

        loreList.add(lore1);
        loreList.add(lore2);

        return loreList;

    }

    HashMap<Enchantment, Integer> validEnchantments = new HashMap();

    private ItemMeta randomItemEnchantmentConstructor(Material material, ItemMeta oldMeta, int rankLevel){

        if (material.equals(Material.DIAMOND_SWORD) || material.equals(Material.GOLD_SWORD) ||
                material.equals(Material.IRON_SWORD) || material.equals(Material.STONE_SWORD) ||
                material.equals(Material.WOOD_SWORD)) {

            validateEnchantment("DAMAGE_ALL");
            validateEnchantment("DAMAGE_ARTHROPODS");
            validateEnchantment("DAMAGE_UNDEAD");
            validateEnchantment("DURABILITY");
            validateEnchantment("FIRE_ASPECT");
            validateEnchantment("KNOCKBACK");
            validateEnchantment("LOOT_BONUS_MOBS");
            validateEnchantment("MENDING");
//            validateEnchantment("SWEEPING_EDGE");
            validateEnchantment("VANISHING_CURSE");

        } else if (material.equals(Material.BOW)) {

            validateEnchantment("ARROW_DAMAGE");
            validateEnchantment("ARROW_FIRE");
            validateEnchantment("ARROW_INFINITE");
            validateEnchantment("ARROW_KNOCKBACK");
            validateEnchantment("DURABILITY");
            validateEnchantment("MENDING");
            validateEnchantment("VANISHING_CURSE");

        } else if (material.equals(Material.DIAMOND_PICKAXE) || material.equals(Material.GOLD_PICKAXE) ||
                material.equals(Material.IRON_PICKAXE) || material.equals(Material.STONE_PICKAXE) ||
                material.equals(Material.WOOD_PICKAXE)) {

            validateEnchantment("DURABILITY");
            validateEnchantment("MENDING");
            validateEnchantment("VANISHING_CURSE");
            validateEnchantment("DIG_SPEED");
            validateEnchantment("LOOT_BONUS_BLOCKS");
            validateEnchantment("SILK_TOUCH");

        } else if (material.equals(Material.DIAMOND_SPADE) || material.equals(Material.GOLD_SPADE) ||
                material.equals(Material.IRON_SPADE) || material.equals(Material.STONE_SPADE) ||
                material.equals(Material.WOOD_SPADE)){

            validateEnchantment("DURABILITY");
            validateEnchantment("MENDING");
            validateEnchantment("VANISHING_CURSE");
            validateEnchantment("DIG_SPEED");
            validateEnchantment("LOOT_BONUS_BLOCKS");
            validateEnchantment("SILK_TOUCH");

        } else if (material.equals(Material.DIAMOND_HOE) || material.equals(Material.GOLD_HOE) ||
                material.equals(Material.IRON_HOE) || material.equals(Material.STONE_HOE) ||
                material.equals(Material.WOOD_HOE) || material.equals(Material.SHIELD)) {

            validateEnchantment("DURABILITY");
            validateEnchantment("MENDING");
            validateEnchantment("VANISHING_CURSE");

        } else if (material.equals(Material.DIAMOND_AXE) || material.equals(Material.GOLD_AXE) ||
                material.equals(Material.IRON_AXE) || material.equals(Material.STONE_AXE) ||
                material.equals(Material.WOOD_AXE)) {

            validateEnchantment("DAMAGE_ALL");
            validateEnchantment("DAMAGE_ARTHROPODS");
            validateEnchantment("DAMAGE_UNDEAD");
            validateEnchantment("DURABILITY");
            validateEnchantment("FIRE_ASPECT");
            validateEnchantment("KNOCKBACK");
            validateEnchantment("LOOT_BONUS_MOBS");
            validateEnchantment("MENDING");
            validateEnchantment("VANISHING_CURSE");
            validateEnchantment("DIG_SPEED");
            validateEnchantment("LOOT_BONUS_BLOCKS");

        } else if (material.equals(Material.CHAINMAIL_HELMET) || material.equals(Material.DIAMOND_HELMET) ||
                material.equals(Material.GOLD_HELMET) || material.equals(Material.IRON_HELMET) ||
                material.equals(Material.LEATHER_HELMET)) {

            validateEnchantment("BINDING_CURSE");
            validateEnchantment("DURABILITY");
            validateEnchantment("MENDING");
            validateEnchantment("OXYGEN");
            validateEnchantment("PROTECTION_ENVIRONMENTAL");
            validateEnchantment("PROTECTION_EXPLOSIONS");
            validateEnchantment("PROTECTION_FIRE");
            validateEnchantment("PROTECTION_PROJECTILE");
            validateEnchantment("THORNS");
            validateEnchantment("VANISHING_CURSE");
            validateEnchantment("WATER_WORKER");

        } else if (material.equals(Material.CHAINMAIL_CHESTPLATE) || material.equals(Material.DIAMOND_CHESTPLATE) ||
                material.equals(Material.GOLD_CHESTPLATE) || material.equals(Material.IRON_CHESTPLATE) ||
                material.equals(Material.LEATHER_CHESTPLATE)) {

            validateEnchantment("BINDING_CURSE");
            validateEnchantment("DURABILITY");
            validateEnchantment("MENDING");
            validateEnchantment("PROTECTION_ENVIRONMENTAL");
            validateEnchantment("PROTECTION_EXPLOSIONS");
            validateEnchantment("PROTECTION_FIRE");
            validateEnchantment("PROTECTION_PROJECTILE");
            validateEnchantment("THORNS");
            validateEnchantment("VANISHING_CURSE");

        } else if (material.equals(Material.CHAINMAIL_LEGGINGS) || material.equals(Material.DIAMOND_LEGGINGS) ||
                material.equals(Material.GOLD_LEGGINGS) || material.equals(Material.IRON_LEGGINGS) ||
                material.equals(Material.LEATHER_LEGGINGS)) {

            validateEnchantment("BINDING_CURSE");
            validateEnchantment("DURABILITY");
            validateEnchantment("MENDING");
            validateEnchantment("PROTECTION_ENVIRONMENTAL");
            validateEnchantment("PROTECTION_EXPLOSIONS");
            validateEnchantment("PROTECTION_FIRE");
            validateEnchantment("PROTECTION_PROJECTILE");
            validateEnchantment("THORNS");
            validateEnchantment("VANISHING_CURSE");

        } else if (material.equals(Material.CHAINMAIL_BOOTS) || material.equals(Material.DIAMOND_BOOTS) ||
                material.equals(Material.GOLD_BOOTS) || material.equals(Material.IRON_BOOTS) ||
                material.equals(Material.LEATHER_BOOTS)) {

            validateEnchantment("BINDING_CURSE");
            validateEnchantment("DURABILITY");
            validateEnchantment("MENDING");
            validateEnchantment("PROTECTION_ENVIRONMENTAL");
            validateEnchantment("PROTECTION_EXPLOSIONS");
            validateEnchantment("PROTECTION_FALL");
            validateEnchantment("PROTECTION_FIRE");
            validateEnchantment("PROTECTION_PROJECTILE");
            validateEnchantment("THORNS");
            validateEnchantment("VANISHING_CURSE");
            validateEnchantment("DEPTH_STRIDER");
            validateEnchantment("FROST_WALKER");


        } else if (material.equals(Material.FISHING_ROD)) {

            validateEnchantment("BINDING_CURSE");
            validateEnchantment("DURABILITY");
            validateEnchantment("MENDING");
            validateEnchantment("LUCK");
            validateEnchantment("LURE");

        } else if (material.equals(Material.SHEARS)) {

            validateEnchantment("BINDING_CURSE");
            validateEnchantment("DIG_SPEED");
            validateEnchantment("MENDING");
            validateEnchantment("DURABILITY");

        }

        if (validEnchantments.size() == 0) {

            return oldMeta;

        }

        ItemMeta newMeta = oldMeta;

        HashMap<Enchantment, Integer> validEnchantmentsClone = (HashMap<Enchantment, Integer>) validEnchantments.clone();

        int materialRank = ItemRankHandler.itemTypePower(material);
        rankLevel -= materialRank;

        //randomizer for enchantments
        for (int i = 0; i < rankLevel; i++) {

            if (validEnchantments.size() < 1) {

                break;

            }

            int randomIndex = random.nextInt(validEnchantments.size());

            List<Enchantment> enchantmentList = new ArrayList();

            for (Enchantment enchantment : validEnchantments.keySet()) {

                enchantmentList.add(enchantment);

            }

            String enchantmentString = enchantmentList.get(randomIndex).getName();

            Enchantment enchantment = enchantmentList.get(randomIndex);

            validEnchantments.put(enchantment, validEnchantments.get(enchantment) - 1);

            if (ConfigValues.randomItemsConfig.contains("Valid Enchantments." + enchantmentString + ".Max Level")) {

                int finalEnchantLevel = validEnchantmentsClone.get(enchantment) - validEnchantments.get(enchantment);

                newMeta.addEnchant(enchantment, finalEnchantLevel, true);

            } else {

                newMeta.addEnchant(enchantment, 1, true);

            }

            int newEnchantInt = validEnchantments.get(enchantment);

            if (newEnchantInt == 0) {

                validEnchantments.remove(enchantment);

            }

        }

        return newMeta;

    }

    private void validateEnchantment(String string) {

        String mainString = "Valid Enchantments." + string;

        if (ConfigValues.randomItemsConfig.getBoolean(mainString + ".Allow")) {

            int enchantmentLevel;

            if (ConfigValues.randomItemsConfig.contains(mainString + ".Max Level")) {

                enchantmentLevel = ConfigValues.randomItemsConfig.getInt(mainString + ".Max Level");

            } else {

                enchantmentLevel = 1;

            }

            //index for random getting
            validEnchantments.put(Enchantment.getByName(string), enchantmentLevel);

        }

    }

}
