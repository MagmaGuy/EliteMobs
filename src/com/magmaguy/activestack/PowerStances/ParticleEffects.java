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

package com.magmaguy.activestack.PowerStances;

import com.magmaguy.activestack.ActiveStack;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

import java.util.List;

/**
 * Created by MagmaGuy on 04/11/2016.
 */
public class ParticleEffects {

    private ActiveStack plugin;

    public ParticleEffects(Plugin plugin){

        this.plugin = (ActiveStack) plugin;

    }


    private int processID;

    public void invulnerabilityFireEffect (Entity entity)
    {

        if (entity.hasMetadata("InvulnerabilityFire"))
        {

            PowerStanceMath powerStanceMath = new PowerStanceMath(plugin);

            if (entity.isValid())
            {

                processID = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {

                    public void run() {

                        double radiusHorizontal = 1.0;
                        double radiusVertical = 1.0;
                        double speedHorizontal = 20;
                        int offset = 2;

                        List<Location> particleLocations = powerStanceMath.cylindricalPowerStance(entity, radiusHorizontal, radiusVertical, speedHorizontal, offset);
                        Location location1 = particleLocations.get(0);
                        Location location2 = particleLocations.get(1);

                        entity.getWorld().spawnParticle(Particle.FLAME, location1.getX(), location1.getY(), location1.getZ(), 1, 0.0, 0.0, 0.0, 0.01);
                        entity.getWorld().spawnParticle(Particle.FLAME, location2.getX(), location2.getY(), location2.getZ(), 1, 0.0, 0.0, 0.0, 0.01);

                        if (!entity.isValid())
                        {

                            Bukkit.getScheduler().cancelTask(processID);

                        }

                    }

                }, 1, 1);

            }

        }

    }

    private Item floatable1;
    private Item floatable2;
    private Location location1;
    private Location location2;
    Vector verticalVelocity = new Vector(0, 0.1, 0);

    public void invulnerabilityArrowEffect (Entity entity)
    {

        if (entity.hasMetadata("InvulnerabilityArrow"))
        {

            PowerStanceMath powerStanceMath = new PowerStanceMath(plugin);

            LivingEntity livingEntity = (LivingEntity) entity;

            if (entity.isValid())
            {

                processID = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {

                    public void run() {

                        double radiusHorizontal = 1.0;
                        double radiusVertical = 1.0;
                        double speedHorizontal = 20;
                        int offset = 0;

                        List<Location> particleLocations = powerStanceMath.cylindricalPowerStance(entity, radiusHorizontal, radiusVertical, speedHorizontal, offset);
                        location1 = particleLocations.get(0);
                        location2 = particleLocations.get(1);

                        ItemStack emptyArrowStack1 = new ItemStack(Material.SPECTRAL_ARROW, 0);
                        ItemStack emptyArrowStack2 = new ItemStack(Material.TIPPED_ARROW, 0);

                        if (floatable1 == null && floatable2 == null)
                        {

                            floatable1 = entity.getWorld().dropItem(location1, emptyArrowStack1);
                            floatable1.setVelocity(verticalVelocity);

                            floatable2 = entity.getWorld().dropItem(location2, emptyArrowStack2);
                            floatable2.setVelocity(verticalVelocity);

                        } else {

                            floatable1.remove();
                            floatable2.remove();

                            floatable1 = entity.getWorld().dropItem(location1, emptyArrowStack1);
                            floatable1.setVelocity(verticalVelocity);

                            floatable2 = entity.getWorld().dropItem(location2, emptyArrowStack2);
                            floatable2.setVelocity(verticalVelocity);

                        }

                        if (!entity.isValid())
                        {

                            floatable1.remove();
                            floatable2.remove();
                            Bukkit.getScheduler().cancelTask(processID);

                        }

                    }

                }, 1L, 1L);

            }

        }

    }


    public void attackGravityEffect (Entity entity)
    {

        if (entity.hasMetadata("AttackGravity"))
        {

            PowerStanceMath powerStanceMath = new PowerStanceMath(plugin);

            LivingEntity livingEntity = (LivingEntity) entity;

            if(entity.isValid())
            {

                processID = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {

                    @Override
                    public void run() {

                        double radiusHorizontal = 1.0;
                        double radiusVertical = 0.5;
                        double speedHorizontal = 20;
                        int offset = 2;

                        List<Location> particleLocations = powerStanceMath.cylindricalPowerStance(entity, radiusHorizontal, radiusVertical, speedHorizontal, offset);
                        location1 = particleLocations.get(0);
                        location2 = particleLocations.get(1);

                        ItemStack emptyElytraStack = new ItemStack(Material.ELYTRA, 0);

                        if (floatable1 == null && floatable2 == null)
                        {

                            floatable1 = entity.getWorld().dropItem(location1, emptyElytraStack);
                            floatable1.setVelocity(verticalVelocity);

                            floatable2 = entity.getWorld().dropItem(location2, emptyElytraStack);
                            floatable2.setVelocity(verticalVelocity);

                        } else {

                            floatable1.remove();
                            floatable2.remove();

                            floatable1 = entity.getWorld().dropItem(location1, emptyElytraStack);
                            floatable1.setVelocity(verticalVelocity);

                            floatable2 = entity.getWorld().dropItem(location2, emptyElytraStack);
                            floatable2.setVelocity(verticalVelocity);

                        }

                        if (!entity.isValid())
                        {

                            floatable1.remove();
                            floatable2.remove();
                            Bukkit.getScheduler().cancelTask(processID);

                        }

                    }

                }, 1L, 1L);

            }

        }

    }

    public void attackPushEffect(Entity entity)
    {

        if (entity.hasMetadata("AttackGravity"))
        {

            if (entity.isValid())
            {

                processID = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {

                    @Override
                    public void run() {

                        //TODO: add correct particles here
                        //Entity type:

                    }

                }, 1L, 1L);

            }

        }

    }

}
