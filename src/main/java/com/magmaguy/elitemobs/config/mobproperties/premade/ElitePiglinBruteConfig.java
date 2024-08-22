package com.magmaguy.elitemobs.config.mobproperties.premade;

import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfigFields;
import org.bukkit.entity.EntityType;

import java.util.ArrayList;
import java.util.List;

public class ElitePiglinBruteConfig extends MobPropertiesConfigFields {
    public ElitePiglinBruteConfig() {
        super("elite_piglin_brute",
                EntityType.PIGLIN_BRUTE,
                true,
                "&fLvl &2$level &fElite &cPiglin Brute",
                new ArrayList<>(List.of("$entity &ctenderized $player!",
                        "$entity &cbrutalized $player!")),
                19.5);
    }
}
