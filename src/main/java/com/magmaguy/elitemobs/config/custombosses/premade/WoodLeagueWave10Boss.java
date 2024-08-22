package com.magmaguy.elitemobs.config.custombosses.premade;

import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import org.bukkit.entity.EntityType;

import java.util.ArrayList;
import java.util.List;

public class WoodLeagueWave10Boss extends CustomBossesConfigFields {
    public WoodLeagueWave10Boss() {
        super("wood_league_wave_10_boss",
                EntityType.WOLF,
                true,
                "$bossLevel &4Bad Doggo",
                "10");
        setFollowDistance(60);

        setPowers(new ArrayList<>(List.of("ground_pound.yml",
                "summonable:summonType=ON_HIT:filename=wood_league_wave_10_reinforcement.yml:amount=2:chance=0.1:inheritAggro=true:spawnNearby=true")));
        setMovementSpeedAttribute(0.6D);
        setHealthMultiplier(5D);
        setDamageMultiplier(2D);
    }
}
