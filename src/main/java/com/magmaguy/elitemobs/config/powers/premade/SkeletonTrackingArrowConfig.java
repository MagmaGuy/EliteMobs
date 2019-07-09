package com.magmaguy.elitemobs.config.powers.premade;

import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import org.bukkit.Material;

public class SkeletonTrackingArrowConfig extends PowersConfigFields {
    public SkeletonTrackingArrowConfig() {
        super("skeleton_tracking_arrow",
                true,
                "Tracking Arrow",
                Material.ARROW.toString());
    }
}
