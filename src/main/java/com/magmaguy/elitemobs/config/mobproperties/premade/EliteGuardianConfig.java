package com.magmaguy.elitemobs.config.mobproperties.premade;

import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfigFields;
import org.bukkit.entity.EntityType;

import java.util.Arrays;

public class EliteGuardianConfig extends MobPropertiesConfigFields {
    public EliteGuardianConfig() {
        super("elite_guardian",
                EntityType.GUARDIAN,
                true,
                "&fLvl &2$level &fElite &3Guardian",
                Arrays.asList("$entity &cprevented $player &cfrom exploring the depths!"),
                9);
    }
}
