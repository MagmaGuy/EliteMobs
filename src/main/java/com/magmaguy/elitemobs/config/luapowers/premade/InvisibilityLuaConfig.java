package com.magmaguy.elitemobs.config.luapowers.premade;

import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import org.bukkit.Material;

public class InvisibilityLuaConfig extends SpawnSelfPotionLuaPowerConfig {
    public InvisibilityLuaConfig() {
        super("invisibility",
                Material.GLASS_PANE.toString(),
                PowersConfigFields.PowerType.MISCELLANEOUS,
                "invisibility",
                Integer.MAX_VALUE,
                0);
    }
}
