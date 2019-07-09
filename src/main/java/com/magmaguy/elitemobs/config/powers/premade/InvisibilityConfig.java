package com.magmaguy.elitemobs.config.powers.premade;

import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import org.bukkit.Material;

public class InvisibilityConfig extends PowersConfigFields {
    public InvisibilityConfig() {
        super("invisibility",
                true,
                "Invisibility",
                Material.GLASS_PANE.toString());
    }
}
