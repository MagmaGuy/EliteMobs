package com.magmaguy.elitemobs.config.powers.premade;

import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import org.bukkit.Material;

public class AttackLightningConfig extends PowersConfigFields {
    public AttackLightningConfig() {
        super("attack_lightning",
                true,
                "Thunderous",
                Material.HORN_CORAL.toString());
    }
}