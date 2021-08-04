package com.magmaguy.elitemobs.config.customevents;

import com.magmaguy.elitemobs.config.CustomConfig;

import java.util.HashMap;

public class CustomEventsConfig extends CustomConfig {

    private static HashMap<String, CustomEventsConfigFields> customEvents;

    public CustomEventsConfig() {
        super("customevents", "com.magmaguy.elitemobs.config.customevents.premade", CustomEventsConfigFields.class);
        customEvents = new HashMap<>();
        for (String key : super.getCustomConfigFieldsHashMap().keySet())
            customEvents.put(key, (CustomEventsConfigFields) super.getCustomConfigFieldsHashMap().get(key));
    }

    public static HashMap<String, ? extends CustomEventsConfigFields> getCustomEvents() {
        return customEvents;
    }

    public static CustomEventsConfigFields getCustomEvent(String fileName) {
        return customEvents.get(fileName);
    }

}
