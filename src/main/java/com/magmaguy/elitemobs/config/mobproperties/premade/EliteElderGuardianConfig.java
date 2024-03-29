package com.magmaguy.elitemobs.config.mobproperties.premade;

import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfigFields;
import org.bukkit.entity.EntityType;

import java.util.List;

public class EliteElderGuardianConfig extends MobPropertiesConfigFields {
    public EliteElderGuardianConfig() {
        super("elite_elder_guardian",
                EntityType.ELDER_GUARDIAN,
                true,
                "&fLvl &2$level &fElite &3Elder Guardian",
                List.of("$entity &cprevented $player &cfrom exploring the depths!"),
                12);
    }
}
