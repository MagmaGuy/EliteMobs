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

package com.magmaguy.magmasmobs.Mobs;

import com.magmaguy.magmasmobs.MobScanner.DefaultMaxHealthGuesser;
import com.magmaguy.magmasmobs.SuperDrops.ItemDropVelocity;
import com.magmaguy.magmasmobs.MagmasMobs;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Husk;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.Random;

import static org.bukkit.Material.*;

/**
 * Created by MagmaGuy on 28/11/2016.
 */
public class HuskHandler implements Listener {

    private MagmasMobs plugin;

    public HuskHandler (Plugin plugin) {

        this.plugin = (MagmasMobs) plugin;

    }


    @EventHandler
    public void onHit(EntityDamageEvent event)
    {

        if(event.getEntity() instanceof Husk && event.getEntity().hasMetadata("MagmasSuperMob"))
        {

            Random random = new Random();

            Husk husk = (Husk) event.getEntity();

            double damage = event.getFinalDamage();
            double dropChance = damage / DefaultMaxHealthGuesser.defaultMaxHealthGuesser(husk);
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

                    husk.getWorld().dropItem(husk.getLocation(), rottenFleshStack).setVelocity(ItemDropVelocity.ItemDropVelocity());

                }

                if (random.nextDouble() < 0.03)
                {

                    husk.getWorld().dropItem(husk.getLocation(), ironStack).setVelocity(ItemDropVelocity.ItemDropVelocity());

                }

                if (random.nextDouble() < 0.03)
                {

                    husk.getWorld().dropItem(husk.getLocation(), carrotStack).setVelocity(ItemDropVelocity.ItemDropVelocity());

                }

                if (random.nextDouble() < 0.03)
                {

                    husk.getWorld().dropItem(husk.getLocation(), potatoesStack).setVelocity(ItemDropVelocity.ItemDropVelocity());

                }

                ExperienceOrb xpDrop = husk.getWorld().spawn(husk.getLocation(), ExperienceOrb.class);
                xpDrop.setExperience(5);

            }

            if (dropChance > dropRandomizer)
            {

                if (rottenFleshStack.getAmount() != 0)
                {

                    husk.getWorld().dropItem(husk.getLocation(), rottenFleshStack).setVelocity(ItemDropVelocity.ItemDropVelocity());

                }

                if (random.nextDouble() < 0.03)
                {

                    husk.getWorld().dropItem(husk.getLocation(), ironStack).setVelocity(ItemDropVelocity.ItemDropVelocity());

                }

                if (random.nextDouble() < 0.03)
                {

                    husk.getWorld().dropItem(husk.getLocation(), carrotStack).setVelocity(ItemDropVelocity.ItemDropVelocity());

                }

                if (random.nextDouble() < 0.03)
                {

                    husk.getWorld().dropItem(husk.getLocation(), potatoesStack).setVelocity(ItemDropVelocity.ItemDropVelocity());

                }

                ExperienceOrb xpDrop = husk.getWorld().spawn(husk.getLocation(), ExperienceOrb.class);
                xpDrop.setExperience(5);

            }

        }

    }

}