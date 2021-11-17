package com.magmaguy.elitemobs.items;

import com.magmaguy.elitemobs.api.EliteMobDeathEvent;
import com.magmaguy.elitemobs.config.ItemSettingsConfig;
import org.bukkit.Material;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by MagmaGuy on 04/06/2017.
 */
public class DefaultDropsHandler implements Listener {

    private final List<ItemStack> wornItems = new ArrayList<>();

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDeath(EliteMobDeathEvent event) {

        if (!(event.getEntity() instanceof LivingEntity)) return;
        if (!event.getEliteEntity().isVanillaLoot()) return;
        if (event.getEntityDeathEvent() == null) return;

        List<ItemStack> droppedItems = event.getEntityDeathEvent().getDrops();
        int mobLevel = event.getEliteEntity().getLevel();

        if (mobLevel > ItemSettingsConfig.getMaxLevelForDefaultLootMultiplier())
            mobLevel = ItemSettingsConfig.getMaxLevelForDefaultLootMultiplier();

        inventoryItemsConstructor((LivingEntity) event.getEntity());

        if (ItemSettingsConfig.getDefaultLootMultiplier() != 0) {
            for (ItemStack itemStack : droppedItems) {

                if (itemStack == null) continue;
                if (itemStack.getType().equals(Material.AIR)) continue;
                boolean itemIsWorn = false;

                for (ItemStack wornItem : wornItems)
                    if (wornItem.isSimilar(itemStack))
                        itemIsWorn = true;

                if (!itemIsWorn)
                    for (int i = 0; i < mobLevel * 0.1 * ItemSettingsConfig.getDefaultLootMultiplier(); i++)
                        event.getEntity().getLocation().getWorld().dropItem(event.getEntity().getLocation(), itemStack);

            }
        }

        mobLevel = (int) (event.getEliteEntity().getLevel() * ItemSettingsConfig.getDefaultExperienceMultiplier());

        int droppedXP = (int) (event.getEntityDeathEvent().getDroppedExp() + event.getEntityDeathEvent().getDroppedExp() * 0.1 * mobLevel);
        event.getEntityDeathEvent().setDroppedExp(0);
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
