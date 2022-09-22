package com.magmaguy.elitemobs.config.translations;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.ConfigurationEngine;
import com.magmaguy.elitemobs.config.CustomConfigFields;
import com.magmaguy.elitemobs.config.CustomConfigFieldsInterface;
import com.magmaguy.elitemobs.utils.InfoMessage;
import com.magmaguy.elitemobs.utils.WarningMessage;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class TranslationsConfigFields extends CustomConfigFields implements CustomConfigFieldsInterface {

    private final List<String> outdatedCustomKeys = new ArrayList<>();
    boolean saving = false;
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
        //If the file doesn't exist, try to load it from the plugin resources to get the prepackaged translations
        String parsedFilename = filename;
        if (!parsedFilename.contains(".yml")) parsedFilename += ".yml";
        Path realPath = Paths.get(MetadataHandler.PLUGIN.getDataFolder().getAbsolutePath() + File.separatorChar + "translations" + File.separatorChar + parsedFilename);
        //This happens the first time the plugin gets installed
        if (!Files.exists(realPath)) {
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(MetadataHandler.PLUGIN.getResource("translations/" + parsedFilename), StandardCharsets.UTF_8));
                YamlConfiguration config = YamlConfiguration.loadConfiguration(in);
                ConfigurationEngine.fileSaverCustomValues(config, realPath.toFile());
            } catch (Exception ex) {
                new InfoMessage("Translation filename " + parsedFilename + " is not prepackaged. This is fine if it is meant to be a custom translation.");
                customLanguage = true;
            }
        }

    }

    @Override
    public void processConfigFields() {
        if (MetadataHandler.PLUGIN.getResource("translations/" + filename) == null) return;
        if (customLanguage) return;
        /*
        Create translation_data file which will store the premade translations. This will be used to check if new premade translations are here.
         */
        String languageDataFilename = filename.replace(".yml", ".translation_data");
        Path dataPath = Paths.get(MetadataHandler.PLUGIN.getDataFolder().getAbsolutePath() + File.separatorChar + "translations" + File.separatorChar + languageDataFilename);
        if (!Files.exists(dataPath))
            try {
                dataPath.toFile().createNewFile();
            } catch (Exception ex) {
                new InfoMessage("Failed to create language data file for file " + filename + " backup file should've been " + languageDataFilename);
            }


        translationDataFile = dataPath.toFile();
        try {
            translationData = YamlConfiguration.loadConfiguration(new InputStreamReader(new FileInputStream(translationDataFile), StandardCharsets.UTF_8));
        } catch (Exception ex) {
            new WarningMessage("Failed to read translation data!");
            return;
        }
        /*
        Get the prepackaged translations file
         */
        premadeTranslationsConfiguration = YamlConfiguration.loadConfiguration(new InputStreamReader(MetadataHandler.PLUGIN.getResource("translations/" + filename), StandardCharsets.UTF_8));

        //Compare the prepackaged translation to the translation data
        for (String path : premadeTranslationsConfiguration.getKeys(true)) {
            //Value from the current premade translations
            Object premadeValue = premadeTranslationsConfiguration.get(path);
            if (!(premadeValue instanceof String || premadeValue instanceof List)) continue;

            //Value from the last known premade translations
            Object dataValue = translationData.get(path);
            //Actual current live value
            Object liveValue = fileConfiguration.get(path);

            if (premadeValue == null) {
                new WarningMessage("Something went wrong updating the translations, report this to the developer!");
                continue;
            }

            //Handle case in which the live translation does not have any values by assigning it the default value
            if (dataValue == null) {
                //Check if the live translation already has a custom value, shouldn't happen but who knows
                if (liveValue != null) {
                    //If that is the case, the value is custom and the admin should be notified that it didn't update
                    outdatedCustomKeys.add(path);
                    new InfoMessage("Did not modify " + path + " because the value was custom");
                } else
                    //If there is no value set yet, set the default value
                    fileConfiguration.set(path, premadeValue);
            }
            //Check if the prepackaged value and the data value are different, meaning that the premade translation updated
            else if (!premadeValue.equals(dataValue)) {
                //Check if the live value is custom
                if (!dataValue.equals(liveValue)) {
                    //The value is custom
                    outdatedCustomKeys.add(path);
                    new InfoMessage("Did not modify " + path + " because the value was custom");
                } else {
                    //The value is not custom, can safely be autoupdated
                    fileConfiguration.set(path, premadeValue);
                    new InfoMessage("Updated translation entry " + path + " for language " + filename);
                }
            }

            //Finally, make sure the translation data gets updated
            if (!premadeValue.equals(dataValue))
                translationData.set(path, premadeValue);
        }

        try {
            //Save changes
            translationData.save(translationDataFile);
            fileConfiguration.save(file);
        } catch (Exception exception) {
            new WarningMessage("Failed to save language files, report this to the developer!");
        }
    }

    public void add(String filename, String key, Object value) {
        if (value == null) return;

        //Clean the value up
        String filteredFilename = filename.replace(".yml", "");
        if (value instanceof String) {
            value = fixConfigColors((String) value);
            if (((String) value).isEmpty()) return;
        } else if (value instanceof List) {
            value = fixConfigColors((List<String>) value);
            if (((List<?>) value).isEmpty()) return;
        }

        String realKey = filteredFilename + "." + key;

        if (fileConfiguration.get(realKey) != null) {
            return;
        }

        //Commit the value
        fileConfiguration.set(realKey, value);
        save();
    }

    private void save() {
        if (saving) return;
        saving = true;
        Bukkit.getScheduler().scheduleSyncDelayedTask(MetadataHandler.PLUGIN, () -> {
            ConfigurationEngine.fileSaverCustomValues(fileConfiguration, file);
            if (translationData != null)
                ConfigurationEngine.fileSaverCustomValues(translationData, translationDataFile);
            saving = false;
        });
    }

    public Object get(String filename, String key) {
        String filteredFilename = filename.replace(".yml", "");
        Object object = fileConfiguration.get(filteredFilename + "." + key);
        if (object instanceof String)
            object = ChatColorConverter.convert((String) object);
        else if (object instanceof List)
            object = ChatColorConverter.convert((List<String>) object);
        return object;
    }

    private String fixConfigColors(String value) {
        return value.replace("§", "&");
    }

    private List<String> fixConfigColors(List<String> values) {
        List<String> uncoloredList = new ArrayList<>();
        values.forEach(s -> uncoloredList.add(fixConfigColors(s)));
        return uncoloredList;
    }

}
