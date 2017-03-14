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

package com.magmaguy.magmasmobs.mobs.aggressive;

import com.magmaguy.magmasmobs.MagmasMobs;
import com.magmaguy.magmasmobs.mobscanner.DefaultMaxHealthGuesser;
import com.magmaguy.magmasmobs.superdrops.ItemDropVelocity;
import org.bukkit.Material;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Spider;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.Random;

/**
 * Created by MagmaGuy on 08/10/2016.
 */
public class SpiderHandler implements Listener {

    private MagmasMobs plugin;

    public SpiderHandler(Plugin plugin) {

        this.plugin = (MagmasMobs) plugin;

    }


    @EventHandler
    public void onHit(EntityDamageEvent event) {

        if (event.getEntity() instanceof Spider && event.getEntity().hasMetadata("MagmasSuperMob")) {

            Random random = new Random();

            Spider spider = (Spider) event.getEntity();

            double damage = event.getFinalDamage();
            double dropChance = damage / DefaultMaxHealthGuesser.defaultMaxHealthGuesser(spider);
            double dropRandomizer = random.nextDouble();
            //this rounds down
            int dropMinAmount = (int) dropChance;

            ItemStack stringStack = new ItemStack(Material.STRING, random.nextInt(3));
            ItemStack spiderEyeStack = new ItemStack(Material.SPIDER_EYE, 1);

            for (int i = 0; i < dropMinAmount; i++) {

                if (stringStack.getAmount() != 0) {

                    spider.getWorld().dropItem(spider.getLocation(), stringStack).setVelocity(ItemDropVelocity.ItemDropVelocity());

                }

                if (random.nextDouble() < 0.33) {

                    spider.getWorld().dropItem(spider.getLocation(), spiderEyeStack).setVelocity(ItemDropVelocity.ItemDropVelocity());

                }

                ExperienceOrb xpDrop = spider.getWorld().spawn(spider.getLocation(), ExperienceOrb.class);
                xpDrop.setExperience(5);

            }

            if (dropChance > dropRandomizer) {

                if (stringStack.getAmount() != 0) {

                    spider.getWorld().dropItem(spider.getLocation(), stringStack).setVelocity(ItemDropVelocity.ItemDropVelocity());

                }

                if (random.nextDouble() < 0.33) {

                    spider.getWorld().dropItem(spider.getLocation(), spiderEyeStack).setVelocity(ItemDropVelocity.ItemDropVelocity());

                }

                ExperienceOrb xpDrop = spider.getWorld().spawn(spider.getLocation(), ExperienceOrb.class);
                xpDrop.setExperience(5);

            }

        }

    }

}