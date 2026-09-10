package com.magmaguy.elitemobs.config.customitems.premade;

import com.magmaguy.elitemobs.config.customitems.AdventurerRewardItemConfig;
import org.bukkit.Material;

import java.util.List;

public final class AdventurerScytheConfig extends AdventurerRewardItemConfig {
    public AdventurerScytheConfig() {
        super("Scythe", Material.DIAMOND_HOE, List.of("SHARPNESS,3", "UNBREAKING,3", "FIRE_ASPECT,1"));
    }
}
