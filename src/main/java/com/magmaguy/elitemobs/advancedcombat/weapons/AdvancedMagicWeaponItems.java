package com.magmaguy.elitemobs.advancedcombat.weapons;

import com.magmaguy.elitemobs.config.customitems.CustomItemsConfigFields;
import com.magmaguy.elitemobs.items.customitems.CustomItem;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.WeaponIdentityResolver;
import com.magmaguy.freeminecraftmodels.api.magic.MagicWeaponAPI;
import com.magmaguy.magmacore.util.Logger;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/** Registers code-owned prototype magic weapons with the normal scalable loot pipeline. */
public final class AdvancedMagicWeaponItems {
    public static final String STAFF_ITEM_ID = "advanced_arcane_staff";
    public static final String WAND_ITEM_ID = "advanced_arcane_wand";
    public static final String STAFF_FMM_ITEM_ID = MagicWeaponAPI.DEFAULT_STAFF_ID;
    public static final String WAND_FMM_ITEM_ID = MagicWeaponAPI.DEFAULT_WAND_ID;
    private static final List<String> DEBUG_LOADOUT_IDS = List.of(STAFF_ITEM_ID, WAND_ITEM_ID);

    private AdvancedMagicWeaponItems() {
    }

    public static void register() {
        if (CustomItem.getCustomItem(STAFF_ITEM_ID) == null) new CustomItem(staff());
        if (CustomItem.getCustomItem(WAND_ITEM_ID) == null) new CustomItem(wand());
    }

    /**
     * Builds the prototype magic weapons through the same scalable custom-item path used by
     * ordinary EliteMobs loot. This preserves their stable item id, explicit progression skill,
     * requested item level, generated lore and FMM presentation data in the debug loadout.
     */
    public static List<ItemStack> generateDebugLoadout(int itemLevel, Player player) {
        List<ItemStack> items = new ArrayList<>(DEBUG_LOADOUT_IDS.size());
        for (String itemId : DEBUG_LOADOUT_IDS) {
            CustomItem customItem = CustomItem.getCustomItem(itemId);
            if (customItem == null) {
                Logger.warn("[Alpha] Advanced Combat System debug item '" + itemId + "' is not registered.");
                continue;
            }
            ItemStack itemStack = customItem.generateItemStackExact(itemLevel, player, null);
            if (itemStack == null) continue;
            SkillType expectedSkill = STAFF_ITEM_ID.equals(itemId) ? SkillType.STAVES : SkillType.WANDS;
            boolean validIdentity = WeaponIdentityResolver.progressionSkill(itemStack) == expectedSkill;
            if (!validIdentity) {
                Logger.warn("[Alpha] Advanced Combat System debug item '" + itemId
                        + "' lost its registered magic-weapon identity and was not granted.");
                continue;
            }
            items.add(itemStack);
        }
        return List.copyOf(items);
    }

    static List<String> debugLoadoutIds() {
        return DEBUG_LOADOUT_IDS;
    }

    // Staves use a spear item (1.21.11+) - use STICK as fallback for older versions
    private static Material staffMaterial() {
        try {
            return Material.WOODEN_SPEAR;
        } catch (NoSuchFieldError e) {
            return Material.STICK;
        }
    }

    private static CustomItemsConfigFields staff() {
        CustomItemsConfigFields fields = new CustomItemsConfigFields(
                STAFF_ITEM_ID + ".yml",
                true,
                staffMaterial(),
                "<gradient:#76EFC4:#00C96E:#0A7A44>Advanced Staff of Wonders</gradient>",
                List.of(
                        "&7Left-click for a weak close-range strike.",
                        "&7Right-click to launch a slow area fireball.",
                        "&8Scales with Staves and item level."));
        fields.setWeaponType(SkillType.STAVES);
        fields.setProceduralEnchantments(true);
        fields.setFmmItemModel(STAFF_FMM_ITEM_ID);
        fields.setScalability(CustomItem.Scalability.SCALABLE);
        fields.setItemType(CustomItem.ItemType.CUSTOM);
        fields.setLevel(1);
        fields.setShowSource(false);
        return fields;
    }

    private static CustomItemsConfigFields wand() {
        CustomItemsConfigFields fields = new CustomItemsConfigFields(
                WAND_ITEM_ID + ".yml",
                true,
                Material.BLAZE_ROD,
                "<gradient:#D9B8FF:#8A5CFF>Advanced Arcane Wand</gradient>",
                List.of(
                        "&7Left-click to fire an aim-assisted magic missile.",
                        "&7Missiles arc upward and sideways toward their target.",
                        "&8Scales with Wands and item level."));
        fields.setWeaponType(SkillType.WANDS);
        fields.setProceduralEnchantments(true);
        fields.setFmmItemModel(WAND_FMM_ITEM_ID);
        fields.setScalability(CustomItem.Scalability.SCALABLE);
        fields.setItemType(CustomItem.ItemType.CUSTOM);
        fields.setLevel(1);
        fields.setShowSource(false);
        return fields;
    }
}
