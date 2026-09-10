package com.magmaguy.elitemobs.config.customitems.premade;

import com.magmaguy.elitemobs.config.customitems.AdventurerRewardItemConfig;
import org.bukkit.Material;

import java.util.List;

public final class AdventurerHelmetConfig extends AdventurerRewardItemConfig {
    public AdventurerHelmetConfig() {
        super("Helmet", Material.DIAMOND_HELMET, List.of("SHARPNESS,4", "PROTECTION,2", "UNBREAKING,2"));
    }
}
