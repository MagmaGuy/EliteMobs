package com.magmaguy.elitemobs.config.custombosses.premade;

import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import org.bukkit.entity.EntityType;

public class NecronomiconSkeletonConfig extends CustomBossesConfigFields {
    public NecronomiconSkeletonConfig() {
        super("necronomicon_skeleton",
                EntityType.SKELETON,
                true,
                "$reinforcementLevel &7Summoned Skeleton",
                "dynamic");
        setHealthMultiplier(0.3);
        setDamageMultiplier(0.1);
        setDropsVanillaLoot(false);
        setDropsEliteMobsLoot(false);
    }
}
