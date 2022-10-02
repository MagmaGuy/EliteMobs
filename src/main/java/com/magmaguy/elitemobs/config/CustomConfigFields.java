package com.magmaguy.elitemobs.config;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.config.translations.TranslationsConfig;
import com.magmaguy.elitemobs.utils.ConfigurationLocation;
import com.magmaguy.elitemobs.utils.ItemStackGenerator;
import com.magmaguy.elitemobs.utils.WarningMessage;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.util.Vector;

import java.io.File;
import java.util.*;

public class CustomConfigFields implements CustomConfigFieldsInterface {

    protected String filename;
    protected boolean isEnabled;
    protected FileConfiguration fileConfiguration;
    protected File file;

    /**
     * Used by plugin-generated files (defaults)
     *
     * @param filename
     * @param isEnabled
     */
    public CustomConfigFields(String filename, boolean isEnabled) {
        this.filename = filename.contains(".yml") ? filename : filename + ".yml";
        this.isEnabled = isEnabled;
    }

    public String getFilename() {
        return filename;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }

    public FileConfiguration getFileConfiguration() {
        return fileConfiguration;
    }

    public void setFileConfiguration(FileConfiguration fileConfiguration) {
        this.fileConfiguration = fileConfiguration;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    @Override
    public void processConfigFields() {

    }

    protected boolean configHas(String configKey) {
        return fileConfiguration.contains(configKey);
    }

    protected String translatable(String filename, String key, String value) {
        return TranslationsConfig.add(filename, key, value);
    }

    protected List<String> translatable(String filename, String key, List<String> value) {
        return TranslationsConfig.add(filename, key, value);
    }

    protected String processString(String path, String value, String pluginDefault, boolean forceWriteDefault) {
        if (!configHas(path)) {
            if (forceWriteDefault || !Objects.equals(value, pluginDefault))
                fileConfiguration.addDefault(path, value);
            return value;
        }
        try {
            return ChatColorConverter.convert(fileConfiguration.getString(path));
        } catch (Exception ex) {
            new WarningMessage("File " + filename + " has an incorrect entry for " + path);
            new WarningMessage("Entry: " + value);
        }
        return value;
    }


    public List<Object> processList(String path, List<Object> value, List<Object> pluginDefault, boolean forceWriteDefault) {
        if (!configHas(path)) {
            if (forceWriteDefault || value != pluginDefault)
                fileConfiguration.addDefault(path, value);
            return value;
        }
        try {
            return new ArrayList<>(Objects.requireNonNull(fileConfiguration.getList(path)));
        } catch (Exception ex) {
            new WarningMessage("File " + filename + " has an incorrect entry for " + path);
            new WarningMessage("Entry: " + value);
        }
        return value;
    }

    public List<String> processStringList(String path, List<String> value, List<String> pluginDefault, boolean forceWriteDefault) {
        if (!configHas(path)) {
            if (forceWriteDefault || value != pluginDefault)
                fileConfiguration.addDefault(path, value);
            return value;
        }
        try {
            List<String> list = new ArrayList<>();
            for (String string : fileConfiguration.getStringList(path))
                list.add(ChatColorConverter.convert(string));
            return list;
        } catch (Exception ex) {
            new WarningMessage("File " + filename + " has an incorrect entry for " + path);
            new WarningMessage("Entry: " + value);
        }
        return value;
    }

    /**
     * This not only gets a list of worlds, but gets a list of already loaded worlds. This might cause issues if the worlds
     * aren't loaded when the code for getting worlds runs.
     *
     * @param path          Configuration path
     * @param pluginDefault Default value - should be null or empty
     * @return Worlds from the list that are loaded at the time this runs, probably on startup
     */
    protected List<World> processWorldList(String path, List<World> value, List<World> pluginDefault, boolean forceWriteDefault) {
        if (!configHas(path)) {
            if (value != null && (forceWriteDefault || value != pluginDefault))
                processStringList(path, worldListToStringListConverter(value), worldListToStringListConverter(pluginDefault), forceWriteDefault);
            return value;
        }
        try {
            List<String> validWorldStrings = processStringList(path, worldListToStringListConverter(pluginDefault), worldListToStringListConverter(value), forceWriteDefault);
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
            new WarningMessage("Entry: " + value);
        }
        return value;
    }

    private List<String> worldListToStringListConverter(List<World> pluginDefault) {
        if (pluginDefault == null) return null;
        List<String> newList = new ArrayList<>();
        pluginDefault.forEach(element -> newList.add(element.getName()));
        return newList;
    }


    protected <T extends Enum<T>> List<T> processEnumList(String path, List<T> value, List<T> pluginDefault, Class<T> enumClass, boolean forceWriteDefault) {
        if (!configHas(path)) {
            if (forceWriteDefault || value != pluginDefault)
                processStringList(path, enumListToStringListConverter(value), enumListToStringListConverter(pluginDefault), forceWriteDefault);
            return value;
        }
        try {
            List<T> newList = new ArrayList<>();
            List<String> stringList = processStringList(path, enumListToStringListConverter(value), enumListToStringListConverter(pluginDefault), forceWriteDefault);
            stringList.forEach(string -> {
                try {
                    newList.add(Enum.valueOf(enumClass, string.toUpperCase()));
                } catch (Exception ex) {
                    new WarningMessage(filename + " : " + "Value " + string + " is not a valid for " + path + " ! This may be due to your server version, or due to an invalid value!");
                }
            });
            return newList;
        } catch (
                Exception ex) {
            ex.printStackTrace();
            new WarningMessage("File " + filename + " has an incorrect entry for " + path);
            new WarningMessage("Entry: " + value);
        }
        return value;
    }

    private <T extends Enum<T>> List<String> enumListToStringListConverter(List<T> list) {
        if (list == null) return Collections.emptyList();
        List<String> newList = new ArrayList<>();
        list.forEach(element -> newList.add(element.toString()));
        return newList;
    }

    protected int processInt(String path, int value, int pluginDefault, boolean forceWriteDefault) {
        if (!configHas(path)) {
            if (forceWriteDefault || value != pluginDefault) fileConfiguration.addDefault(path, value);
            return value;
        }
        try {
            return fileConfiguration.getInt(path);
        } catch (Exception ex) {
            new WarningMessage("File " + filename + " has an incorrect entry for " + path);
            new WarningMessage("Entry: " + value);
        }
        return value;
    }

    protected long processLong(String path, long value, long pluginDefault, boolean forceWriteDefault) {
        if (!configHas(path)) {
            if (forceWriteDefault || value != pluginDefault) fileConfiguration.addDefault(path, value);
            return value;
        }
        try {
            return fileConfiguration.getLong(path);
        } catch (Exception ex) {
            new WarningMessage("File " + filename + " has an incorrect entry for " + path);
            new WarningMessage("Entry: " + value);
        }
        return value;
    }


    protected double processDouble(String path, double value, double pluginDefault, boolean forceWriteDefault) {
        if (!configHas(path)) {
            if (forceWriteDefault || value != pluginDefault) fileConfiguration.addDefault(path, value);
            return value;
        }
        try {
            return fileConfiguration.getDouble(path);
        } catch (Exception ex) {
            new WarningMessage("File " + filename + " has an incorrect entry for " + path);
            new WarningMessage("Entry: " + value);
        }
        return value;
    }

    protected Double processDouble(String path, Double value, Double pluginDefault, boolean forceWriteDefault) {
        if (!configHas(path)) {
            if (forceWriteDefault || !Objects.equals(value, pluginDefault)) fileConfiguration.addDefault(path, value);
            return value;
        }
        try {
            return fileConfiguration.getDouble(path);
        } catch (Exception ex) {
            new WarningMessage("File " + filename + " has an incorrect entry for " + path);
            new WarningMessage("Entry: " + value);
        }
        return value;
    }

    protected boolean processBoolean(String path, boolean value, boolean pluginDefault, boolean forceWriteDefault) {
        if (!configHas(path)) {
            if (forceWriteDefault || value != pluginDefault) fileConfiguration.addDefault(path, value);
            return value;
        }
        try {
            return fileConfiguration.getBoolean(path);
        } catch (Exception ex) {
            new WarningMessage("File " + filename + " has an incorrect entry for " + path);
            new WarningMessage("Entry: " + value);
        }
        return value;
    }

    public <T extends Enum<T>> T processEnum(String path, T value, T pluginDefault, Class<T> enumClass, boolean forceWriteDefault) {
        if (!configHas(path)) {
            if (forceWriteDefault || value != pluginDefault) {
                String valueString = null;
                if (value != null)
                    valueString = value.toString().toUpperCase();
                String pluginDefaultString = null;
                if (pluginDefault != null)
                    pluginDefaultString = pluginDefault.toString().toUpperCase();
                processString(path, valueString, pluginDefaultString, forceWriteDefault);
            }
            return value;
        }
        try {
            return Enum.valueOf(enumClass, fileConfiguration.getString(path).toUpperCase());
        } catch (Exception ex) {
            new WarningMessage("File " + filename + " has an incorrect entry for " + path);
            new WarningMessage("Entry: " + value);
        }
        return value;
    }

    public ItemStack processItemStack(String path, ItemStack value, ItemStack pluginDefault, boolean forceWriteDefault) {
        if (!configHas(path)) {
            if (forceWriteDefault || value != pluginDefault)
                processString(path, itemStackDeserializer(value), itemStackDeserializer(pluginDefault), forceWriteDefault);
            return value;
        }
        try {
            String materialString = processString(path, itemStackDeserializer(value), itemStackDeserializer(pluginDefault), forceWriteDefault);
            if (materialString == null)
                return null;
            if (materialString.matches("[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}")) {
                ItemStack playerHead = new ItemStack(Material.PLAYER_HEAD);
                SkullMeta skullMeta = (SkullMeta) playerHead.getItemMeta();
                skullMeta.setOwningPlayer(Bukkit.getOfflinePlayer(UUID.fromString(materialString)));
                playerHead.setItemMeta(skullMeta);
                return playerHead;
            }
            if (materialString.contains(":")) {
                ItemStack itemStack = ItemStackGenerator.generateItemStack(Material.getMaterial(materialString.split(":")[0]));
                if (materialString.split(":")[0].contains("leather_") || materialString.split(":")[0].contains("LEATHER_")) {
                    LeatherArmorMeta leatherArmorMeta = (LeatherArmorMeta) itemStack.getItemMeta();
                    leatherArmorMeta.setColor(Color.fromRGB(Integer.parseInt(materialString.split(":")[1])));
                    itemStack.setItemMeta(leatherArmorMeta);
                } else {
                    ItemMeta itemMeta = itemStack.getItemMeta();
                    itemMeta.setCustomModelData(Integer.parseInt(materialString.split(":")[1]));
                    itemStack.setItemMeta(itemMeta);
                }
                return itemStack;
            } else
                return ItemStackGenerator.generateItemStack(Material.getMaterial(materialString));
        } catch (Exception ex) {
            new WarningMessage("File " + filename + " has an incorrect entry for " + path);
            new WarningMessage("Entry: " + value);
        }
        return value;
    }

    public Map<String, Object> processMap(String path, Map<String, Object> value) {
        if (!configHas(path) && value != null)
            fileConfiguration.addDefaults(value);
        if (fileConfiguration.get(path) == null) return Collections.emptyMap();
        return fileConfiguration.getConfigurationSection(path).getValues(false);
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

    private String itemStackDeserializer(ItemStack itemStack) {
        if (itemStack == null) return null;
        return itemStack.getType().toString();
    }

    protected Location processLocation(String path, Location value, String pluginDefault, boolean forceWriteDefault) {
        if (!configHas(path)) {
            if (forceWriteDefault || !Objects.equals(value, pluginDefault))
                fileConfiguration.addDefault(path, ConfigurationLocation.deserialize(value));
            return value;
        }
        try {
            return ConfigurationLocation.serialize(fileConfiguration.getString(path));
        } catch (Exception ex) {
            new WarningMessage("File " + filename + " has an incorrect entry for " + path);
            new WarningMessage("Entry: " + value);
        }
        return null;
    }

    protected Vector processVector(String path, Vector value, Vector pluginDefault, boolean forceWriteDefault) {
        if (!configHas(path)) {
            if (forceWriteDefault || !Objects.equals(value, pluginDefault))
                if (pluginDefault != null) {
                    String vectorString = value.getX() + "," + value.getY() + "," + value.getZ();
                    fileConfiguration.addDefault(path, vectorString);
                }
            return value;
        }
        try {
            String string = fileConfiguration.getString(path);
            if (string == null) return null;
            String[] strings = string.split(",");
            if (strings.length < 3) {
                new WarningMessage("File " + filename + " has an incorrect entry for " + path);
                return null;
            }
            return new Vector(Double.parseDouble(strings[0]), Double.parseDouble(strings[1]), Double.parseDouble(strings[2]));
        } catch (Exception ex) {
            new WarningMessage("File " + filename + " has an incorrect entry for " + path);
            new WarningMessage("Entry: " + value);
        }
        return null;
    }

}
