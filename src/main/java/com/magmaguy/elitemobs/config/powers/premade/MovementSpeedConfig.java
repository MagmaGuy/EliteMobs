package com.magmaguy.elitemobs.config.powers.premade;

import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import org.bukkit.Material;

public class MovementSpeedConfig extends PowersConfigFields {
    public MovementSpeedConfig() {
        super("movement_speed",
                true,
                "movement_speed",
                Material.GOLDEN_BOOTS.toString());
    }
}
