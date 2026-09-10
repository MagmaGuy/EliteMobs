package com.magmaguy.elitemobs.config.customitems.premade;

import com.magmaguy.elitemobs.config.customitems.AdventurerRewardItemConfig;
import org.bukkit.Material;

import java.util.List;

public final class AdventurerLeggingsConfig extends AdventurerRewardItemConfig {
    public AdventurerLeggingsConfig() {
        super("Leggings", Material.DIAMOND_LEGGINGS, List.of("SHARPNESS,4", "PROTECTION,2", "UNBREAKING,1"));
    }
}
