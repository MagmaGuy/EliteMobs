package com.magmaguy.elitemobs.config.wormholes;

import com.magmaguy.elitemobs.wormhole.Wormhole;
import com.magmaguy.magmacore.config.CustomConfig;
import com.magmaguy.magmacore.util.Logger;
import lombok.Getter;

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
                if (super.getCustomConfigFieldsHashMap().get(key).isEnabled()) {
                    WormholeConfigFields fields = (WormholeConfigFields) super.getCustomConfigFieldsHashMap().get(key);
                    if (fields.isInstanced()) {
                        if (isValidInstancedWormhole(fields)) Wormhole.registerInstancedBlueprint(fields);
                    } else new Wormhole(fields);
                }
            }
        }
    }

    /**
     * Instanced wormholes are copied into each dungeon instance, so both ends must sit in the same blueprint world.
     * Only a flight portal may leave location2 out.
     */
    private static boolean isValidInstancedWormhole(WormholeConfigFields fields) {
        String location1 = fields.getLocation1();
        if (location1 == null || !location1.contains(",")) {
            Logger.warn("Instanced wormhole " + fields.getFilename() + " needs location1 set to coordinates in its dungeon's blueprint world! It will not be created.");
            return false;
        }
        String location2 = fields.getLocation2();
        if (location2 == null && fields.isFlightPortal()) return true;
        if (location2 == null || !location2.contains(",") || !location2.split(",")[0].equals(location1.split(",")[0])) {
            Logger.warn("Instanced wormhole " + fields.getFilename() + " must have both ends in the same dungeon blueprint world! It will not be created.");
            return false;
        }
        return true;
    }
}
