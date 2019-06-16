package com.magmaguy.elitemobs;

import com.magmaguy.elitemobs.config.custombosses.CustomBossConfigFields;
import org.bukkit.entity.EntityType;

public class TheReturnedConfig extends CustomBossConfigFields {
    public TheReturnedConfig() {
        super("the_returned",
                EntityType.ZOMBIE.toString(),
                true,
                "&6The Returned",
                "dynamic",
                0,
                false,
                0.5,
                0.5,
                null,
                null,
                null,
                null,
                null,
                null,
                false,
                null,
                null,
                null,
                null,
                null,
                null,
                false,
                true,
                null);
    }
}
