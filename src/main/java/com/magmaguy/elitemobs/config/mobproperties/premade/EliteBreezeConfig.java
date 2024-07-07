package com.magmaguy.elitemobs.config.mobproperties.premade;

import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfigFields;
import org.bukkit.entity.EntityType;

import java.util.List;

public class EliteBreezeConfig extends MobPropertiesConfigFields {
    public EliteBreezeConfig() {
        super("elite_breeze",
                EntityType.BREEZE,
                true,
                "&2Lvl &2$level &2Elite &eBreeze",
                List.of("$player &cwas blown away by $entity!"),
                1.5);
    }
}
