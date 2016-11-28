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
import org.bukkit.entity.PigZombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.Random;

import static org.bukkit.Material.GOLD_NUGGET;
import static org.bukkit.Material.ROTTEN_FLESH;

/**
 * Created by MagmaGuy on 08/10/2016.
 */
public class PigZombieHandler implements Listener{

    private ActiveStack plugin;

    public PigZombieHandler(Plugin plugin){

        this.plugin = (ActiveStack) plugin;

    }


    @EventHandler
    public void onHit(EntityDamageEvent event){

        if (event.getEntity() instanceof PigZombieHandler && event.getEntity().hasMetadata("MagmasSuperMob"))
        {

            Random random = new Random();

            PigZombie pigZombie = (PigZombie) event.getEntity();

            double damage = event.getFinalDamage();
            double dropChance = damage / DefaultMaxHealthGuesser.defaultMaxHealthGuesser(pigZombie);
            double dropRandomizer = random.nextDouble();
            //this rounds down
            int dropMinAmount = (int) dropChance;

            ItemStack rottenFleshStack = new ItemStack(ROTTEN_FLESH, random.nextInt());
            ItemStack goldNuggetStack = new ItemStack(GOLD_NUGGET, 1);

            for (int i = 0; i < dropMinAmount; i++)
            {

                if (rottenFleshStack.getAmount() != 0)
                {

                    pigZombie.getWorld().dropItem(pigZombie.getLocation(), rottenFleshStack).setVelocity(ItemDropVelocity.ItemDropVelocity());

                }

                if (random.nextDouble() < 0.3)
                {

                    pigZombie.getWorld().dropItem(pigZombie.getLocation(), goldNuggetStack).setVelocity(ItemDropVelocity.ItemDropVelocity());

                }

                ExperienceOrb xpDrop = pigZombie.getWorld().spawn(pigZombie.getLocation(), ExperienceOrb.class);
                xpDrop.setExperience(5);

            }

            if (dropChance > dropRandomizer)
            {

                if (rottenFleshStack.getAmount() != 0)
                {

                    pigZombie.getWorld().dropItem(pigZombie.getLocation(), rottenFleshStack).setVelocity(ItemDropVelocity.ItemDropVelocity());

                }

                if (random.nextDouble() < 0.3)
                {

                    pigZombie.getWorld().dropItem(pigZombie.getLocation(), goldNuggetStack).setVelocity(ItemDropVelocity.ItemDropVelocity());

                }

                ExperienceOrb xpDrop = pigZombie.getWorld().spawn(pigZombie.getLocation(), ExperienceOrb.class);
                xpDrop.setExperience(5);

            }

        }

    }


    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event){

        if (event.getDamager() instanceof PigZombie && event.getDamager().hasMetadata("MagmasSuperMob"))
        {

            event.setDamage(event.getFinalDamage() * event.getDamager().getMetadata("MagmasSuperMob").get(0).asInt());

        }

    }

}