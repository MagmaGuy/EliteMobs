package com.magmaguy.elitemobs.config.custombosses.premade;

import com.magmaguy.elitemobs.config.custombosses.CustomBossConfigFields;
import org.bukkit.Particle;
import org.bukkit.entity.EntityType;

import java.util.Arrays;
import java.util.Collections;

public class KillerRabbitOfCaerbannogConfig extends CustomBossConfigFields {
    public KillerRabbitOfCaerbannogConfig() {
        super("killer_rabbit_of_caerbannog",
                EntityType.RABBIT.toString(),
                true,
                "$eventBossLevel &cKiller Rabbit of Caerbannog",
                "dynamic");
        setUniqueLootList(Collections.singletonList("rod_of_the_depths.yml:1"));
        setTrails(Arrays.asList(Particle.SMOKE_LARGE.toString(),
                Particle.SMOKE_LARGE.toString(),
                Particle.SMOKE_LARGE.toString(),
                Particle.SMOKE_LARGE.toString(),
                Particle.FLAME.toString(),
                Particle.FLAME.toString()));
    }
}
