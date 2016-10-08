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
import org.bukkit.entity.Arrow;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Skeleton;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.Random;

import static org.bukkit.Material.*;

/**
 * Created by MagmaGuy on 08/10/2016.
 */
public class SkeletonHandler implements Listener{

    private ActiveStack plugin;

    public SkeletonHandler (Plugin plugin){

        this.plugin = (ActiveStack) plugin;

    }


    @EventHandler
    public void onHit(EntityDamageEvent event){

        if(event.getEntity() instanceof Skeleton && event.getEntity().hasMetadata("SuperMob"))
        {

            Random random = new Random();

            Skeleton skeleton = (Skeleton) event.getEntity();

            double damage = event.getFinalDamage();
            //health is hardcoded here, maybe change it at some point
            double dropChance = damage / MobScanner.skeletonHealth;
            double dropRandomizer = random.nextDouble();
            //this rounds down
            int dropMinAmount = (int) dropChance;

            ItemStack boneStack = new ItemStack(BONE, random.nextInt(3));
            ItemStack arrowStack = new ItemStack(ARROW, random.nextInt(3));
            //dropped item chance = 9%
            ItemStack bowStack = new ItemStack(BOW, 1);

            for (int i = 0; i < dropMinAmount; i++)
            {

                if (boneStack.getAmount() != 0)
                {

                    skeleton.getWorld().dropItem(skeleton.getLocation(), boneStack).setVelocity(ItemDropVelocity.ItemDropVelocity());

                }

                if (arrowStack.getAmount() != 0)
                {

                    skeleton.getWorld().dropItem(skeleton.getLocation(), arrowStack).setVelocity(ItemDropVelocity.ItemDropVelocity());

                }

                //lowered chance to drop from 9% to 3% to account for it never dropping damaged bows
                if (random.nextDouble() < 0.03)
                {

                    skeleton.getWorld().dropItem(skeleton.getLocation(), bowStack).setVelocity(ItemDropVelocity.ItemDropVelocity());

                }

                ExperienceOrb xpDrop = skeleton.getWorld().spawn(skeleton.getLocation(), ExperienceOrb.class);
                xpDrop.setExperience(5);

            }

            if (dropChance > dropRandomizer)
            {

                if (boneStack.getAmount() != 0)
                {

                    skeleton.getWorld().dropItem(skeleton.getLocation(), boneStack).setVelocity(ItemDropVelocity.ItemDropVelocity());

                }

                if (arrowStack.getAmount() != 0)
                {

                    skeleton.getWorld().dropItem(skeleton.getLocation(), arrowStack).setVelocity(ItemDropVelocity.ItemDropVelocity());

                }

                //lowered chance to drop from 9% to 3% to account for it never dropping damaged bows
                if (random.nextDouble() < 0.03)
                {

                    skeleton.getWorld().dropItem(skeleton.getLocation(), bowStack).setVelocity(ItemDropVelocity.ItemDropVelocity());

                }

                ExperienceOrb xpDrop = skeleton.getWorld().spawn(skeleton.getLocation(), ExperienceOrb.class);
                xpDrop.setExperience(5);

            }

        }

    }


    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event){

        if (event.getDamager() instanceof Arrow)
        {

            Arrow arrow = (Arrow) event.getDamager();

            if (arrow.getShooter() instanceof Skeleton && ((Skeleton) arrow.getShooter()).hasMetadata("SuperMob"))
            {

                event.setDamage(event.getDamage() * ((Skeleton) arrow.getShooter()).getMetadata("SuperMob").get(0).asInt());

            }

        }

    }

}
