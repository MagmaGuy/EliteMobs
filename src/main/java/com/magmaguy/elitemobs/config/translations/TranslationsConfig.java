package com.magmaguy.elitemobs.config.translations;

import com.magmaguy.elitemobs.config.DefaultConfig;
import com.magmaguy.magmacore.util.ChatColorConverter;
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
     * Adds a translatable string and returns the display-ready (color-converted) value.
     * Callers receive a value with gradients/hex expanded for in-game rendering.
     * The CSV stores the raw format (e.g. gradient tags) so translators see readable text.
     */
    public static String add(String filename, String key, String value) {
        if (isEnglish()) return ChatColorConverter.convert(value);

        if (translationsConfigFields == null) {
            Logger.warn("TranslationsConfig not initialized, defaulting to English! (String)");
            return ChatColorConverter.convert(value);
        }

        translationsConfigFields.add(filename, key, value);
        Object result = translationsConfigFields.get(filename, key);
        return result instanceof String ? (String) result : ChatColorConverter.convert(value);
    }

    /**
     * Adds a translatable list and returns the display-ready (color-converted) value.
     * Callers receive values with gradients/hex expanded for in-game rendering.
     * The CSV stores the raw format so translators see readable text.
     */
    @SuppressWarnings("unchecked")
    public static List<String> add(String filename, String key, List<String> value) {
        if (isEnglish()) return ChatColorConverter.convert(value);

        if (translationsConfigFields == null) {
            Logger.warn("TranslationsConfig not initialized, defaulting to English! (List)");
            return ChatColorConverter.convert(value);
        }

        translationsConfigFields.add(filename, key, value);
        Object result = translationsConfigFields.get(filename, key);
        return result instanceof List ? (List<String>) result : ChatColorConverter.convert(value);
    }

    /**
     * Checks if the current language is English (plugin defaults mode).
     */
    public static boolean isEnglish() {
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
