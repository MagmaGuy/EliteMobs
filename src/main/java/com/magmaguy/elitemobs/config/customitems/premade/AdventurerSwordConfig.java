package com.magmaguy.elitemobs.config.customitems.premade;

import com.magmaguy.elitemobs.config.customitems.AdventurerRewardItemConfig;
import org.bukkit.Material;

import java.util.List;

public final class AdventurerSwordConfig extends AdventurerRewardItemConfig {
    public AdventurerSwordConfig() {
        super("Sword", Material.DIAMOND_SWORD, List.of("SHARPNESS,4", "UNBREAKING,2", "FIRE_ASPECT,1"));
    }
}
