package com.magmaguy.elitemobs.config.powers.premade;

import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import com.magmaguy.elitemobs.powers.DeathSlice;

public class DeathSliceConfig extends PowersConfigFields {
    public DeathSliceConfig() {
        super("death_slice",
                true,
                null,
                DeathSlice.class,
                PowerType.UNIQUE);
    }
}
