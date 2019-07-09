package com.magmaguy.elitemobs.config.powers.premade;

import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import org.bukkit.Material;

public class MoonwalkConfig extends PowersConfigFields {
    public MoonwalkConfig() {
        super("moonwalk",
                true,
                "Moonwalk",
                Material.SLIME_BLOCK.toString());
    }
}
