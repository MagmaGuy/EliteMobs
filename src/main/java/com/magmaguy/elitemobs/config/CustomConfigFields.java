package com.magmaguy.elitemobs.config;

import com.magmaguy.elitemobs.config.translations.TranslationsConfig;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CustomConfigFields extends com.magmaguy.magmacore.config.CustomConfigFields {

    @Override
    public <T extends Enum<T>> T processEnum(String path, T value, T pluginDefault, Class<T> enumClass, boolean forceWriteDefault) {
        // Materials for future versions (e.g. spears) may not exist yet — skip silently
        if (enumClass == Material.class && configHas(path)) {
            String rawValue = fileConfiguration.getString(path);
            if (rawValue != null && rawValue.toUpperCase(Locale.ROOT).endsWith("_SPEAR")
                    && com.magmaguy.elitemobs.versionnotifier.VersionChecker.serverVersionOlderThan(21, 11))
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

    /**
     * Registers a translatable value with the translation system and returns the display-ready value for
     * the configured language. The source key is deliberately left untouched in the configuration.
     * <p>
     * Between 10.0.1 and 10.7.x this also ran {@code fileConfiguration.set(key, null)} on non-English servers
     * so translated text would only be served from translations/&lt;language&gt;.csv. That was survivable while
     * the strip stayed in memory for user files: the source text of a plugin-generated config lives in a Java
     * field and is re-created on every boot. It stopped being survivable once MagmaCore's
     * {@code CustomConfig#initialize(File)} — the user-file / content-package load path — started calling
     * {@code ConfigurationEngine#fileSaverCustomValues} after parsing (MagmaCore 2db8d8e, 2026-06-21). The
     * strip is now flushed to the YAML, so the first boot on a translated server erases name / lore / dialog /
     * messages from every downloaded content package, and every boot after that parses a file that no longer
     * has them. Content packages have no Java-side defaults to fall back on, so the text is simply gone.
     * <p>
     * Keeping the key cannot clobber an existing translation: {@link TranslationsConfig#add} reconciles the shipped
     * default against a recorded baseline and only ever writes the {@code en} column of the CSV - never the language
     * column a translator owns - and it returns the translated value either way. The only visible difference on a
     * translated server is that the YAML keeps showing the source text, exactly as it does on an English server.
     */
    protected String translatable(String filename, String key, String value) {
        return TranslationsConfig.add(filename, key, value);
    }

    /**
     * List counterpart of {@link #translatable(String, String, String)}. See that method for why the source key
     * must survive in the configuration.
     */
    protected List<String> translatable(String filename, String key, List<String> value) {
        return TranslationsConfig.add(filename, key, value);
    }

    /**
     * Registers the translatable text inside a configuration section and hands back a section carrying the translated
     * values.
     * <p>
     * The translated text is deliberately <strong>not</strong> written back into {@code fileConfiguration}. Powers are
     * loaded from user files through MagmaCore's {@code CustomConfig#initialize(File)}, which saves the parsed
     * configuration straight back over the YAML: writing the resolved text in place would replace the author's source
     * text with the current language on the first boot, and the second boot would then register that translated text
     * as though it were the shipped English default. Exactly the failure mode described on
     * {@link #translatable(String, String, String)}.
     * <p>
     * The copy is only made when there is actually something to translate, so the usual case costs one key scan.
     */
    public ConfigurationSection processConfigurationSection(String path, Map<String, Object> value) {
        if (!configHas(path) && value != null)
            fileConfiguration.addDefaults(value);
        ConfigurationSection source = fileConfiguration.getConfigurationSection(path);
        if (source == null) return null;

        List<String> translatableKeys = source.getKeys(true).stream()
                .filter(key -> key.equalsIgnoreCase("message"))
                .filter(key -> source.getString(key) != null)
                .toList();
        if (translatableKeys.isEmpty()) return source;

        ConfigurationSection translated = new YamlConfiguration().createSection(path);
        copySection(source, translated);
        for (String key : translatableKeys)
            translated.set(key, TranslationsConfig.add(filename, key, source.getString(key)));
        return translated;
    }

    /**
     * Detaches a section from its configuration. Values are carried over by reference; nothing here mutates them, and
     * only the scalar keys replaced above are ever overwritten.
     */
    private static void copySection(ConfigurationSection source, ConfigurationSection target) {
        for (String key : source.getKeys(false)) {
            Object value = source.get(key);
            if (value instanceof ConfigurationSection child)
                copySection(child, target.createSection(key));
            else
                target.set(key, value);
        }
    }
}
