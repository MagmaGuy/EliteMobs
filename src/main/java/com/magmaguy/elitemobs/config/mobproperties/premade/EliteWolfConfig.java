package com.magmaguy.elitemobs.config.mobproperties.premade;

import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfigFields;
import org.bukkit.entity.EntityType;

import java.util.Arrays;

public class EliteWolfConfig extends MobPropertiesConfigFields {
    public EliteWolfConfig() {
        super("elite_wolf",
                EntityType.WOLF,
                true,
                "[$level] Elite Wolf",
                Arrays.asList("$entity tore $player apart!",
                        "$player was torn to shreds by $entity!"),
                6);
    }
}
