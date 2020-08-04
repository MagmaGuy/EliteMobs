package com.magmaguy.elitemobs.config.powers.premade;

import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import org.bukkit.Particle;

public class ArrowRainConfig extends PowersConfigFields {
    public ArrowRainConfig() {
        super("arrow_rain",
                true,
                "Arrow Rain",
                Particle.DRIP_WATER.toString());
    }
}
