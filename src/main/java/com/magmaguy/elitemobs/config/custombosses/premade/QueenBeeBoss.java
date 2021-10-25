package com.magmaguy.elitemobs.config.custombosses.premade;

import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import com.magmaguy.elitemobs.utils.VersionChecker;
import org.bukkit.Particle;
import org.bukkit.entity.EntityType;

import java.util.Arrays;
import java.util.Collections;

public class QueenBeeBoss extends CustomBossesConfigFields {
    public QueenBeeBoss() {
        super("queen_bee",
                EntityType.ZOMBIE,
                true,
                "$eventBossLevel &6Queen Bee",
                "dynamic");
        if (!VersionChecker.serverVersionOlderThan(15, 0))
            setEntityType(EntityType.BEE);
        setPowers(Collections.singletonList("summonable:summonType=ON_HIT:chance=0.5:filename=imperial_bee_guard.yml"));
        setUniqueLootList(Collections.singletonList("the_stinger.yml:1"));
        setTrails(Arrays.asList(Particle.SMOKE_LARGE.toString(),
                Particle.SMOKE_LARGE.toString(),
                Particle.SMOKE_LARGE.toString(),
                Particle.SMOKE_LARGE.toString(),
                Particle.FLAME.toString(),
                Particle.FLAME.toString()));
    }
}
