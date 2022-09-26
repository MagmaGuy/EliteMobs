package com.magmaguy.elitemobs.config.powers.premade;

import com.magmaguy.elitemobs.config.ConfigurationEngine;
import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import com.magmaguy.elitemobs.powers.AttackLightning;
import org.bukkit.Material;

public class AttackLightningConfig extends PowersConfigFields {
    public static int delayBetweenStrikes;

    public AttackLightningConfig() {
        super("attack_lightning",
                true,
                Material.HORN_CORAL.toString(),
                AttackLightning.class,
                PowerType.OFFENSIVE);
    }

    @Override
    public void processAdditionalFields() {
        delayBetweenStrikes = ConfigurationEngine.setInt(fileConfiguration, "delayBetweenStrikes", 15);
    }
}