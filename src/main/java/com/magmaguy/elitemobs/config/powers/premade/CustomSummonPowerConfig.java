package com.magmaguy.elitemobs.config.powers.premade;

import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import com.magmaguy.elitemobs.powers.meta.CustomSummonPower;
import org.bukkit.Material;

public class CustomSummonPowerConfig extends PowersConfigFields {
    public CustomSummonPowerConfig() {
        super("custom_summon",
                true,
                Material.AIR.toString(),
                CustomSummonPower.class,
                PowerType.UNIQUE);
    }
}
