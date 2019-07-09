package com.magmaguy.elitemobs.config.mobproperties.premade;

import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfigFields;
import org.bukkit.entity.EntityType;

import java.util.Arrays;

public class EliteSilverfishConfig extends MobPropertiesConfigFields {
    public EliteSilverfishConfig() {
        super("elite_silverfish",
                EntityType.SILVERFISH,
                true,
                "&fLvl &2$level &fElite &7Silverfish",
                Arrays.asList("$player mistook $entity for a stone block!"));
    }
}
