package com.magmaguy.elitemobs.config.powers.premade;

import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import org.bukkit.Material;

public class CorpseConfig extends PowersConfigFields {
    public CorpseConfig() {
        super("corpse",
                true,
                "Corpse",
                Material.BONE_BLOCK.toString());
    }
}