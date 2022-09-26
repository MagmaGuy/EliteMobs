package com.magmaguy.elitemobs.config.powers.premade;

import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import com.magmaguy.elitemobs.powers.EnderDragonTornado;

public class EnderDragonTornadoConfig extends PowersConfigFields {
    public EnderDragonTornadoConfig() {
        super("ender_dragon_tornado",
                true,
                null,
                60,
                30,
                EnderDragonTornado.class,
                PowerType.UNIQUE);
    }
}
