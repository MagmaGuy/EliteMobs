package com.magmaguy.elitemobs.config.mobproperties.premade;

import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfigFields;
import org.bukkit.entity.EntityType;

import java.util.List;

public class EliteSilverfishConfig extends MobPropertiesConfigFields {
    public EliteSilverfishConfig() {
        super("elite_silverfish",
                EntityType.SILVERFISH,
                true,
                "&fLvl &2$level &fElite &7Silverfish",
                List.of("$player &cmistook $entity &cfor a stone block!"),
                1);
    }
}
