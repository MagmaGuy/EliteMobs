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
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by MagmaGuy on 11/05/2017.
 */
public class MajorPowerPowerStance implements Listener {

    private Plugin plugin = Bukkit.getPluginManager().getPlugin(MetadataHandler.ELITE_MOBS);
    private static int trackAmount = 4;
    private static int itemsPerTrack = 2;

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

            new BukkitRunnable(){

                    int counter = 0;
                    int zombieFriendsCounter = 0;
                    int zombieNecronomiconCounter = 0;
                    int zombieTeamRocketCounter = 0;
                    int zombieParentsCounter = 0;

                    @Override
                    public void run() {

//                    int effectQuantity = 0;
                        int effectIteration = 0;

                        if (counter >= 10) {

                            counter = 0;

                        }

//                    for (String string : MetadataHandler.majorPowerList()) {
//
//                        if (entity.hasMetadata(string)){
//
//                            effectQuantity++;
//
//                        }
//
//                    }

                        if (entity.hasMetadata(MetadataHandler.ZOMBIE_FRIENDS_MD)) {

                            ItemStack itemStack = new ItemStack(Material.SKULL_ITEM, 1, (short) 2);

                            itemProcessor(powerItemLocationTracker, itemStack, effectIteration, entity, zombieFriendsCounter);

                            effectIteration++;
                            zombieFriendsCounter++;
                            if (zombieFriendsCounter >= MajorPowerStanceMath.NUMBER_OF_POINTS_PER_FULL_ROTATION) {

                                zombieFriendsCounter = 0;

                            }

                        }

                        if (entity.hasMetadata(MetadataHandler.ZOMBIE_NECRONOMICON_MD)) {


                            ItemStack itemStack = new ItemStack(Material.WRITTEN_BOOK, 1);

                            itemProcessor(powerItemLocationTracker, itemStack, effectIteration, entity, zombieNecronomiconCounter);

                            effectIteration++;
                            zombieNecronomiconCounter++;
                            if (zombieNecronomiconCounter >= MajorPowerStanceMath.NUMBER_OF_POINTS_PER_FULL_ROTATION) {

                                zombieNecronomiconCounter = 0;

                            }

                        }

                        if (entity.hasMetadata(MetadataHandler.ZOMBIE_TEAM_ROCKET_MD)) {

                            ItemStack itemStack = new ItemStack(Material.FIREWORK, 1);

                            itemProcessor(powerItemLocationTracker, itemStack, effectIteration, entity, zombieTeamRocketCounter);

                            effectIteration++;
                            zombieTeamRocketCounter++;
                            if (zombieTeamRocketCounter >= MajorPowerStanceMath.NUMBER_OF_POINTS_PER_FULL_ROTATION) {

                                zombieTeamRocketCounter = 0;

                            }

                        }

                        if (entity.hasMetadata(MetadataHandler.ZOMBIE_PARENTS_MD)) {

                            ItemStack itemStack = new ItemStack(Material.MONSTER_EGG, 1, (short) 0 ,(byte) 54);

                            itemProcessor(powerItemLocationTracker, itemStack, effectIteration, entity, zombieParentsCounter);

                            effectIteration++;
                            zombieParentsCounter++;
                            if (zombieParentsCounter >= MajorPowerStanceMath.NUMBER_OF_POINTS_PER_FULL_ROTATION) {

                                zombieParentsCounter = 0;

                            }

                        }

                        if (!entity.isValid() || entity.isDead()) {

//                            for (int i = 0; i < powerItemLocationTracker.size(); i++) {
//
//                                for (int j = 0; j < trackAmount; j++) {
//
//                                    for (int h = 0; h < itemsPerTrack; h++) {
//
//                                        powerItemLocationTracker.get(i).get(j).get(h).remove();
//                                        powerItemLocationTracker.get(i).get(j).get(h).removeMetadata(MetadataHandler.MAJOR_VISUAL_EFFECT_MD, plugin);
//
//                                    }
//
//                                }
//
//                            }

                            for (int i = 0; i < powerItemLocationTracker.size(); i++) {

                                for (int j = 0; j < trackAmount; j++) {

                                    for (int h = 0; h < itemsPerTrack; h++) {

                                        powerItemLocationTracker.get(i).get(j).get(h).remove();
                                        powerItemLocationTracker.get(i).get(j).get(h).removeMetadata(MetadataHandler.MAJOR_VISUAL_EFFECT_MD, plugin);
                                        powerItemLocationTracker.get(i).get(j).get(h).removeMetadata(MetadataHandler.BETTERDROPS_COMPATIBILITY_MD, plugin);

                                    }

                                }

                            }

                            entity.removeMetadata(MetadataHandler.MAJOR_VISUAL_EFFECT_MD, plugin);

                            cancel();
                            return;

                        }

                        counter++;

                    }



            }.runTaskTimer(plugin, 0, 5);

        }

    }

    public void itemProcessor(HashMap<Integer, HashMap<Integer, List<Item>>> powerItemLocationTracker, ItemStack itemStack, int effectIteration, Entity entity, int counter) {

        boolean effectAlreadyPresent = false;

        if (!powerItemLocationTracker.isEmpty()) {

            for(int i = 0; i < powerItemLocationTracker.size(); i++) {

                if (powerItemLocationTracker.get(i).get(0).get(0).getItemStack().getType().equals(itemStack.getType())) {

                    effectAlreadyPresent = true;
                    break;

                }

            }

        }

        HashMap<Integer, List<Vector>> trackHashMap = MajorPowerStanceMath.majorPowerLocationConstructor(trackAmount, itemsPerTrack, counter);
        Location centerLocation = entity.getLocation().add(new Vector(0, 1, 0));

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
                    Location newLocation = new Location(entity.getWorld(), trackHashMap.get(i).get(j).getX(),
                            trackHashMap.get(i).get(j).getY(), trackHashMap.get(i).get(j).getZ()).add(centerLocation);
                    Location currentLocation = item.getLocation();

                    if (counter % (29) == 0 ||
                            item.getWorld() != entity.getWorld()){

                        item.teleport(item.getLocation());

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
        item.setMetadata(MetadataHandler.BETTERDROPS_COMPATIBILITY_MD, new FixedMetadataValue(plugin, true));
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


    //TODO: this is a lazy solution
    @EventHandler
    public void portalPrevention (EntityPortalEvent event) {

        if (event.getEntity().hasMetadata(MetadataHandler.MAJOR_VISUAL_EFFECT_MD) ||
                event.getEntity().hasMetadata(MetadataHandler.VISUAL_EFFECT_MD) ||
                event.getEntity().hasMetadata(MetadataHandler.ELITE_MOB_MD)) {

            event.setCancelled(true);

        }

    }

}
