package com.magmaguy.elitemobs.config.translations;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.DefaultConfig;
import com.magmaguy.magmacore.util.ChatColorConverter;
import com.magmaguy.magmacore.util.Logger;
import org.bukkit.Bukkit;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Handles loading and accessing translations from a per-language CSV file.
 * Each language has its own CSV with format: "key","en","<lang_code>"
 * Example: french.csv has columns "key","en","fr"
 */
public class TranslationsConfigFields {

    private static final String TRANSLATIONS_DATA_SUFFIX = "_data.csv";

    private final String languageName; // e.g., "french"
    private final String languageCode; // e.g., "fr"
    private TranslationCsvParser.TranslationData translationData;
    private TranslationCsvParser.TranslationData translationDataSnapshot;
    private Path translationsPath;
    private Path translationsDataPath;
    private boolean saving = false;
    private boolean dirty = false;

    public TranslationsConfigFields() {
        // Parse language name from config (e.g., "french" or "french.yml" -> "french")
        String configLang = DefaultConfig.getLanguage();
        this.languageName = configLang.replace(".yml", "").replace(".csv", "");
        this.languageCode = getLanguageCode(languageName.toLowerCase());
        initialize();
    }

    /**
     * Maps language names to their ISO codes for CSV column headers.
     * Special case: "custom" uses "custom" as both name and code.
     */
    private String getLanguageCode(String languageName) {
        return switch (languageName) {
            case "english" -> "en";
            case "custom" -> "custom"; // Special: custom language for user modifications
            case "french" -> "fr";
            case "german" -> "de";
            case "spanish" -> "es";
            case "italian" -> "it";
            case "brazilianportuguese" -> "pt";
            case "russian" -> "ru";
            case "chinese", "chinesesimplified" -> "zh_cn";
            case "chinesetraditional" -> "zh_tw";
            case "japanese" -> "ja";
            case "korean" -> "ko";
            case "polish" -> "pl";
            case "dutch" -> "nl";
            case "czech" -> "cs";
            case "hungarian" -> "hu";
            case "romanian" -> "ro";
            case "turkish" -> "tr";
            case "vietnamese" -> "vi";
            case "indonesian" -> "id";
            default -> languageName; // Use name as code if unknown
        };
    }

    private void initialize() {
        Path folder = Paths.get(MetadataHandler.PLUGIN.getDataFolder().getAbsolutePath(), "translations");
        String csvFilename = languageName + ".csv";
        translationsPath = folder.resolve(csvFilename);
        translationsDataPath = folder.resolve(languageName + TRANSLATIONS_DATA_SUFFIX);

        try {
            Files.createDirectories(folder);

            // Try to load existing CSV
            if (Files.exists(translationsPath)) {
                translationData = TranslationCsvParser.parse(translationsPath);
                Logger.info("Loaded translations from " + csvFilename);
            } else {
                // Try to copy bundled CSV from resources
                try (InputStream in = MetadataHandler.PLUGIN.getResource("translations/" + csvFilename)) {
                    if (in != null) {
                        Files.copy(in, translationsPath);
                        translationData = TranslationCsvParser.parse(translationsPath);
                        Logger.info("Copied bundled translation: " + csvFilename);
                    } else {
                        // Create empty translation data
                        translationData = new TranslationCsvParser.TranslationData(List.of("en", languageCode));
                        Logger.info("Created new empty translations for: " + languageName);
                    }
                }
            }

            // Ensure both language columns exist
            translationData.addLanguage("en");
            translationData.addLanguage(languageCode);

            // Load or create snapshot for tracking customizations
            if (Files.exists(translationsDataPath)) {
                translationDataSnapshot = TranslationCsvParser.parse(translationsDataPath);
            } else {
                translationDataSnapshot = new TranslationCsvParser.TranslationData(List.of("en", languageCode));
            }

            // Process bundled translations to auto-update unchanged entries
            processConfigFields();

        } catch (IOException e) {
            Logger.warn("Failed to initialize translations: " + e.getMessage());
            translationData = new TranslationCsvParser.TranslationData(List.of("en", languageCode));
            translationDataSnapshot = new TranslationCsvParser.TranslationData(List.of("en", languageCode));
        }
    }

    /**
     * Processes bundled translations and auto-updates unchanged entries.
     */
    private void processConfigFields() {
        String csvFilename = languageName + ".csv";
        try (InputStream in = MetadataHandler.PLUGIN.getResource("translations/" + csvFilename)) {
            if (in == null) return;

            Path tempPath = Files.createTempFile("elitemobs_trans_", ".csv");
            Files.copy(in, tempPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            TranslationCsvParser.TranslationData bundledData = TranslationCsvParser.parse(tempPath);
            Files.delete(tempPath);

            boolean updated = false;

            for (String key : bundledData.getKeys()) {
                Object bundledEnValue = bundledData.get(key, "en");
                if (bundledEnValue == null) continue;

                Object snapshotEnValue = translationDataSnapshot.get(key, "en");
                Object liveEnValue = translationData.get(key, "en");

                if (snapshotEnValue == null) {
                    // New key: add if not already present
                    if (liveEnValue == null) {
                        translationData.set(key, "en", bundledEnValue);
                        Object bundledLangValue = bundledData.get(key, languageCode);
                        if (bundledLangValue != null) {
                            translationData.set(key, languageCode, bundledLangValue);
                        }
                        updated = true;
                    }
                } else if (!bundledEnValue.equals(snapshotEnValue)) {
                    // Default changed: update if user hasn't customized
                    if (snapshotEnValue.equals(liveEnValue)) {
                        translationData.set(key, "en", bundledEnValue);
                        Object bundledLangValue = bundledData.get(key, languageCode);
                        if (bundledLangValue != null) {
                            translationData.set(key, languageCode, bundledLangValue);
                        }
                        Logger.info("Auto-updated translation: " + key);
                        updated = true;
                    }
                }

                // Update snapshot
                translationDataSnapshot.set(key, "en", bundledEnValue);
                Object bundledLangValue = bundledData.get(key, languageCode);
                if (bundledLangValue != null) {
                    translationDataSnapshot.set(key, languageCode, bundledLangValue);
                }
            }

            if (updated) {
                save();
                saveSnapshot();
            }

        } catch (IOException e) {
            Logger.warn("Failed to process bundled translations: " + e.getMessage());
        }
    }

    /**
     * Adds a translation key with its English value at runtime.
     */
    public void add(String filename, String key, Object value) {
        if (value == null) return;

        String filteredFilename = filename.replace(".yml", "");
        String realKey = filteredFilename + "." + key;

        // Process value (fix color codes)
        if (value instanceof String s) {
            String fixed = fixConfigColors(s);
            if (fixed.isEmpty()) return;
            value = fixed;
        } else if (value instanceof List<?> list) {
            @SuppressWarnings("unchecked")
            List<String> fixedList = fixConfigColors((List<String>) list);
            if (fixedList.isEmpty()) return;
            value = fixedList;
        }

        // Only add if key doesn't exist for English
        if (translationData.get(realKey, "en") != null) return;

        // Add English value
        translationData.set(realKey, "en", value);

        // Also add to target language column (placeholder for translator)
        if (!languageCode.equals("en") && translationData.get(realKey, languageCode) == null) {
            translationData.set(realKey, languageCode, value);
        }

        dirty = true;
        scheduleSave();
    }

    /**
     * Gets the translated value for a key.
     * Returns target language value if available, otherwise falls back to English.
     */
    public Object get(String filename, String key) {
        String filteredFilename = filename.replace(".yml", "");
        String realKey = filteredFilename + "." + key;

        // Try target language first (unless we're English)
        Object value = null;
        if (!languageCode.equals("en")) {
            value = translationData.get(realKey, languageCode);
        }

        // Fall back to English
        if (value == null) {
            value = translationData.get(realKey, "en");
        }

        // Apply color conversion
        if (value instanceof String s) {
            return ChatColorConverter.convert(s);
        } else if (value instanceof List<?> list) {
            @SuppressWarnings("unchecked")
            List<String> stringList = (List<String>) list;
            return ChatColorConverter.convert(stringList);
        }

        return value;
    }

    private void scheduleSave() {
        if (saving) return;
        saving = true;
        Bukkit.getScheduler().scheduleSyncDelayedTask(MetadataHandler.PLUGIN, () -> {
            if (dirty) {
                save();
                dirty = false;
            }
            saving = false;
        }, 100L);
    }

    private void save() {
        try {
            TranslationCsvParser.write(translationData, translationsPath);
        } catch (IOException e) {
            Logger.warn("Failed to save translations: " + e.getMessage());
        }
    }

    private void saveSnapshot() {
        try {
            TranslationCsvParser.write(translationDataSnapshot, translationsDataPath);
        } catch (IOException e) {
            Logger.warn("Failed to save translation snapshot: " + e.getMessage());
        }
    }

    private String fixConfigColors(String value) {
        if (value == null) return null;
        return value.replace("§", "&");
    }

    private List<String> fixConfigColors(List<String> values) {
        return values.stream()
                .map(this::fixConfigColors)
                .toList();
    }

    public String getLanguageName() {
        return languageName;
    }

    public String getLanguageCode() {
        return languageCode;
    }

    public void shutdown() {
        if (dirty) {
            save();
            dirty = false;
        }
    }
}
