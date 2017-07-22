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
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by MagmaGuy on 04/11/2016.
 */
public class MinorPowerPowerStance implements Listener {

    private Plugin plugin = Bukkit.getPluginManager().getPlugin(MetadataHandler.ELITE_MOBS);

    private static int trackAmount = 1;
    private static int itemsPerTrack = 2;

    //Secondary effect particle processing
//    private void particleEffect(Entity entity, Particle particle, int particleAmount, double v, double v1, double v2, double v3) {

//        if (ConfigValues.defaultConfig.getBoolean("Turn on visual effects for natural or plugin-spawned EliteMobs")) {
//
//            if (ConfigValues.defaultConfig.getBoolean("Turn off visual effects for non-natural or non-plugin-spawned EliteMobs")
//                    && !entity.hasMetadata(MetadataHandler.NATURAL_MOB_MD)) {
//
//                return;
//
//            }
//
//            processID = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
//
//                float counter = 0;
//
//                public void run() {
//
//                    List<Location> particleLocations = powerStanceMath.cylindricalPowerStance(entity, radiusHorizontal, radiusVertical, speedHorizontal, speedVertical, counter);
//
//                    Location location1 = particleLocations.get(0);
//                    Location location2 = particleLocations.get(1);
//
//                    entity.getWorld().spawnParticle(particle, location1, particleAmount, v, v1, v2, v3);
//                    entity.getWorld().spawnParticle(particle, location2, particleAmount, v, v1, v2, v3);
//
//                    if (!entity.isValid()) {
//
//                        Bukkit.getScheduler().cancelTask(processID);
//
//                    }
//
//                    counter++;
//
//                }
//
//            }, 1L, 1L);
//
//        }
//
//    }

//    public void attackConfusing(Entity entity) {
//
//        particleEffect(entity, Particle.SPELL_MOB, 5, 0, 0, 0, 0.01);
//
//    }
//
//    public void invulnerabilityFireEffect(Entity entity) {
//
//        particleEffect(entity, Particle.FLAME, 5, 0, 0, 0, 0.01);
//
//    }

    //Secondary effect item processing
    public void itemEffect(Entity entity) {

        if (ConfigValues.defaultConfig.getBoolean("Turn on visual effects for natural or plugin-spawned EliteMobs")) {

            if (ConfigValues.defaultConfig.getBoolean("Turn off visual effects for non-natural or non-plugin-spawned EliteMobs")
                    && !entity.hasMetadata(MetadataHandler.NATURAL_MOB_MD)) {

                return;

            }

            if (entity.hasMetadata(MetadataHandler.VISUAL_EFFECT_MD)) {

                return;

            }

            entity.setMetadata("VisualEffect", new FixedMetadataValue(plugin, true));

            //contains all items around a given entity
            HashMap<Integer, HashMap<Integer, List<Item>>> powerItemLocationTracker = new HashMap<>();

            new BukkitRunnable(){

                int globalPositionCounter = 0;
                int effectQuantity = 0;

                public void run() {

                    int itemStackHashMapPosition = 0;
                    int individualPositionCounter = 0;
                    int effectQuantityChecksum = 0;

                    //count amount of active effects
                    for (String string : MetadataHandler.minorPowerList()) {

                        if (entity.hasMetadata(string)) {

                            effectQuantityChecksum++;

                        }

                    }

                    if (effectQuantity == 0) {

                        effectQuantity = effectQuantityChecksum;

                    } else if (effectQuantity != effectQuantityChecksum) {

                        for (int i = 0; i < powerItemLocationTracker.size(); i++) {

                            for (int j = 0; j < trackAmount; j++) {

                                for (int h = 0; h < itemsPerTrack; h++) {

                                    powerItemLocationTracker.get(i).get(j).get(h).remove();
                                    powerItemLocationTracker.get(i).get(j).get(h).removeMetadata(MetadataHandler.MAJOR_VISUAL_EFFECT_MD, plugin);
                                    powerItemLocationTracker.get(i).get(j).get(h).removeMetadata(MetadataHandler.BETTERDROPS_COMPATIBILITY_MD, plugin);

                                }

                            }

                        }

                        powerItemLocationTracker.clear();

                        effectQuantity = effectQuantityChecksum;

                    }

                    if (globalPositionCounter >= MinorPowerStanceMath.NUMBER_OF_POINTS_PER_FULL_ROTATION) {

                        globalPositionCounter = 1;

                    }

                    //apply new positioning
                    if (entity.hasMetadata(MetadataHandler.ATTACK_ARROW_MD)) {

                        ItemStack itemStack = new ItemStack(Material.ARROW, 1);

                        itemProcessor(powerItemLocationTracker, itemStack, itemStackHashMapPosition, entity, adjustTrackPosition(effectQuantity, globalPositionCounter, individualPositionCounter));

                        individualPositionCounter++;
                        itemStackHashMapPosition++;

                    }

                    if (entity.hasMetadata(MetadataHandler.ATTACK_BLINDING_MD)) {

                        ItemStack itemStack = new ItemStack(Material.EYE_OF_ENDER, 1);

                        itemProcessor(powerItemLocationTracker, itemStack, itemStackHashMapPosition, entity, adjustTrackPosition(effectQuantity, globalPositionCounter, individualPositionCounter));

                        individualPositionCounter++;
                        itemStackHashMapPosition++;

                    }

                    if (entity.hasMetadata(MetadataHandler.ATTACK_FIRE_MD)) {

                        ItemStack itemStack = new ItemStack(Material.LAVA_BUCKET, 1);

                        itemProcessor(powerItemLocationTracker, itemStack, itemStackHashMapPosition, entity, adjustTrackPosition(effectQuantity, globalPositionCounter, individualPositionCounter));

                        individualPositionCounter++;
                        itemStackHashMapPosition++;

                    }

                    if (entity.hasMetadata(MetadataHandler.ATTACK_FIREBALL_MD)) {

                        ItemStack itemStack = new ItemStack(Material.FIREBALL, 1);

                        itemProcessor(powerItemLocationTracker, itemStack, itemStackHashMapPosition, entity, adjustTrackPosition(effectQuantity, globalPositionCounter, individualPositionCounter));

                        individualPositionCounter++;
                        itemStackHashMapPosition++;

                    }

                    if (entity.hasMetadata(MetadataHandler.ATTACK_FREEZE_MD)) {

                        ItemStack itemStack = new ItemStack(Material.PACKED_ICE, 1);

                        itemProcessor(powerItemLocationTracker, itemStack, itemStackHashMapPosition, entity, adjustTrackPosition(effectQuantity, globalPositionCounter, individualPositionCounter));

                        individualPositionCounter++;
                        itemStackHashMapPosition++;

                    }

                    if (entity.hasMetadata(MetadataHandler.ATTACK_GRAVITY_MD)) {

                        ItemStack itemStack = new ItemStack(Material.ELYTRA, 1);

                        itemProcessor(powerItemLocationTracker, itemStack, itemStackHashMapPosition, entity, adjustTrackPosition(effectQuantity, globalPositionCounter, individualPositionCounter));

                        individualPositionCounter++;
                        itemStackHashMapPosition++;

                    }

                    if (entity.hasMetadata(MetadataHandler.ATTACK_POISON_MD)) {

                        ItemStack itemStack = new ItemStack(Material.EMERALD, 1);

                        itemProcessor(powerItemLocationTracker, itemStack, itemStackHashMapPosition, entity, adjustTrackPosition(effectQuantity, globalPositionCounter, individualPositionCounter));

                        individualPositionCounter++;
                        itemStackHashMapPosition++;

                    }

                    if (entity.hasMetadata(MetadataHandler.ATTACK_PUSH_MD)) {

                        ItemStack itemStack = new ItemStack(Material.PISTON_BASE, 1);

                        itemProcessor(powerItemLocationTracker, itemStack, itemStackHashMapPosition, entity, adjustTrackPosition(effectQuantity, globalPositionCounter, individualPositionCounter));

                        individualPositionCounter++;
                        itemStackHashMapPosition++;

                    }

                    if (entity.hasMetadata(MetadataHandler.ATTACK_WEAKNESS_MD)) {

                        ItemStack itemStack = new ItemStack(Material.TOTEM, 1);

                        itemProcessor(powerItemLocationTracker, itemStack, itemStackHashMapPosition, entity, adjustTrackPosition(effectQuantity, globalPositionCounter, individualPositionCounter));

                        individualPositionCounter++;
                        itemStackHashMapPosition++;

                    }

                    if (entity.hasMetadata(MetadataHandler.ATTACK_WEB_MD)) {

                        ItemStack itemStack = new ItemStack(Material.WEB, 1);

                        itemProcessor(powerItemLocationTracker, itemStack, itemStackHashMapPosition, entity, adjustTrackPosition(effectQuantity, globalPositionCounter, individualPositionCounter));

                        individualPositionCounter++;
                        itemStackHashMapPosition++;

                    }

                    if (entity.hasMetadata(MetadataHandler.ATTACK_WITHER_MD)) {

                        ItemStack itemStack = new ItemStack(Material.SKULL_ITEM, 1, (short) 1);

                        itemProcessor(powerItemLocationTracker, itemStack, itemStackHashMapPosition, entity, adjustTrackPosition(effectQuantity, globalPositionCounter, individualPositionCounter));

                        individualPositionCounter++;
                        itemStackHashMapPosition++;

                    }

                    if (entity.hasMetadata(MetadataHandler.BONUS_LOOT_MD)) {

                        ItemStack itemStack = new ItemStack(Material.CHEST, 1);

                        itemProcessor(powerItemLocationTracker, itemStack, itemStackHashMapPosition, entity, adjustTrackPosition(effectQuantity, globalPositionCounter, individualPositionCounter));

                        individualPositionCounter++;
                        itemStackHashMapPosition++;

                    }

                    if (entity.hasMetadata(MetadataHandler.DOUBLE_DAMAGE_MD)) {

                        ItemStack itemStack = new ItemStack(Material.GOLD_SWORD, 1);

                        itemProcessor(powerItemLocationTracker, itemStack, itemStackHashMapPosition, entity, adjustTrackPosition(effectQuantity, globalPositionCounter, individualPositionCounter));

                        individualPositionCounter++;
                        itemStackHashMapPosition++;

                    }

                    if (entity.hasMetadata(MetadataHandler.DOUBLE_HEALTH_MD)) {

                        ItemStack itemStack = new ItemStack(Material.SHIELD, 1);

                        itemProcessor(powerItemLocationTracker, itemStack, itemStackHashMapPosition, entity, adjustTrackPosition(effectQuantity, globalPositionCounter, individualPositionCounter));

                        individualPositionCounter++;
                        itemStackHashMapPosition++;

                    }

                    if (entity.hasMetadata(MetadataHandler.INVISIBILITY_MD)) {

                        ItemStack itemStack = new ItemStack(Material.THIN_GLASS, 1);

                        itemProcessor(powerItemLocationTracker, itemStack, itemStackHashMapPosition, entity, adjustTrackPosition(effectQuantity, globalPositionCounter, individualPositionCounter));

                        individualPositionCounter++;
                        itemStackHashMapPosition++;

                    }

                    if (entity.hasMetadata(MetadataHandler.INVULNERABILITY_ARROW_MD)) {

                        ItemStack itemStack = new ItemStack(Material.SPECTRAL_ARROW, 1);

                        itemProcessor(powerItemLocationTracker, itemStack, itemStackHashMapPosition, entity, adjustTrackPosition(effectQuantity, globalPositionCounter, individualPositionCounter));

                        individualPositionCounter++;
                        itemStackHashMapPosition++;

                    }

                    if (entity.hasMetadata(MetadataHandler.INVULNERABILITY_FALL_DAMAGE_MD)) {

                        ItemStack itemStack = new ItemStack(Material.FEATHER, 1);

                        itemProcessor(powerItemLocationTracker, itemStack, itemStackHashMapPosition, entity, adjustTrackPosition(effectQuantity, globalPositionCounter, individualPositionCounter));

                        individualPositionCounter++;
                        itemStackHashMapPosition++;

                    }

                    if (entity.hasMetadata(MetadataHandler.INVULNERABILITY_KNOCKBACK_MD)) {

                        ItemStack itemStack = new ItemStack(Material.ANVIL, 1);

                        itemProcessor(powerItemLocationTracker, itemStack, itemStackHashMapPosition, entity, adjustTrackPosition(effectQuantity, globalPositionCounter, individualPositionCounter));

                        individualPositionCounter++;
                        itemStackHashMapPosition++;

                    }

                    if (entity.hasMetadata(MetadataHandler.MOVEMENT_SPEED_MD)) {

                        ItemStack itemStack = new ItemStack(Material.GOLD_BOOTS, 1);

                        itemProcessor(powerItemLocationTracker, itemStack, itemStackHashMapPosition, entity, adjustTrackPosition(effectQuantity, globalPositionCounter, individualPositionCounter));

                        individualPositionCounter++;
                        itemStackHashMapPosition++;
                    }

                    if (entity.hasMetadata(MetadataHandler.TAUNT_MD)) {

                        ItemStack itemStack = new ItemStack(Material.JUKEBOX, 1);

                        itemProcessor(powerItemLocationTracker, itemStack, itemStackHashMapPosition, entity, adjustTrackPosition(effectQuantity, globalPositionCounter, individualPositionCounter));

                        individualPositionCounter++;
                        itemStackHashMapPosition++;

                    }

                    globalPositionCounter++;

                    if (!entity.isValid() || entity.isDead()) {

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

                }

                //can only run every 5 ticks or it causes effect-breaking visual glitches client-side

            }.runTaskTimer(plugin, 0, 5);

        }

    }


    private int adjustTrackPosition(int effectQuantity, int globalPositionCounter, int individualPositionCounter) {

        return (int) ((MinorPowerStanceMath.NUMBER_OF_POINTS_PER_FULL_ROTATION / itemsPerTrack) / effectQuantity * individualPositionCounter + globalPositionCounter);

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

        HashMap<Integer, List<Vector>> trackHashMap = MinorPowerStanceMath.majorPowerLocationConstructor(trackAmount, itemsPerTrack, counter);
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

                    if (item.getWorld() != entity.getWorld()) {

                        item.teleport(item.getLocation());

                    }

//                    newLocation.add(new Vector(0, Math.cos(counter), 0));
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
        item.setMetadata(MetadataHandler.VISUAL_EFFECT_MD, new FixedMetadataValue(plugin, 0));
        item.setMetadata(MetadataHandler.BETTERDROPS_COMPATIBILITY_MD, new FixedMetadataValue(plugin, true));
        item.setGravity(false);

        return item;

    }

}
