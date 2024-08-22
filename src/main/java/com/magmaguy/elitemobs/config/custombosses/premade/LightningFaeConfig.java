package com.magmaguy.elitemobs.config.custombosses.premade;

import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import org.bukkit.Particle;
import org.bukkit.entity.EntityType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LightningFaeConfig extends CustomBossesConfigFields {
    public LightningFaeConfig() {
        super("lightning_fae",
                EntityType.VEX,
                true,
                "$eventBossLevel &eLightning Fae",
                "dynamic");
        setHealthMultiplier(0.3);
        setDamageMultiplier(0.3);
        setPowers(new ArrayList<>(List.of("attack_lightning.yml")));
        setUniqueLootList(Collections.singletonList("the_feller.yml:0.3"));
        setTrails(Collections.singletonList(Particle.WITCH.toString()));
    }
}
