package com.magmaguy.elitemobs.config.custombosses.premade;

import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import org.bukkit.entity.EntityType;

public class ZombieMomConfig extends CustomBossesConfigFields {
    public ZombieMomConfig() {
        super("zombie_parents_mom",
                EntityType.SKELETON,
                true,
                "$reinforcementLevel &7Zombie Mom",
                "dynamic");
        setHealthMultiplier(0.5);
        setDamageMultiplier(0.5);
        setDropsVanillaLoot(false);
        setDropsEliteMobsLoot(false);
    }
}
