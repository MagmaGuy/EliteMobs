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
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.bukkit.Material.*;

/**
 * Created by MagmaGuy on 19/12/2016.
 */
public class ChickenHandler implements Listener {

    private MagmasMobs plugin;

    public ChickenHandler(Plugin plugin) {

        this.plugin = (MagmasMobs) plugin;

    }


    @EventHandler
    public void superDrops(EntityDamageByEntityEvent event) {

        if (event.getEntity() instanceof Chicken && event.getEntity().hasMetadata("MagmasPassiveSupermob")) {

            Random random = new Random();

            Chicken chicken = (Chicken) event.getEntity();

            double damage = event.getFinalDamage();
            //health is hardcoded here, maybe change it at some point?
            double dropChance = damage / 4;
            double dropRandomizer = random.nextDouble();
            //this rounds down
            int dropMinAmount = (int) dropChance;

            ItemStack featherStack = new ItemStack(FEATHER, (random.nextInt(2) + 1));
            ItemStack poultryStack = new ItemStack(RAW_CHICKEN, 1);

            for (int i = 0; i < dropMinAmount; i++) {

                chicken.getWorld().dropItem(chicken.getLocation(), featherStack).setVelocity(ItemDropVelocity.ItemDropVelocity());
                chicken.getWorld().dropItem(chicken.getLocation(), poultryStack).setVelocity(ItemDropVelocity.ItemDropVelocity());

                ExperienceOrb xpDrop = chicken.getWorld().spawn(chicken.getLocation(), ExperienceOrb.class);
                xpDrop.setExperience(random.nextInt(3) + 1);

            }

            if (dropChance > dropRandomizer) {

                chicken.getWorld().dropItem(chicken.getLocation(), featherStack).setVelocity(ItemDropVelocity.ItemDropVelocity());
                chicken.getWorld().dropItem(chicken.getLocation(), poultryStack).setVelocity(ItemDropVelocity.ItemDropVelocity());

                ExperienceOrb xpDrop = chicken.getWorld().spawn(chicken.getLocation(), ExperienceOrb.class);
                xpDrop.setExperience(random.nextInt(3) + 1);

            }

        }

    }


    @EventHandler
    public void onDeath(EntityDeathEvent event) {

        if (event.getEntity() instanceof Chicken && event.getEntity().hasMetadata("MagmasPassiveSupermob")) {

            ItemStack chickenMonsterEgg = new ItemStack(MONSTER_EGG, 2, (short) 93);
            event.getEntity().getWorld().dropItem(event.getEntity().getLocation(), chickenMonsterEgg);

            event.getEntity().removeMetadata("MagmasPassiveSupermob", plugin);

        }

    }

    public static void superEggs(Entity entity, int passiveStackAmount) {

        List<Chicken> tempChickenList = new ArrayList<>();

        if (entity instanceof Chicken && entity.hasMetadata("MagmasPassiveSupermob")) {

            tempChickenList.add((Chicken) entity);

        }

        if (tempChickenList.size() > 0) {

            Random random = new Random();
            int eggChance = random.nextInt(12000/passiveStackAmount);

            //Chicken lay eggs every 5-10 minutes, assuming 10 min that's 12000 ticks
            //should spawn 1 by 1 but the odds of it spawning are scaled to fit config passivemob stack size
            if (eggChance == 1) {

                for (Chicken chicken : tempChickenList) {

                    ItemStack eggStack = new ItemStack(EGG, 1);

                    chicken.getWorld().dropItem(chicken.getLocation(), eggStack).setVelocity(ItemDropVelocity.ItemDropVelocity());

                }

            }

        }

    }

}
