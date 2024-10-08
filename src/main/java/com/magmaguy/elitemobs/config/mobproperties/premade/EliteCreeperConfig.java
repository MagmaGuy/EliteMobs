package com.magmaguy.elitemobs.config.mobproperties.premade;

import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfigFields;
import org.bukkit.entity.EntityType;

import java.util.ArrayList;
import java.util.List;

public class EliteCreeperConfig extends MobPropertiesConfigFields {
    public EliteCreeperConfig() {
        super("elite_creeper",
                EntityType.CREEPER,
                true,
                "&fLvl &2$level &fElite &2Creeper",
                new ArrayList<>(List.of("$player &cwas blasted away by $entity!",
                        "$entity &cjust oh man'd $player&c!")),
                64.5);
    }
}
