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

import com.magmaguy.elitemobs.EliteMobs;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.ConfigValues;
import com.magmaguy.elitemobs.config.DefaultConfig;
import com.magmaguy.elitemobs.config.ItemsUniqueConfig;
import com.magmaguy.elitemobs.config.ValidMobsConfig;
import com.magmaguy.elitemobs.elitedrops.UniqueItemConstructor;
import com.magmaguy.elitemobs.mobscanner.ValidAgressiveMobFilter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;

import java.util.Random;

import static org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason.CUSTOM;
import static org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason.NATURAL;

/**
 * Created by MagmaGuy on 24/04/2017.
 */
public class NaturalMobMetadataAssigner implements Listener {

    private static Random random = new Random();
    private EliteMobs plugin;
    private int range = Bukkit.getServer().getViewDistance() * 16;


    public NaturalMobMetadataAssigner(Plugin plugin) {

        this.plugin = (EliteMobs) plugin;

    }

    @EventHandler
    public void onSpawn(CreatureSpawnEvent event) {

        if (!ConfigValues.defaultConfig.getBoolean(DefaultConfig.NATURAL_MOB_SPAWNING) ||
                !ConfigValues.validMobsConfig.getBoolean(ValidMobsConfig.ALLOW_AGGRESSIVE_ELITEMOBS) ||
                !ConfigValues.validWorldsConfig.getBoolean("Valid worlds." + event.getEntity().getWorld().getName())) {

            return;

        }

        if (event.getSpawnReason() == NATURAL || event.getSpawnReason() == CUSTOM) {

            Entity entity = event.getEntity();

            if (ValidAgressiveMobFilter.ValidAgressiveMobFilter(entity)) {

                entity.setMetadata(MetadataHandler.NATURAL_MOB_MD, new FixedMetadataValue(plugin, true));

                int huntingGearChanceAdder = 0;

                for (Player player : Bukkit.getOnlinePlayers()) {

                    if (player.getWorld().equals(entity.getWorld())) {

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

                            Bukkit.getLogger().info(huntingGearChanceAdder + "");

                        }

                    }

                }

                Double validChance = (ConfigValues.defaultConfig.getDouble(DefaultConfig.AGGRESSIVE_MOB_CONVERSION_PERCENTAGE) +
                        (huntingGearChanceAdder * ConfigValues.itemsUniqueConfig.getInt(ItemsUniqueConfig.HUNTING_SET_CHANCE_INCREASER))) / 100;

                if (random.nextDouble() < validChance) {

                    NaturalMobSpawner naturalMobSpawner = new NaturalMobSpawner(plugin);
                    naturalMobSpawner.naturalMobProcessor(entity);

                }

            }

        }

    }

}
