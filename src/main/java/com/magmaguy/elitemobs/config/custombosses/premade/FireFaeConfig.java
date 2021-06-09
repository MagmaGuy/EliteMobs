package com.magmaguy.elitemobs.config.custombosses.premade;

import com.magmaguy.elitemobs.config.custombosses.CustomBossConfigFields;
import org.bukkit.Particle;
import org.bukkit.entity.EntityType;

import java.util.Arrays;
import java.util.Collections;

public class FireFaeConfig extends CustomBossConfigFields {
    public FireFaeConfig() {
        super("fire_fae",
                EntityType.VEX.toString(),
                true,
                "$eventBossLevel &cFire Fae",
                "dynamic");
        setHealthMultiplier(0.3);
        setDamageMultiplier(0.3);
        setPowers(Collections.singletonList("attack_fire.yml"));
        setUniqueLootList(Collections.singletonList("the_feller.yml:0.3"));
        setTrails(Collections.singletonList(Particle.FLAME.toString()));
    }
}
