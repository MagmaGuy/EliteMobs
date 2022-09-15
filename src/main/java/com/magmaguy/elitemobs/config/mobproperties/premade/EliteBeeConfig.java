package com.magmaguy.elitemobs.config.mobproperties.premade;

import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfigFields;
import org.bukkit.entity.EntityType;

import java.util.List;

public class EliteBeeConfig extends MobPropertiesConfigFields {
    public EliteBeeConfig() {
        super("elite_bee",
                EntityType.BEE,
                true,
                "&fLvl &2$level &fElite &eBee",
                List.of("$player &cwas stung by $entity!"),
                3);
    }
}
