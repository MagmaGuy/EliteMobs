package com.magmaguy.elitemobs.config.mobproperties.premade;

import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfigFields;
import org.bukkit.entity.EntityType;

import java.util.ArrayList;
import java.util.List;

public class EliteSkeletonConfig extends MobPropertiesConfigFields {
    public EliteSkeletonConfig() {
        super("elite_skeleton",
                EntityType.SKELETON,
                true,
                "&fLvl &2$level &fElite &fSkeleton",
                new ArrayList<>(List.of("$player &cbecame $entity's &cpin cushion!",
                        "$entity &cwanted to see $player's &cbones!")),
                5);
    }
}
