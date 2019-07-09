package com.magmaguy.elitemobs.config.powers.premade;

import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import org.bukkit.Material;

public class AttackGravityConfig extends PowersConfigFields {
    public AttackGravityConfig() {
        super("attack_gravity",
                true,
                "Levitator",
                Material.ELYTRA.toString());
    }
}