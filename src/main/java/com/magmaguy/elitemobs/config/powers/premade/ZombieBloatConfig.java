package com.magmaguy.elitemobs.config.powers.premade;

import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import com.magmaguy.elitemobs.powers.ZombieBloat;
import org.bukkit.Particle;

public class ZombieBloatConfig extends PowersConfigFields {
    public ZombieBloatConfig() {
        super("zombie_bloat",
                true,
                Particle.TOTEM.toString(),
                ZombieBloat.class,
                PowerType.MAJOR_ZOMBIE);
    }
}
