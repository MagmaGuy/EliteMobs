package com.magmaguy.elitemobs.config.custombosses.premade;

import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import org.bukkit.Particle;
import org.bukkit.entity.EntityType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class KrakenConfig extends CustomBossesConfigFields {
    public KrakenConfig() {
        super("kraken",
                EntityType.ELDER_GUARDIAN,
                true,
                "$eventBossLevel &3Kraken",
                "dynamic");
        setUniqueLootList(Collections.singletonList("rod_of_the_depths.yml:1"));
        setTrails(new ArrayList<>(List.of(Particle.LARGE_SMOKE.toString(),
                Particle.LARGE_SMOKE.toString(),
                Particle.LARGE_SMOKE.toString(),
                Particle.LARGE_SMOKE.toString(),
                Particle.FLAME.toString(),
                Particle.FLAME.toString())));
    }
}
