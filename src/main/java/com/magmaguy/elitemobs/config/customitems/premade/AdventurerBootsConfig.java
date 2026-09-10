package com.magmaguy.elitemobs.config.customitems.premade;

import com.magmaguy.elitemobs.config.customitems.AdventurerRewardItemConfig;
import org.bukkit.Material;

import java.util.List;

public final class AdventurerBootsConfig extends AdventurerRewardItemConfig {
    public AdventurerBootsConfig() {
        super("Boots", Material.DIAMOND_BOOTS, List.of("SHARPNESS,4", "PROTECTION,2", "UNBREAKING,2", "FEATHER_FALLING,2"));
    }
}
