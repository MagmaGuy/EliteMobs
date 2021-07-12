package com.magmaguy.elitemobs.config;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.customspawns.CustomSpawnConfigFields;
import com.magmaguy.elitemobs.utils.WarningMessage;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;

import java.io.File;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class CustomConfig {

    public HashMap<String, CustomConfigFields> getCustomConfigFieldsHashMap() {
        return customConfigFieldsHashMap;
    }

    /**
     * Adds entry to custom config fields. This is done directly by the custom config fields as they are iterated through.
     *
     * @param filename           Name of the file , using the format filename.yml
     * @param customConfigFields Custom Config Fields, should be from an extended subclass
     */
    public void addCustomConfigFields(String filename, CustomConfigFields customConfigFields) {
        customConfigFieldsHashMap.put(filename, customConfigFields);
    }

    //This stores configurations long term, ? is the specific extended custom config field
    private final HashMap<String, CustomConfigFields> customConfigFieldsHashMap = new HashMap<>();

    //This is only used for loading configurations in to check if the machine has all of the default files
    private ArrayList<CustomConfigFields> customConfigFieldsArrayList = new ArrayList<>();

    private String path;

    /**
     * Initializes all configurations and stores them in a static list for later access
     */
    public CustomConfig(String path, String packageName) {
        this.path = path;

        //Set defaults through reflections by getting everything that extends specific CustomConfigFields within specific package scopes
        Reflections reflections = new Reflections(packageName, new SubTypesScanner(false));

        Set<Class> classSet = new HashSet<>(reflections.getSubTypesOf(CustomSpawnConfigFields.class));
        classSet.forEach(aClass -> {
            try {
                customConfigFieldsArrayList.add((CustomConfigFields) aClass.newInstance());
            } catch (Exception ex) {
                new WarningMessage("Failed to generate plugin default classes for " + path + " ! This is very bad, warn the developer!");
                ex.printStackTrace();
            }
        });

        //Check if the directory doesn't exist
        try {
            if (!Files.isDirectory(Paths.get(MetadataHandler.PLUGIN.getDataFolder().getPath() + path))) {
                generateFreshConfigurations();
                return;
            }
        } catch (Exception ex) {
            new WarningMessage("Failed to generate plugin default files for " + path + " ! This is very bad, warn the developer!");
            ex.printStackTrace();
            return;
        }

        //Runs if the directory exists
        //Check if all the defaults exist
        for (File file : (new File(MetadataHandler.PLUGIN.getDataFolder().getPath() + path)).listFiles()) {
            try {
                boolean isPremade = false;
                for (Object object : customConfigFieldsArrayList) {
                    Method getFilename = CustomConfigFields.class.getDeclaredMethod("getFilename");
                    if (file.getName().equalsIgnoreCase((String) getFilename.invoke(object))) {
                        customConfigFieldsArrayList.remove(object);
                        initialize((CustomConfigFields) object);
                        isPremade = true;
                        break;
                    }
                }
                if (!isPremade)
                    initialize(file);
            } catch (Exception ex) {
                new WarningMessage("Failed to read plugin files for " + path + " ! This is very bad, warn the developer!");
                ex.printStackTrace();
                return;
            }
        }

        try {
            //Generate missing default config files, might've been deleted or might have been added in newer version
            if (!customConfigFieldsArrayList.isEmpty())
                generateFreshConfigurations();
        } catch (Exception ex) {
            new WarningMessage("Failed to finish generating default plugin files for " + path + " ! This is very bad, warn the developer!");
            ex.printStackTrace();
            return;
        }

    }

    /**
     * Called when the appropriate configurations directory does not exist
     */
    private void generateFreshConfigurations() {
        for (CustomConfigFields customConfigFields : customConfigFieldsArrayList)
            initialize(customConfigFields);
    }

    /**
     * Initializes a single instance of a premade configuration using the default values.
     */
    private void initialize(CustomConfigFields customConfigFields) {
        //Create configuration file from defaults if it does not exist
        File file = ConfigurationEngine.fileCreator(path, customConfigFields.getFilename());
        //Get config file
        FileConfiguration fileConfiguration = ConfigurationEngine.fileConfigurationCreator(file);
        //Generate config defaults
        customConfigFields.generateConfigDefaults(fileConfiguration, file);

        //todo: remove the commented line below
        //customConfigFields.generateConfigDefaults(fileConfiguration, file);

        //Save all configuration values as they exist
        ConfigurationEngine.fileSaverCustomValues(fileConfiguration, file);

        //Associate config
        customConfigFields.setCustomConfig(this);
        customConfigFields.setFile(file);
        customConfigFields.setFileConfiguration(fileConfiguration);
        addCustomConfigFields(customConfigFields.getFilename(), customConfigFields);

        //Parse actual fields and load into RAM to be used
        customConfigFields.processCustomSpawnConfigFields();
    }

    /**
     * Called when a user-made file is detected.
     */
    private void initialize(File file) {
        //Load file configuration from file
        FileConfiguration fileConfiguration = YamlConfiguration.loadConfiguration(file);

        //Parse actual fields and load into RAM to be used
        new CustomConfigFields(this, fileConfiguration, file);
    }

}
