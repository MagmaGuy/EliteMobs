package com.magmaguy.elitemobs.items;

import com.magmaguy.elitemobs.skills.WeaponIdentityResolver;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;

/** Native item durability, including modeled weapons whose carrier has no durability. */
public final class ItemDurability {
    public static final int MAGIC_WEAPON_DURABILITY = 384;

    private ItemDurability() {}

    public static int maximum(ItemStack item) {
        if (item == null || item.getType().isAir()) return 0;
        if (item.getItemMeta() instanceof Damageable meta && meta.hasMaxDamage())
            return meta.getMaxDamage();
        if (WeaponIdentityResolver.isMagicWeapon(item)) return MAGIC_WEAPON_DURABILITY;
        return item.getType().getMaxDurability();
    }

    /** Retains authored durability and damage while upgrading legacy magic items in place. */
    public static void prepareMagicWeapon(ItemStack item) {
        if (!WeaponIdentityResolver.isMagicWeapon(item)
                || !(item.getItemMeta() instanceof Damageable meta)) return;
        if (!meta.hasMaxDamage()) meta.setMaxDamage(MAGIC_WEAPON_DURABILITY);
        meta.setMaxStackSize(1);
        item.setItemMeta(meta);
    }
}
