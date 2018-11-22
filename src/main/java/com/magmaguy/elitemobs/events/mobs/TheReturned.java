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

package com.magmaguy.elitemobs.events.mobs;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.EntityTracker;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.ConfigValues;
import com.magmaguy.elitemobs.config.EventsConfig;
import com.magmaguy.elitemobs.events.DeadMoon;
import com.magmaguy.elitemobs.mobconstructor.EliteMobEntity;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Husk;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.concurrent.ThreadLocalRandom;

public class TheReturned implements Listener {

    private static HashSet<LivingEntity> theReturnedList = new HashSet<>();

    public static boolean isTheReturned(LivingEntity livingEntity) {
        return theReturnedList.contains(livingEntity);
    }

    @EventHandler
    public void onSpawn(EntitySpawnEvent event) {

        if (!DeadMoon.eventOngoing) return;

        if (event.getEntity() instanceof LivingEntity && theReturnedList.contains(event.getEntity()))
            spawnTheReturned((Zombie) event.getEntity());

    }

    private void spawnTheReturned(Zombie zombie) {

        /*
        Wait a tick before spawning as third party compatibility can get messy with spawn events
         */
        new BukkitRunnable() {

            @Override
            public void run() {

                if (!zombie.isValid()) return;

                if (theReturnedList.contains(zombie)) return;

                int mobLevel;

                if (EntityTracker.isEliteMob(zombie))
                    mobLevel = (int) Math.ceil(EntityTracker.getEliteMobEntity(zombie).getLevel() / 2);
                else mobLevel = 1;

                theReturnedConstructor(mobLevel, zombie);
                theReturnedConstructor(mobLevel, zombie);

            }

        }.runTaskLater(MetadataHandler.PLUGIN, 1);

    }

    public static void theReturnedConstructor(int mobLevel, Zombie zombie) {

        Location spawnLocation = zombie.getLocation();

        Husk theReturned = (Husk) spawnLocation.getWorld().spawnEntity(spawnLocation, EntityType.HUSK);

        EliteMobEntity eliteMobEntity = new EliteMobEntity(theReturned, mobLevel);
        eliteMobEntity.setHasCustomArmor(true);
        eliteMobEntity.setHasCustomPowers(true);
        eliteMobEntity.setHasStacking(false);
        eliteMobEntity.setName(ChatColorConverter.convert(ConfigValues.eventsConfig.getString(EventsConfig.DEAD_MOON_THE_RETURNED_NAME)));

        double x = ThreadLocalRandom.current().nextDouble() - 0.5;
        double z = ThreadLocalRandom.current().nextDouble() - 0.5;

        theReturned.setVelocity(new Vector(x, 0.5, z));

        new BukkitRunnable() {

            @Override
            public void run() {

                theReturned.getEquipment().setBoots(new ItemStack(Material.AIR));
                theReturned.getEquipment().setLeggings(new ItemStack(Material.AIR));
                theReturned.getEquipment().setChestplate(new ItemStack(Material.AIR));
                theReturned.getEquipment().setHelmet(new ItemStack(Material.AIR));
                theReturned.getEquipment().setItemInMainHand(new ItemStack(Material.AIR));

            }

        }.runTaskLater(MetadataHandler.PLUGIN, 1);

    }

    @EventHandler
    public void onDeath(EntityDeathEvent event) {
        theReturnedList.remove(event.getEntity());
    }

}
