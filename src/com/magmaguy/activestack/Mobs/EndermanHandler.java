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
import org.bukkit.Material;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.Random;

/**
 * Created by MagmaGuy on 08/10/2016.
 */
public class EndermanHandler implements Listener{

    private ActiveStack plugin;

    public EndermanHandler(Plugin plugin) {

        this.plugin = (ActiveStack) plugin;

    }


    @EventHandler
    public void onHit(EntityDamageEvent event){

        if (event.getEntity() instanceof Enderman && event.getEntity().hasMetadata("SuperMob"))
        {

            Random random = new Random();

            Enderman enderman = (Enderman) event.getEntity();

            double damage = event.getFinalDamage();
            double dropChance = damage / DefaultMaxHealthGuesser.defaultMaxHealthGuesser(enderman);
            double droprandomizer = random.nextDouble();
            //this rounds down
            int dropMinAmount = (int) dropChance;

            ItemStack enderPearlStack = new ItemStack(Material.ENDER_PEARL, random.nextInt());

            for (int i = 0; i < dropMinAmount; i++)
            {

                if (enderPearlStack.getAmount() != 0)
                {

                    enderman.getWorld().dropItem(enderman.getLocation(), enderPearlStack).setVelocity(ItemDropVelocity.ItemDropVelocity());

                }

                ExperienceOrb xpDrop = enderman.getWorld().spawn(enderman.getLocation(), ExperienceOrb.class);
                xpDrop.setExperience(5);

            }

            if (dropChance > droprandomizer)
            {

                if (enderPearlStack.getAmount() != 0)
                {

                    enderman.getWorld().dropItem(enderman.getLocation(), enderPearlStack).setVelocity(ItemDropVelocity.ItemDropVelocity());

                }

                ExperienceOrb xpDrop = enderman.getWorld().spawn(enderman.getLocation(), ExperienceOrb.class);
                xpDrop.setExperience(5);

            }

        }

    }


    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event){

        if (event.getDamager() instanceof Enderman && event.getDamager().hasMetadata("SuperMob"))
        {

            event.setDamage(event.getFinalDamage() * event.getDamager().getMetadata("SuperMob").get(0).asInt());

        }

    }

}
