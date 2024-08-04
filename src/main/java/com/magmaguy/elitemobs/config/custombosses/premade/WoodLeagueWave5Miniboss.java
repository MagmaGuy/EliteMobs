package com.magmaguy.elitemobs.config.custombosses.premade;

import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import org.bukkit.entity.EntityType;

import java.util.Arrays;
import java.util.List;

public class WoodLeagueWave5Miniboss extends CustomBossesConfigFields {
    public WoodLeagueWave5Miniboss() {
        super("wood_league_wave_5_miniboss",
                EntityType.RAVAGER,
                true,
                "$minibossLevel &cWeird Cow",
                "5");
        setPowers(Arrays.asList("attack_push.yml", "attack_gravity.yml"));
        setOnDamagedMessages(List.of("Woof!"));
        setHealthMultiplier(3D);
        setDamageMultiplier(2D);
        setFollowDistance(60);
        setMovementSpeedAttribute(0.6D);

    }
}