package com.magmaguy.elitemobs.config.powers.premade;

import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import com.magmaguy.elitemobs.powers.GoldShotgun;

public class GoldShotgunConfig extends PowersConfigFields {
    public GoldShotgunConfig() {
        super("gold_shotgun",
                true,
                null,
                GoldShotgun.class,
                PowerType.UNIQUE);
    }
}