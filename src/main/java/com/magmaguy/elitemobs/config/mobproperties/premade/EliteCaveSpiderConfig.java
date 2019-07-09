package com.magmaguy.elitemobs.config.mobproperties.premade;

import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfigFields;
import org.bukkit.entity.EntityType;

import java.util.Arrays;

public class EliteCaveSpiderConfig extends MobPropertiesConfigFields {
    public EliteCaveSpiderConfig() {
        super("elite_cave_spider",
                EntityType.CAVE_SPIDER,
                true,
                "&fLvl &2$level &fElite &3Cave Spider",
                Arrays.asList("$player became entangled in $entity's web!"));
    }
}
