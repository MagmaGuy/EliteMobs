package com.magmaguy.elitemobs.config.powers.premade;

import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import com.magmaguy.elitemobs.powers.Thunderstorm;

public class ThunderstormConfig extends PowersConfigFields {
    public ThunderstormConfig() {
        super("thunderstorm",
                true,
                null,
                Thunderstorm.class,
                PowerType.UNIQUE);
    }
}
