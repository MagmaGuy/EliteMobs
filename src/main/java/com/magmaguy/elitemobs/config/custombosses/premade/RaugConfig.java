package com.magmaguy.elitemobs.config.custombosses.premade;

import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import org.bukkit.Particle;
import org.bukkit.entity.EntityType;

import java.util.Collections;

public class RaugConfig extends CustomBossesConfigFields {
    public RaugConfig() {
        super("raug",
                EntityType.SILVERFISH,
                true,
                "$reinforcementLevel <g:#8B0000:#FF4500>Raug</g>",
                "dynamic");
        setHealthMultiplier(0.1);
        setDamageMultiplier(0.1);
        setTrails(Collections.singletonList(Particle.LARGE_SMOKE.toString()));
    }
}
