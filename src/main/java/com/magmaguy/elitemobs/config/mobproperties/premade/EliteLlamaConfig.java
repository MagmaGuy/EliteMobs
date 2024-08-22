package com.magmaguy.elitemobs.config.mobproperties.premade;

import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfigFields;
import org.bukkit.entity.EntityType;

import java.util.ArrayList;
import java.util.List;

public class EliteLlamaConfig extends MobPropertiesConfigFields {
    public EliteLlamaConfig() {
        super("elite_llama",
                EntityType.LLAMA,
                true,
                "&fLvl &2$level &fElite &5Llama",
                new ArrayList<>(List.of("$player &cwas spit on by $entity&c!",
                        "$player &cwas made $entity&c angry!")),
                1);
    }
}
