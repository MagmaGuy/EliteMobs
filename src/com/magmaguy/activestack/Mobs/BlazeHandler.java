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
import com.magmaguy.activestack.ItemDropVelocity;
import com.magmaguy.activestack.MobScanner;
import org.bukkit.Material;
import org.bukkit.entity.Blaze;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Fireball;
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
public class BlazeHandler implements Listener{

    private ActiveStack plugin;

    public BlazeHandler (Plugin plugin){

        this.plugin = (ActiveStack) plugin;

    }


    @EventHandler
    public void onHit(EntityDamageEvent event){

        if (event.getEntity() instanceof Blaze && event.getEntity().hasMetadata("SuperMob"))
        {

            Random random = new Random();

            Blaze blaze = (Blaze) event.getEntity();

            double damage = event.getFinalDamage();
            //health is hardcoded here, maybe change it at some point
            double dropChance = damage / MobScanner.blazeHealth;
            double dropRandomizer = random.nextDouble();
            //this rounds down
            int dropMinAmount = (int) dropChance;

            ItemStack blazeRodStack = new ItemStack(Material.BLAZE_ROD, random.nextInt(2));
            
            for (int i = 0; i < dropMinAmount; i++)
            {
                
                if (blazeRodStack.getAmount() != 0)
                {

                    blaze.getWorld().dropItem(blaze.getLocation(), blazeRodStack).setVelocity(ItemDropVelocity.ItemDropVelocity());

                }

                ExperienceOrb xpDrop = blaze.getWorld().spawn(blaze.getLocation(), ExperienceOrb.class);
                xpDrop.setExperience(10);

            }

            if (dropChance > dropRandomizer)
            {

                if (blazeRodStack.getAmount() != 0)
                {

                    blaze.getWorld().dropItem(blaze.getLocation(), blazeRodStack).setVelocity(ItemDropVelocity.ItemDropVelocity());

                }

                ExperienceOrb xpDrop = blaze.getWorld().spawn(blaze.getLocation(), ExperienceOrb.class);
                xpDrop.setExperience(10);

            }

        }

    }


    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event){

        if (event.getDamager() instanceof Fireball)
        {

            Fireball fireball = (Fireball) event.getDamager();

            if (fireball.getShooter() instanceof  Blaze && ((Blaze) fireball.getShooter()).hasMetadata("SuperMob"))
            {

                event.setDamage(event.getDamage() * ((Blaze) fireball.getShooter()).getMetadata("SuperMob").get(0).asInt());

            }

        }

    }

}
