package com.magmaguy.elitemobs.config.mobproperties.premade;

import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfigFields;
import org.bukkit.entity.EntityType;

import java.util.Arrays;

public class EliteIronGolemConfig extends MobPropertiesConfigFields {
    public EliteIronGolemConfig() {
        super("elite_iron_golem",
                EntityType.IRON_GOLEM,
                true,
                "&fLvl &2$level &fElite &fIron Golem",
                Arrays.asList("$player messed with the wrong $entity!",
                        "$player has been taught the way of fist by $entity!"));
    }
}
