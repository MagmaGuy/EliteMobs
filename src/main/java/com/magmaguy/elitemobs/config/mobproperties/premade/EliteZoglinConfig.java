package com.magmaguy.elitemobs.config.mobproperties.premade;

import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfigFields;
import org.bukkit.entity.EntityType;

import java.util.ArrayList;
import java.util.List;

public class EliteZoglinConfig extends MobPropertiesConfigFields {
    public EliteZoglinConfig() {
        super("elite_zoglin",
                EntityType.ZOGLIN,
                true,
                "&fLvl &2$level &fElite &dZoglin",
                new ArrayList<>(List.of("$player &cmessed with the $entity &cand got the horns!",
                        "$entity &cgot $player's &cbacon!")),
                12);
    }
}
