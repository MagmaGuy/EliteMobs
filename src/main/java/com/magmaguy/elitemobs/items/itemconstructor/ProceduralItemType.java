package com.magmaguy.elitemobs.items.itemconstructor;

import com.magmaguy.elitemobs.config.ProceduralItemGenerationSettingsConfig;
import com.magmaguy.elitemobs.items.ItemDurability;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.WeaponIdentityResolver;
import com.magmaguy.freeminecraftmodels.api.magic.MagicWeaponAPI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

/** Preserves weapon identity when several item types share a vanilla material. */
public record ProceduralItemType(Material material, SkillType magicSkill) {
    public static final ProceduralItemType STAFF = new ProceduralItemType(
            Objects.requireNonNullElse(Material.getMaterial("WOODEN_SPEAR"), Material.STICK), SkillType.STAVES);
    public static final ProceduralItemType WAND = new ProceduralItemType(Material.BLAZE_ROD, SkillType.WANDS);

    public ProceduralItemType {
        Objects.requireNonNull(material);
        if (magicSkill != null && magicSkill != SkillType.STAVES && magicSkill != SkillType.WANDS)
            throw new IllegalArgumentException("Unsupported magic weapon skill: " + magicSkill);
    }

    public static ProceduralItemType vanilla(Material material) {
        return new ProceduralItemType(material, null);
    }

    public SkillType skill() {
        return magicSkill != null ? magicSkill : SkillType.fromMaterialIncludingArmor(material);
    }

    public String fmmItemId() {
        if (magicSkill == null) return null;
        return magicSkill == SkillType.STAVES ? MagicWeaponAPI.DEFAULT_STAFF_ID : MagicWeaponAPI.DEFAULT_WAND_ID;
    }

    /** Evaluated at selection time so FMM startup, reload and shutdown cannot leave a stale pool. */
    public boolean isAvailable() {
        if (magicSkill == null) return true;
        if (magicSkill == SkillType.STAVES && !ProceduralItemGenerationSettingsConfig.isStavesEnabled()) return false;
        if (magicSkill == SkillType.WANDS && !ProceduralItemGenerationSettingsConfig.isWandsEnabled()) return false;
        if (!Bukkit.getPluginManager().isPluginEnabled("FreeMinecraftModels")) return false;
        try {
            return MagicWeaponAPI.capabilityVersion() >= 4 && MagicWeaponAPI.isOperational()
                    && MagicWeaponAPI.isBuiltInWeapon(fmmItemId());
        } catch (LinkageError incompatibleFmm) {
            return false;
        }
    }

    /** Never return an inert material as a generated magic weapon if FMM cannot initialize it. */
    public boolean applyMagicData(ItemStack item) {
        if (magicSkill == null) return true;
        if (!isAvailable()) return false;
        try {
            if (!MagicWeaponAPI.applyBuiltInWeaponData(item, fmmItemId())) return false;
        } catch (LinkageError incompatibleFmm) {
            return false;
        }
        WeaponIdentityResolver.stamp(item, magicSkill);
        ItemDurability.prepareMagicWeapon(item);
        return true;
    }
}
