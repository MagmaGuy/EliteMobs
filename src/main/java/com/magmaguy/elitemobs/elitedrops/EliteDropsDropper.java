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
import com.magmaguy.elitemobs.config.RandomItemsSettingsConfig;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

/**
 * Created by MagmaGuy on 04/06/2017.
 */
public class EliteDropsDropper implements Listener {

    private Random random = new Random();

    @EventHandler(priority = EventPriority.LOWEST)
    public void onDeath(EntityDeathEvent event) {

        if (!ConfigValues.defaultConfig.getBoolean("Aggressive EliteMobs can drop custom loot") &&
                !ConfigValues.randomItemsConfig.getBoolean(RandomItemsSettingsConfig.DROP_ITEMS_ON_DEATH)) {

            return;

        }

        Entity entity = event.getEntity();

        dropItem(entity);

    }

    public void dropItem(Entity entity) {

        if (entity.hasMetadata(MetadataHandler.NATURAL_MOB_MD) &&
                entity.hasMetadata(MetadataHandler.ELITE_MOB_MD)) {

            if (entity.getMetadata(MetadataHandler.ELITE_MOB_MD).get(0).asInt() < 2) {

                return;

            }

            int mobLevel = entity.getMetadata(MetadataHandler.ELITE_MOB_MD).get(0).asInt();

            mobLevel = (int) ((mobLevel * ConfigValues.randomItemsConfig.getDouble("Mob level to item rank multiplier")) + (random.nextInt(6) + 1 - 3));

            if (mobLevel < 1) {

                mobLevel = 0;

            }

            double chanceToDrop = ConfigValues.defaultConfig.getDouble("Aggressive EliteMobs flat loot drop rate %") / 100 +
                    (ConfigValues.defaultConfig.getDouble("Aggressive EliteMobs drop rate % increase per mob level") *
                            entity.getMetadata(MetadataHandler.ELITE_MOB_MD).get(0).asInt() / 100);

            if (random.nextDouble() < chanceToDrop) {

                if (!CustomDropsConstructor.rankedItemStacks.isEmpty() && ConfigValues.defaultConfig.getBoolean("Aggressive EliteMobs can drop custom loot") &&
                        ConfigValues.randomItemsConfig.getBoolean(RandomItemsSettingsConfig.DROP_ITEMS_ON_DEATH)) {

                    if (CustomDropsConstructor.rankedItemStacks.containsKey(mobLevel)) {

                        if (random.nextDouble() * 100 <= ConfigValues.randomItemsConfig.getDouble("Percentage (%) of times random item drop instead of custom loot")) {

                            //create random loot
                            ProceduralItemGenerator proceduralItemGenerator = new ProceduralItemGenerator();
                            ItemStack randomLoot = proceduralItemGenerator.randomItemGenerator(mobLevel, entity);

                            entity.getWorld().dropItem(entity.getLocation(), randomLoot);

                        } else {

                            //drop custom loot
                            int randomCustomDrop = random.nextInt(CustomDropsConstructor.rankedItemStacks.get(mobLevel).size());

                            //get rank matching randomizer and item matching randomized index
                            entity.getWorld().dropItem(entity.getLocation(), CustomDropsConstructor.rankedItemStacks.get(mobLevel).get(randomCustomDrop));

                        }

                    } else {

                        ProceduralItemGenerator proceduralItemGenerator = new ProceduralItemGenerator();
                        ItemStack randomLoot = proceduralItemGenerator.randomItemGenerator(mobLevel, entity);

                        entity.getWorld().dropItem(entity.getLocation(), randomLoot);

                    }

                } else if (!CustomDropsConstructor.rankedItemStacks.isEmpty() && ConfigValues.defaultConfig.getBoolean("Aggressive EliteMobs can drop custom loot") &&
                        !ConfigValues.randomItemsConfig.getBoolean(RandomItemsSettingsConfig.DROP_ITEMS_ON_DEATH)) {

                    //WARNING: THIS DOES NOT SCALE ITEM LEVEL (since I don't know how much loot is available, approximating will not make sense
                    int customItemIndex = random.nextInt(CustomDropsConstructor.lootList.size());
                    ItemStack randomizedCustomItem = CustomDropsConstructor.lootList.get(customItemIndex);

                    entity.getWorld().dropItem(entity.getLocation(), randomizedCustomItem);

                } else if (!CustomDropsConstructor.rankedItemStacks.isEmpty() && !ConfigValues.defaultConfig.getBoolean("Aggressive EliteMobs can drop custom loot") &&
                        ConfigValues.randomItemsConfig.getBoolean(RandomItemsSettingsConfig.DROP_ITEMS_ON_DEATH)) {

                    ProceduralItemGenerator proceduralItemGenerator = new ProceduralItemGenerator();
                    ItemStack randomLoot = proceduralItemGenerator.randomItemGenerator(mobLevel, entity);

                    entity.getWorld().dropItem(entity.getLocation(), randomLoot);

                } else if (CustomDropsConstructor.rankedItemStacks.isEmpty() && ConfigValues.randomItemsConfig.getBoolean(RandomItemsSettingsConfig.DROP_ITEMS_ON_DEATH)) {

                    ProceduralItemGenerator proceduralItemGenerator = new ProceduralItemGenerator();
                    ItemStack randomLoot = proceduralItemGenerator.randomItemGenerator(mobLevel, entity);

                    entity.getWorld().dropItem(entity.getLocation(), randomLoot);

                }

            }

        }

    }

}
