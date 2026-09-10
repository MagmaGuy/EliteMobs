package com.magmaguy.elitemobs.config.customitems.premade;

import com.magmaguy.elitemobs.config.customitems.AdventurerRewardItemConfig;
import org.bukkit.Material;

import java.util.List;

public final class AdventurerSpearConfig extends AdventurerRewardItemConfig {
    public AdventurerSpearConfig() {
        super("Spear", Material.getMaterial("DIAMOND_SPEAR"), List.of("SHARPNESS,3", "UNBREAKING,2", "FIRE_ASPECT,1"));
    }
}
