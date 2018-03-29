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

package com.magmaguy.elitemobs.naturalmobspawner;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.ConfigValues;
import com.magmaguy.elitemobs.config.ItemsUniqueConfig;
import com.magmaguy.elitemobs.config.MobCombatSettingsConfig;
import com.magmaguy.elitemobs.config.ValidMobsConfig;
import com.magmaguy.elitemobs.elitedrops.UniqueItemConstructor;
import com.magmaguy.elitemobs.mobscanner.ValidAgressiveMobFilter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.Random;

import static org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason.CUSTOM;
import static org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason.NATURAL;

/**
 * Created by MagmaGuy on 24/04/2017.
 */
public class NaturalMobMetadataAssigner implements Listener {

    private static Random random = new Random();
    private int range = Bukkit.getServer().getViewDistance() * 16;

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onSpawn(CreatureSpawnEvent event) {

        if (event.getEntity().hasMetadata(MetadataHandler.ELITE_MOB_MD)) return;
        if (event.getEntity().getCustomName() != null) return;

        if (!ConfigValues.mobCombatSettingsConfig.getBoolean(MobCombatSettingsConfig.NATURAL_MOB_SPAWNING) ||
                !ConfigValues.validMobsConfig.getBoolean(ValidMobsConfig.ALLOW_AGGRESSIVE_ELITEMOBS) ||
                !ConfigValues.validWorldsConfig.getBoolean("Valid worlds." + event.getEntity().getWorld().getName())) {

            return;

        }

        if (event.getSpawnReason().equals(CreatureSpawnEvent.SpawnReason.SPAWNER) && !ConfigValues.mobCombatSettingsConfig.getBoolean(MobCombatSettingsConfig.SPAWNERS_SPAWN_ELITE_MOBS))
            return;

        if (event.getSpawnReason() == NATURAL || event.getSpawnReason() == CUSTOM) {

            Entity entity = event.getEntity();

            if (ValidAgressiveMobFilter.ValidAgressiveMobFilter(entity)) {

                entity.setMetadata(MetadataHandler.NATURAL_MOB_MD, new FixedMetadataValue(MetadataHandler.PLUGIN, true));

                int huntingGearChanceAdder = 0;

                for (Player player : Bukkit.getOnlinePlayers()) {

                    if (player.getWorld().equals(entity.getWorld()) &&
                            (!player.hasMetadata(MetadataHandler.VANISH_NO_PACKET) ||
                                    player.hasMetadata(MetadataHandler.VANISH_NO_PACKET) && !player.getMetadata(MetadataHandler.VANISH_NO_PACKET).get(0).asBoolean())) {

                        if (player.getLocation().distance(entity.getLocation()) < range) {

                            ItemStack helmet = player.getInventory().getHelmet();

                            ItemStack chestplate = player.getInventory().getChestplate();

                            ItemStack leggings = player.getInventory().getLeggings();

                            ItemStack boots = player.getInventory().getBoots();

                            ItemStack heldItem = player.getInventory().getItemInMainHand();

                            UniqueItemConstructor uniqueItemConstructor = new UniqueItemConstructor();

                            if (uniqueItemConstructor.huntingSetItemDetector(helmet)) huntingGearChanceAdder++;
                            if (uniqueItemConstructor.huntingSetItemDetector(chestplate)) huntingGearChanceAdder++;
                            if (uniqueItemConstructor.huntingSetItemDetector(leggings)) huntingGearChanceAdder++;
                            if (uniqueItemConstructor.huntingSetItemDetector(boots)) huntingGearChanceAdder++;
                            if (uniqueItemConstructor.huntingSetItemDetector(heldItem)) huntingGearChanceAdder++;

                        }

                    }

                }

                Double validChance = (ConfigValues.mobCombatSettingsConfig.getDouble(MobCombatSettingsConfig.AGGRESSIVE_MOB_CONVERSION_PERCENTAGE) +
                        (huntingGearChanceAdder * ConfigValues.itemsUniqueConfig.getInt(ItemsUniqueConfig.HUNTING_SET_CHANCE_INCREASER))) / 100;

                if (random.nextDouble() < validChance) {

                    NaturalMobSpawner naturalMobSpawner = new NaturalMobSpawner();
                    naturalMobSpawner.naturalMobProcessor(entity);

                }

            }

        }

    }

}
