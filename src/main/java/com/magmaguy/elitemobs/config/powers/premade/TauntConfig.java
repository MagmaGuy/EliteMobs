package com.magmaguy.elitemobs.config.powers.premade;

import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import org.bukkit.Material;

public class TauntConfig extends PowersConfigFields {
    public TauntConfig() {
        super("taunt",
                true,
                "Taunt",
                Material.JUKEBOX.toString());
    }
}
