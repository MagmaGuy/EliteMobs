package com.magmaguy.elitemobs.config.powers.premade;

import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import com.magmaguy.elitemobs.powers.ArrowRain;
import org.bukkit.Particle;

public class ArrowRainConfig extends PowersConfigFields {
    public ArrowRainConfig() {
        super("arrow_rain",
                true,
                Particle.DRIP_WATER.toString(),
                ArrowRain.class,
                PowerType.OFFENSIVE);
    }
}
