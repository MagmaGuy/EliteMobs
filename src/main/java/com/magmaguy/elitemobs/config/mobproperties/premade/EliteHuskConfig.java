package com.magmaguy.elitemobs.config.mobproperties.premade;

import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfigFields;
import org.bukkit.entity.EntityType;

import java.util.List;

public class EliteHuskConfig extends MobPropertiesConfigFields {
    public EliteHuskConfig() {
        super("elite_husk",
                EntityType.HUSK,
                true,
                "&fLvl &2$level &fElite &7Husk",
                List.of("$player &cwas hollowed out by $entity&c!"),
                5);
    }
}
