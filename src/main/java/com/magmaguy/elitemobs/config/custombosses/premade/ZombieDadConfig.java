package com.magmaguy.elitemobs.config.custombosses.premade;

import com.magmaguy.elitemobs.config.custombosses.CustomBossConfigFields;
import org.bukkit.entity.EntityType;

public class ZombieDadConfig extends CustomBossConfigFields {
    public ZombieDadConfig() {
        super("zombie_parents_dad",
                EntityType.SKELETON.toString(),
                true,
                "$reinforcementLevel &7Zombie Dad",
                "dynamic");
        setHealthMultiplier(0.5);
        setDamageMultiplier(0.5);
        setDropsVanillaLoot(false);
        setDropsEliteMobsLoot(false);
    }
}
