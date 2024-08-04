package com.magmaguy.elitemobs.config.custombosses.premade;

import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import org.bukkit.entity.EntityType;

import java.util.Arrays;

public class WoodLeagueWave17Melee extends CustomBossesConfigFields {
    public WoodLeagueWave17Melee() {
        super("wood_league_wave_17_melee",
                EntityType.ZOMBIE,
                true,
                "$normalLevel &4Arena Zombie",
                "17");
        setFollowDistance(60);
        setPowers(Arrays.asList("corpse.yml", "attack_fire.yml"));

    }
}
