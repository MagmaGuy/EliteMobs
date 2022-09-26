package com.magmaguy.elitemobs.config.powers.premade;

import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import com.magmaguy.elitemobs.powers.FrostCone;

public class FrostConeConfig extends PowersConfigFields {
    public FrostConeConfig() {
        super("frost_cone",
                true,
                null,
                FrostCone.class,
                PowerType.UNIQUE
        );
    }
}
