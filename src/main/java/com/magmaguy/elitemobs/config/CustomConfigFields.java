package com.magmaguy.elitemobs.config;

import com.magmaguy.elitemobs.config.translations.TranslationsConfig;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CustomConfigFields extends com.magmaguy.magmacore.config.CustomConfigFields {

    @Override
    public <T extends Enum<T>> T processEnum(String path, T value, T pluginDefault, Class<T> enumClass, boolean forceWriteDefault) {
        // Materials for future versions (e.g. spears) may not exist yet — skip silently
        if (enumClass == Material.class && configHas(path)) {
            String rawValue = fileConfiguration.getString(path);
            if (rawValue != null && rawValue.toUpperCase(Locale.ROOT).endsWith("_SPEAR"))
                return pluginDefault;
        }
        return super.processEnum(path, value, pluginDefault, enumClass, forceWriteDefault);
    }

    /**
     * Used by plugin-generated files (defaults)
     *
     * @param filename
     * @param isEnabled
     */
    public CustomConfigFields(String filename, boolean isEnabled) {
        super(filename, isEnabled);
    }

    @Override
    public void processConfigFields() {

    }

    @Override
    public String getFilename() {
        return super.getFilename();
    }

    protected String translatable(String filename, String key, String value) {
        return TranslationsConfig.add(filename, key, value);
    }

    protected List<String> translatable(String filename, String key, List<String> value) {
        return TranslationsConfig.add(filename, key, value);
    }

    public ConfigurationSection processConfigurationSection(String path, Map<String, Object> value) {
        if (!configHas(path) && value != null)
            fileConfiguration.addDefaults(value);
        ConfigurationSection newValue = fileConfiguration.getConfigurationSection(path);
        if (newValue == null) return null;

        for (String key : newValue.getKeys(true))
            if (key.equalsIgnoreCase("message"))
                newValue.set(key, translatable(filename, key, (String) newValue.get(key)));
        return newValue;
    }
}
