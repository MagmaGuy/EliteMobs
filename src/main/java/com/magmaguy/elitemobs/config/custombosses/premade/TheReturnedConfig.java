package com.magmaguy.elitemobs.config.custombosses.premade;

import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import org.bukkit.entity.EntityType;

public class TheReturnedConfig extends CustomBossesConfigFields {
    public TheReturnedConfig() {
        super("the_returned",
                EntityType.HUSK,
                true,
                "$reinforcementLevel <g:#696969:#A9A9A9>The Returned</g>",
                "dynamic");
        setHealthMultiplier(0.5);
        setDamageMultiplier(0.5);
        setDropsEliteMobsLoot(false);
    }
}
