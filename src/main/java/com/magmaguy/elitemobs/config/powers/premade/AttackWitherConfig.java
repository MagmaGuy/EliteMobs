package com.magmaguy.elitemobs.config.powers.premade;

import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import org.bukkit.Material;

public class AttackWitherConfig extends PowersConfigFields {
    public AttackWitherConfig() {
        super("attack_wither",
                true,
                "Withering",
                Material.WITHER_SKELETON_SKULL.toString());
    }
}
