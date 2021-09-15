package com.magmaguy.elitemobs.config.powers.premade;

import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import org.bukkit.Material;

public class ShieldWallConfig extends PowersConfigFields {
    public ShieldWallConfig() {
        super("shield_wall",
                true,
                "Shield Wall",
                Material.SHIELD.toString(),
                60,
                7);
    }


}
