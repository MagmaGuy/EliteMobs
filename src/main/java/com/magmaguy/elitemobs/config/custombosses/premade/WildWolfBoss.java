package com.magmaguy.elitemobs.config.custombosses.premade;

import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import org.bukkit.entity.EntityType;

public class WildWolfBoss extends CustomBossesConfigFields {
    public WildWolfBoss(){
        super("wild_wolf.yml",
                EntityType.WOLF,
                true,
                "$reinforcementLevel &7Wild Wolf",
                "dynamic");
        setHealthMultiplier(0.2);
        setDamageMultiplier(0.2);
        setDropsEliteMobsLoot(false);
        setDropsVanillaLoot(false);
    }
}
