package com.magmaguy.elitemobs.config.custombosses.premade;

import com.magmaguy.elitemobs.config.custombosses.CustomBossConfigFields;
import org.bukkit.Particle;
import org.bukkit.entity.EntityType;

import java.util.Arrays;

public class LightningFaeConfig extends CustomBossConfigFields {
    public LightningFaeConfig() {
        super("lightning_fae",
                EntityType.VEX.toString(),
                true,
                "&eLightning Fae",
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
                false,
                Arrays.asList("attack_lightning.yml"),
                null,
                null,
                null,
                null,
                null,
                true,
                true,
                Arrays.asList(Particle.SPELL.toString()),
                null,
                null);
    }
}
