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

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.ConfigValues;
import com.magmaguy.elitemobs.config.LootCustomConfig;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import static com.magmaguy.elitemobs.ChatColorConverter.chatColorConverter;
import static org.bukkit.Bukkit.getLogger;

/**
 * Created by MagmaGuy on 29/11/2016.
 */
public class EliteDropsHandler implements Listener {

    public static List<ItemStack> lootList = new ArrayList();
    public static HashMap<ItemStack, List<PotionEffect>> potionEffectItemList = new HashMap();
    public static HashMap<Integer, List<ItemStack>> rankedItemStacks = new HashMap<>();
    Plugin plugin = Bukkit.getPluginManager().getPlugin(MetadataHandler.ELITE_MOBS);

    LootCustomConfig lootCustomConfig = new LootCustomConfig();

    public void superDropParser() {

        List<String> lootCount = lootCounter();

        for (String lootEntry : lootCount) {

            int itemPower = 0;

            StringBuilder path = new StringBuilder();
            path.append(lootEntry);

            String previousPath = path.toString();


            String itemType = itemTypeHandler(previousPath);
            itemPower += itemTypePower(Material.getMaterial(itemType));
            Bukkit.getLogger().info("Adding: " + previousPath);
            String itemName = itemNameHandler(previousPath);
            List itemLore = itemLoreHandler(previousPath);

            List itemEnchantments = itemEnchantmentHandler(previousPath);
            List potionEffects = itemPotionEffectHandler(previousPath);

            ItemStack itemStack = new ItemStack(Material.getMaterial(itemType), 1);
            ItemMeta itemMeta = itemStack.getItemMeta();
            itemMeta.setDisplayName(itemName);
            itemMeta.setLore(itemLore);

            if (itemEnchantments != null) {

                for (Object object : itemEnchantments) {

                    String string = object.toString();

                    String[] parsedString = string.split(",");

                    String enchantmentName = parsedString[0];
                    Enchantment enchantmentType = Enchantment.getByName(enchantmentName);

                    int enchantmentLevel = Integer.parseInt(parsedString[1]);
                    itemPower += enchantmentLevel;

                    itemMeta.addEnchant(enchantmentType, enchantmentLevel, true);

                }

            }

            itemStack.setItemMeta(itemMeta);

            lootList.add(itemStack);

            List<PotionEffect> parsedPotionEffect = new ArrayList();

            //Add potion effects to a separate list to reduce i/o operations
            if (potionEffects != null) {

                for (Object object : potionEffects) {

                    String string = object.toString();

                    String[] parsedString = string.split(",");

                    String potionEffectTypeString = parsedString[0];
                    PotionEffectType potionEffectType = PotionEffectType.getByName(potionEffectTypeString);

                    //this is a really bad way of doing things, two wrongs make a right
                    if (parsedString.length % 2 != 0) {

                        getLogger().info("Your item " + itemName + " has a problematic potions effect entry.");

                    }

                    int potionEffectAmplifier = Integer.parseInt(parsedString[1]);
                    itemPower += potionEffectAmplifier;

                    PotionEffect potionEffect = new PotionEffect(potionEffectType, 40, potionEffectAmplifier);

                    parsedPotionEffect.add(potionEffect);

                }

                potionEffectItemList.put(itemStack, parsedPotionEffect);

            }

            rankedItemMapCreator(itemPower, itemStack);

        }

    }


    public List<String> lootCounter() {

        List<String> lootCount = new ArrayList();

        for (String configIterator : lootCustomConfig.getLootConfig().getKeys(true)) {

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

    public String automatedStringBuilder(String previousPath, String append) {

        StringBuilder automatedStringBuilder = new StringBuilder();

        automatedStringBuilder.append(previousPath);
        automatedStringBuilder.append(".");
        automatedStringBuilder.append(append);

        String path = automatedStringBuilder.toString();

        return path;

    }

    public String itemTypeHandler(String previousPath) {

        String path = automatedStringBuilder(previousPath, "Item Type");

        String itemType = lootCustomConfig.getLootConfig().getString(path);

        return itemType;

    }

    public int itemTypePower (Material material) {

        switch (material) {

            case DIAMOND:
                return 5;
            case DIAMOND_AXE:
                return 5;
            case DIAMOND_BARDING:
                return 5;
            case DIAMOND_BLOCK:
                return 5;
            case DIAMOND_BOOTS:
                return 5;
            case DIAMOND_CHESTPLATE:
                return 5;
            case DIAMOND_HELMET:
                return 5;
            case DIAMOND_HOE:
                return 5;
            case DIAMOND_LEGGINGS:
                return 5;
            case DIAMOND_ORE:
                return 5;
            case DIAMOND_PICKAXE:
                return 5;
            case DIAMOND_SPADE:
                return 5;
            case DIAMOND_SWORD:
                return 5;
            case IRON_AXE:
                return 4;
            case IRON_BARDING:
                return 4;
            case IRON_BLOCK:
                return 4;
            case IRON_BOOTS:
                return 4;
            case IRON_CHESTPLATE:
                return 4;
            case IRON_HELMET:
                return 4;
            case IRON_HOE:
                return 4;
            case IRON_INGOT:
                return 4;
            case IRON_LEGGINGS:
                return 4;
            case IRON_NUGGET:
                return 4;
            case IRON_ORE:
                return 4;
            case IRON_PICKAXE:
                return 4;
            case IRON_SPADE:
                return 4;
            case IRON_SWORD:
                return 4;
            case CHAINMAIL_BOOTS:
                return 3;
            case CHAINMAIL_CHESTPLATE:
                return 3;
            case CHAINMAIL_HELMET:
                return 3;
            case CHAINMAIL_LEGGINGS:
                return 3;
            case GOLD_AXE:
                return 2;
            case GOLD_BARDING:
                return 2;
            case GOLD_BLOCK:
                return 2;
            case GOLD_BOOTS:
                return 2;
            case GOLD_CHESTPLATE:
                return 2;
            case GOLD_HELMET:
                return 2;
            case GOLD_HOE:
                return 2;
            case GOLD_INGOT:
                return 2;
            case GOLD_LEGGINGS:
                return 2;
            case GOLD_NUGGET:
                return 2;
            case GOLD_ORE:
                return 2;
            case GOLD_PICKAXE:
                return 2;
            case GOLD_SPADE:
                return 2;
            case GOLD_SWORD:
                return 2;
            case GOLDEN_APPLE:
                return 2;
            case GOLDEN_CARROT:
                return 2;
            case LEATHER_BOOTS:
                return 1;
            case LEATHER_CHESTPLATE:
                return 1;
            case LEATHER_HELMET:
                return 1;
            case LEATHER_LEGGINGS:
                return 1;
            default:
                return 0;

        }

    }

    public String itemNameHandler(String previousPath) {

        String path = automatedStringBuilder(previousPath, "Item Name");

        String itemName = chatColorConverter(lootCustomConfig.getLootConfig().getString(path));

        return itemName;

    }

    public List itemLoreHandler(String previousPath) {

        String path = automatedStringBuilder(previousPath, "Item Lore");

        List<String> itemLore = (List<String>) lootCustomConfig.getLootConfig().getList(path);

        if (itemLore == null || itemLore.isEmpty()) {

            return itemLore;

        }

        List<String> newList = new ArrayList<>();

        for (String string : itemLore) {

            if (string != null && !string.isEmpty()) {

                newList.add(itemLore.indexOf(string), chatColorConverter(string));

            }

        }

        if (newList == null || newList.isEmpty()) {

            return itemLore;

        }

        return newList;

    }

    public List itemEnchantmentHandler(String previousPath) {

        String path = automatedStringBuilder(previousPath, "Enchantments");

        List enchantments = lootCustomConfig.getLootConfig().getList(path);

        return enchantments;

    }

    public List itemPotionEffectHandler(String previousPath) {

        String path = automatedStringBuilder(previousPath, "Potion Effects");

        List potionEffects = lootCustomConfig.getLootConfig().getList(path);

        return potionEffects;

    }

    public void rankedItemMapCreator (int itemPower, ItemStack itemStack) {

        if (itemPower == 0) {

            if (rankedItemStacks.get(itemPower) == null) {

                List<ItemStack> list = new ArrayList<>();

                list.add(itemStack);

                rankedItemStacks.put(itemPower, list);

            } else {

                List<ItemStack> list = rankedItemStacks.get(itemPower);

                list.add(itemStack);

                rankedItemStacks.put(itemPower, list);

            }

        } else {

            int itemRank = (int) Math.ceil(itemPower / 5);

            if (rankedItemStacks.get(itemRank) == null) {

                List<ItemStack> list = new ArrayList<>();

                list.add(itemStack);

                rankedItemStacks.put(itemRank, list);

            } else {

                List<ItemStack> list = rankedItemStacks.get(itemRank);

                list.add(itemStack);

                rankedItemStacks.put(itemRank, list);

            }

        }

    }


    @EventHandler
    public void onDeath(EntityDeathEvent event) {

        Entity entity = event.getEntity();

        Random random = new Random();

        if (entity.hasMetadata(MetadataHandler.NATURAL_MOB_MD) &&
                entity.hasMetadata(MetadataHandler.ELITE_MOB_MD) &&
                entity.getMetadata(MetadataHandler.ELITE_MOB_MD).get(0).asInt() > 4) {


            if (random.nextDouble() < ConfigValues.defaultConfig.getDouble("Aggressive EliteMobs flat loot drop rate %") / 100) {

                int randomDrop = random.nextInt(lootList.size());

                entity.getWorld().dropItem(entity.getLocation(), lootList.get(randomDrop));

            }

            //double drops
            if (ConfigValues.defaultConfig.getBoolean("Aggressive EliteMobs can drop additional loot with drop % based on EliteMob level (higher is more likely)") &&
                    random.nextDouble() < (entity.getMetadata(MetadataHandler.ELITE_MOB_MD).get(0).asInt() / 100)) {

                int randomDrop = random.nextInt(lootList.size());

                entity.getWorld().dropItem(entity.getLocation(), lootList.get(randomDrop));

            }

            entity.removeMetadata(MetadataHandler.ELITE_MOB_MD, plugin);

            if (entity.hasMetadata(MetadataHandler.NATURAL_MOB_MD)) {

                entity.removeMetadata(MetadataHandler.NATURAL_MOB_MD, plugin);

            }

        }

    }

}
