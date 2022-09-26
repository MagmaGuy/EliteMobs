package com.magmaguy.elitemobs.config.powers.premade;

import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import com.magmaguy.elitemobs.powers.BonusCoins;

public class BonusCoinsConfig extends PowersConfigFields {
    public BonusCoinsConfig() {
        super("bonus_coins",
                true,
                null,
                BonusCoins.class,
                PowerType.MISCELLANEOUS);
    }
}
