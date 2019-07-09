package com.magmaguy.elitemobs.config.mobproperties.premade;

import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfigFields;
import org.bukkit.entity.EntityType;

import java.util.Arrays;

public class EliteSpiderConfig extends MobPropertiesConfigFields {
    public EliteSpiderConfig() {
        super("elite_spider",
                EntityType.SPIDER,
                true,
                "&fLvl &2$level &fElite &7Spider",
                Arrays.asList("$player became entangled in $entity's web!",
                        "$entity has devoured $player!"));
    }
}
