package com.magmaguy.elitemobs.config.translations;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.ConfigurationEngine;
import com.magmaguy.elitemobs.config.CustomConfigFields;
import com.magmaguy.elitemobs.config.DefaultConfig;
import com.magmaguy.magmacore.util.ChatColorConverter;
import com.magmaguy.magmacore.util.Logger;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class TranslationsConfigFields extends CustomConfigFields {

    private final List<String> outdatedCustomKeys = new ArrayList<>();
    private boolean saving = false;
    private File translationDataFile;
    private FileConfiguration translationData;
    private FileConfiguration premadeTranslationsConfiguration;
    private boolean customLanguage = false;

    /**
     * Used by plugin-generated files (defaults)
     *
     * @param filename
     * @param isEnabled
     */
    public TranslationsConfigFields(String filename, boolean isEnabled) {
        super(filename, isEnabled);

        // only load the currently-selected language
        if (!DefaultConfig.getLanguage().equals(filename)) return;

        // build path: plugins/…/translations/<filename>.yml
        String parsedFilename = filename.endsWith(".yml") ? filename : filename + ".yml";
        Path folder = Paths.get(MetadataHandler.PLUGIN.getDataFolder().getAbsolutePath(), "translations");
        Path realPath = folder.resolve(parsedFilename);

        // if missing, try to copy from bundled resources; else mark as custom
        if (!Files.exists(realPath)) {
            try (InputStream in = MetadataHandler.PLUGIN.getResource("translations/" + parsedFilename)) {
                if (in != null) {
                    Files.createDirectories(realPath.getParent());
                    Files.copy(in, realPath);
                    Logger.info("Copied bundled translation " + parsedFilename);
                } else {
                    Logger.info("No bundled translation for " + parsedFilename + ", treating as custom");
                    customLanguage = true;
                }
            } catch (IOException ex) {
                Logger.warn("Error creating translation file " + parsedFilename + ": " + ex.getMessage());
                customLanguage = true;
            }
        }

        // now load the file
        try {
            this.file = realPath.toFile();
            this.fileConfiguration = YamlConfiguration.loadConfiguration(
                    new InputStreamReader(Files.newInputStream(realPath), StandardCharsets.UTF_8)
            );
        } catch (IOException e) {
            Logger.warn("Could not load translation file " + parsedFilename + ": " + e.getMessage());
        }
    }

    @Override
    public void processConfigFields() {
        // only process if this is the selected language, and we have a bundled premade to compare
        if (!DefaultConfig.getLanguage().equals(filename)) return;
        if (MetadataHandler.PLUGIN.getResource("translations/" + filename) == null) return;
        if (customLanguage) return;

        // prepare .translation_data backup
        String languageDataFilename = filename.replace(".yml", ".translation_data");
        Path dataPath = Paths.get(
                MetadataHandler.PLUGIN.getDataFolder().getAbsolutePath(),
                "translations", languageDataFilename
        );
        if (!Files.exists(dataPath)) {
            try {
                Files.createDirectories(dataPath.getParent());
                dataPath.toFile().createNewFile();
            } catch (Exception ex) {
                Logger.info("Could not create data file " + languageDataFilename);
            }
        }

        translationDataFile = dataPath.toFile();
        try {
            translationData = YamlConfiguration.loadConfiguration(
                    new InputStreamReader(new FileInputStream(translationDataFile), StandardCharsets.UTF_8)
            );
        } catch (Exception ex) {
            Logger.warn("Failed to read translation data!");
            return;
        }

        // load the bundled premade for diff’ing
        premadeTranslationsConfiguration = YamlConfiguration.loadConfiguration(
                new InputStreamReader(
                        MetadataHandler.PLUGIN.getResource("translations/" + filename),
                        StandardCharsets.UTF_8
                )
        );

        // compare & auto-update unchanged entries
        for (String path : premadeTranslationsConfiguration.getKeys(true)) {
            Object premadeValue = premadeTranslationsConfiguration.get(path);
            if (!(premadeValue instanceof String || premadeValue instanceof List)) continue;

            Object dataValue = translationData.get(path);
            Object liveValue = fileConfiguration.get(path);

            if (dataValue == null) {
                // never-seen key: if live is empty, write default; else leave custom
                if (liveValue != null) {
                    outdatedCustomKeys.add(path);
                    Logger.info("Skipped custom key " + path);
                } else {
                    fileConfiguration.set(path, premadeValue);
                }
            } else if (!premadeValue.equals(dataValue)) {
                // updated default: if live still matches old data, overwrite; else leave custom
                if (!dataValue.equals(liveValue)) {
                    outdatedCustomKeys.add(path);
                    Logger.info("Skipped custom key " + path);
                } else {
                    fileConfiguration.set(path, premadeValue);
                    Logger.info("Auto-updated translation " + path);
                }
            }

            // always refresh our data snapshot
            if (!premadeValue.equals(dataValue)) {
                translationData.set(path, premadeValue);
            }
        }

        // save both files
        try {
            translationData.save(translationDataFile);
            fileConfiguration.save(file);
        } catch (Exception ex) {
            Logger.warn("Failed to save language files: " + ex.getMessage());
        }
    }

    /** Adds a missing key to the language file at runtime. */
    public void add(String filename, String key, Object value) {
        if (value == null) return;
        String filteredFilename = filename.replace(".yml", "");

        if (value instanceof String) {
            String s = fixConfigColors((String) value);
            if (s.isEmpty()) return;
            value = s;
        } else if (value instanceof List) {
            List<String> list = fixConfigColors((List<String>) value);
            if (list.isEmpty()) return;
            value = list;
        }

        String realKey = filteredFilename + "." + key;
        if (fileConfiguration.get(realKey) != null) return;

        fileConfiguration.set(realKey, value);
        save();
    }

    /** Debounced async save of new keys. */
    private void save() {
        if (saving) return;
        saving = true;
        Bukkit.getScheduler().scheduleSyncDelayedTask(MetadataHandler.PLUGIN, () -> {
            ConfigurationEngine.fileSaverCustomValues(fileConfiguration, file);
            if (translationData != null) {
                ConfigurationEngine.fileSaverCustomValues(translationData, translationDataFile);
            }
            saving = false;
        });
    }

    /** Reads a translated value (with color codes converted). */
    public Object get(String filename, String key) {
        String filteredFilename = filename.replace(".yml", "");
        Object obj = fileConfiguration.get(filteredFilename + "." + key);
        if (obj instanceof String) {
            return ChatColorConverter.convert((String) obj);
        } else if (obj instanceof List) {
            return ChatColorConverter.convert((List<String>) obj);
        }
        return obj;
    }

    private String fixConfigColors(String value) {
        if (value == null) return null;
        return value.replace("§", "&");
    }

    private List<String> fixConfigColors(List<String> values) {
        List<String> clean = new ArrayList<>();
        values.forEach(s -> clean.add(fixConfigColors(s)));
        return clean;
    }
}
