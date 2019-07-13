package com.magmaguy.elitemobs.config.custombosses.premade;

import com.magmaguy.elitemobs.config.custombosses.CustomBossConfigFields;
import org.bukkit.Particle;
import org.bukkit.entity.EntityType;

import java.util.Arrays;

public class BalrogConfig extends CustomBossConfigFields {
    public BalrogConfig() {
        super("balrog",
                EntityType.SILVERFISH.toString(),
                true,
                "&4Balrog",
                "dynamic",
                0,
                false,
                1,
                1,
                null,
                null,
                null,
                null,
                null,
                null,
                false,
                Arrays.asList("summon_raug.yml"),
                null,
                null,
                null,
                null,
                Arrays.asList("dwarven_greed.yml:1"),
                true,
                true,
                Arrays.asList(Particle.SMOKE_LARGE.toString(), Particle.SMOKE_LARGE.toString(), Particle.SMOKE_LARGE.toString(), Particle.SMOKE_LARGE.toString(),
                        Particle.FLAME.toString(), Particle.FLAME.toString()),
                null,
                null);
    }
}
