package com.magmaguy.elitemobs.config.custombosses.premade;

import com.magmaguy.elitemobs.config.custombosses.CustomBossConfigFields;
import org.bukkit.Particle;
import org.bukkit.entity.EntityType;

import java.util.Arrays;

public class EmberConfig extends CustomBossConfigFields {
    public EmberConfig() {
        super("ember",
                EntityType.BLAZE.toString(),
                true,
                "$reinforcementLevel &cEmber",
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
                Arrays.asList(),
                null,
                null,
                null,
                null,
                null,
                Arrays.asList(),
                false,
                true,
                Arrays.asList(Particle.SPELL_WITCH.toString()),
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
