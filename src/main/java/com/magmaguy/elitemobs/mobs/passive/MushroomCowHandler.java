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

package com.magmaguy.elitemobs.mobs.passive;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.elitedrops.ItemDropVelocity;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.MushroomCow;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerShearEntityEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

import static org.bukkit.Material.*;

/**
 * Created by MagmaGuy on 19/12/2016.
 */
public class MushroomCowHandler implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void superDrops(EntityDamageByEntityEvent event) {

        if (event.getEntity() instanceof MushroomCow && event.getEntity().hasMetadata(MetadataHandler.PASSIVE_ELITE_MOB_MD)) {

            Random random = new Random();

            MushroomCow mushroomCow = (MushroomCow) event.getEntity();

            double damage = event.getFinalDamage();
            //health is hardcoded here, maybe change it at some point?
            double dropChance = damage / 10;
            double dropRandomizer = random.nextDouble();
            //this rounds down
            int dropMinAmount = (int) dropChance;

            ItemStack beefStack = new ItemStack(RAW_BEEF, (random.nextInt(3) + 1));
            //leather can drop 0, meaning that it could create visual artifacts. Have to filter that out.
            ItemStack leatherStack = new ItemStack(LEATHER, (random.nextInt(2)));

            for (int i = 0; i < dropMinAmount; i++) {

                mushroomCow.getWorld().dropItem(mushroomCow.getLocation(), beefStack).setVelocity(ItemDropVelocity.ItemDropVelocity());

                ExperienceOrb xpDrop = mushroomCow.getWorld().spawn(mushroomCow.getLocation(), ExperienceOrb.class);
                xpDrop.setExperience(random.nextInt(3) + 1);

                if (leatherStack.getAmount() != 0) {

                    mushroomCow.getWorld().dropItem(mushroomCow.getLocation(), leatherStack).setVelocity(ItemDropVelocity.ItemDropVelocity());

                }

            }

            if (dropChance > dropRandomizer) {

                mushroomCow.getWorld().dropItem(mushroomCow.getLocation(), beefStack).setVelocity(ItemDropVelocity.ItemDropVelocity());

                ExperienceOrb xpDrop = mushroomCow.getWorld().spawn(mushroomCow.getLocation(), ExperienceOrb.class);
                xpDrop.setExperience(random.nextInt(3) + 1);

                if (leatherStack.getAmount() != 0) {

                    mushroomCow.getWorld().dropItem(mushroomCow.getLocation(), leatherStack).setVelocity(ItemDropVelocity.ItemDropVelocity());

                }

            }


        }

    }


    @EventHandler
    public void onShear(PlayerShearEntityEvent event) {

        if (event.getEntity() instanceof MushroomCow && event.getEntity().hasMetadata(MetadataHandler.PASSIVE_ELITE_MOB_MD)) {

            MushroomCow mushroomCow = (MushroomCow) event.getEntity();

            ItemStack mushroomStack = new ItemStack(RED_MUSHROOM, 5);

            for (int i = 0; i < 50; i++) {

                mushroomCow.getWorld().dropItem(mushroomCow.getLocation(), mushroomStack).setVelocity(ItemDropVelocity.ItemDropVelocity());

            }

        }

    }

}
