package com.magmaguy.elitemobs.config.mobproperties.premade;

import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfigFields;
import org.bukkit.entity.EntityType;

import java.util.Arrays;

public class ElitePigZombieConfig extends MobPropertiesConfigFields {
    public ElitePigZombieConfig() {
        super("elite_pig_zombie",
                EntityType.PIG_ZOMBIE,
                true,
                "&fLvl &2$level &fElite &6Zombie Pigman",
                Arrays.asList("$player was mobbed to death by $entity!",
                        "$entity got $player's bacon!"));
    }
}
