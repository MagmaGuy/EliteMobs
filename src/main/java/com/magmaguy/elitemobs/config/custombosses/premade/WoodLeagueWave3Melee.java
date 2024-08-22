package com.magmaguy.elitemobs.config.custombosses.premade;

import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import org.bukkit.entity.EntityType;

import java.util.ArrayList;
import java.util.List;

public class WoodLeagueWave3Melee extends CustomBossesConfigFields {
    public WoodLeagueWave3Melee() {
        super("wood_league_wave_3_melee",
                EntityType.ZOMBIE,
                true,
                "$normalLevel Arena Zombie",
                "3");
        setPowers(new ArrayList<>(List.of("attack_fire.yml")));
        setFollowDistance(60);

    }
}
