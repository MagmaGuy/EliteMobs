package com.magmaguy.elitemobs.config.custombosses.premade;

import com.magmaguy.elitemobs.config.custombosses.CustomBossConfigFields;
import org.bukkit.entity.EntityType;

public class NecronomiconSkeletonConfig extends CustomBossConfigFields {
    public NecronomiconSkeletonConfig() {
        super("necronomicon_skeleton",
                EntityType.SKELETON.toString(),
                true,
                "$reinforcementLevel &7Summoned Skeleton",
                "dynamic");
        setHealthMultiplier(0.3);
        setDamageMultiplier(0.1);
        setDropsVanillaLoot(false);
        setDropsEliteMobsLoot(false);
    }
}
