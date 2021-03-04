package com.magmaguy.elitemobs.config.powers.premade;

import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import org.bukkit.Material;

public class TrackingFireballConfig extends PowersConfigFields {
    public TrackingFireballConfig() {
        super("tracking_fireball",
                true,
                "Tracking Fireball",
                Material.FIRE_CHARGE.toString());
        getAdditionalConfigOptions().put("fireballSpeed", 0.5);
    }
}
