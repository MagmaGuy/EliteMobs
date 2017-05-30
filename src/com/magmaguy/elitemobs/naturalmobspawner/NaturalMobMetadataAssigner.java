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
import com.magmaguy.elitemobs.elitedrops.EliteDropsHandler;
import com.magmaguy.elitemobs.mobscanner.ValidAgressiveMobFilter;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;

import java.util.Random;

import static com.magmaguy.elitemobs.ChatColorConverter.chatColorConverter;
import static org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason.CUSTOM;
import static org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason.NATURAL;

/**
 * Created by MagmaGuy on 24/04/2017.
 */
public class NaturalMobMetadataAssigner implements Listener {

    private EliteMobs plugin;
    private int range = 100;


    public NaturalMobMetadataAssigner(Plugin plugin) {

        this.plugin = (EliteMobs) plugin;

    }

    @EventHandler
    public void onSpawn(CreatureSpawnEvent event) {

        if (!ConfigValues.defaultConfig.getBoolean("Natural aggressive EliteMob spawning") ||
                !ConfigValues.defaultConfig.getBoolean("Allow aggressive EliteMobs") ||
                !ConfigValues.defaultConfig.getBoolean("Valid worlds." + event.getEntity().getWorld().getName().toString())){

            return;

        }

        if (event.getSpawnReason() == NATURAL || event.getSpawnReason() == CUSTOM) {

            Entity entity = event.getEntity();

            if (ValidAgressiveMobFilter.ValidAgressiveMobFilter(entity)) {

                Random random = new Random();

                entity.setMetadata(MetadataHandler.NATURAL_MOB_MD, new FixedMetadataValue(plugin, true));

                //20% chance of turning a mob into a EliteMob unless special gear is equipped
                int huntingGearChanceAdder = 0;

                for (Entity surroundingEntity : entity.getNearbyEntities(range, range, range)) {

                    if (surroundingEntity instanceof Player) {

                        Player player = (Player) surroundingEntity;

                        ItemStack helmet = player.getInventory().getHelmet();
                        ItemStack chestplate = player.getInventory().getChestplate();
                        ItemStack leggings = player.getInventory().getLeggings();
                        ItemStack boots = player.getInventory().getBoots();
                        ItemStack heldItem = player.getInventory().getItemInMainHand();

                        for (ItemStack itemStack : EliteDropsHandler.lootList) {

                            if (helmet != null && itemStack.getItemMeta().equals(helmet.getItemMeta()) &&
                                    helmet.getItemMeta().getDisplayName().equalsIgnoreCase(chatColorConverter("&4elite mob hunting helmet"))) {

                                huntingGearChanceAdder++;

                            } else if (chestplate != null && chestplate.getItemMeta().equals(itemStack.getItemMeta())
                                    && chestplate.getItemMeta().getDisplayName().equalsIgnoreCase(chatColorConverter("&4elite mob hunting chestplate"))) {

                                huntingGearChanceAdder++;


                            } else if (leggings != null && leggings.getItemMeta().equals(itemStack.getItemMeta())
                                    && leggings.getItemMeta().getDisplayName().equalsIgnoreCase(chatColorConverter("&4elite mob hunting leggings"))) {

                                huntingGearChanceAdder++;


                            } else if (boots != null && boots.getItemMeta().equals(itemStack.getItemMeta())
                                    && boots.getItemMeta().getDisplayName().equalsIgnoreCase(chatColorConverter("&4elite mob hunting boots"))) {

                                huntingGearChanceAdder++;

                            } else if (heldItem != null && heldItem.getType() != Material.AIR && heldItem.getItemMeta().equals(itemStack.getItemMeta())
                                    && heldItem.getItemMeta().getDisplayName().equalsIgnoreCase(chatColorConverter("&4elite mob hunting bow"))) {

                                huntingGearChanceAdder++;

                            }

                        }

                    }

                }

                if (random.nextDouble() < ConfigValues.defaultConfig.getDouble("Percentage (%) of aggressive mobs that get converted to EliteMobs when they spawn") / 100
                        + huntingGearChanceAdder * 10) {

                    NaturalMobSpawner naturalMobSpawner = new NaturalMobSpawner(plugin);
                    naturalMobSpawner.naturalMobProcessor(entity);

                }

            }

        }

    }

}
