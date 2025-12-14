package com.magmaguy.elitemobs.config.translations;

import com.magmaguy.elitemobs.config.DefaultConfig;
import com.magmaguy.magmacore.util.Logger;
import lombok.Getter;

import java.util.List;

/**
 * Manages translations using per-language CSV files.
 * CSV format: "key","en","<lang_code>" with indexed keys for lists.
 *
 * Special modes:
 * - "english": Bypasses CSV entirely, uses plugin defaults
 * - "custom": Uses auto-generated English→English CSV for customization
 */
public class TranslationsConfig {

    @Getter
    private static TranslationsConfigFields translationsConfigFields;

    /**
     * Initializes the translation system.
     * Only creates TranslationsConfigFields if not using English.
     */
    public TranslationsConfig() {
        if (isEnglish()) {
            translationsConfigFields = null;
            Logger.info("Language set to English - using plugin defaults");
        } else {
            translationsConfigFields = new TranslationsConfigFields();
        }
    }

    /**
     * Adds a translatable string and returns the translated value.
     * If the language is English, returns the original value (no CSV involved).
     */
    public static String add(String filename, String key, String value) {
        if (isEnglish()) return value;

        if (translationsConfigFields == null) {
            Logger.warn("TranslationsConfig not initialized, defaulting to English! (String)");
            return value;
        }

        translationsConfigFields.add(filename, key, value);
        Object result = translationsConfigFields.get(filename, key);
        return result instanceof String ? (String) result : value;
    }

    /**
     * Adds a translatable list and returns the translated value.
     * If the language is English, returns the original value (no CSV involved).
     */
    @SuppressWarnings("unchecked")
    public static List<String> add(String filename, String key, List<String> value) {
        if (isEnglish()) return value;

        if (translationsConfigFields == null) {
            Logger.warn("TranslationsConfig not initialized, defaulting to English! (List)");
            return value;
        }

        translationsConfigFields.add(filename, key, value);
        Object result = translationsConfigFields.get(filename, key);
        return result instanceof List ? (List<String>) result : value;
    }

    /**
     * Checks if the current language is English (plugin defaults mode).
     */
    private static boolean isEnglish() {
        String lang = DefaultConfig.getLanguage();
        if (lang == null) return true;
        lang = lang.toLowerCase().replace(".yml", "").replace(".csv", "");
        return lang.equals("english") || lang.equals("en");
    }

    /**
     * Shuts down the translation system, saving any pending changes.
     */
    public static void shutdown() {
        if (translationsConfigFields != null) {
            translationsConfigFields.shutdown();
        }
    }
}
