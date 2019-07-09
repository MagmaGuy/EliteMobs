package com.magmaguy.elitemobs.config.mobproperties.premade;

import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfigFields;
import org.bukkit.entity.EntityType;

import java.util.Arrays;

public class EliteVindicatorConfig extends MobPropertiesConfigFields {
    public EliteVindicatorConfig() {
        super("elite_vindicator",
                EntityType.VINDICATOR,
                true,
                "&fLvl &2$level &fElite &7Vindicator",
                Arrays.asList("$entity has vindicated his combat skills from $player!"));
    }
}
