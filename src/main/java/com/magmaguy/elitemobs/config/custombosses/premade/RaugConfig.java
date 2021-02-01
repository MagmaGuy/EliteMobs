package com.magmaguy.elitemobs.config.custombosses.premade;

import com.magmaguy.elitemobs.config.custombosses.CustomBossConfigFields;
import org.bukkit.Particle;
import org.bukkit.entity.EntityType;

import java.util.Arrays;

public class RaugConfig extends CustomBossConfigFields {
    public RaugConfig() {
        super("raug",
                EntityType.SILVERFISH.toString(),
                true,
                "$reinforcementLevel &4Raug",
                "dynamic",
                0,
                false,
                0.1,
                0.1,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                false,
                false,
                Arrays.asList(Particle.SMOKE_LARGE.toString()),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null);
    }
}
