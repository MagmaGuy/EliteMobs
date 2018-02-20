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
import com.magmaguy.elitemobs.config.DefaultConfig;
import com.magmaguy.elitemobs.config.ItemsProceduralSettingsConfig;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Random;

/**
 * Created by MagmaGuy on 04/06/2017.
 */
public class EliteDropsDropper implements Listener {

    private Random random = new Random();

    @EventHandler(priority = EventPriority.LOWEST)
    public void onDeath(EntityDeathEvent event) {

        if (!ConfigValues.defaultConfig.getBoolean(DefaultConfig.ENABLE_PLUGIN_LOOT)) return;

        Entity entity = event.getEntity();

        if (!entity.hasMetadata(MetadataHandler.NATURAL_MOB_MD) ||
                !entity.hasMetadata(MetadataHandler.ELITE_MOB_MD)) return;

        if (entity.getMetadata(MetadataHandler.ELITE_MOB_MD).get(0).asInt() < 2) return;

        dropItem(entity);

    }

    public void dropItem(Entity entity) {

        //remember that this is used by other classes, like the extra loot power
        double chanceToDrop = ConfigValues.defaultConfig.getDouble(DefaultConfig.ELITE_ITEM_FLAT_DROP_RATE) / 100 +
                (ConfigValues.defaultConfig.getDouble(DefaultConfig.ELITE_ITEM_LEVEL_DROP_RATE) / 100 *
                        entity.getMetadata(MetadataHandler.ELITE_MOB_MD).get(0).asInt());

        if (random.nextDouble() > chanceToDrop) return;

        boolean proceduralItemsOn = ConfigValues.itemsProceduralSettingsConfig.getBoolean(ItemsProceduralSettingsConfig.DROP_ITEMS_ON_DEATH);

        boolean customItemsOn = ConfigValues.defaultConfig.getBoolean(DefaultConfig.DROP_CUSTOM_ITEMS);

        boolean staticCustomItemsExist = CustomItemConstructor.staticCustomItemHashMap.size() > 0;

        int itemRankWeighedWithMobLevel = entity.getMetadata(MetadataHandler.ELITE_MOB_MD).get(0).asInt();

        //random adds a little wiggle room for item rank power
        itemRankWeighedWithMobLevel = (int) ((itemRankWeighedWithMobLevel * ConfigValues.itemsProceduralSettingsConfig.getDouble(ItemsProceduralSettingsConfig.MOB_LEVEL_TO_RANK_MULTIPLIER)) + (random.nextInt(6) + 1 - 3));

        if (itemRankWeighedWithMobLevel < 1) itemRankWeighedWithMobLevel = 0;

        boolean customDynamicDropExists = CustomItemConstructor.dynamicRankedItemStacks.containsKey(itemRankWeighedWithMobLevel);

        if (proceduralItemsOn && !customItemsOn) {

            dropProcedurallyGeneratedItem(itemRankWeighedWithMobLevel, entity);
            return;

        }

        if (!proceduralItemsOn && customItemsOn) {

            if (!customDynamicDropExists && !staticCustomItemsExist) {

                return;

            }

            if (!customDynamicDropExists && staticCustomItemsExist) {

                dropCustomStaticLoot(entity);
                return;

            }

            if (customDynamicDropExists && !staticCustomItemsExist) {

                dropCustomDynamicLoot(itemRankWeighedWithMobLevel, entity);

            }

            if (customDynamicDropExists && staticCustomItemsExist) {

                HashMap<String, Double> weighedConfigValues = new HashMap<>();
                weighedConfigValues.put(DefaultConfig.CUSTOM_DYNAMIC_ITEM_WEIGHT, ConfigValues.defaultConfig.getDouble(DefaultConfig.CUSTOM_DYNAMIC_ITEM_WEIGHT));
                weighedConfigValues.put(DefaultConfig.CUSTOM_STATIC_ITEM_WEIGHT, ConfigValues.defaultConfig.getDouble(DefaultConfig.CUSTOM_STATIC_ITEM_WEIGHT));

                String selectedLootSystem = pickWeighedLootSystem(weighedConfigValues);

                if (selectedLootSystem.equals(DefaultConfig.CUSTOM_DYNAMIC_ITEM_WEIGHT))
                    dropCustomDynamicLoot(itemRankWeighedWithMobLevel, entity);
                if (selectedLootSystem.equals(DefaultConfig.CUSTOM_STATIC_ITEM_WEIGHT)) dropCustomStaticLoot(entity);

                return;

            }

        }

        if (proceduralItemsOn && customItemsOn) {

            if (!customDynamicDropExists && !staticCustomItemsExist) {

                dropProcedurallyGeneratedItem(itemRankWeighedWithMobLevel, entity);
                return;

            }

            if (!customDynamicDropExists && staticCustomItemsExist) {

                HashMap<String, Double> weighedConfigValues = new HashMap<>();
                weighedConfigValues.put(DefaultConfig.PROCEDURAL_ITEM_WEIGHT, ConfigValues.defaultConfig.getDouble(DefaultConfig.PROCEDURAL_ITEM_WEIGHT));
                weighedConfigValues.put(DefaultConfig.CUSTOM_STATIC_ITEM_WEIGHT, ConfigValues.defaultConfig.getDouble(DefaultConfig.CUSTOM_STATIC_ITEM_WEIGHT));

                String selectedLootSystem = pickWeighedLootSystem(weighedConfigValues);

                if (selectedLootSystem.equals(DefaultConfig.PROCEDURAL_ITEM_WEIGHT))
                    dropProcedurallyGeneratedItem(itemRankWeighedWithMobLevel, entity);
                if (selectedLootSystem.equals(DefaultConfig.CUSTOM_STATIC_ITEM_WEIGHT)) dropCustomStaticLoot(entity);

                return;

            }

            if (customDynamicDropExists && !staticCustomItemsExist) {

                HashMap<String, Double> weighedConfigValues = new HashMap<>();
                weighedConfigValues.put(DefaultConfig.PROCEDURAL_ITEM_WEIGHT, ConfigValues.defaultConfig.getDouble(DefaultConfig.PROCEDURAL_ITEM_WEIGHT));
                weighedConfigValues.put(DefaultConfig.CUSTOM_DYNAMIC_ITEM_WEIGHT, ConfigValues.defaultConfig.getDouble(DefaultConfig.CUSTOM_DYNAMIC_ITEM_WEIGHT));

                String selectedLootSystem = pickWeighedLootSystem(weighedConfigValues);

                if (selectedLootSystem.equals(DefaultConfig.PROCEDURAL_ITEM_WEIGHT))
                    dropProcedurallyGeneratedItem(itemRankWeighedWithMobLevel, entity);
                if (selectedLootSystem.equals(DefaultConfig.CUSTOM_DYNAMIC_ITEM_WEIGHT))
                    dropCustomDynamicLoot(itemRankWeighedWithMobLevel, entity);

                return;

            }

            if (customDynamicDropExists && staticCustomItemsExist) {

                HashMap<String, Double> weighedConfigValues = new HashMap<>();
                weighedConfigValues.put(DefaultConfig.PROCEDURAL_ITEM_WEIGHT, ConfigValues.defaultConfig.getDouble(DefaultConfig.PROCEDURAL_ITEM_WEIGHT));
                weighedConfigValues.put(DefaultConfig.CUSTOM_DYNAMIC_ITEM_WEIGHT, ConfigValues.defaultConfig.getDouble(DefaultConfig.CUSTOM_DYNAMIC_ITEM_WEIGHT));
                weighedConfigValues.put(DefaultConfig.CUSTOM_STATIC_ITEM_WEIGHT, ConfigValues.defaultConfig.getDouble(DefaultConfig.CUSTOM_STATIC_ITEM_WEIGHT));

                String selectedLootSystem = pickWeighedLootSystem(weighedConfigValues);

                if (selectedLootSystem.equals(DefaultConfig.PROCEDURAL_ITEM_WEIGHT))
                    dropProcedurallyGeneratedItem(itemRankWeighedWithMobLevel, entity);
                if (selectedLootSystem.equals(DefaultConfig.CUSTOM_DYNAMIC_ITEM_WEIGHT))
                    dropCustomDynamicLoot(itemRankWeighedWithMobLevel, entity);
                if (selectedLootSystem.equals(DefaultConfig.CUSTOM_STATIC_ITEM_WEIGHT)) dropCustomStaticLoot(entity);

                return;

            }

        }

    }

    private String pickWeighedLootSystem(HashMap<String, Double> weighedConfigValues) {

        double totalWeight = 0;

        for (String string : weighedConfigValues.keySet()) {

            totalWeight += weighedConfigValues.get(string);

        }

        String selectedLootSystem = null;
        double random = Math.random() * totalWeight;

        for (String string : weighedConfigValues.keySet()) {

            random -= weighedConfigValues.get(string);


            if (random <= 0) {

                selectedLootSystem = string;

                break;

            }

        }

        return selectedLootSystem;

    }

    private void dropCustomDynamicLoot(int mobLevel, Entity entity) {

        int randomCustomDrop = random.nextInt(CustomItemConstructor.dynamicRankedItemStacks.get(mobLevel).size());

        //get rank matching randomizer and item matching randomized index
        entity.getWorld().dropItem(entity.getLocation(), CustomItemConstructor.dynamicRankedItemStacks.get(mobLevel).get(randomCustomDrop));

    }

    private void dropCustomStaticLoot(Entity entity) {

        double totalWeight = 0;

        for (ItemStack itemStack : CustomItemConstructor.staticCustomItemHashMap.keySet()) {

            totalWeight += CustomItemConstructor.staticCustomItemHashMap.get(itemStack);

        }

        ItemStack generatedItemStack = null;
        double random = Math.random() * totalWeight;

        for (ItemStack itemStack : CustomItemConstructor.staticCustomItemHashMap.keySet()) {

            random -= CustomItemConstructor.staticCustomItemHashMap.get(itemStack);

            if (random <= 0) {

                generatedItemStack = itemStack;
                break;

            }

        }

        entity.getWorld().dropItem(entity.getLocation(), generatedItemStack);

    }

    private void dropProcedurallyGeneratedItem(int mobLevel, Entity entity) {

        ProceduralItemGenerator proceduralItemGenerator = new ProceduralItemGenerator();
        ItemStack randomLoot = proceduralItemGenerator.randomItemGenerator(mobLevel, entity);

        entity.getWorld().dropItem(entity.getLocation(), randomLoot);

    }

}
