package com.magmaguy.elitemobs.config;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.utils.ItemStackGenerator;
import com.magmaguy.elitemobs.utils.WarningMessage;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CustomConfigFields implements CustomConfigFieldsInterface {

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
        this.filename = filename.contains(".yml") ? filename : filename + ".yml";
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

    @Override
    public void generateConfigDefaults() {

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
    public void processConfigFields() {

    }

    protected boolean configHas(String configKey) {
        return fileConfiguration.contains(configKey);
    }

    protected String processString(String path, String pluginDefault) {
        if (!configHas(path))
            return pluginDefault;
        try {
            return ChatColorConverter.convert(fileConfiguration.getString(path));
        } catch (Exception ex) {
            new WarningMessage("File " + filename + " has an incorrect entry for " + path);
        }
        return pluginDefault;
    }

    protected List<String> processStringList(String path, List<String> pluginDefault) {
        if (!configHas(path))
            return pluginDefault;
        try {
            List<String> list = new ArrayList<>(); ;
            for (String string : fileConfiguration.getStringList(path))
                list.add(ChatColorConverter.convert(string));
            return list;
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

    public ItemStack processItemStack(String path, ItemStack pluginDefault) {
        if (!configHas(path)) {
            return pluginDefault;
        }
        try {
            String materialString = processString(path, null);
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
                if (materialString.split(":")[1].contains("leather_") || materialString.split(":")[1].contains("LEATHER_") ) {
                    ItemStack itemStack = ItemStackGenerator.generateItemStack(Material.getMaterial(materialString.split(":")[0]));
                    LeatherArmorMeta leatherArmorMeta = (LeatherArmorMeta) itemStack.getItemMeta();
                    leatherArmorMeta.setColor(Color.fromRGB(Integer.parseInt(materialString.split(":")[1])));
                    itemStack.setItemMeta(leatherArmorMeta);
                    return itemStack;
                } else {
                    ItemStack itemStack = ItemStackGenerator.generateItemStack(Material.getMaterial(materialString.split(":")[0]));
                    ItemMeta itemMeta = itemStack.getItemMeta();
                    itemMeta.setCustomModelData(Integer.parseInt(materialString.split(":")[1]));
                    itemStack.setItemMeta(itemMeta);
                    return itemStack;
                }
            } else
                return ItemStackGenerator.generateItemStack(Material.getMaterial(materialString));
        } catch (Exception ex) {
            new WarningMessage("File " + filename + " has an incorrect entry for " + path);
        }
        return null;
    }

}
