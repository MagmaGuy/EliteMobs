package com.magmaguy.elitemobs.config.mobproperties.premade;

import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfigFields;
import org.bukkit.entity.EntityType;

import java.util.Arrays;

public class EliteKillerBunnyConfig extends MobPropertiesConfigFields {
    public EliteKillerBunnyConfig(){
        super("elite_killer_bunny",
                EntityType.RABBIT,
                true,
                "&fLvl &2$level &fElite &cKiller Rabbit",
                Arrays.asList("$entity &cmade $player &cgo live in a farm upstate!",
                        "$entity &ceducated $player using a stick and not a carrot!"),
                12);
    }
}
