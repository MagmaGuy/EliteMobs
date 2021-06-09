package com.magmaguy.elitemobs.config.custombosses.premade;

import com.magmaguy.elitemobs.config.custombosses.CustomBossConfigFields;
import org.bukkit.Particle;
import org.bukkit.entity.EntityType;

import java.util.Arrays;
import java.util.Collections;

public class LightningFaeConfig extends CustomBossConfigFields {
    public LightningFaeConfig() {
        super("lightning_fae",
                EntityType.VEX.toString(),
                true,
                "$eventBossLevel &eLightning Fae",
                "dynamic");
        setHealthMultiplier(0.3);
        setDamageMultiplier(0.3);
        setPowers(Collections.singletonList("attack_lightning.yml"));
        setUniqueLootList(Collections.singletonList("the_feller.yml:0.3"));
        setTrails(Collections.singletonList(Particle.SPELL.toString()));
    }
}
