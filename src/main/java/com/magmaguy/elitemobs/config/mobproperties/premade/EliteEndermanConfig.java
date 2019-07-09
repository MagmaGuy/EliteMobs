package com.magmaguy.elitemobs.config.mobproperties.premade;

import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfigFields;
import org.bukkit.entity.EntityType;

import java.util.Arrays;

public class EliteEndermanConfig extends MobPropertiesConfigFields {
    public EliteEndermanConfig() {
        super("elite_enderman",
                EntityType.ENDERMAN,
                true,
                "&fLvl &2$level &fElite &5Enderman",
                Arrays.asList("$entity sent $player into the void!",
                        "$player looked at $entity wrong!"));
    }
}
