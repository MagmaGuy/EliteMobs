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

//                ItemStack can be null for some reason, probably due to other plugins, same with air
            if (itemStack == null) continue;
            if (itemStack.getType().equals(Material.AIR)) continue;
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
