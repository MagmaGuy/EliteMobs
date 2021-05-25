package com.magmaguy.elitemobs.config.mobproperties.premade;

import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfigFields;
import org.bukkit.entity.EntityType;

import java.util.Arrays;

public class EliteEndCrystal extends MobPropertiesConfigFields {
    public EliteEndCrystal() {
        super("elite_end_crystal",
                EntityType.ENDER_CRYSTAL,
                true,
                "&fLvl &2$level &fElite &5End Crystal",
                Arrays.asList(("$entity &ccrystallized $player!")),
                1);
    }
}
