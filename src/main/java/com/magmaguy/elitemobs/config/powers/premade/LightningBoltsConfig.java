package com.magmaguy.elitemobs.config.powers.premade;

import com.magmaguy.elitemobs.config.powers.PowersConfigFields;

public class LightningBoltsConfig extends PowersConfigFields {
    public LightningBoltsConfig(){
        super("lightning_bolts",
                true,
                "Lightning Bolt",
                null);
        super.getAdditionalConfigOptions().put("delayBetweenStrikes", 40);
    }
}
