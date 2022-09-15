package com.magmaguy.elitemobs.config.mobproperties.premade;

import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfigFields;
import org.bukkit.entity.EntityType;

import java.util.List;

public class EliteEnderDragon extends MobPropertiesConfigFields {
    public EliteEnderDragon() {
        super("elite_ender_dragon",
                EntityType.ENDER_DRAGON,
                true,
                "&fLvl &2$level &fElite &5Ender Dragon",
                List.of("$entity &chas ended $player!"),
                7);
    }
}
