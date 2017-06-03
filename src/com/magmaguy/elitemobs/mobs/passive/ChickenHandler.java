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
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Entity;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.bukkit.Material.EGG;

/**
 * Created by MagmaGuy on 19/12/2016.
 */
public class ChickenHandler implements Listener {

    public static void superEggs(Entity entity, int passiveStackAmount) {

        List<Chicken> tempChickenList = new ArrayList<>();

        if (entity instanceof Chicken && entity.hasMetadata(MetadataHandler.PASSIVE_ELITE_MOB_MD)) {

            tempChickenList.add((Chicken) entity);

        }

        if (tempChickenList.size() > 0) {

            Random random = new Random();
            int eggChance = random.nextInt(12000 / passiveStackAmount);

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
