package com.magmaguy.elitemobs.config.enchantments.premade;

import com.magmaguy.elitemobs.config.ConfigurationEngine;
import com.magmaguy.elitemobs.config.enchantments.EnchantmentsConfigFields;
import lombok.Getter;

public class EarthquakeConfig extends EnchantmentsConfigFields {
    @Getter
    private static String earthquakeActivationMessage;
    @Getter
    private static String earthquakeAvailableMessage;

    public EarthquakeConfig() {
        super("earthquake",
                true,
                "Earthquake",
                5,
                50);
    }
    @Override
    public void processAdditionalFields(){
        earthquakeActivationMessage = ConfigurationEngine.setString(fileConfiguration, "earthquakeActivationMessage", "&8[EliteMobs] &aEarthquake triggered! &c2 min cooldown!");
        earthquakeAvailableMessage = ConfigurationEngine.setString(fileConfiguration, "earthquakeAvailableMessage", "&8[EliteMobs] &2Earthquake cooldown over!");
    }

}
