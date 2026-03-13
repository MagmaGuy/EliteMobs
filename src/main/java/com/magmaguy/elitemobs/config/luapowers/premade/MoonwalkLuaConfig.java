package com.magmaguy.elitemobs.config.luapowers.premade;

import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import org.bukkit.Material;

public class MoonwalkLuaConfig extends SpawnSelfPotionLuaPowerConfig {
    public MoonwalkLuaConfig() {
        super("moonwalk",
                Material.SLIME_BLOCK.toString(),
                PowersConfigFields.PowerType.MISCELLANEOUS,
                "jump_boost",
                Integer.MAX_VALUE,
                3);
    }
}
