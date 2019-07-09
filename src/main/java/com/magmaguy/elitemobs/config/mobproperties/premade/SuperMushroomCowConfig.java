package com.magmaguy.elitemobs.config.mobproperties.premade;

import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfigFields;
import org.bukkit.entity.EntityType;

public class SuperMushroomCowConfig extends MobPropertiesConfigFields {
    public SuperMushroomCowConfig() {
        super("super_mushroom_cow",
                EntityType.MUSHROOM_COW,
                true,
                "&2Super Mooshroom Cow",
                null);
    }
}
