package com.magmaguy.elitemobs.config.custombosses.premade;

import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import org.bukkit.entity.EntityType;

public class TheReturnedConfig extends CustomBossesConfigFields {
    public TheReturnedConfig() {
        super("the_returned",
                EntityType.HUSK,
                true,
                "$reinforcementLevel &6The Returned",
                "dynamic");
        setHealthMultiplier(0.5);
        setDamageMultiplier(0.5);
        setDropsEliteMobsLoot(false);
    }
}
