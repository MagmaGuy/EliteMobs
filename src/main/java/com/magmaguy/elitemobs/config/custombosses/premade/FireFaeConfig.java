package com.magmaguy.elitemobs.config.custombosses.premade;

import com.magmaguy.elitemobs.config.custombosses.CustomBossConfigFields;
import org.bukkit.Particle;
import org.bukkit.entity.EntityType;

import java.util.Arrays;

public class FireFaeConfig extends CustomBossConfigFields {
    public FireFaeConfig() {
        super("fire_fae",
                EntityType.VEX.toString(),
                true,
                "$eventBossLevel &cFire Fae",
                "dynamic",
                0,
                false,
                0.3,
                0.3,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                Arrays.asList("attack_fire.yml"),
                null,
                null,
                null,
                null,
                null,
                Arrays.asList("the_feller.yml:0.3"),
                true,
                true,
                Arrays.asList(Particle.FLAME.toString()),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null);
    }
}
