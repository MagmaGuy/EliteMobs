package com.magmaguy.elitemobs.config.mobproperties.premade;

import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfigFields;
import org.bukkit.entity.EntityType;

public class SuperPigConfig extends MobPropertiesConfigFields {
    public SuperPigConfig() {
        super("super_pig",
                EntityType.PIG,
                true,
                "&dSuper Pig",
                null);
    }
}
