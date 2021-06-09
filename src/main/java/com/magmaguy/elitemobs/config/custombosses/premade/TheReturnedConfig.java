package com.magmaguy.elitemobs.config.custombosses.premade;

import com.magmaguy.elitemobs.config.custombosses.CustomBossConfigFields;
import org.bukkit.entity.EntityType;

public class TheReturnedConfig extends CustomBossConfigFields {
    public TheReturnedConfig() {
        super("the_returned",
                EntityType.HUSK.toString(),
                true,
                "$reinforcementLevel &6The Returned",
                "dynamic");
        setHealthMultiplier(0.5);
        setDamageMultiplier(0.5);
        setDropsEliteMobsLoot(false);
    }
}
