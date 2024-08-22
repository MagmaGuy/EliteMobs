package com.magmaguy.elitemobs.config.mobproperties.premade;

import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfigFields;
import org.bukkit.entity.EntityType;

import java.util.ArrayList;
import java.util.List;

public class EliteZombiefiedPiglin extends MobPropertiesConfigFields {
    public EliteZombiefiedPiglin() {
        super("elite_zombified_piglin",
                EntityType.ZOMBIFIED_PIGLIN,
                true,
                "&fLvl &2$level &fElite &6Zombified Piglin",
                new ArrayList<>(List.of("$player &cwas mobbed to death by $entity&c!",
                        "$entity &cgot $player's &cbacon!")),
                12);
    }
}
