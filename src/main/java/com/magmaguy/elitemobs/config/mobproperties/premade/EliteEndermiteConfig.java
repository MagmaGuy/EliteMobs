package com.magmaguy.elitemobs.config.mobproperties.premade;

import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfigFields;
import org.bukkit.entity.EntityType;

import java.util.Arrays;

public class EliteEndermiteConfig extends MobPropertiesConfigFields {
    public EliteEndermiteConfig() {
        super("elitemob_endermite",
                EntityType.ENDERMITE,
                true,
                "&fLvl &2$level &fElite &7Endermite",
                Arrays.asList("$entity is to $player what David is to Goliath!"));
    }
}
