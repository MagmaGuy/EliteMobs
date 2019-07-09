package com.magmaguy.elitemobs.config.mobproperties.premade;

import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfigFields;
import org.bukkit.entity.EntityType;

import java.util.Arrays;

public class EliteStrayConfig extends MobPropertiesConfigFields {
    public EliteStrayConfig() {
        super("elite_stray",
                EntityType.STRAY,
                true,
                "&fLvl &2$level &fElite &bStray",
                Arrays.asList("$player was led astray by $entity!"));
    }
}
