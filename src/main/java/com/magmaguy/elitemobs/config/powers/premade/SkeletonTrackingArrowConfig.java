package com.magmaguy.elitemobs.config.powers.premade;

import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import com.magmaguy.elitemobs.powers.SkeletonTrackingArrow;
import org.bukkit.Material;

public class SkeletonTrackingArrowConfig extends PowersConfigFields {
    public SkeletonTrackingArrowConfig() {
        super("skeleton_tracking_arrow",
                true,
                Material.ARROW.toString(),
                SkeletonTrackingArrow.class,
                PowerType.MAJOR_SKELETON);
    }
}
