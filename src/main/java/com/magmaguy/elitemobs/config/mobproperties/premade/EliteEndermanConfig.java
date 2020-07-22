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
                Arrays.asList("$entity &csent $player &cinto the void!",
                        "$player &clooked at $entity &cwrong!",
                        "$player &cand $entity &cbecame best friends!"),
                10);
    }
}
