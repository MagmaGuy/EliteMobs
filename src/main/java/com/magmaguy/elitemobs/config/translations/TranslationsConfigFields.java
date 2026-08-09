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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Handles loading and accessing translations from a per-language CSV file.
 * Each language has its own CSV with format: "key","en","<lang_code>"
 * Example: french.csv has columns "key","en","fr"
 * <p>
 * <b>How a corrected English default reaches an already-translated server</b>
 * The live CSV alone cannot answer the only question that matters when the plugin ships corrected text:
 * <em>did the shipped default change, or did a human deliberately write this?</em> Both look identical on disk.
 * Before this was tracked, {@code add} simply refused to touch any key that already had an {@code en} value, so a
 * server running French kept the original wrong English - and the translation derived from it - forever.
 * <p>
 * The reference point is persisted in a sidecar CSV, {@code <language>_data.csv}, with the same schema as the live
 * file. For every key it records the pair the live row was last reconciled against:
 * <ul>
 *     <li>{@code en} - the shipped English default the stored row was derived from.</li>
 *     <li>{@code <lang_code>} - the translation that was present at that same moment.</li>
 * </ul>
 * A sidecar rather than a fourth column in {@code <language>.csv}, because the live file is a published artifact:
 * it is downloaded verbatim from {@code magmaguy.com/api/elitemobs_translations/<language>.csv} and edited by
 * translators in spreadsheet software. {@link TranslationCsvParser.TranslationData} also models every column after
 * the key as a <em>language</em>, so a baseline column would be a fake language that
 * {@link TranslationCsvParser.TranslationData#getLanguages()} would happily hand to player-facing lookups. The
 * sidecar keeps the published schema untouched, is already excluded from the {@code /em language} listing, and a
 * missing one is a well defined state rather than a parse error.
 * <p>
 * <b>Ownership</b>
 * The {@code en} column is plugin-owned reference data: on a translated server it is only ever displayed when the
 * language cell is blank, so it is the shipped default's mirror, not a place anyone edits to change what players
 * see. The {@code <lang_code>} column is human-owned. Reconciliation therefore <strong>never writes the language
 * column of an existing row</strong> - it only ever refreshes {@code en}, and reports how many translations were
 * left pointing at English text that has since changed.
 */
public class TranslationsConfigFields {

    private static final String TRANSLATIONS_DATA_SUFFIX = "_data.csv";
    private static final String ENGLISH_COLUMN = "en";
    /**
     * Reconciliation counters are summarised in a single console line, so only a handful of example keys are kept.
     */
    private static final int REPORTED_KEY_SAMPLE_SIZE = 5;
    /**
     * Upper bound on how long the pending write may be pushed back while keys are still being registered.
     */
    private static final int MAX_FLUSH_DEFERRALS = 20;

    private final String languageName; // e.g., "french"
    private final String languageCode; // e.g., "fr"
    private final List<String> refreshedSample = new ArrayList<>();
    private final List<String> staleSample = new ArrayList<>();
    private final List<String> handWrittenSample = new ArrayList<>();
    private final List<String> customizedSample = new ArrayList<>();
    private TranslationCsvParser.TranslationData translationData;
    /**
     * The shipped English / shipped translation each live row was last reconciled against. Persisted to
     * {@code <language>_data.csv}.
     */
    private TranslationCsvParser.TranslationData baselineData;
    private Path translationsPath;
    private Path baselinePath;
    private boolean saving = false;
    private boolean dirty = false;
    private boolean baselineDirty = false;
    /**
     * Cleared when the CSV on disk could not be read, so a file EliteMobs failed to parse is never replaced by one
     * rebuilt from English defaults.
     */
    private boolean writable = true;
    private int addedKeyCount = 0;
    private int adoptedKeyCount = 0;
    private int refreshedKeyCount = 0;
    private int staleTranslationCount = 0;
    private int handWrittenTranslationCount = 0;
    private int customizedEnglishCount = 0;
    private boolean reported = false;
    /**
     * Bumped by every reconciliation that changed something, so a pending flush can tell whether boot has settled.
     */
    private int changeGeneration = 0;

    public TranslationsConfigFields() {
        // Parse language name from config (e.g., "french" or "french.yml" -> "french")
        String configLang = DefaultConfig.getLanguage();
        this.languageName = configLang.replace(".yml", "").replace(".csv", "");
        this.languageCode = getLanguageCode(languageName.toLowerCase());
        initialize();
    }

    /**
     * Reconciles one key against the English default the plugin currently ships.
     * <p>
     * Package-private and static so the decision table can be exercised without a server: this is the whole of the
     * "shipped default changed" versus "a human customised this" logic.
     *
     * @param live           the CSV that is served to players and edited by translators
     * @param baseline       the sidecar recording what each live row was derived from; mutated in place
     * @param key            the fully qualified translation key
     * @param shippedDefault the English default the running plugin currently ships, {@link String} or {@link List}
     * @param languageCode   the language column of the live CSV
     * @return what was decided, for reporting
     */
    static ReconciliationOutcome reconcile(TranslationCsvParser.TranslationData live,
                                           TranslationCsvParser.TranslationData baseline,
                                           String key,
                                           Object shippedDefault,
                                           String languageCode) {
        Object liveEnglish = live.get(key, ENGLISH_COLUMN);

        if (liveEnglish == null) {
            //Brand new key. The shipped default is both the English and the starting point for the translator.
            live.set(key, ENGLISH_COLUMN, shippedDefault);
            if (!ENGLISH_COLUMN.equals(languageCode) && live.get(key, languageCode) == null)
                live.set(key, languageCode, shippedDefault);
            baseline.set(key, ENGLISH_COLUMN, shippedDefault);
            recordTranslationBaseline(live, baseline, key, languageCode);
            return ReconciliationOutcome.ADDED;
        }

        Object baselineEnglish = baseline.get(key, ENGLISH_COLUMN);
        boolean adopting = baselineEnglish == null;
        if (adopting) {
            //A CSV written before baselines were tracked. The row as it stands becomes the reference point, which
            //resolves this boot toward refreshing the en cell: on a file with no recorded history there is nothing to
            //distinguish "the shipped default was corrected" from "someone edited the en cell", and en is the column
            //the plugin owns. The translation is recorded and left exactly as it is, so nothing a human wrote is lost.
            baselineEnglish = liveEnglish;
            baseline.set(key, ENGLISH_COLUMN, liveEnglish);
            recordTranslationBaseline(live, baseline, key, languageCode);
        }

        if (Objects.equals(shippedDefault, baselineEnglish))
            //The overwhelming majority of keys on every boot after the first.
            return adopting ? ReconciliationOutcome.BASELINE_ADOPTED : ReconciliationOutcome.UNCHANGED;

        if (!Objects.equals(liveEnglish, baselineEnglish))
            //Someone edited the en cell after it was last reconciled. Refreshing would overwrite that edit, so the
            //whole row is left alone and reported instead. The baseline is deliberately not advanced, so the key keeps
            //being reported until a human resolves it.
            return ReconciliationOutcome.ENGLISH_CUSTOMIZED;

        //The en cell still matches what the plugin shipped last time, so nobody has claimed it and the corrected
        //default is safe to write. The language column is untouched either way.
        live.set(key, ENGLISH_COLUMN, shippedDefault);
        baseline.set(key, ENGLISH_COLUMN, shippedDefault);
        return ReconciliationOutcome.ENGLISH_REFRESHED;
    }

    /**
     * Drops the recorded baselines for a language.
     * <p>
     * Must be called whenever {@code <language>.csv} is replaced from outside the plugin - a download from
     * {@code magmaguy.com/api/elitemobs_translations}, or a regenerated {@code custom.csv}. The sidecar describes the
     * file it was derived from; against a different file every row whose English happens to differ looks hand-edited,
     * and reconciliation would then correctly but uselessly refuse to touch any of them. Deleting it puts the language
     * back into the adoption path, which records the new file as the reference point without rewriting anything.
     */
    public static void discardBaseline(Path translationsFolder, String languageName) {
        try {
            Files.deleteIfExists(translationsFolder.resolve(languageName + TRANSLATIONS_DATA_SUFFIX));
        } catch (IOException e) {
            Logger.warn("Could not remove the stale translation baselines for " + languageName + ": " + e.getMessage());
        }
    }

    /**
     * Records the translation the live row currently carries, so a later boot can tell an untouched shipped
     * translation apart from one a translator wrote. Never records a null: an all-null row would be written back to
     * the sidecar as an empty line.
     */
    private static void recordTranslationBaseline(TranslationCsvParser.TranslationData live,
                                                  TranslationCsvParser.TranslationData baseline,
                                                  String key,
                                                  String languageCode) {
        if (ENGLISH_COLUMN.equals(languageCode)) return;
        Object translation = live.get(key, languageCode);
        if (translation == null) return;
        baseline.set(key, languageCode, translation);
    }

    /**
     * Maps language names to their ISO codes for CSV column headers.
     * Special case: "custom" uses "custom" as both name and code.
     */
    private String getLanguageCode(String languageName) {
        return switch (languageName) {
            case "english" -> ENGLISH_COLUMN;
            case "custom" -> "custom"; // Special: custom language for user modifications
            case "french" -> "fr";
            case "german" -> "de";
            case "spanish" -> "es";
            case "italian" -> "it";
            case "brazilianportuguese", "portuguesebrazilian" -> "pt_br";
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
        baselinePath = folder.resolve(languageName + TRANSLATIONS_DATA_SUFFIX);

        try {
            Files.createDirectories(folder);
        } catch (IOException e) {
            Logger.warn("Failed to create the translations folder: " + e.getMessage());
        }

        translationData = loadTranslations(csvFilename);
        translationData.addLanguage(ENGLISH_COLUMN);
        translationData.addLanguage(languageCode);

        //Loaded separately and never allowed to take the live translations down with it: an empty or truncated sidecar
        //is recoverable, and treating it as a total failure would blank the in-memory CSV and then write that blank
        //back over the translator's file.
        baselineData = loadBaseline();
        baselineData.addLanguage(ENGLISH_COLUMN);
        baselineData.addLanguage(languageCode);
    }

    private TranslationCsvParser.TranslationData loadTranslations(String csvFilename) {
        if (Files.exists(translationsPath)) {
            try {
                TranslationCsvParser.TranslationData parsed = TranslationCsvParser.parse(translationsPath);
                Logger.info("Loaded translations from " + csvFilename);
                return parsed;
            } catch (IOException e) {
                //Refuse to write anything for the rest of the session. Starting from a blank slate and saving would
                //replace a file EliteMobs merely failed to read with English defaults.
                writable = false;
                Logger.warn("Could not read translations/" + csvFilename + " (" + e.getMessage()
                        + "). EliteMobs is falling back to English text and will not modify the file. Fix or delete it"
                        + " and run '/em language " + languageName + "' to restore translations.");
                return new TranslationCsvParser.TranslationData(List.of(ENGLISH_COLUMN, languageCode));
            }
        }

        // Translations are downloaded on demand by /em language rather than bundled (they multiply the jar size
        // several times over), but honour a shaded-in CSV if one is ever present.
        try (InputStream in = MetadataHandler.PLUGIN.getResource("translations/" + csvFilename)) {
            if (in != null) {
                Files.copy(in, translationsPath);
                TranslationCsvParser.TranslationData parsed = TranslationCsvParser.parse(translationsPath);
                Logger.info("Copied bundled translation: " + csvFilename);
                return parsed;
            }
        } catch (IOException e) {
            Logger.warn("Failed to install the bundled translation " + csvFilename + ": " + e.getMessage());
        }

        Logger.info("Created new empty translations for: " + languageName);
        return new TranslationCsvParser.TranslationData(List.of(ENGLISH_COLUMN, languageCode));
    }

    /**
     * A missing sidecar is the normal state for every server upgrading from a build that did not record baselines:
     * every key then takes the adoption path, which records the file as it stands and rewrites nothing a human owns.
     */
    private TranslationCsvParser.TranslationData loadBaseline() {
        if (!Files.exists(baselinePath))
            return new TranslationCsvParser.TranslationData(List.of(ENGLISH_COLUMN, languageCode));
        try {
            return TranslationCsvParser.parse(baselinePath);
        } catch (IOException e) {
            Logger.warn("Could not read translations/" + languageName + TRANSLATIONS_DATA_SUFFIX + " (" + e.getMessage()
                    + "); rebuilding it from the current translations.");
            return new TranslationCsvParser.TranslationData(List.of(ENGLISH_COLUMN, languageCode));
        }
    }

    /**
     * Registers a translation key with the English default the plugin currently ships, reconciling it against the
     * recorded baseline. See the class javadoc for what each outcome means.
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

        ReconciliationOutcome outcome = reconcile(translationData, baselineData, realKey, value, languageCode);

        switch (outcome) {
            case UNCHANGED -> {
                //Nothing moved, so neither file needs rewriting. This is every key on a steady-state boot, which is
                //why an unchanged server no longer rewrites its CSV at all.
                return;
            }
            case ADDED -> {
                addedKeyCount++;
                dirty = true;
                baselineDirty = true;
            }
            case BASELINE_ADOPTED -> {
                adoptedKeyCount++;
                baselineDirty = true;
            }
            case ENGLISH_REFRESHED -> {
                refreshedKeyCount++;
                recordSample(refreshedSample, realKey);
                recordTranslationStaleness(realKey, value);
                dirty = true;
                baselineDirty = true;
            }
            case ENGLISH_CUSTOMIZED -> {
                customizedEnglishCount++;
                recordSample(customizedSample, realKey);
            }
        }

        changeGeneration++;
        scheduleSave();
    }

    /**
     * A refreshed English default leaves any existing translation describing text that no longer exists. The
     * translation is never deleted - on a translated server the CSV is the only copy of it - it is only counted so the
     * operator knows how much of the file is now out of date, and which remedy applies.
     * <p>
     * The recorded baseline translation separates the two cases: a translation that still matches what shipped can be
     * replaced wholesale by re-downloading the language, while one that no longer matches was written by a human and
     * a re-download would destroy it.
     */
    private void recordTranslationStaleness(String realKey, Object refreshedEnglish) {
        if (ENGLISH_COLUMN.equals(languageCode)) return;
        Object translated = translationData.get(realKey, languageCode);
        if (isMissingTranslation(translated)) return;
        //A translation identical to the new English is simply an untranslated placeholder.
        if (Objects.equals(translated, refreshedEnglish)) return;

        if (Objects.equals(translated, baselineData.get(realKey, languageCode))) {
            staleTranslationCount++;
            recordSample(staleSample, realKey);
        } else {
            handWrittenTranslationCount++;
            recordSample(handWrittenSample, realKey);
        }
    }

    private void recordSample(List<String> sample, String realKey) {
        if (sample.size() < REPORTED_KEY_SAMPLE_SIZE) sample.add(realKey);
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
        if (!ENGLISH_COLUMN.equals(languageCode)) {
            value = translationData.get(realKey, languageCode);
            if (isMissingTranslation(value)) {
                value = null;
            }
        }

        // Fall back to English
        if (value == null) {
            value = translationData.get(realKey, ENGLISH_COLUMN);
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

    private boolean isMissingTranslation(Object value) {
        if (value == null) return true;
        if (value instanceof String string) return string.isBlank();
        if (value instanceof List<?> list) {
            return list.isEmpty() || list.stream().allMatch(item ->
                    item == null || item.toString().isBlank());
        }
        return false;
    }

    /**
     * Configuration files register their keys across the whole of boot, so both the write and the report are held
     * until registrations have gone quiet for five seconds. A leading-edge debounce would report a fraction of the
     * corrections and call it the total.
     */
    private void scheduleSave() {
        if (saving) return;
        saving = true;
        scheduleFlush(0);
    }

    private void scheduleFlush(int attempt) {
        int generation = changeGeneration;
        Bukkit.getScheduler().scheduleSyncDelayedTask(MetadataHandler.PLUGIN, () -> {
            //Still registering keys, so the counters are not final yet. Bounded so a pathological caller cannot keep
            //the pending write in memory forever.
            if (changeGeneration != generation && attempt < MAX_FLUSH_DEFERRALS) {
                scheduleFlush(attempt + 1);
                return;
            }
            flush();
            saving = false;
        }, 100L);
    }

    /**
     * Writes whatever changed and reports the reconciliation once. Configuration files register their keys across the
     * whole of boot, so the report can only be accurate after that has settled.
     */
    private void flush() {
        if (dirty) {
            save();
            dirty = false;
        }
        if (baselineDirty) {
            saveBaseline();
            baselineDirty = false;
        }
        report();
    }

    /**
     * One console line for what changed, plus a warning only when something needs a human. Never one line per key:
     * a corrected release touches thousands of them.
     */
    private void report() {
        //A CSV that could not be read has already produced its own warning, and every counter below would just be
        //describing the empty in-memory copy.
        if (reported || !writable) return;
        reported = true;

        if (adoptedKeyCount > 0 && refreshedKeyCount == 0 && customizedEnglishCount == 0 && addedKeyCount == 0) {
            Logger.info("Recorded translation baselines for " + adoptedKeyCount + " keys in "
                    + languageName + ".csv - future corrections to the English defaults will now be applied automatically.");
            return;
        }

        if (addedKeyCount == 0 && refreshedKeyCount == 0 && customizedEnglishCount == 0) return;

        Logger.info(languageName + ".csv: " + addedKeyCount + " new keys, "
                + refreshedKeyCount + " English defaults corrected, "
                + customizedEnglishCount + " left alone because the English column was edited by hand"
                + (refreshedSample.isEmpty() ? "" : " (e.g. " + String.join(", ", refreshedSample) + ")"));

        if (staleTranslationCount > 0)
            Logger.warn(staleTranslationCount + " shipped translations in " + languageName
                    + ".csv now describe English text that has been corrected"
                    + (staleSample.isEmpty() ? "" : " (e.g. " + String.join(", ", staleSample) + ")")
                    + ". Nothing was overwritten. To pull the current translation, delete translations/"
                    + languageName + ".csv and translations/" + languageName + TRANSLATIONS_DATA_SUFFIX
                    + ", then run '/em language " + languageName + "'.");

        if (handWrittenTranslationCount > 0)
            Logger.warn(handWrittenTranslationCount + " translations you edited yourself in " + languageName
                    + ".csv now describe English text that has been corrected"
                    + (handWrittenSample.isEmpty() ? "" : " (e.g. " + String.join(", ", handWrittenSample) + ")")
                    + ". They were left exactly as you wrote them and need reviewing by hand - re-downloading the "
                    + "language would discard them.");

        if (customizedEnglishCount > 0 && !customizedSample.isEmpty())
            Logger.warn("Hand-edited English cells in " + languageName + ".csv were not refreshed (e.g. "
                    + String.join(", ", customizedSample) + "). Clear those cells to let EliteMobs manage them again.");
    }

    private void save() {
        //An unreadable CSV holds no translations in memory, so writing would replace the file with English defaults.
        if (!writable) return;
        try {
            TranslationCsvParser.write(translationData, translationsPath);
        } catch (IOException e) {
            Logger.warn("Failed to save translations: " + e.getMessage());
        }
    }

    private void saveBaseline() {
        //Baselines derived from a CSV that could not be read would describe nothing that exists.
        if (!writable) return;
        try {
            TranslationCsvParser.write(baselineData, baselinePath);
        } catch (IOException e) {
            Logger.warn("Failed to save translation baselines: " + e.getMessage());
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
        flush();
    }

    /**
     * What {@link #reconcile} decided for a single key.
     */
    enum ReconciliationOutcome {
        /**
         * The shipped default still matches the recorded baseline.
         */
        UNCHANGED,
        /**
         * The key was not in the CSV at all.
         */
        ADDED,
        /**
         * The key predates baseline tracking; the current row was recorded as the reference point and nothing was
         * rewritten.
         */
        BASELINE_ADOPTED,
        /**
         * The shipped default changed and the {@code en} cell still matched the baseline, so it was corrected.
         */
        ENGLISH_REFRESHED,
        /**
         * The shipped default changed but the {@code en} cell had been edited by hand, so the row was left alone.
         */
        ENGLISH_CUSTOMIZED
    }
}
