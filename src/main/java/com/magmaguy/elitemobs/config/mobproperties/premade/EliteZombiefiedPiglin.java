package com.magmaguy.elitemobs.config.mobproperties.premade;

import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfigFields;
import org.bukkit.entity.EntityType;

import java.util.Arrays;

public class EliteZombiefiedPiglin extends MobPropertiesConfigFields {
    public EliteZombiefiedPiglin() {
        super("elite_zombified_piglin",
                EntityType.ZOMBIFIED_PIGLIN,
                true,
                "&fLvl &2$level &fElite &6Zombified Piglin",
                Arrays.asList("$player &cwas mobbed to death by $entity&c!",
                        "$entity &cgot $player's &cbacon!"),
                12);
    }
}
