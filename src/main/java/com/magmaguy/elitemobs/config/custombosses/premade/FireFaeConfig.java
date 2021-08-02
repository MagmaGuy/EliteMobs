package com.magmaguy.elitemobs.config.custombosses.premade;

import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import org.bukkit.Particle;
import org.bukkit.entity.EntityType;

import java.util.Collections;

public class FireFaeConfig extends CustomBossesConfigFields {
    public FireFaeConfig() {
        super("fire_fae",
                EntityType.VEX,
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
