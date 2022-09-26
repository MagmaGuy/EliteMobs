package com.magmaguy.elitemobs.config.powers.premade;

import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import com.magmaguy.elitemobs.powers.MovementSpeed;
import org.bukkit.Material;

public class MovementSpeedConfig extends PowersConfigFields {
    public MovementSpeedConfig() {
        super("movement_speed",
                true,
                Material.GOLDEN_BOOTS.toString(),
                MovementSpeed.class,
                PowerType.MISCELLANEOUS);
    }
}
