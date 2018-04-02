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
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.ConfigValues;
import com.magmaguy.elitemobs.config.EventsConfig;
import com.magmaguy.elitemobs.events.DeadMoon;
import com.magmaguy.elitemobs.mobcustomizer.AggressiveEliteMobConstructor;
import com.magmaguy.elitemobs.mobcustomizer.NameHandler;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Husk;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Random;

public class TheReturned implements Listener {

    private static Random random = new Random();

    private void spawnTheReturned(Zombie zombie) {

        new BukkitRunnable() {

            @Override
            public void run() {

                if (!zombie.isValid()) return;

                if (zombie.hasMetadata(MetadataHandler.THE_RETURNED)) return;

                int mobLevel;

                if (zombie.hasMetadata(MetadataHandler.ELITE_MOB_MD))
                    mobLevel = (int) Math.ceil(zombie.getMetadata(MetadataHandler.ELITE_MOB_MD).get(0).asInt() / 2);
                else mobLevel = 1;

                theReturnedConstructor(mobLevel, zombie);
                theReturnedConstructor(mobLevel, zombie);

            }

        }.runTaskLater(MetadataHandler.PLUGIN, 1);

    }

    public static void theReturnedConstructor(int mobLevel, Zombie zombie) {

        Location spawnLocation = zombie.getLocation();

        Husk theReturned = (Husk) spawnLocation.getWorld().spawnEntity(spawnLocation, EntityType.HUSK);

        theReturned.setMetadata(MetadataHandler.THE_RETURNED, new FixedMetadataValue(MetadataHandler.PLUGIN, true));
        theReturned.setMetadata(MetadataHandler.ELITE_MOB_MD, new FixedMetadataValue(MetadataHandler.PLUGIN, mobLevel));
        theReturned.setMetadata(MetadataHandler.CUSTOM_ARMOR, new FixedMetadataValue(MetadataHandler.PLUGIN, true));
        theReturned.setMetadata(MetadataHandler.CUSTOM_POWERS_MD, new FixedMetadataValue(MetadataHandler.PLUGIN, true));
        theReturned.setMetadata(MetadataHandler.CUSTOM_STACK, new FixedMetadataValue(MetadataHandler.PLUGIN, true));
        NameHandler.customUniqueNameAssigner(theReturned, ChatColorConverter.chatColorConverter(ConfigValues.eventsConfig.getString(EventsConfig.DEAD_MOON_THE_RETURNED_NAME)));

        double x = random.nextDouble() - 0.5;
        double z = random.nextDouble() - 0.5;

        theReturned.setVelocity(new Vector(x, 0.5, z));

        AggressiveEliteMobConstructor.constructAggressiveEliteMob(theReturned);

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
    public void onSpawn(EntitySpawnEvent event) {

        if (!DeadMoon.eventOngoing) return;

        if (event.getEntity() instanceof Zombie && !event.getEntity().hasMetadata(MetadataHandler.THE_RETURNED))
            spawnTheReturned((Zombie) event.getEntity());

    }

}
