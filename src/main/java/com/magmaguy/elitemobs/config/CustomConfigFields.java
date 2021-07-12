package com.magmaguy.elitemobs.config;

import com.magmaguy.elitemobs.utils.WarningMessage;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CustomConfigFields implements CustomConfigFieldsInterface {

    public CustomConfig getCustomConfig() {
        return customConfig;
    }

    public void setCustomConfig(CustomConfig customConfig) {
        this.customConfig = customConfig;
    }

    protected CustomConfig customConfig;

    public String getFilename() {
        return filename;
    }

    protected String filename;

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }

    protected boolean isEnabled;

    /**
     * Used by plugin-generated files (defaults)
     *
     * @param filename
     * @param isEnabled
     */
    public CustomConfigFields(String filename, boolean isEnabled) {
        this.filename = filename;
        this.isEnabled = isEnabled;
    }

    public FileConfiguration getFileConfiguration() {
        return fileConfiguration;
    }

    public void setFileConfiguration(FileConfiguration fileConfiguration) {
        this.fileConfiguration = fileConfiguration;
    }

    protected FileConfiguration fileConfiguration;

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    protected File file;

    /**
     * Used by admin-created files
     *
     * @param customConfig
     * @param fileConfiguration
     * @param file
     */
    public CustomConfigFields(CustomConfig customConfig, FileConfiguration fileConfiguration, File file) {
        this.customConfig = customConfig;
        this.fileConfiguration = fileConfiguration;
        this.filename = file.getName();
        this.file = file;
        customConfig.addCustomConfigFields(getFilename(), this);
        processCustomSpawnConfigFields();
    }

    @Override
    public void generateConfigDefaults(FileConfiguration fileConfiguration, File file) {

    }

    protected void addDefault(String path, Object value) {
        if (value instanceof List) {
            if (((List) value).size() > 0 && ((List) value).get(0) instanceof Enum) {
                List<String> stringList = new ArrayList<>();
                for (Object object : (List) value)
                    stringList.add(object.toString());
                fileConfiguration.addDefault(path, stringList);
            }
        } else if (value instanceof Enum)
            fileConfiguration.addDefault(path, value.toString());
        else
            fileConfiguration.addDefault(path, value);
    }

    @Override
    public void processCustomSpawnConfigFields() {

    }

    protected boolean configHas(String configKey) {
        return fileConfiguration.contains(configKey);
    }

    protected String processString(String path, String pluginDefault) {
        if (!configHas(path))
            return pluginDefault;
        try {
            return fileConfiguration.getString(path);
        } catch (Exception ex) {
            new WarningMessage("File " + filename + " has an incorrect entry for " + path);
        }
        return pluginDefault;
    }

    protected List<String> processStringList(String path, List<String> pluginDefault) {
        if (!configHas(path))
            return pluginDefault;
        try {
            return fileConfiguration.getStringList(path);
        } catch (Exception ex) {
            new WarningMessage("File " + filename + " has an incorrect entry for " + path);
        }
        return pluginDefault;
    }

    /**
     * This not only gets a list of worlds, but gets a list of already loaded worlds. This might cause issues if the worlds
     * aren't loaded when the code for getting worlds runs.
     *
     * @param path          Configuration path
     * @param pluginDefault Default value - should be null or empty
     * @return Worlds from the list that are loaded at the time this runs, probably on startup
     */
    protected List<World> processWorldList(String path, List<World> pluginDefault) {
        if (!configHas(path))
            return pluginDefault;
        try {
            List<String> validWorldStrings = processStringList(path, new ArrayList<>());
            List<World> validWorlds = new ArrayList<>();
            if (!validWorldStrings.isEmpty())
                for (String string : validWorldStrings) {
                    World world = Bukkit.getWorld(string);
                    if (world != null)
                        validWorlds.add(world);
                }
            return validWorlds;
        } catch (Exception ex) {
            new WarningMessage("File " + filename + " has an incorrect entry for " + path);
        }
        return pluginDefault;
    }


    protected <T extends Enum> List<T> processEnumList(String path, List<T> pluginDefault, Class enumClass) {
        if (!configHas(path))
            return pluginDefault;
        try {
            List<T> newList = new ArrayList<>();
            List<String> stringList = processStringList(path, null);
            for (String string : stringList)
                newList.add((T) Enum.valueOf(enumClass, string));
            return newList;
        } catch (Exception ex) {
            new WarningMessage("File " + filename + " has an incorrect entry for " + path);
        }
        return pluginDefault;
    }

    protected int processInt(String path, int pluginDefault) {
        if (!configHas(path))
            return pluginDefault;
        try {
            return fileConfiguration.getInt(path);
        } catch (Exception ex) {
            new WarningMessage("File " + filename + " has an incorrect entry for " + path);
        }
        return pluginDefault;
    }

    protected long processLong(String path, long pluginDefault) {
        if (!configHas(path))
            return pluginDefault;
        try {
            return fileConfiguration.getInt(path);
        } catch (Exception ex) {
            new WarningMessage("File " + filename + " has an incorrect entry for " + path);
        }
        return pluginDefault;
    }


    protected double processDouble(String path, double pluginDefault) {
        if (!configHas(path))
            return pluginDefault;
        try {
            return fileConfiguration.getDouble(path);
        } catch (Exception ex) {
            new WarningMessage("File " + filename + " has an incorrect entry for " + path);
        }
        return pluginDefault;
    }

    protected boolean processBoolean(String path, boolean pluginDefault) {
        if (!configHas(path))
            return pluginDefault;
        try {
            return fileConfiguration.getBoolean(path);
        } catch (Exception ex) {
            new WarningMessage("File " + filename + " has an incorrect entry for " + path);
        }
        return pluginDefault;
    }

    public <T extends Enum> T processEnum(String path, T pluginDefault) {
        if (!configHas(path))
            return pluginDefault;
        try {
            return (T) Enum.valueOf(pluginDefault.getClass(), fileConfiguration.getString(path));
        } catch (Exception ex) {
            new WarningMessage("File " + filename + " has an incorrect entry for " + path);
        }
        return pluginDefault;
    }

}
