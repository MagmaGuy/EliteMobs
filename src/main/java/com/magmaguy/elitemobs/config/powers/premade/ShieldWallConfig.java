package com.magmaguy.elitemobs.config.powers.premade;

import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import com.magmaguy.elitemobs.powers.ShieldWall;
import org.bukkit.Material;

public class ShieldWallConfig extends PowersConfigFields {
    public ShieldWallConfig() {
        super("shield_wall",
                true,
                Material.SHIELD.toString(),
                60,
                7,
                ShieldWall.class,
                PowerType.DEFENSIVE);
    }


}
