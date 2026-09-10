package com.magmaguy.elitemobs.items;

import com.magmaguy.elitemobs.items.itemconstructor.ProceduralItemType;
import com.magmaguy.elitemobs.skills.SkillType;
import org.bukkit.Material;

/** Loot categories are equipment roles and slots, not additional player skills. */
public enum ClassLootFamily {
    SWORDS(SkillType.SWORDS, "Sword", "DIAMOND_SWORD"),
    AXES(SkillType.AXES, "Axe", "DIAMOND_AXE"),
    BOWS(SkillType.BOWS, "Bow", "BOW"),
    CROSSBOWS(SkillType.CROSSBOWS, "Crossbow", "CROSSBOW"),
    TRIDENTS(SkillType.TRIDENTS, "Trident", "TRIDENT"),
    HOES(SkillType.HOES, "Scythe", "DIAMOND_HOE"),
    MACES(SkillType.MACES, "Mace", "MACE"),
    SPEARS(SkillType.SPEARS, "Spear", "DIAMOND_SPEAR"),
    STAVES(SkillType.STAVES, "Staff", "WOODEN_SPEAR"),
    WANDS(SkillType.WANDS, "Wand", "BLAZE_ROD"),
    DPS_HELMETS(null, "DPS Helmet", "DIAMOND_HELMET"),
    DPS_CHESTPLATES(null, "DPS Chestplate", "DIAMOND_CHESTPLATE"),
    DPS_LEGGINGS(null, "DPS Leggings", "DIAMOND_LEGGINGS"),
    DPS_BOOTS(null, "DPS Boots", "DIAMOND_BOOTS"),
    TANK_HELMETS(null, "Tank Helmet", "DIAMOND_HELMET"),
    TANK_CHESTPLATES(null, "Tank Chestplate", "DIAMOND_CHESTPLATE"),
    TANK_LEGGINGS(null, "Tank Leggings", "DIAMOND_LEGGINGS"),
    TANK_BOOTS(null, "Tank Boots", "DIAMOND_BOOTS"),
    SHIELDS(null, "Shield", "SHIELD");

    private final SkillType skill;
    private final String label;
    private final String materialName;

    ClassLootFamily(SkillType skill, String label, String materialName) {
        this.skill = skill;
        this.label = label;
        this.materialName = materialName;
    }

    public SkillType skill() { return skill; }

    /** Weapons use their existing identity; armor needs an explicit reward role. */
    public static ClassLootFamily resolve(Material material, SkillType weaponType, String explicitFamily) {
        ClassLootFamily inferred = null;
        if (weaponType == SkillType.STAVES) inferred = STAVES;
        else if (weaponType == SkillType.WANDS) inferred = WANDS;
        else if (material != null) {
            SkillType skill = SkillType.fromMaterialIncludingArmor(material);
            for (ClassLootFamily family : values())
                if (family.isWeapon() && family.skill() == skill) { inferred = family; break; }
            if (material == Material.SHIELD) inferred = SHIELDS;
        }
        if (explicitFamily == null || explicitFamily.isBlank()) {
            if (inferred != null) return inferred;
            throw new IllegalArgumentException("classLootFamily is required for armor; use DPS_<slot> or TANK_<slot>");
        }
        ClassLootFamily explicit = valueOf(explicitFamily.toUpperCase(java.util.Locale.ROOT));
        if (inferred != null && inferred == explicit) return explicit;
        if (inferred == null && material != null && explicit.category() == Category.ARMOR
                && material.name().endsWith("_" + explicit.slot())) return explicit;
        throw new IllegalArgumentException("classLootFamily " + explicit + " does not match material/weaponType");
    }
    public enum Category { WEAPONS, ARMOR, SHIELDS }
    public Category category() { return isWeapon() ? Category.WEAPONS : this == SHIELDS ? Category.SHIELDS : Category.ARMOR; }
    public String label() { return label; }
    public boolean isWeapon() { return skill != null; }
    public String slot() { return materialName.substring(materialName.lastIndexOf('_') + 1); }
    public Material material() {
        return magicType() == null ? Material.getMaterial(materialName) : magicType().material();
    }
    public ProceduralItemType magicType() {
        if (this == STAVES) return ProceduralItemType.STAFF;
        if (this == WANDS) return ProceduralItemType.WAND;
        return null;
    }
}
