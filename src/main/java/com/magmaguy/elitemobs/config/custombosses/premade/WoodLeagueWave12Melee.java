package com.magmaguy.elitemobs.config.custombosses.premade;

import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import org.bukkit.entity.EntityType;

import java.util.ArrayList;
import java.util.List;

public class WoodLeagueWave12Melee extends CustomBossesConfigFields {
    public WoodLeagueWave12Melee() {
        super("wood_league_wave_12_melee",
                EntityType.ZOMBIE,
                true,
                "$normalLevel &4Arena Zombie",
                "12");
        setFollowDistance(60);
        setPowers(new ArrayList<>(List.of("corpse.yml")));

    }
}
