package com.magmaguy.elitemobs.config.mobproperties.premade;

import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfigFields;
import org.bukkit.entity.EntityType;

public class SuperSheepConfig extends MobPropertiesConfigFields {
    public SuperSheepConfig() {
        super("super_sheep",
                EntityType.SHEEP,
                true,
                "Super Sheep",
                null);
    }
}
