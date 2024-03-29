package com.magmaguy.elitemobs.config.mobproperties.premade;

import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfigFields;
import org.bukkit.entity.EntityType;

import java.util.List;

public class EliteVindicatorConfig extends MobPropertiesConfigFields {
    public EliteVindicatorConfig() {
        super("elite_vindicator",
                EntityType.VINDICATOR,
                true,
                "&fLvl &2$level &fElite &8Vindicator",
                List.of("$entity &cvindicated his fighting skills over $player&c!"),
                19.5);
    }
}
