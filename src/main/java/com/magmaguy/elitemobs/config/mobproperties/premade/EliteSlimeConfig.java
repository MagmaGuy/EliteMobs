package com.magmaguy.elitemobs.config.mobproperties.premade;

import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfigFields;
import org.bukkit.entity.EntityType;

import java.util.List;

public class EliteSlimeConfig extends MobPropertiesConfigFields {
    public EliteSlimeConfig() {
        super("elite_slime",
                EntityType.SLIME,
                true,
                "&2Lvl &2$level &fElite &2Slime",
                List.of("$player was squished by $entity&f!"),
                6);
    }
}
