package com.magmaguy.elitemobs.config.customitems.premade;

import com.magmaguy.elitemobs.config.customitems.AdventurerRewardItemConfig;
import org.bukkit.Material;

import java.util.List;

public final class AdventurerChestplateConfig extends AdventurerRewardItemConfig {
    public AdventurerChestplateConfig() {
        super("Chestplate", Material.DIAMOND_CHESTPLATE, List.of("SHARPNESS,4", "PROTECTION,2", "UNBREAKING,2"));
    }
}
