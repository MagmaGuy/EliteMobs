package com.magmaguy.elitemobs.config.powers.premade;

import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import org.bukkit.Material;

public class CustomSummonPowerConfig extends PowersConfigFields {
    public CustomSummonPowerConfig() {
        super("custom_summon",
                true,
                "Custom Summon",
                Material.AIR.toString());
    }
}
