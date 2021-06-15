package com.magmaguy.elitemobs.config.custombosses.premade;

import com.magmaguy.elitemobs.config.custombosses.CustomBossConfigFields;
import org.bukkit.Particle;
import org.bukkit.entity.EntityType;

import java.util.Arrays;
import java.util.Collections;

public class BalrogConfig extends CustomBossConfigFields {
    public BalrogConfig() {
        super("balrog",
                EntityType.SILVERFISH.toString(),
                true,
                "$eventBossLevel &4Balrog",
                "dynamic");
        setPowers(Collections.singletonList("summonable:summonType=ON_HIT:chance=0.5:filename=raug.yml"));
        setUniqueLootList(Collections.singletonList("dwarven_greed.yml:1"));
        setTrails(Arrays.asList(Particle.SMOKE_LARGE.toString(),
                Particle.SMOKE_LARGE.toString(),
                Particle.SMOKE_LARGE.toString(),
                Particle.SMOKE_LARGE.toString(),
                Particle.FLAME.toString(),
                Particle.FLAME.toString()));
    }
}
