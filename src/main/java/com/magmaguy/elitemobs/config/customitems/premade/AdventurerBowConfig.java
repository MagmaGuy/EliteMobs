package com.magmaguy.elitemobs.config.customitems.premade;

import com.magmaguy.elitemobs.config.customitems.AdventurerRewardItemConfig;
import org.bukkit.Material;

import java.util.List;

public final class AdventurerBowConfig extends AdventurerRewardItemConfig {
    public AdventurerBowConfig() {
        super("Bow", Material.BOW, List.of("POWER,3", "UNBREAKING,1", "FLAME,1", "INFINITY,1"));
    }
}
