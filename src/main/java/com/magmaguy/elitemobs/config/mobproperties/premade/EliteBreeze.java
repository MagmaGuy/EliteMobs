package com.magmaguy.elitemobs.config.mobproperties.premade;

import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfigFields;
import org.bukkit.entity.EntityType;

import java.util.List;

public class EliteBreeze extends MobPropertiesConfigFields {
    public EliteBreeze() {
        super("elite_breeze",
                EntityType.BOGGED,
                true,
                "&2Lvl &2$level &2Elite &eBreeze",
                List.of("$player &cwas blown away by $entity!"),
                1.5);
    }
}
