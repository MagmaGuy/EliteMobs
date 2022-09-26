package com.magmaguy.elitemobs.config.powers.premade;

import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import com.magmaguy.elitemobs.powers.EnderDragonArrowBombardment;

public class EnderDragonArrowBombardmentConfig extends PowersConfigFields {
    public EnderDragonArrowBombardmentConfig() {
        super("ender_dragon_arrow_bombardment",
                true,
                null,
                30,
                5,
                EnderDragonArrowBombardment.class,
                PowerType.UNIQUE);
    }
}
