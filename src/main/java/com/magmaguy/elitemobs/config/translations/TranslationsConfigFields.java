package com.magmaguy.elitemobs.config.translations;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.ConfigurationEngine;
import com.magmaguy.elitemobs.config.CustomConfigFields;
import com.magmaguy.elitemobs.config.CustomConfigFieldsInterface;
import com.magmaguy.elitemobs.utils.InfoMessage;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class TranslationsConfigFields extends CustomConfigFields implements CustomConfigFieldsInterface {

    boolean saving = false;

    /**
     * Used by plugin-generated files (defaults)
     *
     * @param filename
     * @param isEnabled
     */
    public TranslationsConfigFields(String filename, boolean isEnabled) {
        super(filename, isEnabled);
        //If the file doesn't exist, try to load it from the plugin resources to get the prepackaged translations
        if (!Files.exists(Paths.get(MetadataHandler.PLUGIN.getDataFolder().getAbsolutePath() + File.separatorChar + "translations" + File.separatorChar + filename + ".yml"))) {
            try {
                FileUtils.copyInputStreamToFile(
                        MetadataHandler.PLUGIN.getResource("translations/" + filename + ".yml"),
                        new File(MetadataHandler.PLUGIN.getDataFolder().getAbsolutePath() + File.separatorChar + "translations" + File.separatorChar + filename + ".yml"));
            } catch (Exception ex) {
                new InfoMessage("Translation filename " + filename + " is not prepackaged. This is fine if it is meant to be a custom translation.");
            }
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
        if (saving) return;
        saving = true;
        Bukkit.getScheduler().scheduleSyncDelayedTask(MetadataHandler.PLUGIN, () -> {
            ConfigurationEngine.fileSaverCustomValues(fileConfiguration, file);
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
