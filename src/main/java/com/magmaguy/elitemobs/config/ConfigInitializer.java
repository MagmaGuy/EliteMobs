package com.magmaguy.elitemobs.config;

import com.magmaguy.elitemobs.ChatColorConverter;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

public class ConfigInitializer {

    public static String setString(FileConfiguration fileConfiguration, String key, String defaultValue) {
        fileConfiguration.addDefault(key, defaultValue);
        return ChatColorConverter.convert(fileConfiguration.getString(key));
    }

    public static int setInt(FileConfiguration fileConfiguration, String key, int defaultValue) {
        fileConfiguration.addDefault(key, defaultValue);
        return fileConfiguration.getInt(key);
    }

    public static double setDouble(FileConfiguration fileConfiguration, String key, double defaultValue) {
        fileConfiguration.addDefault(key, defaultValue);
        return fileConfiguration.getDouble(key);
    }

    public static List setList(FileConfiguration fileConfiguration, String key, List defaultValue) {
        fileConfiguration.addDefault(key, defaultValue);
        return fileConfiguration.getList(key);
    }


}
