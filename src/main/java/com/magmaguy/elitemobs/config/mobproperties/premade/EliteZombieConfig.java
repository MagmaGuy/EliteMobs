package com.magmaguy.elitemobs.config.mobproperties.premade;

import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfigFields;
import org.bukkit.entity.EntityType;

import java.util.Arrays;

public class EliteZombieConfig extends MobPropertiesConfigFields {
    public EliteZombieConfig() {
        super("elite_zombie",
                EntityType.ZOMBIE,
                true,
                "&fLvl &2$level &fElite &2Zombie",
                Arrays.asList("$player was devoured by $entity!",
                        "$entity got to $player's brains!"));
    }
}
