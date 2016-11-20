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

package com.magmaguy.activestack.Mobs;

import com.magmaguy.activestack.ActiveStack;
import com.magmaguy.activestack.DefaultMaxHealthGuesser;
import com.magmaguy.activestack.ItemDropVelocity;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.Random;

import static org.bukkit.Material.*;

/**
 * Created by MagmaGuy on 07/10/2016.
 */
public class ZombieHandler implements Listener{

    private ActiveStack plugin;

    public ZombieHandler (Plugin plugin) {

        this.plugin = (ActiveStack) plugin;

    }


    @EventHandler
    public void onHit(EntityDamageEvent event)
    {

        if(event.getEntity() instanceof Zombie && event.getEntity().hasMetadata("SuperMob"))
        {

            Random random = new Random();

            Zombie zombie = (Zombie) event.getEntity();

            double damage = event.getFinalDamage();
            double dropChance = damage / DefaultMaxHealthGuesser.defaultMaxHealthGuesser(zombie);
            double dropRandomizer = random.nextDouble();
            //this rounds down
            int dropMinAmount = (int) dropChance;

            //may drop 0
            ItemStack rottenFleshStack = new ItemStack(ROTTEN_FLESH, random.nextInt(3));
            //rare drops
            ItemStack ironStack = new ItemStack(IRON_INGOT, 1);
            ItemStack carrotStack = new ItemStack(CARROT_ITEM, 1);
            ItemStack potatoesStack = new ItemStack(POTATO_ITEM, 1);

            for (int i = 0; i < dropMinAmount; i++)
            {

                if (rottenFleshStack.getAmount() != 0)
                {

                    zombie.getWorld().dropItem(zombie.getLocation(), rottenFleshStack).setVelocity(ItemDropVelocity.ItemDropVelocity());

                }

                if (random.nextDouble() < 0.03)
                {

                    zombie.getWorld().dropItem(zombie.getLocation(), ironStack).setVelocity(ItemDropVelocity.ItemDropVelocity());

                }

                if (random.nextDouble() < 0.03)
                {

                    zombie.getWorld().dropItem(zombie.getLocation(), carrotStack).setVelocity(ItemDropVelocity.ItemDropVelocity());

                }

                if (random.nextDouble() < 0.03)
                {

                    zombie.getWorld().dropItem(zombie.getLocation(), potatoesStack).setVelocity(ItemDropVelocity.ItemDropVelocity());

                }

                ExperienceOrb xpDrop = zombie.getWorld().spawn(zombie.getLocation(), ExperienceOrb.class);
                xpDrop.setExperience(5);

            }

            if (dropChance > dropRandomizer)
            {

                if (rottenFleshStack.getAmount() != 0)
                {

                    zombie.getWorld().dropItem(zombie.getLocation(), rottenFleshStack).setVelocity(ItemDropVelocity.ItemDropVelocity());

                }

                if (random.nextDouble() < 0.03)
                {

                    zombie.getWorld().dropItem(zombie.getLocation(), ironStack).setVelocity(ItemDropVelocity.ItemDropVelocity());

                }

                if (random.nextDouble() < 0.03)
                {

                    zombie.getWorld().dropItem(zombie.getLocation(), carrotStack).setVelocity(ItemDropVelocity.ItemDropVelocity());

                }

                if (random.nextDouble() < 0.03)
                {

                    zombie.getWorld().dropItem(zombie.getLocation(), potatoesStack).setVelocity(ItemDropVelocity.ItemDropVelocity());

                }

                ExperienceOrb xpDrop = zombie.getWorld().spawn(zombie.getLocation(), ExperienceOrb.class);
                xpDrop.setExperience(5);

            }

        }

    }


    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event){

        if (event.getDamager() instanceof Zombie && event.getDamager().hasMetadata("SuperMob"))
        {

            event.setDamage(event.getFinalDamage() * event.getDamager().getMetadata("SuperMob").get(0).asInt());

        }

    }

}
