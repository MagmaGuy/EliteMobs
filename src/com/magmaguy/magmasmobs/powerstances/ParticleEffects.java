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

package com.magmaguy.magmasmobs.powerstances;

import com.magmaguy.magmasmobs.MagmasMobs;
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

import static org.bukkit.Bukkit.getLogger;

/**
 * Created by MagmaGuy on 04/11/2016.
 */
public class ParticleEffects implements Listener {

    private MagmasMobs plugin;

    public ParticleEffects(Plugin plugin) {

        this.plugin = (MagmasMobs) plugin;

    }


    private int processID;

    //Particle processing
    private void particleEffect(Entity entity, double radiusHorizontal, double radiusVertical, double speedHorizontal, double speedVertical, Particle particle1, Particle particle2, int particleAmount) {

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


    //Generic item processing
    private void itemEffect(Entity entity, double radiusHorizontal, double radiusVertical, double speedHorizontal, double speedVertical, ItemStack itemStack1, ItemStack itemStack2) {

        PowerStanceMath powerStanceMath = new PowerStanceMath(plugin);

        processID = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {

            float counter = 0;

            Item floatable1;
            Item floatable2;

            public void run() {

                if (floatable1 == null && floatable2 == null) {

                    floatable1 = entity.getWorld().dropItem(entity.getLocation(), itemStack1);
                    floatable2 = entity.getWorld().dropItem(entity.getLocation(), itemStack2);

                    List<Location> particleLocations = powerStanceMath.cylindricalPowerStance(entity, radiusHorizontal, radiusVertical, speedHorizontal, speedVertical, counter);
                    Location newLocation1 = particleLocations.get(0);
                    Location newLocation2 = particleLocations.get(1);

//                    floatable1.teleport(newLocation1);
                    floatable1.setPickupDelay(Integer.MAX_VALUE);
                    floatable1.setMetadata("VisualEffect", new FixedMetadataValue(plugin, true));


//                    floatable2.teleport(newLocation2);
                    floatable2.setPickupDelay(Integer.MAX_VALUE);
                    floatable2.setMetadata("VisualEffect", new FixedMetadataValue(plugin, true));

                } else {

                    List<Location> particleLocations = powerStanceMath.cylindricalPowerStance(entity, radiusHorizontal, radiusVertical, speedHorizontal, speedVertical, counter);
                    Location newLocation1 = particleLocations.get(0);
                    Location newLocation2 = particleLocations.get(1);

                    List<Location> oldParticleLocations = powerStanceMath.cylindricalPowerStance(entity, radiusHorizontal, radiusVertical, speedHorizontal, speedVertical, counter - 1);
                    Location oldLocation1 = oldParticleLocations.get(0);
                    Location oldLocation2 = oldParticleLocations.get(1);


//                    if (counter % (4 * 10) == 0) {
//
//                        floatable1.teleport(oldLocation1.add(new Vector(-1,1,-0.5)));
//                        floatable2.teleport(oldLocation2.add(new Vector(-1,1,-0.5)));
//
//                    }

                    floatable1.setGravity(false);
//                    Vector vector1 = (newLocation1.add(new Vector(-1,1,-0.5)).subtract(floatable1.getLocation()).toVector());
                    Vector vector1 = (newLocation1.subtract(floatable1.getLocation()).toVector());
                    vector1 = vector1.multiply(0.3);
                    floatable1.setVelocity(vector1);

                    floatable2.setGravity(false);
//                    Vector vector2 = (newLocation2.add(new Vector(-1,1,-0.5)).subtract(floatable2.getLocation()).toVector());
                    Vector vector2 = (newLocation2.subtract(floatable2.getLocation()).toVector());
                    vector2 = vector2.multiply(0.3);
                    floatable2.setVelocity(vector2);

                }

                if (!entity.isValid()) {

                    floatable1.remove();
                    floatable1.removeMetadata("VisualEffect", plugin);
                    floatable1.removeMetadata("CylindricalPowerStance", plugin);

                    floatable2.remove();
                    floatable2.removeMetadata("VisualEffect", plugin);
                    floatable2.removeMetadata("CylindricalPowerStance", plugin);

                    Bukkit.getScheduler().cancelTask(processID);

                }

                counter++;

            }

            //can only run every 5 ticks or it causes effect-breaking visual glitches client-side
        }, 5, 5);

    }

    //Material item effect preprocessor
    private void itemEffect(Entity entity, double radiusHorizontal, double radiusVertical, double speedHorizontal, double speedVertical, Material material1, Material material2) {

        PowerStanceMath powerStanceMath = new PowerStanceMath(plugin);

        ItemStack itemStack1 = new ItemStack(material1, 1);
        ItemStack itemStack2 = new ItemStack(material2, 1);

        itemEffect(entity, radiusHorizontal, radiusVertical, speedHorizontal, speedVertical, itemStack1, itemStack2);

    }


    public void attackGravityEffect(Entity entity) {

        if (entity.hasMetadata("AttackGravity")) {

            itemEffect(entity, 1.0, 1.0, 20, 2, Material.ELYTRA, Material.ELYTRA);

        }

    }


    public void attackPoisonEffect(Entity entity) {

        if (entity.hasMetadata("AttackPoison")) {

            itemEffect(entity, 1.0, 1.0, 20, 2, Material.EMERALD, Material.EMERALD);

        }

    }


    public void attackPushEffect(Entity entity) {

        if (entity.hasMetadata("AttackPush")) {

            itemEffect(entity, 1.0, 1.0, 20, 2, Material.PISTON_BASE, Material.PISTON_BASE);

        }

    }


    public void attackWitherEffect(Entity entity) {

        if (entity.hasMetadata("AttackWither")) {

            ItemStack itemStack = new ItemStack(Material.SKULL_ITEM, 1, (short) 1);

            itemEffect(entity, 1.0, 1.0, 20, 2, itemStack, itemStack);

        }

    }


    public void invulnerabilityArrowEffect(Entity entity) {

        if (entity.hasMetadata("InvulnerabilityArrow")) {

            itemEffect(entity, 1.0, 1, 20, 0, Material.SPECTRAL_ARROW, Material.TIPPED_ARROW);

        }

    }


    public void invulnerabilityFallDamageEffect(Entity entity) {

        if (entity.hasMetadata("InvulnerabilityFallDamage")) {

            itemEffect(entity, 1.0, 1, 20, 2, Material.FEATHER, Material.FEATHER);
        }

    }


    public void invulnerabilityFireEffect(Entity entity) {

        if (entity.hasMetadata("InvulnerabilityFire")) {

            particleEffect(entity, 1.0, 1.0, 20, 2, Particle.FLAME, Particle.FLAME, 5);

        }

    }


    public void movementSpeedEffect(Entity entity) {

        if (entity.hasMetadata("MovementSpeed")) {

            itemEffect(entity, 1.0, 1.0, 0, 2, Material.GOLD_BOOTS, Material.GOLD_BOOTS);

        }

    }


    //Events
    @EventHandler
    public void lastAntiPickupSafeguard(PlayerPickupItemEvent event) {

        if (event.getItem().hasMetadata("VisualEffect")) {

            event.setCancelled(true);

        }

    }


    @EventHandler
    public void antiHopperPickupSafeguard(InventoryPickupItemEvent event) {

        if (event.getItem().hasMetadata("VisualEffect")) {

            event.setCancelled(true);

        }

    }


    @EventHandler
    public void metadataKiller(EntityDeathEvent event) {

        Entity entity = event.getEntity();

        List<String> metadataList = new ArrayList<String>();
        metadataList.add("AttackGravity");
        metadataList.add("AttackPoison");
        metadataList.add("AttackPush");
        metadataList.add("AttackWither");
        metadataList.add("InvulnerabilityArrow");
        metadataList.add("InvulnerabilityFallDamage");
        metadataList.add("MovementSpeed");

        for (String string : metadataList) {

            if (entity.hasMetadata(string)) {

                entity.removeMetadata(string, plugin);

            }

        }

    }

    @EventHandler
    public void antiItemDespawn (ItemDespawnEvent event) {

        if (event.getEntity().hasMetadata("VisualEffect")) {

            event.setCancelled(true);

        }

    }

}
