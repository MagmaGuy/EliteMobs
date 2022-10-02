package com.magmaguy.elitemobs.config.powers.premade;

import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import com.magmaguy.elitemobs.powers.LightningBolts;

public class LightningBoltsConfig extends PowersConfigFields {
    public static int delayBetweenStrikes;

    public LightningBoltsConfig() {
        super("lightning_bolts",
                true,
                null,
                20*20,
                20*5,
                LightningBolts.class,
                PowerType.OFFENSIVE);
    }

}
