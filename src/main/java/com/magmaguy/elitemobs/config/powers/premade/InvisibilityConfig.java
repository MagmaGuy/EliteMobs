package com.magmaguy.elitemobs.config.powers.premade;

import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import org.bukkit.Material;

import java.util.List;
import java.util.Map;

public class InvisibilityConfig extends PowersConfigFields {
    public InvisibilityConfig() {
        super("invisibility",
                true,
                Material.GLASS_PANE.toString(),
                addScriptEntry("BossInvisibility",
                        List.of("EliteMobSpawnEvent"),
                        null,
                        List.of(Map.of(
                                "action", "POTION_EFFECT",
                                "duration", Integer.MAX_VALUE,
                                "target", "SELF",
                                "amplifier", 0,
                                "potionEffectType", "invisibility"
                        )),
                        null),
                PowerType.MISCELLANEOUS);
    }
}
