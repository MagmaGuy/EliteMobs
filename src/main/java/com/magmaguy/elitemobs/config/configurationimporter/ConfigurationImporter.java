package com.magmaguy.elitemobs.config.configurationimporter;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.utils.DeveloperMessage;
import com.magmaguy.elitemobs.utils.InfoMessage;
import com.magmaguy.elitemobs.utils.UnzipFile;
import com.magmaguy.elitemobs.utils.WarningMessage;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class ConfigurationImporter {

    public static void initializeConfigs() {
        Path configurationsPath = Paths.get(MetadataHandler.PLUGIN.getDataFolder().getAbsolutePath());
        if (!Files.isDirectory(Paths.get(configurationsPath.normalize() + "" + File.separatorChar + "imports"))) {
            try {
                Files.createDirectory(Paths.get(configurationsPath.normalize() + "" + File.separatorChar + "imports"));
            } catch (Exception exception) {
                new WarningMessage("Failed to create import directory! Tell the dev!");
                exception.printStackTrace();
            }
            return;
        }

        File importsFile = null;
        try {
            importsFile = new File(Paths.get(MetadataHandler.PLUGIN.getDataFolder().getCanonicalPath() + File.separatorChar + "imports").toString());
        } catch (Exception ex) {
            new WarningMessage("Failed to get imports folder! Report this to the dev!");
            return;
        }

        if (importsFile.listFiles().length == 0)
            return;

        for (File zippedFile : importsFile.listFiles()) {
            File unzippedFile;
            try {
                if (zippedFile.getName().contains(".zip"))
                    unzippedFile = UnzipFile.run(zippedFile.getName());
                else unzippedFile = zippedFile;
            } catch (Exception e) {
                new WarningMessage("Failed to unzip config file " + zippedFile.getName() + " ! Tell the dev!");
                e.printStackTrace();
                continue;
            }
            try {
                for (File file : unzippedFile.listFiles()) {
                    switch (file.getName()) {
                        case "custombosses":
                            moveDirectory(file, Paths.get(configurationsPath.normalize() + "" + File.separatorChar + "custombosses"));
                            break;
                        case "customitems":
                            moveDirectory(file, Paths.get(configurationsPath.normalize() + "" + File.separatorChar + "customitems"));
                            break;
                        case "customtreasurechests":
                            moveDirectory(file, Paths.get(configurationsPath.normalize() + "" + File.separatorChar + "customtreasurechests"));
                            break;
                        case "dungeonpackages":
                            moveDirectory(file, Paths.get(configurationsPath.normalize() + "" + File.separatorChar + "dungeonpackages"));
                            break;
                        case "worldcontainer":
                            moveWorlds(file);
                            break;
                        case "schematics":
                            if (Bukkit.getPluginManager().isPluginEnabled("FastAsyncWorldEdit")) {
                                moveDirectory(file, Paths.get(file.getParentFile().getParentFile().getParentFile().getParentFile().toString() + File.separatorChar + "FastAsyncWorldEdit" + File.separatorChar + "schematics"));
                            } else if (Bukkit.getPluginManager().isPluginEnabled("WorldEdit")) {
                                moveDirectory(file, Paths.get(file.getParentFile().getParentFile().getParentFile().getParentFile().toString() + File.separatorChar + "WorldEdit" + File.separatorChar + "schematics"));
                            } else
                                new WarningMessage("You need WorldGuard or FastAsyncWorldEdit to install schematic-based minidungeons!");
                            break;
                        default:
                            new WarningMessage("Directory " + file.getName() + " for zipped file " + zippedFile.getName() + " was not a recognized directory for the file import system! Was the zipped file packaged correctly?");
                    }
                    deleteDirectory(file);
                }
            } catch (Exception e) {
                new WarningMessage("Failed to move files from " + zippedFile.getName() + " ! Tell the dev!");
                e.printStackTrace();
                continue;
            }
            try {
                unzippedFile.delete();
                zippedFile.delete();
            } catch (Exception ex) {
                new WarningMessage("Failed to delete zipped file " + zippedFile.getName() + "! Tell the dev!");
                ex.printStackTrace();
            }
        }

    }

    private static void deleteDirectory(File file) {
        if (file == null)
            return;
        if (file.isDirectory())
            for (File iteratedFile : file.listFiles())
                if (iteratedFile != null)
                    deleteDirectory(iteratedFile);
        new InfoMessage("Cleaning up " + file.getPath());
        file.delete();
    }

    private static void moveWorlds(File worldcontainerFile) {
        for (File file : worldcontainerFile.listFiles())
            try {
                File destinationFile = new File(Paths.get(Bukkit.getWorldContainer().getCanonicalPath() + File.separatorChar + file.getName()).normalize().toString());
                if (destinationFile.exists()) {
                    new InfoMessage("Overriding existing directory " + destinationFile.getPath());
                    if (Bukkit.getWorld(file.getName()) != null) {
                        Bukkit.unloadWorld(file.getName(), false);
                        new WarningMessage("Unloaded world " + file.getName() + " for safe replacement!");
                    }
                    deleteDirectory(destinationFile);
                }
                FileUtils.moveDirectory(file, destinationFile);
            } catch (Exception exception) {
                new WarningMessage("Failed to move worlds for " + file.getName() + "! Tell the dev!");
                exception.printStackTrace();
            }
    }

    private static void moveDirectory(File unzippedDirectory, Path targetPath) {
        for (File file : unzippedDirectory.listFiles())
            try {
                new InfoMessage("Adding " + file.getCanonicalPath());
                moveFile(file, targetPath);
            } catch (Exception exception) {
                new WarningMessage("Failed to move directories for " + file.getName() + "! Tell the dev!");
                exception.printStackTrace();
            }
    }

    private static void moveFile(File file, Path targetPath) {
        try {
            new DeveloperMessage("Adding from " + targetPath);
            if (file.isDirectory()) {
                if (Paths.get(targetPath + "" + File.separatorChar + file.getName()).toFile().exists())
                    for (File iteratedFile : file.listFiles())
                        moveFile(iteratedFile, Paths.get(targetPath + "" + File.separatorChar + file.getName()));
                else {
                    new DeveloperMessage("Adding from " + file.toPath());
                    new DeveloperMessage("To " + Paths.get(targetPath + "" + File.separatorChar + file.getName()));
                    Files.move(file.toPath(), Paths.get(targetPath + "" + File.separatorChar + file.getName()), StandardCopyOption.REPLACE_EXISTING);
                }

            } else
                Files.move(file.toPath(), Paths.get(targetPath + "" + File.separatorChar + file.getName()), StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception exception) {
            new WarningMessage("Failed to move directories for " + file.getName() + "! Tell the dev!");
            exception.printStackTrace();
        }
    }

}
