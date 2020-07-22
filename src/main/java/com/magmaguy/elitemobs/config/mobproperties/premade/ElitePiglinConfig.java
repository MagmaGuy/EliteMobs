package com.magmaguy.elitemobs.config.mobproperties.premade;

import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfigFields;
import org.bukkit.entity.EntityType;

import java.util.Arrays;

public class ElitePiglinConfig extends MobPropertiesConfigFields {
    public ElitePiglinConfig() {
        super("elite_piglin",
                EntityType.PIGLIN,
                true,
                "&fLvl &2$level &fElite &ePiglin",
                Arrays.asList("$entity &cwill fetch a good price for $player's remains!",
                        "$entity &ctaught $player &cthe value of gold!"),
                13);
    }
}
