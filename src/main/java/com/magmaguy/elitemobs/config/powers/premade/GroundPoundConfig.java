package com.magmaguy.elitemobs.config.powers.premade;

import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import com.magmaguy.elitemobs.powers.GroundPound;
import org.bukkit.Material;

public class GroundPoundConfig extends PowersConfigFields {
    public GroundPoundConfig() {
        super("ground_pound",
                true,
                Material.DIRT.toString(),
                GroundPound.class,
                PowerType.MISCELLANEOUS);
    }
}
