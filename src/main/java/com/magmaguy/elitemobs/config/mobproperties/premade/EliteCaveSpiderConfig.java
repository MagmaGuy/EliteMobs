package com.magmaguy.elitemobs.config.mobproperties.premade;

import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfigFields;
import org.bukkit.entity.EntityType;

import java.util.List;

public class EliteCaveSpiderConfig extends MobPropertiesConfigFields {
    public EliteCaveSpiderConfig() {
        super("elite_cave_spider",
                EntityType.CAVE_SPIDER,
                true,
                "&fLvl &2$level &fElite &3Cave Spider",
                List.of("$player &cbecame entangled in $entity's &cweb!"),
                3);
    }
}
