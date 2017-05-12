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
    private static int itemsPerTrack = 10;

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

                    for (String string : metadataHandler.majorPowerList()) {

                        if (entity.hasMetadata(string)){

                            effectQuantity++;

                        }

                    }

                    if (entity.hasMetadata(MetadataHandler.ZOMBIE_TEAM_ROCKET_MD)) {

                        ItemStack itemStack = new ItemStack(Material.SKULL_ITEM, 1);

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

//                    if (entity.getMetadata(MetadataHandler.MAJOR_VISUAL_EFFECT_MD).get(0).asInt() % 4 * 10 == 0 ||
//                            item.getWorld() != entity.getWorld()){
//
//                        entity.setMetadata(MetadataHandler.MAJOR_VISUAL_EFFECT_MD, new FixedMetadataValue(plugin, entity.getMetadata(MetadataHandler.MAJOR_VISUAL_EFFECT_MD).get(0).asInt() - 1 ));
//                        Location itemLocation = trackHashMap.get(i).get(j);
//
//                        item.remove();
//                        item.removeMetadata(MetadataHandler.MAJOR_VISUAL_EFFECT_MD, plugin);
//
//                        item = powerItemLocationTracker.get(effectIteration).get(i).set(j, itemInitializer(itemStack, itemLocation));
//
//                        entity.setMetadata(MetadataHandler.MAJOR_VISUAL_EFFECT_MD, new FixedMetadataValue(plugin, entity.getMetadata(MetadataHandler.MAJOR_VISUAL_EFFECT_MD).get(0).asInt() + 1 ));
//
//                    }

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

//    public void itemProcessor (List<Item> items, int effectQuantity, int effectIteration, ItemStack effectItem1,
//                               ItemStack effectItem2, int amountPerTrack, Entity entity, float counter) {
//
//        boolean effectAlreadyPresent = false;
//
//        for (Item currentItem : items){
//
//            if (currentItem.getItemStack().getType().equals(effectItem1.getType())) {
//
//                effectAlreadyPresent = true;
//                break;
//
//            }
//
//        }
//
//        //divided number is based on the amount of counts it takes for the effect to do a full cycle (counter * 12) may change, may be worth putting on constructor? Finnicky math
//        int adjustedCounter = 30 / amountPerTrack;
//        //assuming 2 items per track, dividing the 30 of a full cycle by 2 to operate start positions over 180 degrees
//        int currentItemOffset = (int) Math.ceil((15 / effectQuantity) * effectIteration);
//
//        if (!effectAlreadyPresent) {
//
//            int repeatedCycles = 0;
//
//            while (repeatedCycles != amountPerTrack){
//
//                Item item1 = entity.getWorld().dropItem(entity.getLocation(), effectItem1);
//                Item item2 = entity.getWorld().dropItem(entity.getLocation(), effectItem2);
//
//                item1.setPickupDelay(Integer.MAX_VALUE);
//                item1.setMetadata(MetadataHandler.MAJOR_VISUAL_EFFECT_MD, new FixedMetadataValue(plugin, 0));
//
//                item2.setPickupDelay(Integer.MAX_VALUE);
//                item2.setMetadata(MetadataHandler.MAJOR_VISUAL_EFFECT_MD, new FixedMetadataValue(plugin, 0));
//
//                items.add(item1);
//                items.add(item2);
//
//                adjustedCounter += adjustedCounter;
//
//                repeatedCycles++;
//
//            }
//
//        } else {
//
//            int repeatedCycles = 0;
//
//            while (repeatedCycles != amountPerTrack) {
//
//                List<Location> particleLocations = majorPowerStanceMath.powerStanceConstructor(entity, radiusHorizontal, radiusVertical, speedHorizontal, speedVertical, counter + adjustedCounter + currentItemOffset);
//                Location newLocation1 = particleLocations.get(0);
//                Location newLocation2 = particleLocations.get(1);
//
//                Location worldMovePrevent1 = items.get(((effectIteration - 1) * 4) + repeatedCycles * amountPerTrack).getLocation();
//                Location worldMovePrevent2 = items.get(((effectIteration - 1) * 4) + 1 + repeatedCycles * amountPerTrack).getLocation();
//
//                //Respawn item to avoid too much client-side visual drifting, avoid errors due to portals and entities going through them
//                if (counter % (4 * 3) == 0 || !worldMovePrevent1.getWorld().equals(newLocation1.getWorld()) || !worldMovePrevent2.getWorld().equals(newLocation2.getWorld())) {
//
//                    List<Location> oldParticleLocations = majorPowerStanceMath.powerStanceConstructor(entity, radiusHorizontal, radiusVertical, speedHorizontal, speedVertical, counter + adjustedCounter - 1 + currentItemOffset);
//                    Location oldLocation1 = oldParticleLocations.get(0);
//                    Location oldLocation2 = oldParticleLocations.get(1);
//
//
//                    Item item1 = items.get(((effectIteration-1)*4)+repeatedCycles*amountPerTrack);
//                    Item item2 = items.get(((effectIteration-1)*4)+1+repeatedCycles*amountPerTrack);
//
//                    int index1 = items.indexOf(item1);
//                    int index2 = items.indexOf(item2);
//
//                    items.remove(item1);
//                    items.remove(item2);
//
////                    Location location1 = item1.getLocation();
//                    item1.remove();
//                    item1.removeMetadata(MetadataHandler.MAJOR_VISUAL_EFFECT_MD, plugin);
//
//                    item1 = entity.getWorld().dropItem(oldLocation1, effectItem1);
//                    items.add(index1, item1);
//                    item1.setPickupDelay(Integer.MAX_VALUE);
//                    item1.setMetadata(MetadataHandler.MAJOR_VISUAL_EFFECT_MD, new FixedMetadataValue(plugin, 0));
//
////                    Location location2 = item2.getLocation();
//                    item2.remove();
//                    item2.removeMetadata(MetadataHandler.MAJOR_VISUAL_EFFECT_MD, plugin);
//
//                    item2 = entity.getWorld().dropItem(oldLocation2, effectItem2);
//                    items.add(index2, item2);
//                    item2.setPickupDelay(Integer.MAX_VALUE);
//                    item2.setMetadata(MetadataHandler.MAJOR_VISUAL_EFFECT_MD, new FixedMetadataValue(plugin, 0));
//
//                }
//
//                //there are 4 items in 1 run. 2 items processed at a time (for two tracks)
//                Item item1 = items.get(((effectIteration-1)*4)+repeatedCycles*amountPerTrack);
//                Item item2 = items.get(((effectIteration-1)*4)+1+repeatedCycles*amountPerTrack);
//
//                item1.setGravity(false);
//                Vector vector1 = (newLocation1.subtract(item1.getLocation()).toVector());
//                vector1 = vector1.multiply(0.3);
//                item1.setVelocity(vector1);
//
//                item2.setGravity(false);
//                Vector vector2 = (newLocation2.subtract(item2.getLocation()).toVector());
//                vector2 = vector2.multiply(0.3);
//                item2.setVelocity(vector2);
//
//                adjustedCounter += adjustedCounter;
//
//                repeatedCycles++;
//
//            }
//
//        }
//
//    }

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

        for (String string : metadataHandler.majorPowerList()) {

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
