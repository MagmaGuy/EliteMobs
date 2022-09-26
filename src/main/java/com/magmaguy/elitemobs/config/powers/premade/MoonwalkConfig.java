package com.magmaguy.elitemobs.config.powers.premade;

import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import org.bukkit.Material;

import java.util.List;
import java.util.Map;

public class MoonwalkConfig extends PowersConfigFields {
    public MoonwalkConfig() {
        super("moonwalk",
                true,
                Material.SLIME_BLOCK.toString(),
                addScriptEntry("JumpBoost",
                        List.of("EliteMobSpawnEvent"),
                        null,
                        List.of(Map.of(
                                "action", "POTION_EFFECT",
                                "duration", Integer.MAX_VALUE,
                                "target", "SELF",
                                "amplifier", 3,
                                "potionEffectType", "jump_boost"
                        )),
                        null),
                PowerType.MISCELLANEOUS);
    }
}
