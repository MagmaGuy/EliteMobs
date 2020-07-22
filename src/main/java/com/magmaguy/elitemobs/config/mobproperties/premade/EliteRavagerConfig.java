package com.magmaguy.elitemobs.config.mobproperties.premade;

import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfigFields;
import org.bukkit.entity.EntityType;

import java.util.Arrays;

public class EliteRavagerConfig extends MobPropertiesConfigFields {
    public EliteRavagerConfig() {
        super("elite_ravager",
                EntityType.RAVAGER,
                true,
                "&fLvl &2$level &fElite &6Ravager",
                Arrays.asList("$entity &cshowed $player &cwho's in charge!"),
                18);
    }
}
