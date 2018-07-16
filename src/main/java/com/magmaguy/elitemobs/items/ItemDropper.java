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

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.ConfigValues;
import com.magmaguy.elitemobs.config.ItemsDropSettingsConfig;
import com.magmaguy.elitemobs.config.ItemsProceduralSettingsConfig;
import com.magmaguy.elitemobs.items.itemconstructor.ItemConstructor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by MagmaGuy on 04/06/2017.
 */
public class ItemDropper implements Listener {

    private Random random = new Random();

    @EventHandler(priority = EventPriority.LOWEST)
    public void onDeath(EntityDeathEvent event) {

        Entity entity = event.getEntity();

        if (entity == null) return;
        if (!entity.hasMetadata(MetadataHandler.NATURAL_MOB_MD) ||
                !entity.hasMetadata(MetadataHandler.ELITE_MOB_MD)) return;

        if (entity.getMetadata(MetadataHandler.ELITE_MOB_MD).get(0).asInt() < 2) return;

        determineItemTier((LivingEntity) entity);

    }

    public void determineItemTier(LivingEntity entity) {

        int mobTier = (int) MobTierFinder.findMobTier(entity);

        if (mobTier == 0) {

            generateItem(entity, mobTier);
            return;

        }

        generateTieredItem(mobTier, entity);
    }

    public void generateTieredItem(int mobTier, LivingEntity entity) {

        double chanceToUpgradeTier = 1 / (double) mobTier * ConfigValues.itemsDropSettingsConfig.getDouble(ItemsDropSettingsConfig.MAXIMUM_LOOT_TIER);

//        Bukkit.getLogger().info("Chance to upgrade: " + chanceToUpgradeTier);
//        Bukkit.getLogger().info("Mob Tier: " + mobTier);
//        Bukkit.getLogger().info("Max loot tier: " + ConfigValues.itemsDropSettingsConfig.getDouble(ItemsDropSettingsConfig.MAXIMUM_LOOT_TIER));

        if (ThreadLocalRandom.current().nextDouble() * 100 < chanceToUpgradeTier) {

            generateItem(entity, mobTier + 1);
            return;

        }

        double diceRoll = ThreadLocalRandom.current().nextDouble();

        if (diceRoll < 0.20) {

            generateItem(entity, mobTier);
            return;

        }

        if (diceRoll < 0.40) {

            mobTier -= 1;

            if (mobTier < 0) mobTier = 0;

            generateItem(entity, mobTier);
            return;

        }

        if (diceRoll < 0.60) {

            mobTier -= 2;

            if (mobTier < 0) mobTier = 0;

            generateItem(entity, mobTier);
            return;

        }

        if (diceRoll < 0.80) {

            mobTier -= 3;

            if (mobTier < 0) mobTier = 0;

            generateItem(entity, mobTier);
            return;

        }

        if (diceRoll < 1.00) {

            mobTier -= 4;

            if (mobTier < 0) mobTier = 0;

            generateItem(entity, mobTier);
            return;

        }

    }

    public void generateItem(LivingEntity entity, int itemTier) {

        //remember that this is used by other classes, like the extra loot power
        double chanceToDrop = ConfigValues.itemsDropSettingsConfig.getDouble(ItemsDropSettingsConfig.ELITE_ITEM_FLAT_DROP_RATE) / 100 +
                (ConfigValues.itemsDropSettingsConfig.getDouble(ItemsDropSettingsConfig.ELITE_ITEM_LEVEL_DROP_RATE) / 100 *
                        entity.getMetadata(MetadataHandler.ELITE_MOB_MD).get(0).asInt());

        if (random.nextDouble() > chanceToDrop) return;

        boolean proceduralItemsOn = ConfigValues.itemsProceduralSettingsConfig.getBoolean(ItemsProceduralSettingsConfig.DROP_ITEMS_ON_DEATH);

        boolean customItemsOn = ConfigValues.itemsDropSettingsConfig.getBoolean(ItemsDropSettingsConfig.DROP_CUSTOM_ITEMS);

        boolean staticCustomItemsExist = CustomItemConstructor.staticCustomItemHashMap.size() > 0;

        boolean customDynamicDropExists = CustomItemConstructor.dynamicRankedItemStacks.containsKey(itemTier);

        if (proceduralItemsOn && !customItemsOn) {

            dropProcedurallyGeneratedItem(itemTier, entity);
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

                dropCustomDynamicLoot(itemTier, entity);

            }

            if (customDynamicDropExists && staticCustomItemsExist) {

                HashMap<String, Double> weighedConfigValues = new HashMap<>();
                weighedConfigValues.put(ItemsDropSettingsConfig.CUSTOM_DYNAMIC_ITEM_WEIGHT, ConfigValues.itemsDropSettingsConfig.getDouble(ItemsDropSettingsConfig.CUSTOM_DYNAMIC_ITEM_WEIGHT));
                weighedConfigValues.put(ItemsDropSettingsConfig.CUSTOM_STATIC_ITEM_WEIGHT, ConfigValues.itemsDropSettingsConfig.getDouble(ItemsDropSettingsConfig.CUSTOM_STATIC_ITEM_WEIGHT));

                String selectedLootSystem = pickWeighedLootSystem(weighedConfigValues);

                if (selectedLootSystem.equals(ItemsDropSettingsConfig.CUSTOM_DYNAMIC_ITEM_WEIGHT))
                    dropCustomDynamicLoot(itemTier, entity);
                if (selectedLootSystem.equals(ItemsDropSettingsConfig.CUSTOM_STATIC_ITEM_WEIGHT))
                    dropCustomStaticLoot(entity);

                return;

            }

        }

        if (proceduralItemsOn && customItemsOn) {

            if (!customDynamicDropExists && !staticCustomItemsExist) {

                dropProcedurallyGeneratedItem(itemTier, entity);
                return;

            }

            if (!customDynamicDropExists && staticCustomItemsExist) {

                HashMap<String, Double> weighedConfigValues = new HashMap<>();
                weighedConfigValues.put(ItemsDropSettingsConfig.PROCEDURAL_ITEM_WEIGHT, ConfigValues.itemsDropSettingsConfig.getDouble(ItemsDropSettingsConfig.PROCEDURAL_ITEM_WEIGHT));
                weighedConfigValues.put(ItemsDropSettingsConfig.CUSTOM_STATIC_ITEM_WEIGHT, ConfigValues.itemsDropSettingsConfig.getDouble(ItemsDropSettingsConfig.CUSTOM_STATIC_ITEM_WEIGHT));

                String selectedLootSystem = pickWeighedLootSystem(weighedConfigValues);

                if (selectedLootSystem.equals(ItemsDropSettingsConfig.PROCEDURAL_ITEM_WEIGHT))
                    dropProcedurallyGeneratedItem(itemTier, entity);
                if (selectedLootSystem.equals(ItemsDropSettingsConfig.CUSTOM_STATIC_ITEM_WEIGHT))
                    dropCustomStaticLoot(entity);

                return;

            }

            if (customDynamicDropExists && !staticCustomItemsExist) {

                HashMap<String, Double> weighedConfigValues = new HashMap<>();
                weighedConfigValues.put(ItemsDropSettingsConfig.PROCEDURAL_ITEM_WEIGHT, ConfigValues.itemsDropSettingsConfig.getDouble(ItemsDropSettingsConfig.PROCEDURAL_ITEM_WEIGHT));
                weighedConfigValues.put(ItemsDropSettingsConfig.CUSTOM_DYNAMIC_ITEM_WEIGHT, ConfigValues.itemsDropSettingsConfig.getDouble(ItemsDropSettingsConfig.CUSTOM_DYNAMIC_ITEM_WEIGHT));

                String selectedLootSystem = pickWeighedLootSystem(weighedConfigValues);

                if (selectedLootSystem.equals(ItemsDropSettingsConfig.PROCEDURAL_ITEM_WEIGHT))
                    dropProcedurallyGeneratedItem(itemTier, entity);
                if (selectedLootSystem.equals(ItemsDropSettingsConfig.CUSTOM_DYNAMIC_ITEM_WEIGHT))
                    dropCustomDynamicLoot(itemTier, entity);

                return;

            }

            if (customDynamicDropExists && staticCustomItemsExist) {

                HashMap<String, Double> weighedConfigValues = new HashMap<>();
                weighedConfigValues.put(ItemsDropSettingsConfig.PROCEDURAL_ITEM_WEIGHT, ConfigValues.itemsDropSettingsConfig.getDouble(ItemsDropSettingsConfig.PROCEDURAL_ITEM_WEIGHT));
                weighedConfigValues.put(ItemsDropSettingsConfig.CUSTOM_DYNAMIC_ITEM_WEIGHT, ConfigValues.itemsDropSettingsConfig.getDouble(ItemsDropSettingsConfig.CUSTOM_DYNAMIC_ITEM_WEIGHT));
                weighedConfigValues.put(ItemsDropSettingsConfig.CUSTOM_STATIC_ITEM_WEIGHT, ConfigValues.itemsDropSettingsConfig.getDouble(ItemsDropSettingsConfig.CUSTOM_STATIC_ITEM_WEIGHT));

                String selectedLootSystem = pickWeighedLootSystem(weighedConfigValues);

                if (selectedLootSystem.equals(ItemsDropSettingsConfig.PROCEDURAL_ITEM_WEIGHT))
                    dropProcedurallyGeneratedItem(itemTier, entity);
                if (selectedLootSystem.equals(ItemsDropSettingsConfig.CUSTOM_DYNAMIC_ITEM_WEIGHT))
                    dropCustomDynamicLoot(itemTier, entity);
                if (selectedLootSystem.equals(ItemsDropSettingsConfig.CUSTOM_STATIC_ITEM_WEIGHT))
                    dropCustomStaticLoot(entity);

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

    private void dropCustomDynamicLoot(int itemTier, Entity entity) {

//        double targetItemWorth = ItemWorthCalculator.targetItemWorth(itemTier);
//        int itemRank = (int) (targetItemWorth / 10);

        int randomCustomDrop = random.nextInt(CustomItemConstructor.dynamicRankedItemStacks.get(itemTier).size());

        //get rank matching randomizer and item matching randomized index
        entity.getWorld().dropItem(entity.getLocation(), CustomItemConstructor.dynamicRankedItemStacks.get(itemTier).get(randomCustomDrop));

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

    public void dropProcedurallyGeneratedItem(int tierLevel, LivingEntity livingEntity) {

        ItemStack randomLoot = ItemConstructor.constructItem(tierLevel, livingEntity);

        livingEntity.getWorld().dropItem(livingEntity.getLocation(), randomLoot);

    }

}
