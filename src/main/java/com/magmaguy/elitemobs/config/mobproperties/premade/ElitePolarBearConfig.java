package com.magmaguy.elitemobs.config.mobproperties.premade;

import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfigFields;
import org.bukkit.entity.EntityType;

import java.util.Arrays;

public class ElitePolarBearConfig extends MobPropertiesConfigFields {
    public ElitePolarBearConfig() {
        super("elite_polar_bear",
                EntityType.POLAR_BEAR,
                true,
                "&fLvl &2$level &fElite &fPolar Bear",
                Arrays.asList("$player was clawed to death by $entity!",
                        "$player was mauled to death by $entity!"));
    }
}
