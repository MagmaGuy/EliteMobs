package com.magmaguy.elitemobs.config.powers.premade;

import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import com.magmaguy.elitemobs.powers.FlamePyre;

public class FlamePyreConfig extends PowersConfigFields {
    public FlamePyreConfig() {
        super("flame_pyre",
                true,
                null,
                FlamePyre.class,
                PowerType.MAJOR_BLAZE);
    }
}
