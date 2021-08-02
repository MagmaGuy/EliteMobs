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
                "$reinforcementLevel &4Raug",
                "dynamic");
        setHealthMultiplier(0.1);
        setDamageMultiplier(0.1);
        setTrails(Collections.singletonList(Particle.SMOKE_LARGE.toString()));
    }
}
