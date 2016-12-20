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

package com.magmaguy.magmasmobs.mobs.passive;

import com.magmaguy.magmasmobs.MagmasMobs;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.IronGolem;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.Random;

import static org.bukkit.Material.IRON_INGOT;
import static org.bukkit.Material.RED_ROSE;

/**
 * Created by MagmaGuy on 19/12/2016.
 */
public class IronGolemHandler implements Listener{

    private MagmasMobs plugin;

    public IronGolemHandler(Plugin plugin){

        this.plugin = (MagmasMobs) plugin;

    }

    @EventHandler
    public void superDrops (EntityDamageByEntityEvent event){

        if (event.getEntity() instanceof IronGolem && event.getEntity().hasMetadata("MagmasPassiveSupermob"))
        {

            Random random = new Random();

            org.bukkit.entity.IronGolem ironGolem = (org.bukkit.entity.IronGolem) event.getEntity();

            double damage = event.getFinalDamage();
            //health is hardcoded here, maybe change it at some point?
            double dropChance = damage / 100;
            double dropRandomizer = random.nextDouble();
            //this rounds down
            int dropMinAmount = (int) dropChance;

            ItemStack ironStack = new ItemStack(IRON_INGOT, random.nextInt(3) + 4);
            ItemStack poppyStack = new ItemStack(RED_ROSE, random.nextInt(3));

            for (int i = 0; i < dropMinAmount; i++)
            {

                ironGolem.getWorld().dropItem(ironGolem.getLocation(), ironStack);
                ironGolem.getWorld().dropItem(ironGolem.getLocation(), poppyStack);

                ExperienceOrb xpDrop = ironGolem.getWorld().spawn(ironGolem.getLocation(), ExperienceOrb.class);
                xpDrop.setExperience(random.nextInt(3) + 1);

            }

            if (dropChance > dropRandomizer)
            {

                ironGolem.getWorld().dropItem(ironGolem.getLocation(), ironStack);
                ironGolem.getWorld().dropItem(ironGolem.getLocation(), poppyStack);

                ExperienceOrb xpDrop = ironGolem.getWorld().spawn(ironGolem.getLocation(), ExperienceOrb.class);
                xpDrop.setExperience(random.nextInt(3) + 1);

            }

        }

    }

    @EventHandler
    public void onAttack (EntityDamageByEntityEvent event)
    {

        if (event.getEntity() instanceof IronGolem && event.getEntity().hasMetadata("MagmasPassiveSupermob"))
        {

            event.setDamage(event.getDamage() * 50);

        }

    }

    @EventHandler
    public void onDeath (EntityDeathEvent event)
    {

        if (event.getEntity() instanceof IronGolem && event.getEntity().hasMetadata("MagmasPassiveSupermob"))
        {

            event.getEntity().removeMetadata("MagmasPassiveSupermob", plugin);

        }

    }

}
