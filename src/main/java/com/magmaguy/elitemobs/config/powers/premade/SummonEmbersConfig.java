package com.magmaguy.elitemobs.config.powers.premade;

import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import com.magmaguy.elitemobs.powers.SummonEmbers;

public class SummonEmbersConfig extends PowersConfigFields {
    public SummonEmbersConfig() {
        super("summon_embers",
                true,
                null,
                SummonEmbers.class,
                PowerType.UNIQUE);
    }

}
