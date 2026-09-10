package com.magmaguy.elitemobs.config.customitems.premade;

import com.magmaguy.elitemobs.config.customitems.AdventurerRewardItemConfig;
import com.magmaguy.elitemobs.skills.SkillType;
import org.bukkit.Material;

import java.util.List;

public final class AdventurerStaffConfig extends AdventurerRewardItemConfig {
    public AdventurerStaffConfig() {
        super("Staff", Material.getMaterial("WOODEN_SPEAR"), List.of("POWER,3", "UNBREAKING,2", "BLAST_RADIUS,2", "IGNITION,2"));
        setWeaponType(SkillType.STAVES);
        setFmmItemModel("fmm_default_arcane_staff");
    }
}
