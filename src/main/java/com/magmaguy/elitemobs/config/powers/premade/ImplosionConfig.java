package com.magmaguy.elitemobs.config.powers.premade;

import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import com.magmaguy.elitemobs.powers.Implosion;
import org.bukkit.Material;

public class ImplosionConfig extends PowersConfigFields {
    public ImplosionConfig() {
        super("implosion",
                true,
                Material.SLIME_BALL.toString(),
                Implosion.class,
                PowerType.MISCELLANEOUS);
    }
}
