package com.magmaguy.elitemobs.config.powers.premade;

import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import org.bukkit.Particle;

public class ZombieBloatConfig extends PowersConfigFields {
    public ZombieBloatConfig() {
        super("zombie_bloat",
                true,
                "Bloat",
                Particle.TOTEM.toString());
    }
}
