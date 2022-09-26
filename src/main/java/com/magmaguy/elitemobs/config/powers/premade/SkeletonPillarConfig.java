package com.magmaguy.elitemobs.config.powers.premade;

import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import com.magmaguy.elitemobs.powers.SkeletonPillar;
import org.bukkit.Material;

public class SkeletonPillarConfig extends PowersConfigFields {
    public SkeletonPillarConfig() {
        super("skeleton_pillar",
                true,
                Material.BONE.toString(),
                SkeletonPillar.class,
                PowerType.MAJOR_SKELETON);
    }
}
