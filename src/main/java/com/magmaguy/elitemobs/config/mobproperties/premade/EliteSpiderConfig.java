package com.magmaguy.elitemobs.config.mobproperties.premade;

import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfigFields;
import org.bukkit.entity.EntityType;

import java.util.ArrayList;
import java.util.List;

public class EliteSpiderConfig extends MobPropertiesConfigFields {
    public EliteSpiderConfig() {
        super("elite_spider",
                EntityType.SPIDER,
                true,
                "&fLvl &2$level &fElite &7Spider",
                new ArrayList<>(List.of("$player &cbecame entangled in $entity's &cweb!",
                        "$entity &chas devoured $player&c!")),
                3);
    }
}
