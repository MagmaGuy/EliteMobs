package com.magmaguy.elitemobs.config.mobproperties.premade;

import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfigFields;
import org.bukkit.entity.EntityType;

import java.util.Arrays;

public class ElitePiglinBruteConfig extends MobPropertiesConfigFields {
    public ElitePiglinBruteConfig() {
        super("elite_piglin_brute",
                EntityType.PIGLIN_BRUTE,
                true,
                "&fLvl &2$level &fElite &cPiglin Brute",
                Arrays.asList("$entity &ctenderized $player!",
                        "$entity &cbrutalized $player!"),
                19.5);
    }
}
