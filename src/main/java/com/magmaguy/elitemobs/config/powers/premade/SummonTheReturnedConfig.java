package com.magmaguy.elitemobs.config.powers.premade;

import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import com.magmaguy.elitemobs.powers.SummonTheReturned;

public class SummonTheReturnedConfig extends PowersConfigFields {
    public SummonTheReturnedConfig() {
        super("summon_the_returned",
                true,
                null,
                SummonTheReturned.class,
                PowerType.UNIQUE);
    }
}
