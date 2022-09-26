package com.magmaguy.elitemobs.config.powers.premade;

import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import com.magmaguy.elitemobs.powers.EnderDragonShockwave;

public class EnderDragonShockwaveConfig extends PowersConfigFields {
    public EnderDragonShockwaveConfig() {
        super("ender_dragon_shockwave",
                true,
                null,
                60,
                30,
                EnderDragonShockwave.class,
                PowerType.UNIQUE);
    }
}
