package com.magmaguy.elitemobs.config.mobproperties.premade;

import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfigFields;
import org.bukkit.entity.EntityType;

import java.util.ArrayList;
import java.util.List;

public class EliteWitherConfig extends MobPropertiesConfigFields {
    public EliteWitherConfig() {
        super("elite_wither",
                EntityType.WITHER,
                true,
                "&fLvl &2$level &7Elite &5Wither",
                new ArrayList<>(List.of("$player &cangered $entity&c!",
                        "$player &chas met $entity's &cfury!")),
                12);
    }
}
