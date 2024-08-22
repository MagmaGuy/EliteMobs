package com.magmaguy.elitemobs.config.mobproperties.premade;

import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfigFields;
import org.bukkit.entity.EntityType;

import java.util.ArrayList;
import java.util.List;

public class EliteWitherSkeletonConfig extends MobPropertiesConfigFields {
    public EliteWitherSkeletonConfig() {
        super("elite_wither_skeleton",
                EntityType.WITHER_SKELETON,
                true,
                "&fLvl &2$level &fElite &8Wither Skeleton",
                new ArrayList<>(List.of("$entity's &carrows withered away $player&c!",
                        "$entity's &chas withered $player &caway!")),
                12);
    }
}
