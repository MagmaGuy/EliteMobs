package com.magmaguy.elitemobs.config;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.utils.WarningMessage;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.reflections.Reflections;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class CustomConfig {

    //This stores configurations long term, ? is the specific extended custom config field
    private final HashMap<String, CustomConfigFields> customConfigFieldsHashMap = new HashMap<>();
    //This is only used for loading configurations in to check if the machine has all of the default files
    private final List customConfigFieldsArrayList = new ArrayList<>();
    private final String folderName;
    private final Class<? extends CustomConfigFields> customConfigFields;

    /**
     * Initializes all configurations and stores them in a list for later access
     */
    public CustomConfig(String folderName, String packageName, Class<? extends CustomConfigFields> customConfigFields) {
        this.folderName = folderName;
        this.customConfigFields = customConfigFields;

        //Set defaults through reflections by getting everything that extends specific CustomConfigFields within specific package scopes
        Reflections reflections = new Reflections(packageName);

        Set<Class> classSet = new HashSet<>(reflections.getSubTypesOf(customConfigFields));
        classSet.forEach(aClass -> {
            try {
                customConfigFieldsArrayList.add(aClass.newInstance());
            } catch (Exception ex) {
                new WarningMessage("Failed to generate plugin default classes for " + folderName + " ! This is very bad, warn the developer!");
                ex.printStackTrace();
            }
        });

        //Check if the directory doesn't exist
        try {
            if (!Files.isDirectory(Paths.get(MetadataHandler.PLUGIN.getDataFolder().getPath() + File.separatorChar + folderName))) {
                generateFreshConfigurations();
                return;
            }
        } catch (Exception ex) {
            new WarningMessage("Failed to generate plugin default files for " + folderName + " ! This is very bad, warn the developer!");
            ex.printStackTrace();
            return;
        }

        //Runs if the directory exists
        //Check if all the defaults exist
        directoryCrawler(MetadataHandler.PLUGIN.getDataFolder().getPath() + File.separatorChar + folderName);

        try {
            //Generate missing default config files, might've been deleted or might have been added in newer version
            if (!customConfigFieldsArrayList.isEmpty())
                generateFreshConfigurations();
        } catch (Exception ex) {
            new WarningMessage("Failed to finish generating default plugin files for " + folderName + " ! This is very bad, warn the developer!");
            ex.printStackTrace();
            return;
        }

    }

    private void directoryCrawler(String path) {
        for (File file : Objects.requireNonNull((new File(path)).listFiles())) {
            if (file.isFile())
                fileInitializer(file);
            else if (file.isDirectory())
                directoryCrawler(file.getPath());
        }
    }

    private void fileInitializer(File file) {

        boolean isPremade = false;
        for (Object object : customConfigFieldsArrayList) {
            try {
                Method getFilename = CustomConfigFields.class.getDeclaredMethod("getFilename");
                if (file.getName().equalsIgnoreCase((String) getFilename.invoke(object))) {
                    customConfigFieldsArrayList.remove(object);
                    initialize((CustomConfigFields) object);
                    isPremade = true;
                    break;
                }
            } catch (Exception ex) {
                new WarningMessage("Failed to read plugin files for " + folderName + " ! This is very bad, warn the developer!");
                isPremade = true;
                ex.printStackTrace();
            }
        }
        if (!isPremade)
            initialize(file);

    }

    public HashMap<String, ? extends CustomConfigFields> getCustomConfigFieldsHashMap() {
        return customConfigFieldsHashMap;
    }

    /**
     * Adds entry to custom config fields. This is done directly by the custom config fields as they are iterated through.
     *
     * @param filename           Name of the file , using the format filename.yml
     * @param customConfigFields Custom Config Fields, should be from an extended subclass
     */
    public <V extends CustomConfigFields> void addCustomConfigFields(String filename, CustomConfigFields customConfigFields) {
        customConfigFieldsHashMap.put(filename, customConfigFields);
    }

    /**
     * Called when the appropriate configurations directory does not exist
     */
    private void generateFreshConfigurations() {
        for (Object customConfigFields : customConfigFieldsArrayList)
            initialize((CustomConfigFields) customConfigFields);
    }

    /**
     * Initializes a single instance of a premade configuration using the default values. Writes defaults.
     */
    private void initialize(CustomConfigFields customConfigFields) {
        //Create configuration file from defaults if it does not exist
        File file = ConfigurationEngine.fileCreator(folderName, customConfigFields.getFilename());
        //Get config file
        FileConfiguration fileConfiguration = ConfigurationEngine.fileConfigurationCreator(file);

        //Associate config
        customConfigFields.setFile(file);
        customConfigFields.setFileConfiguration(fileConfiguration);

        //Parse actual fields and load into RAM to be used
        customConfigFields.processConfigFields();

        //Save all configuration values as they exist
        ConfigurationEngine.fileSaverCustomValues(fileConfiguration, file);

        //if (customConfigFields.isEnabled)
        //Store for use by the plugin
        addCustomConfigFields(file.getName(), customConfigFields);
    }

    /**
     * Called when a user-made file is detected.
     */
    private void initialize(File file) {
        //Load file configuration from file
        try {
            //Make sure it's a yml configuration file
            if (!file.getName().endsWith(".yml")) return;
            FileConfiguration fileConfiguration = YamlConfiguration.loadConfiguration(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
            //Instantiate the correct CustomConfigFields instance
            Constructor<?> constructor = customConfigFields.getConstructor(String.class, boolean.class);
            CustomConfigFields instancedCustomConfigFields = (CustomConfigFields) constructor.newInstance(file.getName(), true);
            instancedCustomConfigFields.setFileConfiguration(fileConfiguration);
            instancedCustomConfigFields.setFile(file);
            //Parse actual fields and load into RAM to be used
            instancedCustomConfigFields.processConfigFields();
            //if (instancedCustomConfigFields.isEnabled)
            //Store for use by the plugin
            addCustomConfigFields(file.getName(), instancedCustomConfigFields);
        } catch (Exception ex) {
            new WarningMessage("Bad constructor for file " + file.getName());
            ex.printStackTrace();
        }

    }

}
