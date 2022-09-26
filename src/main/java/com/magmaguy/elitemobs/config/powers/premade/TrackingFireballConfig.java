package com.magmaguy.elitemobs.config.powers.premade;

import com.magmaguy.elitemobs.config.ConfigurationEngine;
import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import com.magmaguy.elitemobs.powers.TrackingFireball;
import org.bukkit.Material;

public class TrackingFireballConfig extends PowersConfigFields {
    public static double fireballSpeed;

    public TrackingFireballConfig() {
        super("tracking_fireball",
                true,
                Material.FIRE_CHARGE.toString(),
                TrackingFireball.class,
                PowerType.MAJOR_GHAST);
    }

    @Override
    public void processAdditionalFields() {
        fireballSpeed = ConfigurationEngine.setDouble(fileConfiguration, "fireballSpeed", 0.5);
    }
}
