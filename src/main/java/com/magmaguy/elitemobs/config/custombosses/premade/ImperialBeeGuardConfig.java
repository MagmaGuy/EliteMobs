package com.magmaguy.elitemobs.config.custombosses.premade;

import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import com.magmaguy.elitemobs.versionnotifier.VersionChecker;
import org.bukkit.Particle;
import org.bukkit.entity.EntityType;

import java.util.Collections;

public class ImperialBeeGuardConfig extends CustomBossesConfigFields {
    public ImperialBeeGuardConfig() {
        super("imperial_bee_guard",
                EntityType.ZOMBIE,
                true,
                "$reinforcementLevel &eImperial Bee Soldier",
                "dynamic");
        if (!VersionChecker.serverVersionOlderThan(15, 0))
            setEntityType(EntityType.BEE);
        setDropsEliteMobsLoot(false);
        setDropsVanillaLoot(false);
        setHealthMultiplier(0.1);
        setDamageMultiplier(0.1);
        setTrails(Collections.singletonList(Particle.SMOKE_LARGE.toString()));
    }
}
