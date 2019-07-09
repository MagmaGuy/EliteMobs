package com.magmaguy.elitemobs.config.powers.premade;

import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import org.bukkit.Material;

public class SkeletonPillarConfig extends PowersConfigFields {
    public SkeletonPillarConfig() {
        super("skeleton_pillar",
                true,
                "Pillar",
                Material.BONE.toString());
    }
}
