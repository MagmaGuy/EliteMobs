package com.magmaguy.elitemobs.config.customarenas;

import com.magmaguy.elitemobs.instanced.arena.ArenaInstance;
import com.magmaguy.magmacore.config.CustomConfig;

import java.util.HashMap;

public class CustomArenasConfig extends CustomConfig {

    private static HashMap<String, CustomArenasConfigFields> customArenas;

    public CustomArenasConfig() {
        super("customarenas", "com.magmaguy.elitemobs.config.customarenas.premade", CustomArenasConfigFields.class);
        customArenas = new HashMap<>();
        for (String key : super.getCustomConfigFieldsHashMap().keySet())
            if (super.getCustomConfigFieldsHashMap().get(key).isEnabled()) {
                customArenas.put(key, (CustomArenasConfigFields) super.getCustomConfigFieldsHashMap().get(key));
                ArenaInstance.initializeArena((CustomArenasConfigFields) super.getCustomConfigFieldsHashMap().get(key));
            }
    }

    public static CustomArenasConfigFields getCustomArena(String string) {
        return customArenas.get(string);
    }

}
