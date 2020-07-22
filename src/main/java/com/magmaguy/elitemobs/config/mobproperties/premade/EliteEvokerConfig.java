package com.magmaguy.elitemobs.config.mobproperties.premade;

import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfigFields;
import org.bukkit.entity.EntityType;

import java.util.Arrays;

public class EliteEvokerConfig extends MobPropertiesConfigFields {
    public EliteEvokerConfig() {
        super("elite_evoker",
                EntityType.EVOKER,
                true,
                "&fLvl &2$level &fElite &5Evoker",
                Arrays.asList("$player &cwas enchanted by $entity&c!"),
                9);
    }
}
