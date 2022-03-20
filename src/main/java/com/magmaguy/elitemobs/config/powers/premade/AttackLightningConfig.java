package com.magmaguy.elitemobs.config.powers.premade;

import com.magmaguy.elitemobs.config.ConfigurationEngine;
import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import org.bukkit.Material;

public class AttackLightningConfig extends PowersConfigFields {
    public static int delayBetweenStrikes;

    public AttackLightningConfig() {
        super("attack_lightning",
                true,
                "Thunderous",
                Material.HORN_CORAL.toString());
    }

    @Override
    public void processAdditionalFields() {
        delayBetweenStrikes = ConfigurationEngine.setInt(fileConfiguration, "delayBetweenStrikes", 15);
    }
}