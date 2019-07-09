package com.magmaguy.elitemobs.config.powers.premade;

import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import org.bukkit.Material;

public class ImplosionConfig extends PowersConfigFields {
    public ImplosionConfig() {
        super("implosion",
                true,
                "Implosion",
                Material.SLIME_BALL.toString());
    }
}
