package com.magmaguy.elitemobs.config.mobproperties.premade;

import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfigFields;
import org.bukkit.entity.EntityType;

import java.util.Arrays;

public class EliteCreeperConfig extends MobPropertiesConfigFields {
    public EliteCreeperConfig() {
        super("elite_creeper",
                EntityType.CREEPER,
                true,
                "&fLvl &2$level &fElite &2Creeper",
                Arrays.asList("$player was blasted away by $entity!"));
    }
}
