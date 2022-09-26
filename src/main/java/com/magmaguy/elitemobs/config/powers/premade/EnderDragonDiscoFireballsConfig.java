package com.magmaguy.elitemobs.config.powers.premade;

import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import com.magmaguy.elitemobs.powers.EnderDragonDiscoFireballs;

public class EnderDragonDiscoFireballsConfig extends PowersConfigFields {
    public EnderDragonDiscoFireballsConfig() {
        super("ender_dragon_disco_fireballs",
                true,
                null,
                120,
                30,
                EnderDragonDiscoFireballs.class,
                PowerType.UNIQUE);
    }
}
