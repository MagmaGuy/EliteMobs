package com.magmaguy.elitemobs.config.mobproperties.premade;

import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfigFields;
import org.bukkit.entity.EntityType;

import java.util.Arrays;

public class EliteVexConfig extends MobPropertiesConfigFields {
    public EliteVexConfig() {
        super("elite_vex",
                EntityType.VEX,
                true,
                "&fLvl &2$level &fElite &bVex",
                Arrays.asList("$entity &chas vexed $player&c!"),
                13);
    }
}
