package com.magmaguy.elitemobs.config.custombosses.premade;

import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import org.bukkit.entity.EntityType;

import java.util.ArrayList;
import java.util.List;

public class WoodLeagueWave11Melee extends CustomBossesConfigFields {
    public WoodLeagueWave11Melee() {
        super("wood_league_wave_11_melee",
                EntityType.ZOMBIE,
                true,
                "$normalLevel &4Arena Zombie",
                "11");
        setFollowDistance(60);
        setPowers(new ArrayList<>(List.of("corpse.yml")));

    }
}
