package com.magmaguy.elitemobs.config.custombosses.premade;

import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import org.bukkit.entity.EntityType;

public class WoodLeagueWave6Melee extends CustomBossesConfigFields {
    public WoodLeagueWave6Melee() {
        super("wood_league_wave_6_melee",
                EntityType.ZOMBIE,
                true,
                "$normalLevel Arena Zombie",
                "6");
        setFollowDistance(60);

        setDamageMultiplier(0.5D);
        setHealthMultiplier(0.75D);
    }
}
