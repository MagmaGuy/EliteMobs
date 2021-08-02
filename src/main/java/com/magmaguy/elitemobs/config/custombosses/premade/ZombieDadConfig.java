package com.magmaguy.elitemobs.config.custombosses.premade;

import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import org.bukkit.entity.EntityType;

public class ZombieDadConfig extends CustomBossesConfigFields {
    public ZombieDadConfig() {
        super("zombie_parents_dad",
                EntityType.SKELETON,
                true,
                "$reinforcementLevel &7Zombie Dad",
                "dynamic");
        setHealthMultiplier(0.5);
        setDamageMultiplier(0.5);
        setDropsVanillaLoot(false);
        setDropsEliteMobsLoot(false);
    }
}
