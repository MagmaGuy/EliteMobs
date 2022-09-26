package com.magmaguy.elitemobs.config.powers.premade;

import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import com.magmaguy.elitemobs.powers.EnderDragonEndermiteBombardment;

public class EnderDragonEndermiteBombardmentConfig extends PowersConfigFields {
    public EnderDragonEndermiteBombardmentConfig() {
        super("ender_dragon_endermite_bombardment",
                true,
                null,
                120,
                30,
                EnderDragonEndermiteBombardment.class,
                PowerType.UNIQUE);
    }
}
