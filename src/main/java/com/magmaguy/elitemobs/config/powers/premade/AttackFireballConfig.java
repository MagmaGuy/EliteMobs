package com.magmaguy.elitemobs.config.powers.premade;

import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import org.bukkit.Material;

public class AttackFireballConfig extends PowersConfigFields {
    public AttackFireballConfig() {
        super("attack_fireball",
                true,
                "Fireball",
                Material.FIRE_CHARGE.toString());
    }
}