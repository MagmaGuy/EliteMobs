package com.magmaguy.elitemobs.items;

import com.magmaguy.elitemobs.items.customitems.CustomItem;
import com.magmaguy.elitemobs.skills.WeaponIdentityResolver;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/** Distribution and provenance depend on the item, not merely on party membership. */
public final class LootItemPolicy {
    private LootItemPolicy() { }

    public static boolean isEquipment(ItemStack item) {
        return item != null && (WeaponIdentityResolver.progressionSkillIncludingArmor(item) != null
                || item.getType() == Material.SHIELD);
    }

    public static boolean isEquipment(CustomItem item) {
        if (item == null) return false;
        var config = item.getCustomItemsConfigFields();
        return config.getWeaponType() != null || isEquipment(new ItemStack(config.getMaterial()));
    }

    public static boolean keepsMobProvenance(ItemStack item) {
        return isEquipment(item) || item.getMaxStackSize() == 1;
    }

    public static boolean shouldSoulbind(ItemStack item) {
        String id = ItemTagger.getCustomItemId(item);
        CustomItem custom = id == null ? null : CustomItem.getCustomItem(id);
        return custom == null || custom.getCustomItemsConfigFields().isSoulbound();
    }
}
