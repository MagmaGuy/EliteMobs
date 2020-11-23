package com.magmaguy.elitemobs.config.mobproperties.premade;

import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfigFields;
import org.bukkit.entity.EntityType;

import java.util.Arrays;

public class EliteBeeConfig extends MobPropertiesConfigFields {
    public EliteBeeConfig() {
        super("elite_bee",
                EntityType.BEE,
                true,
                "&fLvl &2$level &fElite &eBee",
                Arrays.asList("$player &cwas stung by $entity!"),
                3);
    }
}
