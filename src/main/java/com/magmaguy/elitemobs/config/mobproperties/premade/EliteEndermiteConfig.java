package com.magmaguy.elitemobs.config.mobproperties.premade;

import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfigFields;
import org.bukkit.entity.EntityType;

import java.util.List;

public class EliteEndermiteConfig extends MobPropertiesConfigFields {
    public EliteEndermiteConfig() {
        super("elitemob_endermite",
                EntityType.ENDERMITE,
                true,
                "&fLvl &2$level &fElite &7Endermite",
                List.of("$entity &cis to $player &cwhat David is to Goliath!"),
                3);
    }
}
