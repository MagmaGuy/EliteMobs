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
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.WitherSkeleton;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.Random;

import static org.bukkit.Material.*;

/**
 * Created by MagmaGuy on 28/11/2016.
 */
public class WitherSkeletonHandler implements Listener {

    private EliteMobs plugin;

    public WitherSkeletonHandler(Plugin plugin) {

        this.plugin = (EliteMobs) plugin;

    }


    @EventHandler
    public void onHit(EntityDamageEvent event) {

        if (event.getEntity() instanceof WitherSkeleton && !(event.getEntity() instanceof Skeleton) &&
                event.getEntity().hasMetadata(MetadataHandler.ELITE_MOB_MD)) {

            Random random = new Random();

            WitherSkeleton witherSkeleton = (WitherSkeleton) event.getEntity();

            double damage = event.getFinalDamage();
            double dropChance = damage / DefaultMaxHealthGuesser.defaultMaxHealthGuesser(witherSkeleton);
            double dropRandomizer = random.nextDouble();
            //this rounds down
            int dropMinAmount = (int) dropChance;

            ItemStack coalStack = new ItemStack(COAL, random.nextInt(1));
            ItemStack arrowStack = new ItemStack(ARROW, random.nextInt(3));
            //dropped item chance = 9%
            ItemStack bowStack = new ItemStack(BOW, 1);

            for (int i = 0; i < dropMinAmount; i++) {

                if (coalStack.getAmount() != 0) {

                    witherSkeleton.getWorld().dropItem(witherSkeleton.getLocation(), coalStack).setVelocity(ItemDropVelocity.ItemDropVelocity());

                }

                if (arrowStack.getAmount() != 0) {

                    witherSkeleton.getWorld().dropItem(witherSkeleton.getLocation(), arrowStack).setVelocity(ItemDropVelocity.ItemDropVelocity());

                }

                //lowered chance to drop from 9% to 3% to account for it never dropping damaged bows
                if (random.nextDouble() < 0.03) {

                    witherSkeleton.getWorld().dropItem(witherSkeleton.getLocation(), bowStack).setVelocity(ItemDropVelocity.ItemDropVelocity());

                }

                ExperienceOrb xpDrop = witherSkeleton.getWorld().spawn(witherSkeleton.getLocation(), ExperienceOrb.class);
                xpDrop.setExperience(5);

            }

            if (dropChance > dropRandomizer) {

                if (coalStack.getAmount() != 0) {

                    witherSkeleton.getWorld().dropItem(witherSkeleton.getLocation(), coalStack).setVelocity(ItemDropVelocity.ItemDropVelocity());

                }

                if (arrowStack.getAmount() != 0) {

                    witherSkeleton.getWorld().dropItem(witherSkeleton.getLocation(), arrowStack).setVelocity(ItemDropVelocity.ItemDropVelocity());

                }

                //lowered chance to drop from 9% to 3% to account for it never dropping damaged bows
                if (random.nextDouble() < 0.03) {

                    witherSkeleton.getWorld().dropItem(witherSkeleton.getLocation(), bowStack).setVelocity(ItemDropVelocity.ItemDropVelocity());

                }

                ExperienceOrb xpDrop = witherSkeleton.getWorld().spawn(witherSkeleton.getLocation(), ExperienceOrb.class);
                xpDrop.setExperience(5);

            }

        }

    }

}