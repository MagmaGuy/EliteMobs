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

package com.magmaguy.elitemobs.items;

import com.magmaguy.elitemobs.EntityTracker;
import com.magmaguy.elitemobs.config.ConfigValues;
import com.magmaguy.elitemobs.config.ItemsDropSettingsConfig;
import org.bukkit.Material;
import org.bukkit.entity.ExperienceOrb;
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
public class DefaultDropsHandler implements Listener {

    private List<ItemStack> wornItems = new ArrayList<>();

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDeath(EntityDeathEvent event) {

        if (!EntityTracker.isEliteMob(event.getEntity())) return;

        if (!EntityTracker.isNaturalEntity(event.getEntity()) &&
                !ConfigValues.itemsDropSettingsConfig.getBoolean(ItemsDropSettingsConfig.SPAWNER_DEFAULT_LOOT_MULTIPLIER))
            return;

        List<ItemStack> droppedItems = event.getDrops();
        int mobLevel = (int) (EntityTracker.getEliteMobEntity(event.getEntity()).getLevel() *
                ConfigValues.itemsDropSettingsConfig.getDouble(ItemsDropSettingsConfig.DEFAULT_LOOT_MULTIPLIER));

        if (mobLevel > ConfigValues.itemsDropSettingsConfig.getInt(ItemsDropSettingsConfig.MAXIMUM_LEVEL_FOR_LOOT_MULTIPLIER))
            mobLevel = ConfigValues.itemsDropSettingsConfig.getInt(ItemsDropSettingsConfig.MAXIMUM_LEVEL_FOR_LOOT_MULTIPLIER);

        inventoryItemsConstructor(event.getEntity());

        for (ItemStack itemStack : droppedItems) {

//                ItemStack can be null for some reason, probably due to other plugins
            if (itemStack == null) continue;
            boolean itemIsWorn = false;

            for (ItemStack wornItem : wornItems)
                if (wornItem.isSimilar(itemStack))
                    itemIsWorn = true;

            if (!itemIsWorn)
                for (int i = 0; i < mobLevel * 0.1; i++)
                    event.getEntity().getLocation().getWorld().dropItem(event.getEntity().getLocation(), itemStack);

        }

        mobLevel = (int) (EntityTracker.getEliteMobEntity(event.getEntity()).getLevel() *
                ConfigValues.itemsDropSettingsConfig.getDouble(ItemsDropSettingsConfig.EXPERIENCE_LOOT_MULTIPLIER));

        int droppedXP = (int) (event.getDroppedExp() + event.getDroppedExp() * 0.1 * mobLevel);
        event.setDroppedExp(0);
        event.getEntity().getWorld().spawn(event.getEntity().getLocation(), ExperienceOrb.class).setExperience(droppedXP);

    }


    private List<ItemStack> inventoryItemsConstructor(LivingEntity entity) {

        EntityEquipment equipment = entity.getEquipment();

        if (equipment.getItemInMainHand() != null && !equipment.getItemInMainHand().getType().equals(Material.AIR))
            wornItems.add(equipment.getItemInMainHand());

        if (equipment.getHelmet() != null)
            wornItems.add(equipment.getHelmet());

        if (equipment.getChestplate() != null)
            wornItems.add(equipment.getChestplate());

        if (equipment.getLeggings() != null)
            wornItems.add(equipment.getLeggings());

        if (equipment.getBoots() != null)
            wornItems.add(equipment.getBoots());

        return wornItems;

    }

}
