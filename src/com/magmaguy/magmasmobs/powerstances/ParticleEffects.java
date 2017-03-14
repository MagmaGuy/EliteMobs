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
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

import java.util.List;

import static com.magmaguy.magmasmobs.MagmasMobs.worldList;

/**
 * Created by MagmaGuy on 04/11/2016.
 */
public class ParticleEffects implements Listener {

    private MagmasMobs plugin;

    public ParticleEffects(Plugin plugin) {

        this.plugin = (MagmasMobs) plugin;

    }


    private int processID;
    private final Vector verticalVelocity = new Vector(0, 0.1, 0);

    //Particle effect
    private void particleEffect(Entity entity, double radiusHorizontal, double radiusVertical, double speedHorizontal, double offset, Particle particle1, Particle particle2, int particleAmount) {

        PowerStanceMath powerStanceMath = new PowerStanceMath(plugin);

        processID = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {

            public void run() {

                List<Location> particleLocations = powerStanceMath.cylindricalPowerStance(entity, radiusHorizontal, radiusVertical, speedHorizontal, offset);
                Location location1 = particleLocations.get(0);
                Location location2 = particleLocations.get(1);

                entity.getWorld().spawnParticle(particle1, location1.getX(), location1.getY(), location1.getZ(), particleAmount, 0.0, 0.0, 0.0, 0.01);
                entity.getWorld().spawnParticle(particle2, location2.getX(), location2.getY(), location2.getZ(), particleAmount, 0.0, 0.0, 0.0, 0.01);

                if (!entity.isValid()) {

                    Bukkit.getScheduler().cancelTask(processID);

                }

            }

        }, 1L, 1L);

    }


    //Item effect
    private void itemEffect(Entity entity, double radiusHorizontal, double radiusVertical, double speedHorizontal, double offset, Material material1, Material material2) {

        PowerStanceMath powerStanceMath = new PowerStanceMath(plugin);

        processID = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {

            Item floatable1;
            Item floatable2;

            public void run() {


                List<Location> particleLocations = powerStanceMath.cylindricalPowerStance(entity, radiusHorizontal, radiusVertical, speedHorizontal, offset);
                Location location1 = particleLocations.get(0);
                Location location2 = particleLocations.get(1);

                ItemStack itemStack1 = new ItemStack(material1, 1);
                ItemStack itemStack2 = new ItemStack(material2, 1);

                if (floatable1 == null && floatable2 == null) {

                    floatable1 = entity.getWorld().dropItem(location1, itemStack1);

                    floatable2 = entity.getWorld().dropItem(location2, itemStack2);

                } else {

                    floatable1.remove();
                    floatable2.remove();
                    floatable1.removeMetadata("VisualEffect", plugin);
                    floatable2.removeMetadata("VisualEffect", plugin);

                    floatable1 = entity.getWorld().dropItem(location1, itemStack1);

                    floatable2 = entity.getWorld().dropItem(location2, itemStack2);

                }

                floatable1.setPickupDelay(20 * 60 * 6);
                floatable1.setMetadata("VisualEffect", new FixedMetadataValue(plugin, true));
                floatable1.setVelocity(verticalVelocity);

                floatable2.setPickupDelay(20 * 60 * 6);
                floatable2.setMetadata("VisualEffect", new FixedMetadataValue(plugin, true));
                floatable2.setVelocity(verticalVelocity);

                if (!entity.isValid()) {

                    floatable1.remove();
                    floatable2.remove();
                    floatable1.removeMetadata("VisualEffect", plugin);
                    floatable2.removeMetadata("VisualEffect", plugin);
                    Bukkit.getScheduler().cancelTask(processID);

                }

            }

        }, 1L, 1L);

    }

    private void itemEffect(Entity entity, double radiusHorizontal, double radiusVertical, double speedHorizontal, double offset, ItemStack itemStack1, ItemStack itemStack2) {

        PowerStanceMath powerStanceMath = new PowerStanceMath(plugin);

        processID = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {

            Item floatable1;
            Item floatable2;

            public void run() {


                List<Location> particleLocations = powerStanceMath.cylindricalPowerStance(entity, radiusHorizontal, radiusVertical, speedHorizontal, offset);
                Location location1 = particleLocations.get(0);
                Location location2 = particleLocations.get(1);


                if (floatable1 == null && floatable2 == null) {

                    floatable1 = entity.getWorld().dropItem(location1, itemStack1);

                    floatable2 = entity.getWorld().dropItem(location2, itemStack2);

                } else {

                    floatable1.remove();
                    floatable2.remove();
                    floatable1.removeMetadata("VisualEffect", plugin);
                    floatable2.removeMetadata("VisualEffect", plugin);

                    floatable1 = entity.getWorld().dropItem(location1, itemStack1);

                    floatable2 = entity.getWorld().dropItem(location2, itemStack2);

                }

                floatable1.setPickupDelay(20 * 60 * 6);
                floatable1.setMetadata("VisualEffect", new FixedMetadataValue(plugin, true));
                floatable1.setVelocity(verticalVelocity);

                floatable2.setPickupDelay(20 * 60 * 6);
                floatable2.setMetadata("VisualEffect", new FixedMetadataValue(plugin, true));
                floatable2.setVelocity(verticalVelocity);

                if (!entity.isValid()) {

                    floatable1.remove();
                    floatable2.remove();
                    floatable1.removeMetadata("VisualEffect", plugin);
                    floatable2.removeMetadata("VisualEffect", plugin);
                    Bukkit.getScheduler().cancelTask(processID);

                }

            }

        }, 1L, 1L);

    }


    public void attackGravityEffect(Entity entity) {

        if (entity.hasMetadata("AttackGravity")) {

            itemEffect(entity, 1.0, 0.5, 20, 2, Material.ELYTRA, Material.ELYTRA);
            metadataKiller(entity, "AttackGravity");

        }

    }


    public void attackPoisonEffect(Entity entity) {

        if (entity.hasMetadata("AttackPoison")) {

            itemEffect(entity, 1.0, 0.5, 20, 2, Material.EMERALD, Material.EMERALD);
            metadataKiller(entity, "AttackPoison");

        }

    }


    public void attackPushEffect(Entity entity) {

        if (entity.hasMetadata("AttackPush")) {

            itemEffect(entity, 1.0, 0.5, 20, 2, Material.PISTON_BASE, Material.PISTON_BASE);
            metadataKiller(entity, "AttackPush");

        }

    }


    public void attackWitherEffect(Entity entity) {

        if (entity.hasMetadata("AttackWither")) {

            ItemStack itemStack = new ItemStack(Material.SKULL_ITEM, 1, (short) 1);

            itemEffect(entity, 1.0, 0.5, 20, 2, itemStack, itemStack);
            metadataKiller(entity, "AttackWither");

        }

    }


    public void invulnerabilityArrowEffect(Entity entity) {

        if (entity.hasMetadata("InvulnerabilityArrow")) {

            itemEffect(entity, 1.0, 1.0, 20, 0, Material.SPECTRAL_ARROW, Material.TIPPED_ARROW);
            metadataKiller(entity, "InvulnerabilityArrow");

        }

    }


    public void invulnerabilityFallDamageEffect(Entity entity) {

        if (entity.hasMetadata("InvulnerabilityFallDamage")) {

            itemEffect(entity, 1.0, 0.5, 20, 2, Material.FEATHER, Material.FEATHER);
            metadataKiller(entity, "InvulnerabilityFallDamage");
        }

    }


    public void invulnerabilityFireEffect(Entity entity) {

        if (entity.hasMetadata("InvulnerabilityFire")) {

            particleEffect(entity, 1.0, 1.0, 20, 2, Particle.FLAME, Particle.FLAME, 5);
            metadataKiller(entity, "InvulnerabilityFire");

        }

    }


    public void movementSpeedEffect(Entity entity) {

        if (entity.hasMetadata("MovementSpeed")) {

            itemEffect(entity, 1.0, 0.5, 20, 2, Material.GOLD_BOOTS, Material.GOLD_BOOTS);
            metadataKiller(entity, "MovementSpeed");
        }

    }


    //Events
    @EventHandler
    public void lastAntiPickupSafeguard(PlayerPickupItemEvent event) {

        if (event.getItem().hasMetadata("VisualEffect")) {

            event.getItem().remove();
            event.getItem().removeMetadata("VisualEffect", plugin);
            event.setCancelled(true);

        }

    }


    @EventHandler
    public void antiHopperPickupSafeguard(InventoryPickupItemEvent event) {

        if (event.getItem().hasMetadata("VisualEffect")) {

            event.getItem().remove();
            event.getItem().removeMetadata("VisualEffect", plugin);
            event.setCancelled(true);

        }

    }


    private void metadataKiller(Entity entity, String metadataName) {

        if (!entity.isValid()) {

            entity.removeMetadata(metadataName, plugin);

        }

    }


    public void droppedItemsFlush() {

        for (World world : worldList)

        {

            for (Entity entity : world.getEntities()) {

                if (entity.hasMetadata("VisualEffect")) {

                    entity.remove();
                    entity.removeMetadata("VisualEffect", plugin);

                }

            }

        }

    }

}
