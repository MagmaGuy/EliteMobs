package com.magmaguy.elitemobs.config.mobproperties.premade;

import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfigFields;
import org.bukkit.entity.EntityType;

import java.util.Arrays;

public class EliteWitherSkeletonConfig extends MobPropertiesConfigFields {
    public EliteWitherSkeletonConfig() {
        super("elite_wither_skeleton",
                EntityType.WITHER_SKELETON,
                true,
                "&fLvl &2$level &fElite &8Wither Skeleton",
                Arrays.asList("$entity's arrows withered away $player!",
                        "$entity's has withered $player away!"));
    }
}
