package com.magmaguy.elitemobs.config.custombosses.premade;

import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import org.bukkit.entity.EntityType;

import java.util.ArrayList;
import java.util.List;

public class WoodLeagueWave15Miniboss extends CustomBossesConfigFields {
    public WoodLeagueWave15Miniboss() {
        super("wood_league_wave_15_miniboss",
                EntityType.ZOGLIN,
                true,
                "$bossLevel &4Mr. Oinkers",
                "15");
        setFollowDistance(60);

        setPowers(new ArrayList<>(List.of("gold_explosion.yml", "gold_shotgun.yml")));
        setMovementSpeedAttribute(0.6D);
        setHealthMultiplier(3D);
        setDamageMultiplier(2D);
        setOnDamagedMessages(List.of("Oink!?"));
    }
}
