package com.magmaguy.elitemobs.config.mobproperties.premade;

import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfigFields;
import org.bukkit.entity.EntityType;

import java.util.Arrays;

public class EliteHoglinConfig extends MobPropertiesConfigFields {
    public EliteHoglinConfig() {
        super("elite_hoglin",
                EntityType.HOGLIN,
                true,
                "&fLvl &2$level &fElite &dHoglin",
                Arrays.asList("$player &cmessed with the $entity &cand got the horns!",
                        "$entity &cgot $player's &cbacon!"),
                8);
    }
}