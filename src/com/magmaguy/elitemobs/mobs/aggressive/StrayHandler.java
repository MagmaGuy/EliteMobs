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
import org.bukkit.entity.Stray;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;

import java.util.Random;

import static org.bukkit.Material.*;

/**
 * Created by MagmaGuy on 28/11/2016.
 */
public class StrayHandler implements Listener {

    private EliteMobs plugin;

    public StrayHandler(Plugin plugin) {

        this.plugin = (EliteMobs) plugin;

    }


    @EventHandler
    public void onHit(EntityDamageEvent event) {

        MetadataHandler metadataHandler = new MetadataHandler(plugin);

        if (event.getEntity() instanceof Stray && event.getEntity().hasMetadata(metadataHandler.eliteMobMD)) {

            Random random = new Random();

            Stray stray = (Stray) event.getEntity();

            double damage = event.getFinalDamage();
            double dropChance = damage / DefaultMaxHealthGuesser.defaultMaxHealthGuesser(stray);
            double dropRandomizer = random.nextDouble();
            //this rounds down
            int dropMinAmount = (int) dropChance;

            ItemStack boneStack = new ItemStack(BONE, random.nextInt(3));
            ItemStack arrowStack = new ItemStack(ARROW, random.nextInt(3));
            //dropped item chance = 9%
            ItemStack bowStack = new ItemStack(BOW, 1);

            //dealing with tipped arrows is more complicated
            ItemStack tippedArrowStack = new ItemStack(TIPPED_ARROW, 1);
            PotionMeta meta = (PotionMeta) tippedArrowStack.getItemMeta();
            meta.setBasePotionData(new PotionData(PotionType.SLOWNESS));
            tippedArrowStack.setItemMeta(meta);

            for (int i = 0; i < dropMinAmount; i++) {

                if (boneStack.getAmount() != 0) {

                    stray.getWorld().dropItem(stray.getLocation(), boneStack).setVelocity(ItemDropVelocity.ItemDropVelocity());

                }

                if (arrowStack.getAmount() != 0) {

                    stray.getWorld().dropItem(stray.getLocation(), arrowStack).setVelocity(ItemDropVelocity.ItemDropVelocity());

                }

                //lowered chance to drop from 9% to 3% to account for it never dropping damaged bows
                if (random.nextDouble() < 0.03) {

                    stray.getWorld().dropItem(stray.getLocation(), bowStack).setVelocity(ItemDropVelocity.ItemDropVelocity());

                }

                if (random.nextDouble() < 0.5) {

                    stray.getWorld().dropItem(stray.getLocation(), tippedArrowStack).setVelocity(ItemDropVelocity.ItemDropVelocity());

                }

                ExperienceOrb xpDrop = stray.getWorld().spawn(stray.getLocation(), ExperienceOrb.class);
                xpDrop.setExperience(5);

            }

            if (dropChance > dropRandomizer) {

                if (boneStack.getAmount() != 0) {

                    stray.getWorld().dropItem(stray.getLocation(), boneStack).setVelocity(ItemDropVelocity.ItemDropVelocity());

                }

                if (arrowStack.getAmount() != 0) {

                    stray.getWorld().dropItem(stray.getLocation(), arrowStack).setVelocity(ItemDropVelocity.ItemDropVelocity());

                }

                //lowered chance to drop from 9% to 3% to account for it never dropping damaged bows
                if (random.nextDouble() < 0.03) {

                    stray.getWorld().dropItem(stray.getLocation(), bowStack).setVelocity(ItemDropVelocity.ItemDropVelocity());

                }

                if (random.nextDouble() < 0.5) {

                    stray.getWorld().dropItem(stray.getLocation(), tippedArrowStack).setVelocity(ItemDropVelocity.ItemDropVelocity());

                }

                ExperienceOrb xpDrop = stray.getWorld().spawn(stray.getLocation(), ExperienceOrb.class);
                xpDrop.setExperience(5);

            }

        }

    }

}
