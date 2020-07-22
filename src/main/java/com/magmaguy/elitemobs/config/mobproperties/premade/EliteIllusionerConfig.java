package com.magmaguy.elitemobs.config.mobproperties.premade;

import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfigFields;
import org.bukkit.entity.EntityType;

import java.util.Arrays;

public class EliteIllusionerConfig extends MobPropertiesConfigFields {
    public EliteIllusionerConfig() {
        super("elite_illusioner",
                EntityType.ILLUSIONER,
                true,
                "&fLvl &2$level &fElite &8Illusioner",
                Arrays.asList("$player&c fell for $entity's &cillusions!"),
                5);
    }
}
