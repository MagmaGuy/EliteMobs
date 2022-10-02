package com.magmaguy.elitemobs.config.powers.premade;

import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import com.magmaguy.elitemobs.powers.GoldShotgun;

public class GoldShotgunConfig extends PowersConfigFields {
    public GoldShotgunConfig() {
        super("gold_shotgun",
                true,
                null,
                20*20,
                20*7,
                GoldShotgun.class,
                PowerType.UNIQUE);
    }
}