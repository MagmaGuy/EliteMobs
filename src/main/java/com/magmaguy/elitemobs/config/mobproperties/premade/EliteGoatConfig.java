package com.magmaguy.elitemobs.config.mobproperties.premade;

import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfigFields;
import org.bukkit.entity.EntityType;

import java.util.ArrayList;
import java.util.List;

public class EliteGoatConfig extends MobPropertiesConfigFields {
    public EliteGoatConfig() {
        super("elite_goat",
                EntityType.GOAT,
                true,
                "&fLvl &2$level &fElite &5Goat",
                new ArrayList<>(List.of("$player &cwas rammed by $entity&c!",
                        "$player &cwas run over by $entity&c!",
                        "$player &cwas trampled by $entity&c!",
                        "$player &cgot $entity&c horns!")),
                3);
    }
}
