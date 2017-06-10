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

package com.magmaguy.elitemobs.mobcustomizer;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.ConfigValues;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * Created by MagmaGuy on 04/06/2017.
 */
public class DropsHandler implements Listener {

    @EventHandler
    public void onDeath (EntityDeathEvent event){

        if (event.getEntity().hasMetadata(MetadataHandler.ELITE_MOB_MD)){

            if (!event.getEntity().hasMetadata(MetadataHandler.NATURAL_MOB_MD) &&
                    !ConfigValues.defaultConfig.getBoolean("Drop multiplied default loot from aggressive elite mobs spawned in spawners")) {

                return;

            }

            List<ItemStack> droppedItems = event.getDrops();
            int mobLevel = (int) (event.getEntity().getMetadata(MetadataHandler.ELITE_MOB_MD).get(0).asInt() *
                    ConfigValues.defaultConfig.getDouble("Aggressive EliteMob default loot multiplier"));

            for (ItemStack itemStack : droppedItems) {

                for (int i = 0; i < mobLevel; i++) {

                    event.getEntity().getLocation().getWorld().dropItem(event.getEntity().getLocation(), itemStack);

                }

            }

            int droppedXP = event.getDroppedExp();
            event.setDroppedExp(droppedXP * mobLevel);

        }

    }

}
