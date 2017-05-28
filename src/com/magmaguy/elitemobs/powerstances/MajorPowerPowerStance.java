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

package com.magmaguy.elitemobs.powerstances;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.ConfigValues;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by MagmaGuy on 11/05/2017.
 */
public class MajorPowerPowerStance implements Listener {

    Plugin plugin = Bukkit.getPluginManager().getPlugin(MetadataHandler.ELITE_MOBS);
    private int processID;
    private static int trackAmount = 4;
    private static int itemsPerTrack = 4;

    MetadataHandler metadataHandler = new MetadataHandler();

    public void itemEffect (Entity entity) {

        if (ConfigValues.defaultConfig.getBoolean("Turn on visual effects for natural or plugin-spawned EliteMobs")) {

            if (ConfigValues.defaultConfig.getBoolean("Turn off visual effects for non-natural or non-plugin-spawned EliteMobs")
                    && !entity.hasMetadata(MetadataHandler.NATURAL_MOB_MD)) {

                return;

            }

            if (entity.hasMetadata(MetadataHandler.MAJOR_VISUAL_EFFECT_MD)) {

                return;

            }

            entity.setMetadata(MetadataHandler.MAJOR_VISUAL_EFFECT_MD, new FixedMetadataValue(plugin, 0));

            //First integer counts the visual effects it's in, hashmap is from MajorPowerStance's trackHashMap
            HashMap<Integer, HashMap<Integer, List<Item>>> powerItemLocationTracker = new HashMap<>();

            processID = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {

                float counter = 0;

                @Override
                public void run() {

                    int effectQuantity = 0;
                    int effectIteration = 0;

                    for (String string : MetadataHandler.majorPowerList()) {

                        if (entity.hasMetadata(string)){

                            effectQuantity++;

                        }

                    }

                    if (entity.hasMetadata(MetadataHandler.ZOMBIE_FRIENDS_MD)) {


                        ItemStack itemStack = new ItemStack(Material.SKULL_ITEM, 1, (short) 2);

                        itemProcessor(powerItemLocationTracker, itemStack, effectIteration, entity);

                        effectIteration++;

                    }

                    if (entity.hasMetadata(MetadataHandler.ZOMBIE_NECRONOMICON_MD)) {


                        ItemStack itemStack = new ItemStack(Material.WRITTEN_BOOK, 1);

                        itemProcessor(powerItemLocationTracker, itemStack, effectIteration, entity);

                        effectIteration++;

                    }

                    if (entity.hasMetadata(MetadataHandler.ZOMBIE_TEAM_ROCKET_MD)) {

                        ItemStack itemStack = new ItemStack(Material.FIREWORK, 1);

                        itemProcessor(powerItemLocationTracker, itemStack, effectIteration, entity);

                        effectIteration++;

                    }

                    if (entity.hasMetadata(MetadataHandler.ZOMBIE_PARENTS_MD)) {

                        ItemStack itemStack = new ItemStack(Material.MONSTER_EGG, 1, (short) 0 ,(byte) 54);

                        itemProcessor(powerItemLocationTracker, itemStack, effectIteration, entity);

                        effectIteration++;

                    }

                    if (!entity.isValid()) {

                        for (int i = 0; i < powerItemLocationTracker.size(); i++) {

                            for (int j = 0; j < trackAmount; j++) {

                                for (int h = 0; h < itemsPerTrack; h++) {

                                    powerItemLocationTracker.get(i).get(j).get(h).remove();
                                    powerItemLocationTracker.get(i).get(j).get(h).removeMetadata(MetadataHandler.MAJOR_VISUAL_EFFECT_MD, plugin);

                                }

                            }

                        }

                        entity.removeMetadata(MetadataHandler.MAJOR_VISUAL_EFFECT_MD, plugin);

                        Bukkit.getScheduler().cancelTask(processID);

                    }

                    counter++;

                }

            }, 5, 5);

        }

    }

    public void itemProcessor(HashMap<Integer, HashMap<Integer, List<Item>>> powerItemLocationTracker, ItemStack itemStack, int effectIteration, Entity entity) {

        boolean effectAlreadyPresent = false;

        if (!powerItemLocationTracker.isEmpty()) {

            for(int i = 0; i < powerItemLocationTracker.size(); i++) {

                if (powerItemLocationTracker.get(i).get(0).get(0).getItemStack().getType().equals(itemStack.getType())) {

                    effectAlreadyPresent = true;
                    break;

                }

            }

        }

        HashMap<Integer, List<Location>> trackHashMap = MajorPowerStanceMath.majorPowerLocationConstructor(entity, trackAmount, itemsPerTrack);

        if (!effectAlreadyPresent) {

            HashMap<Integer, List<Item>> tempMap = new HashMap<>();

            for (int i = 0; i < trackAmount; i++) {

                List<Item> newItemList = new ArrayList<>();

                for (int j = 0; j < itemsPerTrack; j++) {

                    newItemList.add(itemInitializer(itemStack, entity.getLocation()));

                }

                //same format as trackHashMap
                tempMap.put(i, newItemList);

            }

            powerItemLocationTracker.put(effectIteration, tempMap);

        } else {

            //iterate through the tracks
            for (int i = 0; i < trackAmount; i++) {

                for (int j = 0; j < itemsPerTrack; j++) {

                    Item item = powerItemLocationTracker.get(effectIteration).get(i).get(j);
                    Location newLocation = trackHashMap.get(i).get(j);
                    Location currentLocation = item.getLocation();

                    if (entity.getMetadata(MetadataHandler.MAJOR_VISUAL_EFFECT_MD).get(0).asInt() % (4 * 10) == 0 ||
                            item.getWorld() != entity.getWorld()){

                        entity.setMetadata(MetadataHandler.MAJOR_VISUAL_EFFECT_MD, new FixedMetadataValue(plugin, entity.getMetadata(MetadataHandler.MAJOR_VISUAL_EFFECT_MD).get(0).asInt() - 1 ));
                        Location itemLocation = trackHashMap.get(i).get(j);

                        //teleport is accurate at slower update rates, but when teleporting every tick can get an error margin of up to 2 meters
                        item.teleport(itemLocation);

                        entity.setMetadata(MetadataHandler.MAJOR_VISUAL_EFFECT_MD, new FixedMetadataValue(plugin, entity.getMetadata(MetadataHandler.MAJOR_VISUAL_EFFECT_MD).get(0).asInt() + 1 ));

                    }

                    Vector vector = (newLocation.subtract(currentLocation)).toVector();
                    vector = vector.multiply(0.3);
                    item.setVelocity(vector);

                }

            }

        }

    }

    private Item itemInitializer (ItemStack itemStack, Location location) {

        Item item = location.getWorld().dropItem(location, itemStack);
        item.setPickupDelay(Integer.MAX_VALUE);
        item.setMetadata(MetadataHandler.MAJOR_VISUAL_EFFECT_MD, new FixedMetadataValue(plugin, 0));
        item.setGravity(false);

        return item;

    }

    //Events
    @EventHandler
    public void lastAntiPickupSafeguard(PlayerPickupItemEvent event) {

        if (event.getItem().hasMetadata(MetadataHandler.MAJOR_VISUAL_EFFECT_MD)) {

            event.setCancelled(true);

        }

    }


    @EventHandler
    public void antiHopperPickupSafeguard(InventoryPickupItemEvent event) {

        if (event.getItem().hasMetadata(MetadataHandler.MAJOR_VISUAL_EFFECT_MD)) {

            event.setCancelled(true);

        }

    }


    @EventHandler
    public void metadataKiller(EntityDeathEvent event) {

        Entity entity = event.getEntity();

        for (String string : MetadataHandler.majorPowerList()) {

            if (entity.hasMetadata(string)) {

                entity.removeMetadata(string, plugin);

            }

        }

    }

    @EventHandler
    public void antiItemDespawn (ItemDespawnEvent event) {

        if (event.getEntity().hasMetadata(MetadataHandler.MAJOR_VISUAL_EFFECT_MD)) {

            event.setCancelled(true);

        }

    }

}
