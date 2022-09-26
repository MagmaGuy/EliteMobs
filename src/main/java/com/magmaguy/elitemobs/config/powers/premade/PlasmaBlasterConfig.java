package com.magmaguy.elitemobs.config.powers.premade;

import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import com.magmaguy.elitemobs.powers.PlasmaBlaster;

public class PlasmaBlasterConfig extends PowersConfigFields {
    public PlasmaBlasterConfig() {
        super("plasma_blaster",
                true,
                null,
                PlasmaBlaster.class,
                PowerType.MAJOR_ENDERMAN);
    }
}
