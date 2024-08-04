package com.magmaguy.elitemobs.config.custombosses.premade;

import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import org.bukkit.entity.EntityType;

import java.util.List;

public class WoodLeagueWave14Melee extends CustomBossesConfigFields {
    public WoodLeagueWave14Melee() {
        super("wood_league_wave_14_melee",
                EntityType.ZOMBIE,
                true,
                "$normalLevel &4Arena Zombie",
                "14");
        setFollowDistance(60);
        setPowers(List.of("corpse.yml"));

    }
}
