package com.magmaguy.elitemobs.config.powers.premade;

import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import com.magmaguy.elitemobs.powers.Flamethrower;

public class FlamethrowerConfig extends PowersConfigFields {
    public FlamethrowerConfig() {
        super("flamethrower",
                true,
                null,
                Flamethrower.class,
                PowerType.UNIQUE);
    }
}
