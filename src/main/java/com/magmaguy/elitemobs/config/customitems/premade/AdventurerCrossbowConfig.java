package com.magmaguy.elitemobs.config.customitems.premade;

import com.magmaguy.elitemobs.config.customitems.AdventurerRewardItemConfig;
import org.bukkit.Material;

import java.util.List;

public final class AdventurerCrossbowConfig extends AdventurerRewardItemConfig {
    public AdventurerCrossbowConfig() {
        super("Crossbow", Material.CROSSBOW, List.of("POWER,3", "UNBREAKING,2", "QUICK_CHARGE,2", "PIERCING,1"));
    }
}
