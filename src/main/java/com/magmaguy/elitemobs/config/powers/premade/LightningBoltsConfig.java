package com.magmaguy.elitemobs.config.powers.premade;

import com.magmaguy.elitemobs.config.ConfigurationEngine;
import com.magmaguy.elitemobs.config.powers.PowersConfigFields;

public class LightningBoltsConfig extends PowersConfigFields {
    public static int delayBetweenStrikes;

    public LightningBoltsConfig() {
        super("lightning_bolts",
                true,
                "Lightning Bolt",
                null);
    }

    @Override
    public void processAdditionalFields() {
        delayBetweenStrikes = ConfigurationEngine.setInt(fileConfiguration, "delayBetweenStrikes", 15);
    }
}
