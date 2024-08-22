package com.magmaguy.elitemobs.config.custombosses.premade;

import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import org.bukkit.entity.EntityType;

import java.util.ArrayList;
import java.util.List;

public class WoodLeagueWave8Melee extends CustomBossesConfigFields {
    public WoodLeagueWave8Melee() {
        super("wood_league_wave_8_melee",
                EntityType.ZOMBIE,
                true,
                "$normalLevel Arena Zombie",
                "8");
        setFollowDistance(60);
        setPowers(new ArrayList<>(List.of("corpse.yml")));

    }
}
