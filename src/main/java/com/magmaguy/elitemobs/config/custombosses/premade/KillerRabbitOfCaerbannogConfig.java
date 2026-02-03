package com.magmaguy.elitemobs.config.custombosses.premade;

import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import org.bukkit.Particle;
import org.bukkit.entity.EntityType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class KillerRabbitOfCaerbannogConfig extends CustomBossesConfigFields {
    public KillerRabbitOfCaerbannogConfig() {
        super("killer_rabbit_of_caerbannog",
                EntityType.RABBIT,
                true,
                "$eventBossLevel <g:#FFFFFF:#DC143C>Killer Rabbit of Caerbannog</g>",
                "dynamic");
        setUniqueLootList(Collections.singletonList("rabbit_charm.yml:1"));
        setTrails(new ArrayList<>(List.of(Particle.LARGE_SMOKE.toString(),
                Particle.LARGE_SMOKE.toString(),
                Particle.LARGE_SMOKE.toString(),
                Particle.LARGE_SMOKE.toString(),
                Particle.FLAME.toString(),
                Particle.FLAME.toString())));
    }
}
