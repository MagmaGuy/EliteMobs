package com.magmaguy.elitemobs.config.mobproperties.premade;

import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfigFields;
import org.bukkit.entity.EntityType;

public class SuperCowConfig extends MobPropertiesConfigFields {
    public SuperCowConfig() {
        super("super_cow",
                EntityType.COW,
                true,
                "&2Super Cow",
                null);
    }
}
