package com.magmaguy.elitemobs.config.customitems.premade;

import com.magmaguy.elitemobs.config.customitems.AdventurerRewardItemConfig;
import com.magmaguy.elitemobs.skills.SkillType;
import org.bukkit.Material;

import java.util.List;

public final class AdventurerWandConfig extends AdventurerRewardItemConfig {
    public AdventurerWandConfig() {
        super("Wand", Material.BLAZE_ROD, List.of("POWER,3", "UNBREAKING,2", "MULTICAST,2"));
        setWeaponType(SkillType.WANDS);
        setFmmItemModel("fmm_default_arcane_wand");
    }
}
