package com.magmaguy.elitemobs.config.translations;

import com.magmaguy.elitemobs.config.CustomConfig;

import java.util.HashMap;

public class TranslationsConfig extends CustomConfig {
    private static HashMap<String, TranslationsConfigFields> customArenas;

    /**
     * Initializes all configurations and stores them in a list for later access
     */
    public TranslationsConfig() {
        super("translations", "com.magmaguy.elitemobs.config.translations.premade", TranslationsConfigFields.class);
        customArenas = new HashMap<>();
        for (String key : super.getCustomConfigFieldsHashMap().keySet())
            if (super.getCustomConfigFieldsHashMap().get(key).isEnabled()) {
                customArenas.put(key, (TranslationsConfigFields) super.getCustomConfigFieldsHashMap().get(key));
            }
    }

}
