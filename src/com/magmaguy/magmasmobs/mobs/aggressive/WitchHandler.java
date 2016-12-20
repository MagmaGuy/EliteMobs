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
import org.bukkit.entity.Witch;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.Random;

/**
 * Created by MagmaGuy on 08/10/2016.
 */
public class WitchHandler implements Listener{

    private MagmasMobs plugin;

    public WitchHandler (Plugin plugin){

        this.plugin = (MagmasMobs) plugin;

    }


    @EventHandler
    public void onHit(EntityDamageEvent event){

        if (event.getEntity() instanceof Witch && event.getEntity().hasMetadata("MagmasSuperMob"))
        {

            Random random = new Random();

            Witch witch = (Witch) event.getEntity();

            double damage = event.getFinalDamage();
            double dropChance = damage / DefaultMaxHealthGuesser.defaultMaxHealthGuesser(witch);
            double dropRandomizer = random.nextDouble();
            //this rounds down
            int dropMinAmount = (int) dropChance;

            ItemStack glassBottleStack = new ItemStack(Material.GLASS_BOTTLE, random.nextInt(2) + 1);
            ItemStack glowstoneDustStack = new ItemStack(Material.GLOWSTONE_DUST, random.nextInt(2) + 1);
            ItemStack sulphurStack = new ItemStack(Material.SULPHUR, random.nextInt(2) + 1);
            ItemStack redstoneStack = new ItemStack(Material.REDSTONE, random.nextInt(2) + 1);
            ItemStack spiderEyesStack = new ItemStack(Material.SPIDER_EYE, random.nextInt(2) + 1);
            ItemStack sugarStack = new ItemStack(Material.SUGAR, random.nextInt(2) + 1);
            ItemStack stickStack = new ItemStack(Material.STICK, random.nextInt(2) + 1);

            for (int i = 0; i < dropMinAmount; i++)
            {

                if (random.nextDouble() < 0.12)
                {

                    witch.getWorld().dropItem(witch.getLocation(), glassBottleStack).setVelocity(ItemDropVelocity.ItemDropVelocity());

                }

                if (random.nextDouble() < 0.12)
                {

                    witch.getWorld().dropItem(witch.getLocation(), glowstoneDustStack).setVelocity(ItemDropVelocity.ItemDropVelocity());

                }

                if (random.nextDouble() < 0.12)
                {

                    witch.getWorld().dropItem(witch.getLocation(), sulphurStack).setVelocity(ItemDropVelocity.ItemDropVelocity());

                }

                if (random.nextDouble() < 0.12)
                {

                    witch.getWorld().dropItem(witch.getLocation(), redstoneStack).setVelocity(ItemDropVelocity.ItemDropVelocity());

                }

                if (random.nextDouble() < 0.12)
                {

                    witch.getWorld().dropItem(witch.getLocation(), spiderEyesStack).setVelocity(ItemDropVelocity.ItemDropVelocity());

                }

                if (random.nextDouble() < 0.12)
                {

                    witch.getWorld().dropItem(witch.getLocation(), sugarStack).setVelocity(ItemDropVelocity.ItemDropVelocity());

                }

                if (random.nextDouble() < 0.25)
                {

                    witch.getWorld().dropItem(witch.getLocation(), stickStack).setVelocity(ItemDropVelocity.ItemDropVelocity());

                }

            }

            if (dropChance > dropRandomizer)
            {

                if (random.nextDouble() < 0.12)
                {

                    witch.getWorld().dropItem(witch.getLocation(), glassBottleStack).setVelocity(ItemDropVelocity.ItemDropVelocity());

                }

                if (random.nextDouble() < 0.12)
                {

                    witch.getWorld().dropItem(witch.getLocation(), glowstoneDustStack).setVelocity(ItemDropVelocity.ItemDropVelocity());

                }

                if (random.nextDouble() < 0.12)
                {

                    witch.getWorld().dropItem(witch.getLocation(), sulphurStack).setVelocity(ItemDropVelocity.ItemDropVelocity());

                }

                if (random.nextDouble() < 0.12)
                {

                    witch.getWorld().dropItem(witch.getLocation(), redstoneStack).setVelocity(ItemDropVelocity.ItemDropVelocity());

                }

                if (random.nextDouble() < 0.12)
                {

                    witch.getWorld().dropItem(witch.getLocation(), spiderEyesStack).setVelocity(ItemDropVelocity.ItemDropVelocity());

                }

                if (random.nextDouble() < 0.12)
                {

                    witch.getWorld().dropItem(witch.getLocation(), sugarStack).setVelocity(ItemDropVelocity.ItemDropVelocity());

                }

                if (random.nextDouble() < 0.25)
                {

                    witch.getWorld().dropItem(witch.getLocation(), stickStack).setVelocity(ItemDropVelocity.ItemDropVelocity());

                }

            }

        }

    }

}
