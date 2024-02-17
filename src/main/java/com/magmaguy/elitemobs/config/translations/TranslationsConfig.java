package com.magmaguy.elitemobs.config.translations;

import com.magmaguy.elitemobs.config.CustomConfig;
import com.magmaguy.elitemobs.config.DefaultConfig;
import com.magmaguy.elitemobs.utils.WarningMessage;
import lombok.Getter;

import java.util.HashMap;
import java.util.List;

public class TranslationsConfig extends CustomConfig {
    @Getter
    private static HashMap<String, TranslationsConfigFields> translationConfigs = new HashMap<>();

    /**
     * Initializes all configurations and stores them in a list for later access
     */
    public TranslationsConfig() {
        super("translations", "com.magmaguy.elitemobs.config.translations.premade", TranslationsConfigFields.class);
        translationConfigs = new HashMap<>();
        for (String key : super.getCustomConfigFieldsHashMap().keySet())
            translationConfigs.put(key, (TranslationsConfigFields) super.getCustomConfigFieldsHashMap().get(key));
    }

    /**
     * This saves a translatable element to the configuration files.
     */
    public static String add(String filename, String key, String value) {
        if (DefaultConfig.getLanguage().equals("english"))
            return value;
        TranslationsConfigFields selectedLanguage = translationConfigs.get(DefaultConfig.getLanguage());
        if (selectedLanguage == null) {
            new WarningMessage("Failed to get valid language from " + filename + " , defaulting to English! (String)");
            return value;
        }
        selectedLanguage.add(filename, key, value);
        return (String) selectedLanguage.get(filename, key);
    }

    public static List<String> add(String filename, String key, List<String> value) {
        if (DefaultConfig.getLanguage().equals("english"))
            return value;
        TranslationsConfigFields selectedLanguage = translationConfigs.get(DefaultConfig.getLanguage());
        if (selectedLanguage == null) {
            new WarningMessage("Failed to get valid language from " + filename + " , defaulting to English! (List)");
            return value;
        }
        selectedLanguage.add(filename, key, value);
        return (List<String>) selectedLanguage.get(filename, key);
    }

}
