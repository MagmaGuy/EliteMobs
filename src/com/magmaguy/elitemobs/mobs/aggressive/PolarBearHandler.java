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

package com.magmaguy.elitemobs.mobs.aggressive;

import com.magmaguy.elitemobs.EliteMobs;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.elitedrops.ItemDropVelocity;
import com.magmaguy.elitemobs.mobcustomizer.DefaultMaxHealthGuesser;
import org.bukkit.Material;
import org.bukkit.entity.PolarBear;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.Random;

/**
 * Created by MagmaGuy on 08/10/2016.
 */
public class PolarBearHandler implements Listener {

    private EliteMobs plugin;

    public PolarBearHandler(Plugin plugin) {

        this.plugin = (EliteMobs) plugin;

    }


    @EventHandler
    public void onHit(EntityDamageEvent event) {

        if (event.getEntity() instanceof PolarBear && event.getEntity().hasMetadata(MetadataHandler.ELITE_MOB_MD)) {

            Random random = new Random();

            PolarBear polarBear = (PolarBear) event.getEntity();

            double damage = event.getFinalDamage();
            double dropChance = damage / DefaultMaxHealthGuesser.defaultMaxHealthGuesser(polarBear);
            double dropRandomizer = random.nextDouble();
            //this rounds down
            int dropMinAmount = (int) dropChance;

            ItemStack rawFishStack = new ItemStack(Material.RAW_FISH, random.nextInt(2) + 1);
            //can't add salmon drops because spigot doesn't have them yet, so bonus fish
            ItemStack rawSalmonStack = new ItemStack(Material.RAW_FISH, random.nextInt(2) + 1);

            for (int i = 0; i < dropMinAmount; i++) {
                if (random.nextDouble() < 0.75) {
                    polarBear.getWorld().dropItem(polarBear.getLocation(), rawFishStack).setVelocity(ItemDropVelocity.ItemDropVelocity());

                }

                if (random.nextDouble() < 0.25) {

                    polarBear.getWorld().dropItem(polarBear.getLocation(), rawSalmonStack).setVelocity(ItemDropVelocity.ItemDropVelocity());

                }

            }

            if (dropChance > dropRandomizer) {
                if (random.nextDouble() < 0.75) {
                    polarBear.getWorld().dropItem(polarBear.getLocation(), rawFishStack).setVelocity(ItemDropVelocity.ItemDropVelocity());

                }

                if (random.nextDouble() < 0.25) {

                    polarBear.getWorld().dropItem(polarBear.getLocation(), rawSalmonStack).setVelocity(ItemDropVelocity.ItemDropVelocity());

                }

            }

        }

    }

}
