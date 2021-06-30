package com.magmaguy.elitemobs.config.mobproperties.premade;

import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfigFields;
import org.bukkit.entity.EntityType;

import java.util.Arrays;

public class EliteShulkerConfig extends MobPropertiesConfigFields {
    public EliteShulkerConfig(){
        super("elite_shulker",
                EntityType.SHULKER,
                true,
                "&fLvl &2$level &fElite &5Shulker",
                Arrays.asList("$entity &cshowed $player &cnew heights!"),
                4);
    }
}
