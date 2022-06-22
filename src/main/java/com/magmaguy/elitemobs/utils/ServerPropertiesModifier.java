package com.magmaguy.elitemobs.utils;

import com.magmaguy.elitemobs.MetadataHandler;
import org.bukkit.command.CommandSender;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Paths;
import java.util.Properties;

public class ServerPropertiesModifier {
    public static boolean modify(CommandSender player, String configKey, String configSetting) {
        File serverProperties = null;
        try {
            serverProperties = new File(Paths.get(MetadataHandler.PLUGIN.getDataFolder().getParentFile().getCanonicalFile().getParentFile().toString() + File.separatorChar + "server.properties").toString());
            if (!serverProperties.exists()) {
                player.sendMessage("[EliteMobs] Failed to find server.properties!");
                return false;
            }
        } catch (Exception exception) {
            new WarningMessage("[EliteMobs] Failed to access server.properties!");
            exception.printStackTrace();
            return false;
        }
        try {
            FileInputStream in = new FileInputStream(serverProperties);
            Properties props = new Properties();
            props.load(in);
            in.close();

            java.io.FileOutputStream out = new java.io.FileOutputStream(serverProperties);
            props.setProperty(configKey, configSetting);
            props.store(out, null);
            out.close();
        } catch (Exception ex) {
            player.sendMessage("[EliteMobs] Failed to write to server.properties!");
            return false;
        }
        return true;
    }

    public static String getValue(String configKey){
        File serverProperties = null;
        try {
            serverProperties = new File(Paths.get(MetadataHandler.PLUGIN.getDataFolder().getParentFile().getCanonicalFile().getParentFile().toString() + File.separatorChar + "server.properties").toString());
            if (!serverProperties.exists()) {
                new WarningMessage("Failed to find server.properties!");
                return null;
            }
        } catch (Exception exception) {
            new WarningMessage("Failed to access server.properties!");
            exception.printStackTrace();
            return null;
        }
        try {
            FileInputStream in = new FileInputStream(serverProperties);
            Properties props = new Properties();
            props.load(in);
            in.close();

            java.io.FileOutputStream out = new java.io.FileOutputStream(serverProperties);
            String value = props.getProperty(configKey);
            props.store(out, null);
            out.close();
            return value;
        } catch (Exception ex) {
            new WarningMessage("[EliteMobs] Failed to read server.properties!");
            return null;
        }
    }
}
