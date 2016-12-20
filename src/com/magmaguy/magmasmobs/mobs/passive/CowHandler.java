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
import com.magmaguy.magmasmobs.superdrops.ItemDropVelocity;
import org.bukkit.entity.Cow;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.Random;

import static org.bukkit.Material.*;

/**
 * Created by MagmaGuy on 19/12/2016.
 */
public class CowHandler implements Listener{

    private MagmasMobs plugin;

    public CowHandler(Plugin plugin){

        this.plugin = (MagmasMobs) plugin;

    }


    @EventHandler
    public void superDrops (EntityDamageByEntityEvent event){

        if (event.getEntity() instanceof Cow && event.getEntity().hasMetadata("MagmasPassiveSupermob"))
        {

            Random random = new Random();

            Cow cow = (Cow) event.getEntity();

            double damage = event.getFinalDamage();
            //health is hardcoded here, maybe change it at some point?
            double dropChance = damage / 10;
            double dropRandomizer = random.nextDouble();
            //this rounds down
            int dropMinAmount = (int) dropChance;

            ItemStack beefStack = new ItemStack(RAW_BEEF, (random.nextInt(3) + 1));
            //leather can drop 0, meaning that it could create visual artifacts. Have to filter that out.
            ItemStack leatherStack = new ItemStack (LEATHER , (random.nextInt(2)));

            for (int i = 0; i < dropMinAmount; i++)
            {

                cow.getWorld().dropItem(cow.getLocation(), beefStack).setVelocity(ItemDropVelocity.ItemDropVelocity());

                ExperienceOrb xpDrop = cow.getWorld().spawn(cow.getLocation(), ExperienceOrb.class);
                xpDrop.setExperience(random.nextInt(3) + 1);

                if (leatherStack.getAmount() != 0)
                {

                    cow.getWorld().dropItem(cow.getLocation(), leatherStack).setVelocity(ItemDropVelocity.ItemDropVelocity());

                }

            }

            if (dropChance > dropRandomizer)
            {

                cow.getWorld().dropItem(cow.getLocation(), beefStack).setVelocity(ItemDropVelocity.ItemDropVelocity());

                ExperienceOrb xpDrop = cow.getWorld().spawn(cow.getLocation(), ExperienceOrb.class);
                xpDrop.setExperience(random.nextInt(3) + 1);

                if (leatherStack.getAmount() != 0)
                {

                    cow.getWorld().dropItem(cow.getLocation(), leatherStack).setVelocity(ItemDropVelocity.ItemDropVelocity());

                }

            }

        }

    }


    @EventHandler
    public void onDeath(EntityDeathEvent event){

        if (event.getEntity() instanceof Cow && event.getEntity().hasMetadata("MagmasPassiveSupermob"))
        {

            Cow cow = (Cow) event.getEntity();

            ItemStack cowMonsterEgg = new ItemStack(MONSTER_EGG, 2, (short) 92);
            cow.getWorld().dropItem(cow.getLocation(), cowMonsterEgg);

            event.getEntity().removeMetadata("MagmasPassiveSupermob", plugin);

        }

    }

}
