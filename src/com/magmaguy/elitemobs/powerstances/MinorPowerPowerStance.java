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

import com.magmaguy.elitemobs.EliteMobs;
import com.magmaguy.elitemobs.MetadataHandler;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
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
import java.util.List;

/**
 * Created by MagmaGuy on 04/11/2016.
 */
public class MinorPowerPowerStance implements Listener {

    private EliteMobs plugin;
    private int processID;


    public MinorPowerPowerStance(Plugin plugin) {

        this.plugin = (EliteMobs) plugin;

    }

    //Secondary effect particle processing
    private void particleEffect(Entity entity, double radiusHorizontal, double radiusVertical, double speedHorizontal, double speedVertical, Particle particle1, Particle particle2, int particleAmount) {

        if (plugin.getConfig().getBoolean("Turn on visual effects for natural or plugin-spawned EliteMobs")) {

            MetadataHandler metadataHandler = new MetadataHandler(plugin);

            if (plugin.getConfig().getBoolean("Turn off visual effects for non-natural or non-plugin-spawned EliteMobs")
                    && !entity.hasMetadata(metadataHandler.naturalMob)) {

                return;

            }

            PowerStanceMath powerStanceMath = new PowerStanceMath(plugin);

            processID = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {

                float counter = 0;

                public void run() {

                    List<Location> particleLocations = powerStanceMath.cylindricalPowerStance(entity, radiusHorizontal, radiusVertical, speedHorizontal, speedVertical, counter);
                    Location location1 = particleLocations.get(0);
                    Location location2 = particleLocations.get(1);

                    entity.getWorld().spawnParticle(particle1, location1, particleAmount, 0.0, 0.0, 0.0, 0.01);
                    entity.getWorld().spawnParticle(particle2, location2, particleAmount, 0.0, 0.0, 0.0, 0.01);


                    if (!entity.isValid()) {

                        Bukkit.getScheduler().cancelTask(processID);

                    }

                    counter++;

                }

            }, 1L, 1L);

        }

    }

    //Secondary effect item processing
    public void itemEffect(Entity entity) {

        MetadataHandler metadataHandler = new MetadataHandler(plugin);

        if (plugin.getConfig().getBoolean("Turn on visual effects for natural or plugin-spawned EliteMobs")) {

            if (plugin.getConfig().getBoolean("Turn off visual effects for non-natural or non-plugin-spawned EliteMobs")
                    && !entity.hasMetadata(metadataHandler.naturalMob)) {

                return;

            }

            if (entity.hasMetadata(metadataHandler.visualEffect)) {

                return;

            }

            PowerStanceMath powerStanceMath = new PowerStanceMath(plugin);

            double radiusHorizontal = 1;
            double radiusVertical = 1;
            double speedHorizontal = 20;
            double speedVertical = 2;

            int amountPerTrack = 2;

            entity.setMetadata("VisualEffect", new FixedMetadataValue(plugin, true));

            //contains all items around a given entity
            List<Item> items = new ArrayList<Item>();

            processID = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {

                float counter = 0;

                public void run() {

                    int effectQuantity = 0;
                    int effectIteration = 0;

                    //count amount of active effects
                    if (entity.hasMetadata(metadataHandler.attackFireMD)) {

                        effectQuantity++;

                    }

                    if (entity.hasMetadata(metadataHandler.attackFreezeMD)) {

                        effectQuantity++;

                    }

                    if (entity.hasMetadata(metadataHandler.attackGravityMD)) {

                        effectQuantity++;

                    }

                    if (entity.hasMetadata(metadataHandler.attackPoisonMD)) {

                        effectQuantity++;


                    }

                    if (entity.hasMetadata(metadataHandler.attackPushMD)) {

                        effectQuantity++;


                    }

                    if (entity.hasMetadata(metadataHandler.attackWebMD)) {

                        effectQuantity++;


                    }

                    if (entity.hasMetadata(metadataHandler.attackWitherMD)) {

                        effectQuantity++;


                    }

                    if (entity.hasMetadata(metadataHandler.bonusLootMD)) {

                        effectQuantity++;


                    }

                    if (entity.hasMetadata(metadataHandler.invisibilityMD)) {

                        effectQuantity++;

                    }

                    if (entity.hasMetadata(metadataHandler.invulnerabilityArrowMD)) {

                        effectQuantity++;


                    }

                    if (entity.hasMetadata(metadataHandler.invulnerabilityFallDamageMD)) {

                        effectQuantity++;


                    }

                    if (entity.hasMetadata(metadataHandler.movementSpeedMD)) {

                        effectQuantity++;

                    }

                    if (effectQuantity == 0) {

                        entity.removeMetadata(metadataHandler.visualEffect, plugin);

                        Bukkit.getScheduler().cancelTask(processID);

                    }

                    //apply new positioning

                    if (entity.hasMetadata(metadataHandler.attackFireMD)) {

                        effectIteration++;

                        ItemStack effectItem1 = new ItemStack(Material.LAVA_BUCKET, 1);
                        ItemStack effectItem2 = new ItemStack(Material.LAVA_BUCKET, 1);

                        itemProcessor(items, effectQuantity, effectIteration, effectItem1, effectItem2, amountPerTrack, entity, radiusHorizontal, radiusVertical, speedHorizontal, speedVertical, counter);

                    }

                    if (entity.hasMetadata(metadataHandler.attackFreezeMD)) {

                        effectIteration++;

                        ItemStack effectItem1 = new ItemStack(Material.PACKED_ICE, 1);
                        ItemStack effectItem2 = new ItemStack(Material.FROSTED_ICE, 1);

                        itemProcessor(items, effectQuantity, effectIteration, effectItem1, effectItem2, amountPerTrack, entity, radiusHorizontal, radiusVertical, speedHorizontal, speedVertical, counter);

                    }

                    if (entity.hasMetadata(metadataHandler.attackGravityMD)) {

                        effectIteration++;

                        ItemStack effectItem1 = new ItemStack(Material.ELYTRA, 1);
                        ItemStack effectItem2 = new ItemStack(Material.ELYTRA, 1);

                        itemProcessor(items, effectQuantity, effectIteration, effectItem1, effectItem2, amountPerTrack, entity, radiusHorizontal, radiusVertical, speedHorizontal, speedVertical, counter);

                    }

                    if (entity.hasMetadata(metadataHandler.attackPoisonMD)) {

                        effectIteration++;

                        ItemStack effectItem1 = new ItemStack(Material.EMERALD, 1);
                        ItemStack effectItem2 = new ItemStack(Material.EMERALD, 1);

                        itemProcessor(items, effectQuantity, effectIteration, effectItem1, effectItem2, amountPerTrack, entity, radiusHorizontal, radiusVertical, speedHorizontal, speedVertical, counter);


                    }

                    if (entity.hasMetadata(metadataHandler.attackPushMD)) {

                        effectIteration++;

                        ItemStack effectItem1 = new ItemStack(Material.PISTON_BASE, 1);
                        ItemStack effectItem2 = new ItemStack(Material.PISTON_BASE, 1);

                        itemProcessor(items, effectQuantity, effectIteration, effectItem1, effectItem2, amountPerTrack, entity, radiusHorizontal, radiusVertical, speedHorizontal, speedVertical, counter);

                    }

                    if (entity.hasMetadata(metadataHandler.attackWebMD)) {

                        effectIteration++;

                        ItemStack effectItem1 = new ItemStack(Material.WEB, 1);
                        ItemStack effectItem2 = new ItemStack(Material.WEB, 1);

                        itemProcessor(items, effectQuantity, effectIteration, effectItem1, effectItem2, amountPerTrack, entity, radiusHorizontal, radiusVertical, speedHorizontal, speedVertical, counter);

                    }

                    if (entity.hasMetadata(metadataHandler.attackWitherMD)) {

                        effectIteration++;

                        ItemStack effectItem1 = new ItemStack(Material.SKULL_ITEM, 1, (short) 1);
                        ItemStack effectItem2 = new ItemStack(Material.SKULL_ITEM, 1, (short) 1);

                        itemProcessor(items, effectQuantity, effectIteration, effectItem1, effectItem2, amountPerTrack, entity, radiusHorizontal, radiusVertical, speedHorizontal, speedVertical, counter);


                    }

                    if (entity.hasMetadata(metadataHandler.bonusLootMD)) {

                        effectIteration++;

                        ItemStack effectItem1 = new ItemStack(Material.CHEST, 1);
                        ItemStack effectItem2 = new ItemStack(Material.CHEST, 1);

                        itemProcessor(items, effectQuantity, effectIteration, effectItem1, effectItem2, amountPerTrack, entity, radiusHorizontal, radiusVertical, speedHorizontal, speedVertical, counter);


                    }

                    if (entity.hasMetadata(metadataHandler.invisibilityMD)) {

                        effectIteration++;

                        ItemStack effectItem1 = new ItemStack(Material.THIN_GLASS, 1);
                        ItemStack effectItem2 = new ItemStack(Material.THIN_GLASS, 1);

                        itemProcessor(items, effectQuantity, effectIteration, effectItem1, effectItem2, amountPerTrack, entity, radiusHorizontal, radiusVertical, speedHorizontal, speedVertical, counter);


                    }

                    if (entity.hasMetadata(metadataHandler.invulnerabilityArrowMD)) {

                        effectIteration++;

                        ItemStack effectItem1 = new ItemStack(Material.SPECTRAL_ARROW, 1);
                        ItemStack effectItem2 = new ItemStack(Material.TIPPED_ARROW, 1);

                        itemProcessor(items, effectQuantity, effectIteration, effectItem1, effectItem2, amountPerTrack, entity, radiusHorizontal, radiusVertical, speedHorizontal, speedVertical, counter);


                    }

                    if (entity.hasMetadata(metadataHandler.invulnerabilityFallDamageMD)) {

                        effectIteration++;

                        ItemStack effectItem1 = new ItemStack(Material.FEATHER, 1);
                        ItemStack effectItem2 = new ItemStack(Material.FEATHER, 1);

                        itemProcessor(items, effectQuantity, effectIteration, effectItem1, effectItem2, amountPerTrack, entity, radiusHorizontal, radiusVertical, speedHorizontal, speedVertical, counter);


                    }

                    if (entity.hasMetadata(metadataHandler.movementSpeedMD)) {

                        effectIteration++;

                        ItemStack effectItem1 = new ItemStack(Material.GOLD_BOOTS, 1);
                        ItemStack effectItem2 = new ItemStack(Material.GOLD_BOOTS, 1);

                        itemProcessor(items, effectQuantity, effectIteration, effectItem1, effectItem2, amountPerTrack, entity, radiusHorizontal, radiusVertical, speedHorizontal, speedVertical, counter);


                    }

                    if (!entity.isValid()) {

                        for (Item currentItem : items) {

                            currentItem.remove();
                            currentItem.removeMetadata(metadataHandler.visualEffect, plugin);

                        }

                        entity.removeMetadata(metadataHandler.visualEffect, plugin);

                        Bukkit.getScheduler().cancelTask(processID);

                    }

                    counter++;

                }

                //can only run every 5 ticks or it causes effect-breaking visual glitches client-side
            }, 5, 5);

        }

    }

    public void itemProcessor (List<Item> items, int effectQuantity, int effectIteration, ItemStack effectItem1,
                               ItemStack effectItem2, int amountPerTrack, Entity entity, double radiusHorizontal,
                               double radiusVertical, double speedHorizontal, double speedVertical, float counter) {

        boolean effectAlreadyPresent = false;

        for (Item currentItem : items){

            if (currentItem.getItemStack().getType().equals(effectItem1.getType())) {

                effectAlreadyPresent = true;
                break;

            }

        }

        PowerStanceMath powerStanceMath = new PowerStanceMath(plugin);

        //divided number is based on the amount of counts it takes for the effect to do a full cycle (counter * 12) may change, may be worth putting on constructor? Finnicky math
        int adjustedCounter = 30 / amountPerTrack;
        //assuming 2 items per track, dividing the 30 of a full cycle by 2 to operate start positions over 180 degrees
        int currentItemOffset = (int) Math.ceil((15 / effectQuantity) * effectIteration);

        MetadataHandler metadataHandler = new MetadataHandler(plugin);

        if (!effectAlreadyPresent) {

            int repeatedCycles = 0;

            while (repeatedCycles != amountPerTrack){

                Item item1 = entity.getWorld().dropItem(entity.getLocation(), effectItem1);
                Item item2 = entity.getWorld().dropItem(entity.getLocation(), effectItem2);

                item1.setPickupDelay(Integer.MAX_VALUE);
                item1.setMetadata(metadataHandler.visualEffect, new FixedMetadataValue(plugin, true));

                item2.setPickupDelay(Integer.MAX_VALUE);
                item2.setMetadata(metadataHandler.visualEffect, new FixedMetadataValue(plugin, true));

                items.add(item1);
                items.add(item2);

                adjustedCounter += adjustedCounter;

                repeatedCycles++;

            }

        } else {

            int repeatedCycles = 0;

            while (repeatedCycles != amountPerTrack) {

                List<Location> particleLocations = powerStanceMath.cylindricalPowerStance(entity, radiusHorizontal, radiusVertical, speedHorizontal, speedVertical, counter + adjustedCounter + currentItemOffset);
                Location newLocation1 = particleLocations.get(0);
                Location newLocation2 = particleLocations.get(1);

                Location worldMovePrevent1 = items.get(((effectIteration - 1) * 4) + repeatedCycles * amountPerTrack).getLocation();
                Location worldMovePrevent2 = items.get(((effectIteration - 1) * 4) + 1 + repeatedCycles * amountPerTrack).getLocation();

                //Respawn item to avoid too much client-side visual drifting, avoid errors due to portals and entities going through them
                if (counter % (4 * 3) == 0 || !worldMovePrevent1.getWorld().equals(newLocation1.getWorld()) || !worldMovePrevent2.getWorld().equals(newLocation2.getWorld())) {

                    List<Location> oldParticleLocations = powerStanceMath.cylindricalPowerStance(entity, radiusHorizontal, radiusVertical, speedHorizontal, speedVertical, counter + adjustedCounter - 1 + currentItemOffset);
                    Location oldLocation1 = oldParticleLocations.get(0);
                    Location oldLocation2 = oldParticleLocations.get(1);


                    Item item1 = items.get(((effectIteration-1)*4)+repeatedCycles*amountPerTrack);
                    Item item2 = items.get(((effectIteration-1)*4)+1+repeatedCycles*amountPerTrack);

                    int index1 = items.indexOf(item1);
                    int index2 = items.indexOf(item2);

                    items.remove(item1);
                    items.remove(item2);

//                    Location location1 = item1.getLocation();
                    item1.remove();
                    item1.removeMetadata(metadataHandler.visualEffect, plugin);

                    item1 = entity.getWorld().dropItem(oldLocation1, effectItem1);
                    items.add(index1, item1);
                    item1.setPickupDelay(Integer.MAX_VALUE);
                    item1.setMetadata(metadataHandler.visualEffect, new FixedMetadataValue(plugin, true));

//                    Location location2 = item2.getLocation();
                    item2.remove();
                    item2.removeMetadata(metadataHandler.visualEffect, plugin);

                    item2 = entity.getWorld().dropItem(oldLocation2, effectItem2);
                    items.add(index2, item2);
                    item2.setPickupDelay(Integer.MAX_VALUE);
                    item2.setMetadata(metadataHandler.visualEffect, new FixedMetadataValue(plugin, true));

                }

                //there are 4 items in 1 run. 2 items processed at a time (for two tracks)
                Item item1 = items.get(((effectIteration-1)*4)+repeatedCycles*amountPerTrack);
                Item item2 = items.get(((effectIteration-1)*4)+1+repeatedCycles*amountPerTrack);

                item1.setGravity(false);
                Vector vector1 = (newLocation1.subtract(item1.getLocation()).toVector());
                vector1 = vector1.multiply(0.3);
                item1.setVelocity(vector1);

                item2.setGravity(false);
                Vector vector2 = (newLocation2.subtract(item2.getLocation()).toVector());
                vector2 = vector2.multiply(0.3);
                item2.setVelocity(vector2);

                adjustedCounter += adjustedCounter;

                repeatedCycles++;

            }

        }

    }

    //TODO: particle effects to a new format
    public void invulnerabilityFireEffect(Entity entity) {

        MetadataHandler metadataHandler = new MetadataHandler(plugin);

        if (entity.hasMetadata(metadataHandler.invulnerabilityFireMD)) {

            particleEffect(entity, 1.0, 1.0, 20, 2, Particle.FLAME, Particle.FLAME, 5);

        }

    }


    //Events
    @EventHandler
    public void lastAntiPickupSafeguard(PlayerPickupItemEvent event) {

        MetadataHandler metadataHandler = new MetadataHandler(plugin);

        if (event.getItem().hasMetadata(metadataHandler.visualEffect)) {

            event.setCancelled(true);

        }

    }


    @EventHandler
    public void antiHopperPickupSafeguard(InventoryPickupItemEvent event) {

        MetadataHandler metadataHandler = new MetadataHandler(plugin);

        if (event.getItem().hasMetadata(metadataHandler.visualEffect)) {

            event.setCancelled(true);

        }

    }


    @EventHandler
    public void metadataKiller(EntityDeathEvent event) {

        Entity entity = event.getEntity();

        MetadataHandler metadataHandler = new MetadataHandler(plugin);

        List<String> metadataList = new ArrayList<String>();
        metadataList.add(metadataHandler.attackGravityMD);
        metadataList.add(metadataHandler.attackPoisonMD);
        metadataList.add(metadataHandler.attackPushMD);
        metadataList.add(metadataHandler.attackWitherMD);
        metadataList.add(metadataHandler.invulnerabilityArrowMD);
        metadataList.add(metadataHandler.invulnerabilityFallDamageMD);
        metadataList.add(metadataHandler.movementSpeedMD);

        for (String string : metadataList) {

            if (entity.hasMetadata(string)) {

                entity.removeMetadata(string, plugin);

            }

        }

    }

    @EventHandler
    public void antiItemDespawn (ItemDespawnEvent event) {

        MetadataHandler metadataHandler = new MetadataHandler(plugin);

        if (event.getEntity().hasMetadata(metadataHandler.visualEffect)) {

            event.setCancelled(true);

        }

    }

}
