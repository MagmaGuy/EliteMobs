package com.magmaguy.elitemobs.config.powers.premade;

import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import org.bukkit.Material;

public class GroundPoundConfig extends PowersConfigFields {
    public GroundPoundConfig() {
        super("ground_pound",
                true,
                "Ground Pound",
                Material.DIRT.toString());
    }
}
