package com.magmaguy.elitemobs.config.powers.premade;

import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import com.magmaguy.elitemobs.powers.EnderDragonEmpoweredLightning;

public class EnderDragonEmpoweredLightningConfig extends PowersConfigFields {
    public EnderDragonEmpoweredLightningConfig() {
        super("ender_dragon_empowered_lightning",
                true,
                null,
                120,
                30,
                EnderDragonEmpoweredLightning.class,
                PowerType.UNIQUE);
    }
}
