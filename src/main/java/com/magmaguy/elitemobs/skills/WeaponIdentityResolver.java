package com.magmaguy.elitemobs.skills;

import com.magmaguy.freeminecraftmodels.api.magic.MagicWeaponAPI;
import com.magmaguy.freeminecraftmodels.api.magic.MagicWeaponKind;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * Canonical item-to-weapon boundary.
 *
 * <p>FMM owns authored item identity and weapon family. An unresolved FMM item never
 * falls through to material inference, including while its provider is unavailable.</p>
 */
public final class WeaponIdentityResolver {
    private static final NamespacedKey FMM_ITEM_ID = NamespacedKey.fromString("freeminecraftmodels:fmm_item_id");

    private WeaponIdentityResolver() {
    }

    public static WeaponResolution resolve(@Nullable ItemStack itemStack) {
        if (itemStack == null || itemStack.getType().isAir()) return new WeaponResolution.NotWeapon();
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta != null) {
            String storedValue = itemMeta.getPersistentDataContainer().get(FMM_ITEM_ID, PersistentDataType.STRING);
            if (storedValue != null) {
                NamespacedKey id = NamespacedKey.fromString("freeminecraftmodels:" + storedValue);
                MagicWeaponKind kind = null;
                boolean knownItem = false;
                if (Bukkit.getPluginManager().isPluginEnabled("FreeMinecraftModels")) {
                    try {
                        kind = MagicWeaponAPI.weaponKind(storedValue);
                        knownItem = com.magmaguy.freeminecraftmodels.api.ScriptedItemAPI.isValidItemId(storedValue);
                    }
                    catch (LinkageError incompatibleFmm) { /* The integration reports incompatible providers. */ }
                }
                if (!knownItem || id == null) return new WeaponResolution.InvalidExplicit(storedValue, id);
                if (kind != null) {
                    SkillType skill = kind == MagicWeaponKind.WAND ? SkillType.WANDS : SkillType.STAVES;
                    return new WeaponResolution.Resolved(new WeaponIdentity(id, skill));
                }
            }
        }

        SkillType inferred = SkillType.fromMaterial(itemStack.getType());
        if (inferred == null) return new WeaponResolution.NotWeapon();
        NamespacedKey vanillaId = NamespacedKey.fromString(
                "minecraft:" + inferred.name().toLowerCase(java.util.Locale.ROOT));
        return new WeaponResolution.Resolved(new WeaponIdentity(vanillaId, inferred));
    }

    public static Optional<WeaponIdentity> resolvedIdentity(@Nullable ItemStack itemStack) {
        WeaponResolution resolution = resolve(itemStack);
        return resolution instanceof WeaponResolution.Resolved resolved
                ? Optional.of(resolved.identity())
                : Optional.empty();
    }

    @Nullable
    public static SkillType progressionSkill(@Nullable ItemStack itemStack) {
        return resolvedIdentity(itemStack).map(WeaponIdentity::progressionSkill).orElse(null);
    }

    @Nullable
    public static SkillType progressionSkillIncludingArmor(@Nullable ItemStack itemStack) {
        SkillType weapon = progressionSkill(itemStack);
        if (weapon != null) return weapon;
        if (itemStack == null) return null;
        Material material = itemStack.getType();
        String name = material.name();
        if (name.endsWith("_HELMET") || name.endsWith("_CHESTPLATE")
                || name.endsWith("_LEGGINGS") || name.endsWith("_BOOTS")
                || material == Material.ELYTRA || material == Material.TURTLE_HELMET)
            return SkillType.ARMOR;
        return null;
    }

    public static boolean isMagicWeapon(@Nullable ItemStack itemStack) {
        SkillType skill = progressionSkill(itemStack);
        return skill == SkillType.STAVES || skill == SkillType.WANDS;
    }

}
