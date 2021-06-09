package com.magmaguy.elitemobs.config.custombosses.premade;

import com.magmaguy.elitemobs.config.custombosses.CustomBossConfigFields;
import org.bukkit.entity.EntityType;

public class NecronomiconZombieConfig extends CustomBossConfigFields {
    public NecronomiconZombieConfig() {
        super("necronomicon_zombie",
                EntityType.ZOMBIE.toString(),
                true,
                "$reinforcementLevel &7Summoned Zombie",
                "dynamic");
        setHealthMultiplier(0.3);
        setDamageMultiplier(0.1);
        setDropsVanillaLoot(false);
        setDropsEliteMobsLoot(false);
    }
}
