package com.magmaguy.elitemobs.config.mobproperties.premade;

import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfigFields;
import org.bukkit.entity.EntityType;

import java.util.ArrayList;
import java.util.List;

public class EliteWitchConfig extends MobPropertiesConfigFields {
    public EliteWitchConfig() {
        super("elite_witch",
                EntityType.WITCH,
                true,
                "&fLvl &2$level &fElite &5Witch",
                new ArrayList<>(List.of("$player &cbecame $entity's &ctest subject!",
                        "$player &chas been bewitched by $entity&c!")),
                6);
    }
}
