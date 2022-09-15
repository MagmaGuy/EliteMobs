package com.magmaguy.elitemobs.config.mobproperties.premade;

import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfigFields;
import org.bukkit.entity.EntityType;

import java.util.List;

public class EliteStrayConfig extends MobPropertiesConfigFields {
    public EliteStrayConfig() {
        super("elite_stray",
                EntityType.STRAY,
                true,
                "&fLvl &2$level &fElite &bStray",
                List.of("$player &cwas led astray by $entity&c!"),
                5);
    }
}
