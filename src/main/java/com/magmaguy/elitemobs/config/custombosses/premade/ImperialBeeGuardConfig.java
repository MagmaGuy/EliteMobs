package com.magmaguy.elitemobs.config.custombosses.premade;

import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import org.bukkit.Particle;
import org.bukkit.entity.EntityType;

import java.util.Collections;

public class ImperialBeeGuardConfig extends CustomBossesConfigFields {
    public ImperialBeeGuardConfig() {
        super("imperial_bee_guard",
                EntityType.BEE,
                true,
                "$reinforcementLevel &eImperial Bee Soldier",
                "dynamic");
        setHealthMultiplier(0.1);
        setDamageMultiplier(0.1);
        setTrails(Collections.singletonList(Particle.SMOKE_LARGE.toString()));
    }
}
