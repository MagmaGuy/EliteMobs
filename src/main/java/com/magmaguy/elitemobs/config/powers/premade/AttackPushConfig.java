package com.magmaguy.elitemobs.config.powers.premade;

import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import org.bukkit.Material;

public class AttackPushConfig extends PowersConfigFields {
    public AttackPushConfig() {
        super("attack_push",
                true,
                "Knockback",
                Material.PISTON.toString());
    }
}
