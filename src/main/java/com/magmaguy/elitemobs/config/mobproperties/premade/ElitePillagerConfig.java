package com.magmaguy.elitemobs.config.mobproperties.premade;

import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfigFields;
import org.bukkit.entity.EntityType;

import java.util.Arrays;

public class ElitePillagerConfig extends MobPropertiesConfigFields {
    public ElitePillagerConfig() {
        super("elite_pillager",
                EntityType.PILLAGER,
                true,
                "&fLvl &2$level &fElite &8Pillager",
                Arrays.asList("$entity &cplundered $player&c!",
                        "$entity &cpillaged $player&c!"),
                5);
    }
}
