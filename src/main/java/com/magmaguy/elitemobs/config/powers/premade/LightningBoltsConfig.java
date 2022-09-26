package com.magmaguy.elitemobs.config.powers.premade;

import com.magmaguy.elitemobs.config.ConfigurationEngine;
import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import com.magmaguy.elitemobs.powers.LightningBolts;

public class LightningBoltsConfig extends PowersConfigFields {
    public static int delayBetweenStrikes;

    public LightningBoltsConfig() {
        super("lightning_bolts",
                true,
                null,
                LightningBolts.class,
                PowerType.OFFENSIVE);
    }

    @Override
    public void processAdditionalFields() {
        delayBetweenStrikes = ConfigurationEngine.setInt(fileConfiguration, "delayBetweenStrikes", 15);
    }
}
