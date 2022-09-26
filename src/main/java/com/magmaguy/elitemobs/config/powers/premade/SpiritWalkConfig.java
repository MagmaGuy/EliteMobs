package com.magmaguy.elitemobs.config.powers.premade;

import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import com.magmaguy.elitemobs.powers.SpiritWalk;

public class SpiritWalkConfig extends PowersConfigFields {
    public SpiritWalkConfig() {
        super("spirit_walk",
                true,
                null,
                SpiritWalk.class,
                PowerType.UNIQUE);
    }
}
