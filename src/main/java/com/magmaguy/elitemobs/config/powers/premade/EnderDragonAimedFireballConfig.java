package com.magmaguy.elitemobs.config.powers.premade;

import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import com.magmaguy.elitemobs.powers.EnderDragonAimedFireball;

public class EnderDragonAimedFireballConfig extends PowersConfigFields {
    public EnderDragonAimedFireballConfig() {
        super("ender_dragon_aimed_fireball",
                true,
                null,
                30,
                5,
                EnderDragonAimedFireball.class,
                PowerType.UNIQUE);
    }
}
