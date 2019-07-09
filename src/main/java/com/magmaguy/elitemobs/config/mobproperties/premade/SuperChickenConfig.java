package com.magmaguy.elitemobs.config.mobproperties.premade;

import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfigFields;
import org.bukkit.entity.EntityType;

public class SuperChickenConfig extends MobPropertiesConfigFields {
    public SuperChickenConfig() {
        super("super_chicken",
                EntityType.CHICKEN,
                true,
                "&2Super Chicken",
                null);
    }
}
