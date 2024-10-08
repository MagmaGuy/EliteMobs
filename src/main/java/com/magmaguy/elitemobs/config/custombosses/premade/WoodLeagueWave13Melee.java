package com.magmaguy.elitemobs.config.custombosses.premade;

import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import org.bukkit.entity.EntityType;

import java.util.ArrayList;
import java.util.List;

public class WoodLeagueWave13Melee extends CustomBossesConfigFields {
    public WoodLeagueWave13Melee() {
        super("wood_league_wave_13_melee",
                EntityType.ZOMBIE,
                true,
                "$normalLevel &4Arena Zombie",
                "13");
        setFollowDistance(60);
        setPowers(new ArrayList<>(List.of("corpse.yml", "attack_poison.yml")));

    }
}
