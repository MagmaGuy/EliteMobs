package com.magmaguy.elitemobs.config.powers.premade;

import com.magmaguy.elitemobs.config.ConfigurationEngine;
import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import org.bukkit.Material;

public class TrackingFireballConfig extends PowersConfigFields {
    public static double fireballSpeed;

    public TrackingFireballConfig() {
        super("tracking_fireball",
                true,
                "Tracking Fireball",
                Material.FIRE_CHARGE.toString());
    }

    @Override
    public void processAdditionalFields() {
        fireballSpeed = ConfigurationEngine.setDouble(fileConfiguration, "fireballSpeed", 0.5);
    }
}
