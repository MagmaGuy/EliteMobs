package com.magmaguy.elitemobs.config;

/**
 * Created by MagmaGuy on 01/05/2017.
 */

import com.magmaguy.elitemobs.MetadataHandler;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class CustomConfigConstructor extends YamlConfiguration {

    Plugin plugin = Bukkit.getPluginManager().getPlugin(MetadataHandler.ELITE_MOBS);
    private File file;
    private String defaults;

    /**
     * Creates new PluginFile, without defaults
     *
     * @param fileName - Name of the file
     */
    public CustomConfigConstructor(String fileName) {
        this(fileName, null);
    }

    /**
     * Creates new PluginFile, with defaults
     *
     * @param fileName     - Name of the file
     * @param defaultsName - Name of the defaults
     */
    public CustomConfigConstructor(String fileName, String defaultsName) {
        this.defaults = defaultsName;
        File newFile = new File(plugin.getDataFolder().getPath()+"/data");
        Bukkit.getLogger().info("ATTENTION: " + newFile.toString());
        this.file = new File(newFile, fileName);
        reload();
    }

    /**
     * Reload configuration
     */
    public void reload() {

        if (!file.exists()) {

            try {
                file.getParentFile().mkdirs();
                file.createNewFile();

            } catch (IOException exception) {
                exception.printStackTrace();
                plugin.getLogger().severe("Error while creating file " + file.getName());
            }

        }

        try {
            load(file);

            if (defaults != null) {
                InputStreamReader reader = new InputStreamReader(plugin.getResource(defaults));
                FileConfiguration defaultsConfig = YamlConfiguration.loadConfiguration(reader);

                setDefaults(defaultsConfig);
                options().copyDefaults(true);

                reader.close();
                save();
            }

        } catch (IOException exception) {
            exception.printStackTrace();
            plugin.getLogger().severe("Error while loading file " + file.getName());

        } catch (InvalidConfigurationException exception) {
            exception.printStackTrace();
            plugin.getLogger().severe("Error while loading file " + file.getName());

        }

    }

    /**
     * Save configuration
     */
    public void save() {

        try {
            options().indent(2);
            save(file);

        } catch (IOException exception) {
            exception.printStackTrace();
            plugin.getLogger().severe("Error while saving file " + file.getName());
        }

    }

}