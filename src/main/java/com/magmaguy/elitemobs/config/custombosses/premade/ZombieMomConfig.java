package com.magmaguy.elitemobs.config.custombosses.premade;

import com.magmaguy.elitemobs.config.custombosses.CustomBossConfigFields;
import org.bukkit.entity.EntityType;

public class ZombieMomConfig extends CustomBossConfigFields {
    public ZombieMomConfig() {
        super("zombie_parents_mom",
                EntityType.SKELETON.toString(),
                true,
                "$reinforcementLevel &7Zombie Mom",
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
                null,
                false,
                false,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null);
    }
}
