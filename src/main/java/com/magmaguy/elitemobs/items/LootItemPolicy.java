package com.magmaguy.elitemobs.items;

import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.items.itemconstructor.ItemConstructionContext;
import com.magmaguy.elitemobs.items.customitems.CustomItem;
import com.magmaguy.elitemobs.skills.WeaponIdentityResolver;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/** Distribution and provenance depend on the item, not merely on party membership. */
public final class LootItemPolicy {
    private LootItemPolicy() { }

    public static boolean isEquipment(ItemStack item) {
        return isEquipment(item, null);
    }

    private static boolean isEquipment(ItemStack item, ItemConstructionContext construction) {
        if (item == null) return false;
        var skill = construction == null ? WeaponIdentityResolver.progressionSkillIncludingArmor(item) : construction.skill(item);
        return skill != null || item.getType() == Material.SHIELD
                || SkillType.fromMaterialIncludingArmor(item.getType()) == SkillType.ARMOR;
    }

    public static boolean isEquipment(CustomItem item) {
        if (item == null) return false;
        var config = item.getCustomItemsConfigFields();
        return config.getWeaponType() != null || isEquipment(new ItemStack(config.getMaterial()));
    }

    public static boolean keepsMobProvenance(ItemStack item) {
        return keepsMobProvenance(item, null);
    }

    public static boolean keepsMobProvenance(ItemStack item, ItemConstructionContext construction) {
        return isEquipment(item, construction) || item.getMaxStackSize() == 1;
    }

    public static boolean canReceive(CustomItem item, org.bukkit.entity.Player player) {
        return item != null && item.getCustomItemsConfigFields().isEnabled()
                && (item.getPermission().isEmpty() || player != null && player.hasPermission(item.getPermission()));
    }

    public static boolean canReceive(ItemStack item, org.bukkit.entity.Player player) {
        if (item == null || item.getType().isAir() || item.getAmount() <= 0) return false;
        String id = ItemTagger.getCustomItemId(item);
        return id == null || canReceive(CustomItem.getCustomItem(id), player);
    }

    public static boolean hasRecipientRestriction(ItemStack item) {
        String id = ItemTagger.getCustomItemId(item);
        if (id == null) return false;
        CustomItem definition = CustomItem.getCustomItem(id);
        return definition == null || !definition.getCustomItemsConfigFields().isEnabled()
                || !definition.getPermission().isEmpty();
    }

    public static boolean shouldSoulbind(ItemStack item) {
        String id = ItemTagger.getCustomItemId(item);
        CustomItem custom = id == null ? null : CustomItem.getCustomItem(id);
        return custom == null || custom.getCustomItemsConfigFields().isSoulbound();
    }
}
