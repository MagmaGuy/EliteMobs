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
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by MagmaGuy on 04/06/2017.
 */
public class DropsHandler implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onDeath (EntityDeathEvent event){

        if (event.getEntity().hasMetadata(MetadataHandler.ELITE_MOB_MD)){

            if (!event.getEntity().hasMetadata(MetadataHandler.NATURAL_MOB_MD) &&
                    !ConfigValues.defaultConfig.getBoolean("Drop multiplied default loot from aggressive elite mobs spawned in spawners")) {

                return;

            }

            List<ItemStack> droppedItems = event.getDrops();
            int mobLevel = (int) (event.getEntity().getMetadata(MetadataHandler.ELITE_MOB_MD).get(0).asInt() *
                    ConfigValues.defaultConfig.getDouble("Aggressive EliteMob default loot multiplier"));

            inventoryItemsConstructor(event.getEntity());

            for (ItemStack itemStack : droppedItems) {

                boolean itemIsWorn = false;

                for (ItemStack wornItem : wornItems) {

                    if (wornItem.isSimilar(itemStack)) {

                        itemIsWorn = true;

                    }

                }

                if (!itemIsWorn) {

                    for (int i = 0; i < mobLevel; i++) {

                        event.getEntity().getLocation().getWorld().dropItem(event.getEntity().getLocation(), itemStack);

                    }

                }

            }

            int droppedXP = event.getDroppedExp() * mobLevel;
            event.setDroppedExp(droppedXP);

        }

    }

    private List<ItemStack> wornItems = new ArrayList<>();

    private List<ItemStack> inventoryItemsConstructor(LivingEntity entity) {

        EntityEquipment equipment = entity.getEquipment();

        if (equipment.getItemInMainHand() != null && !equipment.getItemInMainHand().getType().equals(Material.AIR)) {

            wornItems.add(equipment.getItemInMainHand());

        }

        if (equipment.getHelmet() != null) {

            wornItems.add(equipment.getHelmet());
        }

        if (equipment.getChestplate() != null) {

            wornItems.add(equipment.getChestplate());

        }

        if (equipment.getLeggings() != null) {

            wornItems.add(equipment.getLeggings());

        }

        if (equipment.getBoots() != null) {

            wornItems.add(equipment.getBoots());

        }

        return wornItems;

    }

}
