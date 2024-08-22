package com.magmaguy.elitemobs.config.custombosses.premade;

import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import org.bukkit.Particle;
import org.bukkit.entity.EntityType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BalrogConfig extends CustomBossesConfigFields {
    public BalrogConfig() {
        super("balrog",
                EntityType.SILVERFISH,
                true,
                "$eventBossLevel &4Balrog",
                "dynamic");
        setPowers(new ArrayList<>(List.of("summonable:summonType=ON_HIT:chance=0.5:filename=raug.yml")));
        setUniqueLootList(Collections.singletonList("dwarven_greed.yml:1"));
        setTrails(new ArrayList<>(List.of(Particle.LARGE_SMOKE.toString(),
                Particle.LARGE_SMOKE.toString(),
                Particle.LARGE_SMOKE.toString(),
                Particle.LARGE_SMOKE.toString(),
                Particle.FLAME.toString(),
                Particle.FLAME.toString())));
    }
}
