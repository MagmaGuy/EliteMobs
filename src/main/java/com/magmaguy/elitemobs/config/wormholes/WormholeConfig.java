package com.magmaguy.elitemobs.config.wormholes;

import com.magmaguy.elitemobs.config.CustomConfig;
import lombok.Getter;
import wormhole.Wormhole;

import java.util.HashMap;

public class WormholeConfig extends CustomConfig {
    @Getter
    private static HashMap<String, WormholeConfigFields> wormholes;

    public WormholeConfig() {
        super("wormholes", "com.magmaguy.elitemobs.config.wormholes.premade", WormholeConfigFields.class);
        wormholes = new HashMap<>();
        for (String key : super.getCustomConfigFieldsHashMap().keySet()) {
            if (super.getCustomConfigFieldsHashMap().get(key).isEnabled()) {
                wormholes.put(key, (WormholeConfigFields) super.getCustomConfigFieldsHashMap().get(key));
                if (super.getCustomConfigFieldsHashMap().get(key).isEnabled())
                    new Wormhole((WormholeConfigFields) super.getCustomConfigFieldsHashMap().get(key));
            }
        }
    }
}
