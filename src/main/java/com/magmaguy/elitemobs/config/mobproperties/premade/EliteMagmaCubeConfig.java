package com.magmaguy.elitemobs.config.mobproperties.premade;

import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfigFields;
import org.bukkit.entity.EntityType;

import java.util.Arrays;

public class EliteMagmaCubeConfig extends MobPropertiesConfigFields {
    public EliteMagmaCubeConfig() {
        super("elite_magma_cube",
                EntityType.MAGMA_CUBE,
                true,
                "&2Lvl &2$level &fElite &6Magma Cube",
                Arrays.asList("$player was incinerated by $entity&f!", "$player got too close to $entity&f!"),
                6);
    }
}
