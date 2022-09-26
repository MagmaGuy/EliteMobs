package com.magmaguy.elitemobs.config.powers.premade;

import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import com.magmaguy.elitemobs.powers.GoldExplosion;

public class GoldExplosionConfig extends PowersConfigFields {
    public GoldExplosionConfig() {
        super("gold_explosion",
                true,
                null,
                GoldExplosion.class,
                PowerType.UNIQUE);
    }
}
