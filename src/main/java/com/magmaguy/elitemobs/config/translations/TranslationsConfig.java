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
        scriptTextKeys.clear();
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

    // $prefix is replaced as a literal substring and its stock texts run straight into words ("$prefixYou are..."), so it
    // is matched on its own first; otherwise "$prefixYou" and "$prefixDu" would read as two different placeholders.
    private static final java.util.regex.Pattern PLACEHOLDER = java.util.regex.Pattern.compile("\\$prefix|\\$[A-Za-z][A-Za-z0-9_]*");
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
        // One line per boot: an outdated language file can hit dozens of keys, and the remedy is the same for all of them.
        if (reportedStalePlaceholders.add(filename + "." + key) && reportedStalePlaceholders.size() == 1)
            Logger.info("Translation for " + filename.replace(".yml", "") + "." + key + " uses " + String.join(", ", unknown)
                    + ", which the current English text no longer has, so the English text is shown instead. Other lines from "
                    + "an outdated translation file are handled the same way without further messages.");
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

    private static final String SCRIPT_TEXT = "script_text";
    // Colour and hex codes, so that a fragment like "&r " or "&8] " is not offered to translators as a line of its own.
    private static final java.util.regex.Pattern FORMATTING =
            java.util.regex.Pattern.compile("[&§](#[0-9A-Fa-f]{6}|[0-9A-Fa-fK-Ok-oRrXx])");
    private static final java.util.Map<String, String> scriptTextKeys = new java.util.concurrent.ConcurrentHashMap<>();

    /**
     * Translates a line of script text, the messages and titles in EliteScript actions and the literals in Lua powers,
     * and returns it raw: callers fill in placeholders and convert colours afterwards, as they do for English.
     * <p>
     * Script text has no field name to key it by, and content packages are overwritten on every import, so the key is
     * derived from the English itself ({@code script_text.<hash>}, with the line in the {@code en} column). A
     * translation therefore only ever shows for the exact line it was written for, a line repeated across files and
     * packages is translated once, and the CSV, which imports never touch, carries the translation across package
     * updates. A reworded line is a new row and shows in English until it is translated; the old row stays behind.
     */
    public static String translateText(String english) {
        if (english == null || isEnglish() || translationsConfigFields == null) return english;
        String normalized = english.replace('§', '&');
        if (FORMATTING.matcher(normalized).replaceAll("").chars().noneMatch(Character::isLetter)) return english;
        String key = scriptTextKey(normalized);
        String claimed = scriptTextKeys.putIfAbsent(key, normalized);
        // Two lines sharing a hash would share a translation, so the second one stays English.
        if (claimed != null && !claimed.equals(normalized)) return english;
        translationsConfigFields.add(SCRIPT_TEXT, key, normalized);
        if (!(translationsConfigFields.getRaw(SCRIPT_TEXT, key) instanceof String translated)
                || usesUnknownPlaceholders(SCRIPT_TEXT, key, translated, normalized)) return english;
        return translated;
    }

    private static String scriptTextKey(String text) {
        try {
            byte[] digest = java.security.MessageDigest.getInstance("SHA-256")
                    .digest(text.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return java.util.HexFormat.of().formatHex(digest, 0, 6);
        } catch (java.security.NoSuchAlgorithmException impossible) {
            throw new IllegalStateException(impossible);
        }
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
