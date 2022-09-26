package com.magmaguy.elitemobs.config.powers.premade;

import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import com.magmaguy.elitemobs.powers.HyperLoot;

public class HyperLootConfig extends PowersConfigFields {
    public HyperLootConfig() {
        super("hyper_loot",
                true,
                null,
                HyperLoot.class,
                PowerType.UNIQUE);
    }
}
