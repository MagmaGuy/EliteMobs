package com.magmaguy.elitemobs.config.powers.premade;

import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import com.magmaguy.elitemobs.powers.MeteorShower;

public class MeteorShowerConfig extends PowersConfigFields {
    public MeteorShowerConfig() {
        super("meteor_shower",
                true,
                null,
                MeteorShower.class,
                PowerType.UNIQUE);
    }
}
