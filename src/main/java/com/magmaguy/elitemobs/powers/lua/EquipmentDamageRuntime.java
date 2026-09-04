package com.magmaguy.elitemobs.powers.lua;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

/** Bukkit adapter for the bounded equipment-damage policy exposed to Lua entity tables. */
final class EquipmentDamageRuntime {

    private EquipmentDamageRuntime() {
    }

    static int damage(LivingEntity target, EquipmentSlot slot, int requestedDamage) {
        EquipmentDamagePolicy.requireRequestedDamage(requestedDamage);
        if (target == null || !target.isValid() || target.isDead()) {
            return 0;
        }
        EntityEquipment equipment = target.getEquipment();
        if (equipment == null) {
            return 0;
        }
        ItemStack item = equipment.getItem(slot);
        ItemMeta itemMeta = item == null ? null : item.getItemMeta();
        boolean isDamageable = itemMeta instanceof Damageable;
        int maximumDamage = isDamageable
                ? maximumDamage(item, (Damageable) itemMeta)
                : 0;
        if (!EquipmentDamagePolicy.canDamage(
                item != null && !item.getType().isAir(),
                isDamageable,
                itemMeta != null && itemMeta.isUnbreakable(),
                maximumDamage)) {
            return 0;
        }

        Damageable damageable = (Damageable) itemMeta;
        int currentDamage = damageable.getDamage();
        int eventDamage = EquipmentDamagePolicy.requestedDamage(
                requestedDamage, currentDamage, maximumDamage);
        if (eventDamage == 0) {
            return 0;
        }

        boolean permitted = true;
        if (target instanceof Player player) {
            PlayerItemDamageEvent event = new PlayerItemDamageEvent(player, item, eventDamage);
            Bukkit.getPluginManager().callEvent(event);
            permitted = !event.isCancelled();
            eventDamage = event.getDamage();
        }
        EquipmentDamagePolicy.Result result = EquipmentDamagePolicy.applyEventDamage(
                target.isValid() && !target.isDead(),
                permitted,
                eventDamage,
                currentDamage,
                maximumDamage);
        if (result.actualDamage() == 0) {
            return 0;
        }

        if (result.broke()) {
            ItemStack brokenItem = item.clone();
            equipment.setItem(slot, new ItemStack(Material.AIR), false);
            if (target instanceof Player player) {
                Bukkit.getPluginManager().callEvent(new PlayerItemBreakEvent(player, brokenItem));
            }
        } else {
            damageable.setDamage(result.resultingDamage());
            item.setItemMeta(damageable);
            equipment.setItem(slot, item, false);
        }
        return result.actualDamage();
    }

    private static int maximumDamage(ItemStack item, Damageable damageable) {
        return damageable.hasMaxDamage()
                ? damageable.getMaxDamage()
                : item.getType().getMaxDurability();
    }
}
