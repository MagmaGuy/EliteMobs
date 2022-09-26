package com.magmaguy.elitemobs.config.powers.premade;

import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import com.magmaguy.elitemobs.powers.FireworksBarrage;

public class FireworksBarrageConfig extends PowersConfigFields {
    public FireworksBarrageConfig() {
        super("fireworks_barrage",
                true,
                null,
                30,
                20,
                FireworksBarrage.class,
                PowerType.OFFENSIVE);
    }
}
