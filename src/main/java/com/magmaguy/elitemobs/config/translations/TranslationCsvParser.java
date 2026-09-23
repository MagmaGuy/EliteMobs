package com.magmaguy.elitemobs.config.translations;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses and writes CSV translation files with support for indexed list entries.
 * Format: "key","en","fr","de",...
 * List items use indexed keys: "ItemSettings.lore[0]","value","valeur","wert"
 */
public class TranslationCsvParser {

    private static final Pattern LIST_INDEX_PATTERN = Pattern.compile("^(.+)\\[(\\d+)]$");
    // Bound sparse-to-dense amplification before allocating the list backing arrays.
    private static final int MAX_LIST_ENTRIES = 16_384;
    private static final long MAX_EXPANDED_CELLS = 1_000_000;

    /**
     * Parses a CSV translation file.
     * @param file The CSV file to parse
     * @return TranslationData containing all parsed translations
     * @throws IOException if file cannot be read
     */
    public static TranslationData parse(File file) throws IOException {
        return parse(file.toPath());
    }

    /**
     * Parses a CSV translation file.
     * @param path The path to the CSV file
     * @return TranslationData containing all parsed translations
     * @throws IOException if file cannot be read
     */
    public static TranslationData parse(Path path) throws IOException {
        List<String[]> rows = new ArrayList<>();

        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String line;
            StringBuilder currentRow = new StringBuilder();
            boolean inQuotes = false;
            boolean firstLine = true;

            while ((line = reader.readLine()) != null) {
                // Strip UTF-8 BOM from first line if present
                if (firstLine) {
                    firstLine = false;
                    if (line.length() > 0 && line.charAt(0) == '\uFEFF') {
                        line = line.substring(1);
                    }
                }

                // Skip empty lines at the start
                if (currentRow.length() == 0 && line.trim().isEmpty()) continue;
                // Skip comment lines
                if (currentRow.length() == 0 && line.trim().startsWith("#")) continue;

                currentRow.append(line);

                // Count quotes to determine if we're in a multi-line field
                for (char c : line.toCharArray()) {
                    if (c == '"') inQuotes = !inQuotes;
                }

                if (!inQuotes) {
                    // Complete row
                    String[] fields = parseCSVRow(currentRow.toString());
                    if (fields.length > 0) {
                        rows.add(fields);
                    }
                    currentRow = new StringBuilder();
                } else {
                    // Continue to next line (multi-line field)
                    currentRow.append("\n");
                }
            }

            if (inQuotes) {
                throw new IOException("CSV contains an unterminated quoted field");
            }
        }

        if (rows.isEmpty()) {
            throw new IOException("CSV file is empty or has no valid rows");
        }

        // First row is header with languages
        String[] header = rows.get(0);
        if (header.length < 2) {
            throw new IOException("CSV header must have at least 'key' and one language column");
        }
        if (!"key".equalsIgnoreCase(header[0].trim())) {
            throw new IOException("CSV header must start with a 'key' column");
        }

        // Extract languages (skip first column which is "key")
        List<String> languages = new ArrayList<>();
        for (int i = 1; i < header.length; i++) {
            String language = header[i].trim().toLowerCase(Locale.ROOT);
            if (language.isBlank()) {
                throw new IOException("CSV contains a blank language column");
            }
            if (languages.contains(language)) {
                throw new IOException("CSV contains duplicate language column '" + language + "'");
            }
            languages.add(language);
        }

        TranslationData data = new TranslationData(languages);

        // Temporary storage for indexed entries to reconstruct lists
        Map<String, Map<String, TreeMap<Integer, String>>> indexedEntries = new LinkedHashMap<>();

        // Parse data rows
        for (int rowIndex = 1; rowIndex < rows.size(); rowIndex++) {
            String[] row = rows.get(rowIndex);
            if (row.length == 0 || row[0].trim().isEmpty()) continue;
            if (row.length > header.length) {
                throw new IOException(
                        "CSV row " + (rowIndex + 1) + " has more columns than the header");
            }

            String rawKey = row[0].trim();
            Matcher matcher = LIST_INDEX_PATTERN.matcher(rawKey);

            if (matcher.matches()) {
                // This is an indexed list entry
                String baseKey = matcher.group(1);
                if (data.hasKey(baseKey))
                    throw new IOException("CSV row " + (rowIndex + 1) + " mixes scalar and list entries for " + baseKey);
                int index;
                try {
                    index = Integer.parseInt(matcher.group(2));
                } catch (NumberFormatException failure) {
                    throw new IOException("CSV row " + (rowIndex + 1) + " has an invalid list index: " + rawKey, failure);
                }
                if (index >= MAX_LIST_ENTRIES)
                    throw new IOException("CSV row " + (rowIndex + 1) + " exceeds " + MAX_LIST_ENTRIES
                            + " entries per translation list: " + rawKey);

                for (int langIdx = 0; langIdx < languages.size() && (langIdx + 1) < row.length; langIdx++) {
                    String language = languages.get(langIdx);
                    String value = row[langIdx + 1];

                    indexedEntries
                            .computeIfAbsent(baseKey, k -> new LinkedHashMap<>())
                            .computeIfAbsent(language, k -> new TreeMap<>())
                            .put(index, value);
                }
            } else {
                // Regular string entry
                if (indexedEntries.containsKey(rawKey))
                    throw new IOException("CSV row " + (rowIndex + 1) + " mixes scalar and list entries for " + rawKey);
                for (int langIdx = 0; langIdx < languages.size() && (langIdx + 1) < row.length; langIdx++) {
                    String language = languages.get(langIdx);
                    String value = row[langIdx + 1];
                    data.set(rawKey, language, value);
                }
            }
        }

        // Convert indexed entries to lists
        long expandedCells = 0;
        for (Map.Entry<String, Map<String, TreeMap<Integer, String>>> keyEntry : indexedEntries.entrySet()) {
            String baseKey = keyEntry.getKey();
            for (Map.Entry<String, TreeMap<Integer, String>> langEntry : keyEntry.getValue().entrySet()) {
                String language = langEntry.getKey();
                TreeMap<Integer, String> indexMap = langEntry.getValue();
                expandedCells += (long) indexMap.lastKey() + 1;
                if (expandedCells > MAX_EXPANDED_CELLS)
                    throw new IOException("CSV indexed translations exceed " + MAX_EXPANDED_CELLS
                            + " expanded cells at " + baseKey + " / " + language);

                // Build list from indexed entries
                List<String> list = new ArrayList<>();
                for (Map.Entry<Integer, String> indexEntry : indexMap.entrySet()) {
                    int index = indexEntry.getKey();
                    // Fill gaps with empty strings if indices are not contiguous
                    while (list.size() < index) {
                        list.add("");
                    }
                    list.add(indexEntry.getValue());
                }

                data.set(baseKey, language, list);
            }
        }

        return data;
    }

    /**
     * Parses a single CSV row, handling quoted fields with embedded commas and quotes.
     */
    private static String[] parseCSVRow(String row) {
        List<String> fields = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        boolean hadQuotes = false;

        for (int i = 0; i < row.length(); i++) {
            char c = row.charAt(i);

            if (inQuotes) {
                if (c == '"') {
                    // Check for escaped quote
                    if (i + 1 < row.length() && row.charAt(i + 1) == '"') {
                        current.append('"');
                        i++; // Skip next quote
                    } else {
                        // End of quoted field
                        inQuotes = false;
                    }
                } else {
                    current.append(c);
                }
            } else {
                if (c == '"') {
                    inQuotes = true;
                    hadQuotes = true;
                } else if (c == ',') {
                    fields.add(current.toString());
                    current = new StringBuilder();
                    hadQuotes = false;
                } else {
                    current.append(c);
                }
            }
        }

        // Add last field
        fields.add(current.toString());

        return fields.toArray(new String[0]);
    }

    /**
     * Writes translation data to a CSV file.
     * @param data The translation data to write
     * @param file The file to write to
     * @throws IOException if file cannot be written
     */
    public static void write(TranslationData data, File file) throws IOException {
        write(data, file.toPath());
    }

    /**
     * Writes translation data to a CSV file.
     * @param data The translation data to write
     * @param path The path to write to
     * @throws IOException if file cannot be written
     */
    public static void write(TranslationData data, Path path) throws IOException {
        Path destination = path.toAbsolutePath();
        Files.createDirectories(destination.getParent());
        Path staged = Files.createTempFile(destination.getParent(), destination.getFileName().toString(), ".tmp");
        try {
            writeSnapshot(data, staged);
            Files.move(staged, destination, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } finally {
            Files.deleteIfExists(staged);
        }
    }

    private static void writeSnapshot(TranslationData data, Path path) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            // Write UTF-8 BOM for Excel/editor compatibility
            writer.write('\uFEFF');
            List<String> languages = data.getLanguages();

            // Write header
            writer.write(escapeCSV("key"));
            for (String lang : languages) {
                writer.write(",");
                writer.write(escapeCSV(lang));
            }
            writer.newLine();

            // Collect and sort all keys, expanding lists to indexed keys
            List<String> sortedKeys = new ArrayList<>(data.getKeys());
            Collections.sort(sortedKeys);
            long expandedCells = 0;

            for (String key : sortedKeys) {
                Object firstValue = null;
                for (String lang : languages) {
                    firstValue = data.get(key, lang);
                    if (firstValue != null) break;
                }
                for (String lang : languages) {
                    Object value = data.get(key, lang);
                    if (value != null && (value instanceof List) != (firstValue instanceof List))
                        throw new IOException("Translation '" + key + "' mixes scalar and list values; existing CSV was preserved.");
                }

                if (firstValue instanceof List) {
                    // Write list as indexed rows
                    @SuppressWarnings("unchecked")
                    int maxSize = 0;
                    for (String lang : languages) {
                        List<String> list = data.getList(key, lang);
                        if (list != null) maxSize = Math.max(maxSize, list.size());
                    }
                    expandedCells += (long) maxSize * languages.size();
                    if (maxSize > MAX_LIST_ENTRIES || expandedCells > MAX_EXPANDED_CELLS)
                        throw new IOException("Translation lists exceed the CSV resource limit at " + key);

                    for (int i = 0; i < maxSize; i++) {
                        writer.write(escapeCSV(key + "[" + i + "]"));
                        for (String lang : languages) {
                            writer.write(",");
                            List<String> list = data.getList(key, lang);
                            if (list != null && i < list.size()) {
                                writer.write(escapeCSV(list.get(i)));
                            } else {
                                writer.write("\"\"");
                            }
                        }
                        writer.newLine();
                    }
                } else {
                    // Write single string row
                    writer.write(escapeCSV(key));
                    for (String lang : languages) {
                        writer.write(",");
                        String value = data.getString(key, lang);
                        writer.write(escapeCSV(value != null ? value : ""));
                    }
                    writer.newLine();
                }
            }
        }
    }

    /**
     * Escapes a value for CSV format.
     */
    private static String escapeCSV(String value) {
        if (value == null) return "\"\"";

        // Always quote fields to be safe
        StringBuilder sb = new StringBuilder();
        sb.append('"');
        for (char c : value.toCharArray()) {
            if (c == '"') {
                sb.append("\"\""); // Escape quotes
            } else {
                sb.append(c);
            }
        }
        sb.append('"');
        return sb.toString();
    }

    /**
     * Merges new translations into existing data, preserving existing values.
     * @param existing The existing translation data
     * @param additions New translations to add (only added if key doesn't exist)
     */
    public static void mergeNewKeys(TranslationData existing, TranslationData additions) {
        for (String key : additions.getKeys()) {
            if (!existing.hasKey(key)) {
                for (String lang : additions.getLanguages()) {
                    Object value = additions.get(key, lang);
                    if (value != null) {
                        existing.addLanguage(lang);
                        existing.set(key, lang, value);
                    }
                }
            }
        }
    }

    /**
     * Represents parsed translation data from a CSV file.
     */
    public static class TranslationData {
        private final List<String> languages;
        private final Map<String, Map<String, Object>> translations; // key -> (language -> value or List<String>)

        public TranslationData(List<String> languages) {
            this.languages = new ArrayList<>(languages);
            this.translations = new LinkedHashMap<>();
        }

        public List<String> getLanguages() {
            return Collections.unmodifiableList(languages);
        }

        TranslationData snapshot() {
            TranslationData copy = new TranslationData(languages);
            translations.forEach((key, values) -> values.forEach((language, value) ->
                    copy.set(key, language, value instanceof List<?> list ? new ArrayList<>(list) : value)));
            return copy;
        }

        /**
         * Gets a translation value for a key and language.
         * @return String for single values, List<String> for indexed lists, or null if not found
         */
        public Object get(String key, String language) {
            Map<String, Object> langMap = translations.get(key);
            if (langMap == null) return null;
            return langMap.get(language);
        }

        /**
         * Gets a string translation, returns null if it's a list or not found.
         */
        public String getString(String key, String language) {
            Object value = get(key, language);
            if (value instanceof String) return (String) value;
            return null;
        }

        /**
         * Gets a list translation, returns null if it's a string or not found.
         */
        @SuppressWarnings("unchecked")
        public List<String> getList(String key, String language) {
            Object value = get(key, language);
            if (value instanceof List) return (List<String>) value;
            return null;
        }

        /**
         * Sets a translation value (String or List<String>).
         */
        public void set(String key, String language, Object value) {
            translations.computeIfAbsent(key, k -> new LinkedHashMap<>()).put(language, value);
        }

        /**
         * Checks if a key exists.
         */
        public boolean hasKey(String key) {
            return translations.containsKey(key);
        }

        /**
         * Gets all translation keys.
         */
        public Set<String> getKeys() {
            return Collections.unmodifiableSet(translations.keySet());
        }

        /**
         * Adds a language if it doesn't exist.
         */
        public void addLanguage(String language) {
            if (!languages.contains(language)) {
                languages.add(language);
            }
        }
    }
}
