package com.magmaguy.elitemobs.config.custombosses.premade;

import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import org.bukkit.entity.EntityType;

public class WoodLeagueWave2Melee extends CustomBossesConfigFields {
    public WoodLeagueWave2Melee() {
        super("wood_league_wave_2_melee",
                EntityType.ZOMBIE,
                true,
                "$normalLevel Arena Zombie",
                "2");
        setFollowDistance(60);

    }
}
