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
        if (!(result instanceof String translated)) return ChatColorConverter.convert(value);
        if (usesUnknownPlaceholders(filename, key, translated, value)) return ChatColorConverter.convert(value);
        return translated;
    }

    private static final java.util.regex.Pattern PLACEHOLDER = java.util.regex.Pattern.compile("\\$[A-Za-z][A-Za-z0-9_]*");
    private static final java.util.Set<String> reportedStalePlaceholders = java.util.concurrent.ConcurrentHashMap.newKeySet();

    /**
     * A translation that names a placeholder the current English text no longer has was written for an older default
     * (for example a skill template whose $token changed). Rendering it would show the raw token, so the English text is
     * served for that key until the translation is updated. The CSV itself is left untouched.
     */
    private static boolean usesUnknownPlaceholders(String filename, String key, String translated, String english) {
        java.util.Set<String> known = placeholders(english);
        java.util.Set<String> unknown = placeholders(translated);
        unknown.removeAll(known);
        if (unknown.isEmpty()) return false;
        if (reportedStalePlaceholders.add(filename + "." + key))
            Logger.warn("Translation for " + filename.replace(".yml", "") + "." + key + " uses " + String.join(", ", unknown)
                    + ", which the current English text no longer has. Showing the English text until that translation is updated.");
        return true;
    }

    private static java.util.Set<String> placeholders(String text) {
        java.util.Set<String> found = new java.util.TreeSet<>();
        if (text == null) return found;
        java.util.regex.Matcher matcher = PLACEHOLDER.matcher(text);
        while (matcher.find()) found.add(matcher.group());
        return found;
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
        if (!(result instanceof List)) return ChatColorConverter.convert(value);
        List<String> translated = (List<String>) result;
        if (usesUnknownPlaceholders(filename, key, String.join("\n", translated), value == null ? null : String.join("\n", value)))
            return ChatColorConverter.convert(value);
        return translated;
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
