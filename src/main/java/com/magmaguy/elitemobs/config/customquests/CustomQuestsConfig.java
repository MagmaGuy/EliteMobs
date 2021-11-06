package com.magmaguy.elitemobs.config.customquests;

import com.magmaguy.elitemobs.config.CustomConfig;

import java.util.HashMap;

public class CustomQuestsConfig extends CustomConfig {
    private static HashMap<String, CustomQuestsConfigFields> customQuests = new HashMap<>();

    public CustomQuestsConfig() {
        super("customquests", "com.magmaguy.elitemobs.config.customquests.premade", CustomQuestsConfigFields.class);
        customQuests = new HashMap<>();
        for (String key : super.getCustomConfigFieldsHashMap().keySet())
            customQuests.put(key, (CustomQuestsConfigFields) super.getCustomConfigFieldsHashMap().get(key));
    }

    public static HashMap<String, CustomQuestsConfigFields> getCustomQuests(){
        return customQuests;
    }
}
