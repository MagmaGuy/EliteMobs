package com.magmaguy.elitemobs.config.custombosses.premade;

import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import org.bukkit.Particle;
import org.bukkit.entity.EntityType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class QueenBeeBoss extends CustomBossesConfigFields {
    public QueenBeeBoss() {
        super("queen_bee",
                EntityType.BEE,
                true,
                "$eventBossLevel &6Queen Bee",
                "dynamic");
        setPowers(new ArrayList<>(List.of("summonable:summonType=ON_HIT:chance=0.5:filename=imperial_bee_guard.yml")));
        setUniqueLootList(Collections.singletonList("the_stinger.yml:1"));
        setTrails(new ArrayList<>(List.of(Particle.LARGE_SMOKE.toString(),
                Particle.LARGE_SMOKE.toString(),
                Particle.LARGE_SMOKE.toString(),
                Particle.LARGE_SMOKE.toString(),
                Particle.FLAME.toString(),
                Particle.FLAME.toString())));
    }
}
