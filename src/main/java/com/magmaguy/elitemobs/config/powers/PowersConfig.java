package com.magmaguy.elitemobs.config.powers;

import com.magmaguy.elitemobs.powers.meta.ElitePower;
import com.magmaguy.elitemobs.powers.lua.LuaPowerManager;
import com.magmaguy.magmacore.config.CustomConfig;

import java.util.HashMap;
import java.util.Map;

public class PowersConfig extends CustomConfig {

    private static Map<String, PowersConfigFields> powers = new HashMap<>();

    public PowersConfig() {
        super("powers", "com.magmaguy.elitemobs.config.powers.premade", PowersConfigFields.class);
        powers = new HashMap<>();
        for (String key : super.getCustomConfigFieldsHashMap().keySet()) {
            PowersConfigFields powersConfigFields = (PowersConfigFields) super.getCustomConfigFieldsHashMap().get(key);
            powers.put(key, powersConfigFields);
            powersConfigFields.initializeScripts();
        }

        Map<String, PowersConfigFields> premadeLuaPowers = PremadeLuaPowers.load();
        powers.putAll(premadeLuaPowers);

        powers.putAll(LuaPowerManager.discoverLuaPowers(powers.values()));
        powers.putAll(premadeLuaPowers);

        ElitePower.initializePowers();
        powers.values().forEach(ElitePower::registerConfiguredPower);
    }

    public static Map<String, PowersConfigFields> getPowers() {
        return powers;
    }

    public static PowersConfigFields getPower(String filename) {
        return powers.get(filename);
    }

}
