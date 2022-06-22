package com.magmaguy.elitemobs.config;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.utils.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class ConfigurationExporter {
    private ConfigurationExporter() {
    }

    public static void initializeConfigs() {
        Path configurationsPath = Paths.get(MetadataHandler.PLUGIN.getDataFolder().getAbsolutePath());
        if (!Files.isDirectory(Paths.get(configurationsPath.normalize() + "" + File.separatorChar + "exports"))) {
            try {
                Files.createDirectory(Paths.get(configurationsPath.normalize() + "" + File.separatorChar + "exports"));
            } catch (Exception exception) {
                new WarningMessage("Failed to create exports directory! Tell the dev!");
                exception.printStackTrace();
            }
            return;
        }

        try {
            new File(Paths.get(MetadataHandler.PLUGIN.getDataFolder().getCanonicalPath() + File.separatorChar + "exports").toString());
        } catch (Exception ex) {
            new WarningMessage("Failed to get imports folder! Report this to the dev!");
        }

    }

    public static void createResourcePack(CommandSender commandSender) {
        if (!Bukkit.getPluginManager().isPluginEnabled("ModelEngine")) {
            commandSender.sendMessage(ChatColorConverter.convert("&8[EliteMobs]&c Could not generate resource pack because ModelEngine is not installed! Install ModelEngine to use this feature."));
            return;
        }

        File modelEngineResourcePackFile = new File(MetadataHandler.PLUGIN.getDataFolder().getParentFile().toString() + File.separatorChar + "ModelEngine" + File.separatorChar + "resource pack");
        if (!modelEngineResourcePackFile.exists()) {
            commandSender.sendMessage(ChatColorConverter.convert("&8[EliteMobs]&c Could not generate resource pack because ModelEngine is not installed! Install ModelEngine to use this feature."));
            return;
        }


        try {
            if (!Paths.get(MetadataHandler.PLUGIN.getDataFolder() + "" + File.separatorChar + "exports" + File.separatorChar + "elitemobs_resource_pack").toFile().exists())
                Files.createDirectory(Paths.get(MetadataHandler.PLUGIN.getDataFolder() + "" + File.separatorChar + "exports" + File.separatorChar + "elitemobs_resource_pack"));
        } catch (Exception ex) {
            commandSender.sendMessage(ChatColorConverter.convert("&8[EliteMobs]&c Failed to generate target directory plugins/EliteMobs/exports/elitemobs_resource_pack required for storing the resource pack! Report this to the dev!"));
            return;
        }

        copyDirectory(modelEngineResourcePackFile, Paths.get(MetadataHandler.PLUGIN.getDataFolder() + "" + File.separatorChar + "exports" + File.separatorChar + "elitemobs_resource_pack"));
        commandSender.sendMessage(ChatColorConverter.convert("&8[EliteMobs]&f Copied all files from Model Engine to " + MetadataHandler.PLUGIN.getDataFolder() + "" + File.separatorChar + "exports" + File.separatorChar + "elitemobs_resource_pack"));

        if (ZipFile.zip(new File(MetadataHandler.PLUGIN.getDataFolder() + "" + File.separatorChar + "exports" + File.separatorChar + "elitemobs_resource_pack"),
                MetadataHandler.PLUGIN.getDataFolder() + "" + File.separatorChar + "exports" + File.separatorChar + "elitemobs_resource_pack.zip"))
            commandSender.sendMessage(ChatColorConverter.convert("&8[EliteMobs]&f Packaged texture pack into " +
                    MetadataHandler.PLUGIN.getDataFolder() + "" + File.separatorChar + "exports" + File.separatorChar + "elitemobs_resource_pack.zip") +
                    " , ready to distribute!");
        else {
            commandSender.sendMessage(ChatColorConverter.convert("&8[EliteMobs]&c Failed to package the resource pack into a zipped file! Report this to the dev!"));
            return;
        }

        if (commandSender instanceof Player) {
            ((Player) commandSender).spigot().sendMessage(SpigotMessage.commandHoverMessage(ChatColorConverter.convert("Done! &2You can click here to update your server.properties with the new SHA1 value of this texture pack!"),
                    "Click runs the /em updateresourcepack command!", "/em updateresourcepack"));
            commandSender.sendMessage("If you want to do it manually, your SHA1 code is " + generateResourcePackSHA1(commandSender));
        } else {
            commandSender.sendMessage("Done! You can run the command   /em updateresourcepack   in order to put the right SHA1 value into server.properties. Don't forget to upload the texture place to some place where players can get it!");
            commandSender.sendMessage("If you want to do it manually, your SHA1 code is " + generateResourcePackSHA1(commandSender));
        }

    }

    private static String generateResourcePackSHA1(CommandSender commandSender) {
        File zippedResourcePack = Paths.get(MetadataHandler.PLUGIN.getDataFolder() + "" + File.separatorChar + "exports" + File.separatorChar + "elitemobs_resource_pack.zip").toFile();
        if (!zippedResourcePack.exists()) {
            commandSender.sendMessage("[EliteMobs] Failed to generate SHA-1 code, no zipped resource pack found!");
            return null;
        }
        String sha1 = null;
        try {
            sha1 = sha1Code(zippedResourcePack);
        } catch (Exception ex) {
            commandSender.sendMessage("[EliteMobs] Failed to generate SHA-1 code! Report this to the dev!");
            ex.printStackTrace();
            return null;
        }
        if (sha1 == null) {
            commandSender.sendMessage("[EliteMobs] SHA-1 code is null! Report this to the dev!");
            return null;
        }
        return sha1;
    }

    public static String sha1Code(File file) throws IOException, NoSuchAlgorithmException {
        FileInputStream fileInputStream = new FileInputStream(file);
        MessageDigest digest = MessageDigest.getInstance("SHA-1");
        DigestInputStream digestInputStream = new DigestInputStream(fileInputStream, digest);
        byte[] bytes = new byte[1024];
        // read all file content
        while (digestInputStream.read(bytes) > 0)
            digest = digestInputStream.getMessageDigest();
        byte[] resultByteArry = digest.digest();
        return bytesToHexString(resultByteArry);
    }

    public static String bytesToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            int value = b & 0xFF;
            if (value < 16) {
                // if value less than 16, then it's hex String will be only
                // one character, so we need to append a character of '0'
                sb.append("0");
            }
            sb.append(Integer.toHexString(value).toUpperCase());
        }
        return sb.toString();
    }

    private static void copyDirectory(File directoryToClone, Path targetPath) {
        for (File file : directoryToClone.listFiles())
            try {
                new InfoMessage("Adding " + file.getCanonicalPath());
                copyFile(file, targetPath);
            } catch (Exception exception) {
                new WarningMessage("Failed to move directories for " + file.getName() + "! Tell the dev!");
                exception.printStackTrace();
            }
    }

    private static void copyFile(File file, Path targetPath) {
        try {
            if (file.isDirectory()) {
                if (!Paths.get(targetPath + "" + File.separatorChar + file.getName()).toFile().exists())
                    Files.createDirectory(Paths.get(targetPath + "" + File.separatorChar + file.getName()));
                for (File iteratedFile : file.listFiles())
                    copyFile(iteratedFile, Paths.get(targetPath + "" + File.separatorChar + file.getName()));
            } else {
                if (!Paths.get(targetPath + "" + File.separatorChar + file.getName()).toFile().exists() ||
                        !targetPath.toString().contains("pack.png") && !targetPath.toString().contains("pack.mcmeta"))
                    Files.copy(file.toPath(), Paths.get(targetPath + "" + File.separatorChar + file.getName()), StandardCopyOption.REPLACE_EXISTING);
                else
                    new InfoMessage("File " + targetPath + "" + File.separatorChar + file.getName() + " already existed and should not be overwritten, skipping!");
            }
        } catch (Exception exception) {
            new WarningMessage("Failed to copy directories for " + file.getName() + "! Tell the dev!");
            exception.printStackTrace();
        }
    }

    public static void overwriteSHA1(CommandSender commandSender) {
        File serverProperties = null;
        try {
            serverProperties = new File(Paths.get(MetadataHandler.PLUGIN.getDataFolder().getParentFile().getCanonicalFile().getParentFile().toString() + File.separatorChar + "server.properties").toString());
            if (!serverProperties.exists()) {
                commandSender.sendMessage("[EliteMobs] Could not find server.properties file correctly! You will have to set the SHA-1 value manually!");
                return;
            }
        } catch (Exception exception) {
            new WarningMessage("[EliteMobs] Could not find server.properties file correctly! You will have to set the SHA-1 value manually.");
            exception.printStackTrace();
            return;
        }

        String sha1 = generateResourcePackSHA1(commandSender);
        if (sha1 == null) return;
        if (ServerPropertiesModifier.modify((Player) commandSender, "resource-pack-sha1", sha1)) {

            commandSender.sendMessage(ChatColor.GREEN + "[EliteMobs] Successfully set the value resource-pack-sha1=" + sha1 + " in server.properties!");
            commandSender.sendMessage(ChatColor.RED + "[EliteMobs] Don't forget to update the downloadable resource pack at your online location of choice!" +
                    " If you don't update the version people download things won't work correctly!");
            commandSender.sendMessage(ChatColor.GREEN + "[EliteMobs] The server.properties modification will work starting with the next restart!");
        } else {
            commandSender.sendMessage(ChatColor.RED + "[EliteMobs] Failed to write SHA1 value! You will have to add this manually. For reference, you SHA1 value is " + sha1);
        }

    }
}
