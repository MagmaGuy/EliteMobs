package com.magmaguy.elitemobs.config.powers.premade;

import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import org.bukkit.Material;

public class ZombieParentsConfig extends PowersConfigFields {
    public ZombieParentsConfig() {
        super("zombie_parents",
                true,
                "Parents",
                Material.SKELETON_SKULL.toString());
    }
}