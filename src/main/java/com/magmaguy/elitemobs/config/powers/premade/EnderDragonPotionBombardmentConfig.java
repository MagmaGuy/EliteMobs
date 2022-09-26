package com.magmaguy.elitemobs.config.powers.premade;

import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import com.magmaguy.elitemobs.powers.EnderDragonPotionBombardment;

public class EnderDragonPotionBombardmentConfig extends PowersConfigFields {
    public EnderDragonPotionBombardmentConfig() {
        super("ender_dragon_potion_bombardment",
                true,
                null,
                30,
                5,
                EnderDragonPotionBombardment.class,
                PowerType.UNIQUE);
    }
}
