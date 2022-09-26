package com.magmaguy.elitemobs.config.powers.premade;

import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import com.magmaguy.elitemobs.powers.ArrowFireworks;
import org.bukkit.Material;

public class ArrowFireworksConfig extends PowersConfigFields {
    public ArrowFireworksConfig() {
        super("arrow_fireworks",
                true,
                Material.FIREWORK_ROCKET.toString(),
                ArrowFireworks.class,
                PowerType.OFFENSIVE);
    }
}
