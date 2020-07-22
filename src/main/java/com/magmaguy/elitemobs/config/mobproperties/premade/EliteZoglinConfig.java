package com.magmaguy.elitemobs.config.mobproperties.premade;

import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfigFields;
import org.bukkit.entity.EntityType;

import java.util.Arrays;

public class EliteZoglinConfig extends MobPropertiesConfigFields {
    public EliteZoglinConfig() {
        super("elite_zoglin",
                EntityType.ZOGLIN,
                true,
                "&fLvl &2$level &fElite &dZoglin",
                Arrays.asList("$player &cmessed with the $entity &cand got the horns!",
                        "$entity &cgot $player's &cbacon!"),
                12);
    }
}
