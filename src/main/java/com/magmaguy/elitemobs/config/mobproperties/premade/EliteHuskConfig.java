package com.magmaguy.elitemobs.config.mobproperties.premade;

import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfigFields;
import org.bukkit.entity.EntityType;

import java.util.Arrays;

public class EliteHuskConfig extends MobPropertiesConfigFields {
    public EliteHuskConfig() {
        super("elite_husk",
                EntityType.HUSK,
                true,
                "&fLvl &2$level &fElite &7Husk",
                Arrays.asList("$player was hollowed out by $entity!"));
    }
}
