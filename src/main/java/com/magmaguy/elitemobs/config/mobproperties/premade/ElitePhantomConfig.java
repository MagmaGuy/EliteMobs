package com.magmaguy.elitemobs.config.mobproperties.premade;

import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfigFields;
import org.bukkit.entity.EntityType;

import java.util.Arrays;

public class ElitePhantomConfig extends MobPropertiesConfigFields {
    public ElitePhantomConfig() {
        super("elite_phantom",
                EntityType.PHANTOM,
                true,
                "&fLvl &2$level &fElite &9Phantom",
                Arrays.asList("$player just had a bad nightmare about $entity!",
                        "$player will be having nightmares about $entity!",
                        "$entity has taught $player the value of sleep!"));
    }
}
