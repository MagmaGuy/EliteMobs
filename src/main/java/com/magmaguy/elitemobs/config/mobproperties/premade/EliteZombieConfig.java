package com.magmaguy.elitemobs.config.mobproperties.premade;

import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfigFields;
import org.bukkit.entity.EntityType;

import java.util.ArrayList;
import java.util.List;

public class EliteZombieConfig extends MobPropertiesConfigFields {
    public EliteZombieConfig() {
        super("elite_zombie",
                EntityType.ZOMBIE,
                true,
                "&fLvl &2$level &fElite &2Zombie",
                new ArrayList<>(List.of("$player &cwas devoured by $entity&c!",
                        "$entity &cgot to $player's &cbrains!")),
                5);
    }
}
