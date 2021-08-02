package com.magmaguy.elitemobs.config.custombosses.premade;

import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import org.bukkit.entity.EntityType;

public class NecronomiconZombieConfig extends CustomBossesConfigFields {
    public NecronomiconZombieConfig() {
        super("necronomicon_zombie",
                EntityType.ZOMBIE,
                true,
                "$reinforcementLevel &7Summoned Zombie",
                "dynamic");
        setHealthMultiplier(0.3);
        setDamageMultiplier(0.1);
        setDropsVanillaLoot(false);
        setDropsEliteMobsLoot(false);
    }
}
