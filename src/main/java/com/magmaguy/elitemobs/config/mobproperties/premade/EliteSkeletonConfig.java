package com.magmaguy.elitemobs.config.mobproperties.premade;

import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfigFields;
import org.bukkit.entity.EntityType;

import java.util.Arrays;

public class EliteSkeletonConfig extends MobPropertiesConfigFields {
    public EliteSkeletonConfig() {
        super("elite_skeleton",
                EntityType.SKELETON,
                true,
                "&fLvl &2$level &fElite &fSkeleton",
                Arrays.asList("$player became $entity's pin cushion!"));
    }
}
