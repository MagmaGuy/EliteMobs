package com.magmaguy.elitemobs.config.powers.premade;

import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import com.magmaguy.elitemobs.powers.EnderDragonEnderFireballBombardment;

public class EnderDragonFireballBombardment extends PowersConfigFields {
    public EnderDragonFireballBombardment() {
        super("ender_dragon_fireball_bombardment",
                true,
                null,
                30,
                5,
                EnderDragonEnderFireballBombardment.class,
                PowerType.UNIQUE);
    }
}
