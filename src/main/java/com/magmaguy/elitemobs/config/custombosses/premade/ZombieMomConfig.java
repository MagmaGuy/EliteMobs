package com.magmaguy.elitemobs.config.custombosses.premade;

import com.magmaguy.elitemobs.config.custombosses.CustomBossConfigFields;
import org.bukkit.entity.EntityType;

public class ZombieMomConfig extends CustomBossConfigFields {
    public ZombieMomConfig() {
        super("zombie_parents_mom",
                EntityType.SKELETON.toString(),
                true,
                "$reinforcementLevel &7Zombie Mom",
                "dynamic");
        setHealthMultiplier(0.5);
        setDamageMultiplier(0.5);
        setDropsVanillaLoot(false);
        setDropsEliteMobsLoot(false);
    }
}
