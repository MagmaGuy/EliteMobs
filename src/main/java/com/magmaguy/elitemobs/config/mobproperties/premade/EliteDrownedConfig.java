package com.magmaguy.elitemobs.config.mobproperties.premade;

import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfigFields;
import org.bukkit.entity.EntityType;

import java.util.Arrays;

public class EliteDrownedConfig extends MobPropertiesConfigFields {
    public EliteDrownedConfig() {
        super("elite_drowned",
                EntityType.DROWNED,
                true,
                "&fLvl &2$level &fElite &3Drowned",
                Arrays.asList("$player &chas been brought down to the depths by $entity!",
                        "$player &chas been drowned by $entity!"),
                4);
    }
}
