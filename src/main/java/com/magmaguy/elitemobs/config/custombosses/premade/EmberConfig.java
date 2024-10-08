package com.magmaguy.elitemobs.config.custombosses.premade;

import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import org.bukkit.Particle;
import org.bukkit.entity.EntityType;

import java.util.Collections;

public class EmberConfig extends CustomBossesConfigFields {
    public EmberConfig() {
        super("ember",
                EntityType.BLAZE,
                true,
                "$reinforcementLevel &cEmber",
                "dynamic");
        setHealthMultiplier(0.3);
        setDamageMultiplier(0.3);
        setDropsEliteMobsLoot(false);
        setTrails(Collections.singletonList(Particle.WITCH.toString()));
    }
}
