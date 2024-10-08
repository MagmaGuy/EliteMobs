package com.magmaguy.elitemobs.config.mobproperties.premade;

import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfigFields;
import org.bukkit.entity.EntityType;

import java.util.ArrayList;
import java.util.List;

public class ElitePhantomConfig extends MobPropertiesConfigFields {
    public ElitePhantomConfig() {
        super("elite_phantom",
                EntityType.PHANTOM,
                true,
                "&fLvl &2$level &fElite &9Phantom",
                new ArrayList<>(List.of("$player &cjust had a bad nightmare about $entity&c!",
                        "$player &cwill be having nightmares about $entity&c!",
                        "$entity &chas taught $player &cthe value of sleep!")),
                9);
    }
}
