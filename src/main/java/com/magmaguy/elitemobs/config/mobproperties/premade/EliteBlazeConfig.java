package com.magmaguy.elitemobs.config.mobproperties.premade;

import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfigFields;
import org.bukkit.entity.EntityType;

import java.util.List;

public class EliteBlazeConfig extends MobPropertiesConfigFields {
    public EliteBlazeConfig() {
        super("elite_blaze",
                EntityType.BLAZE,
                true,
                "&fLvl &2$level &fElite &eBlaze",
                List.of("$player &cwas lit ablaze by $entity!"),
                9);
    }
}
