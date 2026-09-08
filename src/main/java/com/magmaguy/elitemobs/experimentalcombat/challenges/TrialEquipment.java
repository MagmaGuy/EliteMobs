package com.magmaguy.elitemobs.experimentalcombat.challenges;

import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.WeaponIdentityResolver;
import com.magmaguy.freeminecraftmodels.api.magic.MagicWeaponAPI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/** Trial weapons use the same identity and resource-pack presentation as player weapons. */
final class TrialEquipment {
    enum Magic { WAND, STAFF }
    private TrialEquipment() {}

    static ItemStack magic(Magic kind) {
        if (!Bukkit.getPluginManager().isPluginEnabled("FreeMinecraftModels") || !MagicWeaponAPI.isOperational())
            throw new IllegalStateException("This trial requires operational FreeMinecraftModels magic weapons.");
        boolean wand = kind == Magic.WAND;
        ItemStack item = new ItemStack(wand ? Material.BLAZE_ROD : Material.WOODEN_SPEAR);
        WeaponIdentityResolver.stamp(item, wand ? SkillType.WANDS : SkillType.STAVES);
        if (!MagicWeaponAPI.applyBuiltInWeaponData(item, wand ? MagicWeaponAPI.DEFAULT_WAND_ID : MagicWeaponAPI.DEFAULT_STAFF_ID))
            throw new IllegalStateException("Could not prepare the trial's " + kind + " presentation.");
        var meta = item.getItemMeta();
        meta.setUnbreakable(true);
        item.setItemMeta(meta);
        return item;
    }
}
