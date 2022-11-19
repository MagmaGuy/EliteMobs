package com.magmaguy.elitemobs.config;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.thirdparty.modelengine.CustomModel;
import com.magmaguy.elitemobs.utils.InfoMessage;
import com.magmaguy.elitemobs.utils.SpigotMessage;
import com.magmaguy.elitemobs.utils.WarningMessage;
import com.magmaguy.elitemobs.utils.ZipFile;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class ConfigurationImporter {
    private ConfigurationImporter() {
    }

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
        boolean importedModels = false;

        for (File zippedFile : importsFile.listFiles()) {
            File unzippedFile;
            try {
                if (zippedFile.getName().contains(".zip"))
                    unzippedFile = ZipFile.unzip(zippedFile.getName());
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
                            moveDirectory(file, Paths.get(configurationsPath.normalize() + "" + File.separatorChar + "custombosses"), false);
                            break;
                        case "customitems":
                            moveDirectory(file, Paths.get(configurationsPath.normalize() + "" + File.separatorChar + "customitems"), false);
                            break;
                        case "customtreasurechests":
                            moveDirectory(file, Paths.get(configurationsPath.normalize() + "" + File.separatorChar + "customtreasurechests"), false);
                            break;
                        case "dungeonpackages":
                            moveDirectory(file, Paths.get(configurationsPath.normalize() + "" + File.separatorChar + "dungeonpackages"), false);
                            break;
                        case "customevents":
                            moveDirectory(file, Paths.get(configurationsPath.normalize() + "" + File.separatorChar + "customevents"), false);
                            break;
                        case "customspawns":
                            moveDirectory(file, Paths.get(configurationsPath.normalize() + "" + File.separatorChar + "customspawns"), false);
                            break;
                        case "customquests":
                            moveDirectory(file, Paths.get(configurationsPath.normalize() + "" + File.separatorChar + "customquests"), false);
                            break;
                        case "npcs":
                            moveDirectory(file, Paths.get(configurationsPath.normalize() + "" + File.separatorChar + "npcs"), false);
                            break;
                        case "wormholes":
                            moveDirectory(file, Paths.get(configurationsPath.normalize() + "" + File.separatorChar + "wormholes"), false);
                            break;
                        case "powers":
                            moveDirectory(file, Paths.get(configurationsPath.normalize() + "" + File.separatorChar + "powers"), false);
                            break;
                        case "worldcontainer":
                            moveWorlds(file);
                            break;
                        case "world_blueprints":
                            moveDirectory(file, Paths.get(configurationsPath.normalize() + "" + File.separatorChar + "world_blueprints"), false);
                            break;
                        case "ModelEngine":
                            //todo: check if the "force" code is required, check if file is getting saved with modelengine doesn't have a configuration folder
                            if (Bukkit.getPluginManager().isPluginEnabled("ModelEngine_Beta"))  //todo: this is just temporary
                                moveDirectory(file, Paths.get(file.getParentFile().getParentFile().getParentFile().getParentFile().toString()
                                        + File.separatorChar + "ModelEngine_Beta" + File.separatorChar + "blueprints"), true);
                            moveDirectory(file, Paths.get(file.getParentFile().getParentFile().getParentFile().getParentFile().toString()
                                    + File.separatorChar + "ModelEngine" + File.separatorChar + "blueprints"), true);
                            if (Bukkit.getPluginManager().isPluginEnabled("ModelEngine")) {
                                importedModels = true;
                            } else new WarningMessage("You need ModelEngine to use custom models!");
                            break;
                        case "schematics":
                            moveDirectory(file, Paths.get(configurationsPath.normalize() + "" + File.separatorChar + "schematics"), false);
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

        if (importedModels) {
            CustomModel.reloadModels();
            for (Player player : Bukkit.getOnlinePlayers())
                if (player.hasPermission("elitemobs.*"))
                    player.spigot().sendMessage(SpigotMessage.commandHoverMessage(
                            ChatColorConverter.convert("&8[EliteMobs] &fEliteMobs just detected that recently imported files had Custom Models in them! " +
                                    "&2Click here to generate the EliteMobs resource pack for those models!"),
                            "Clicking will run the command /em generateresourcepack",
                            "/em generateresourcepack"));
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

    private static void moveDirectory(File unzippedDirectory, Path targetPath, boolean force) {
        for (File file : unzippedDirectory.listFiles())
            try {
                new InfoMessage("Adding " + file.getCanonicalPath());
                moveFile(file, targetPath, force);
            } catch (Exception exception) {
                new WarningMessage("Failed to move directories for " + file.getName() + "! Tell the dev!");
                exception.printStackTrace();
            }
    }

    private static void moveFile(File file, Path targetPath, boolean force) {
        try {
            if (file.isDirectory()) {
                if (Paths.get(targetPath + "" + File.separatorChar + file.getName()).toFile().exists())
                    for (File iteratedFile : file.listFiles())
                        moveFile(iteratedFile, Paths.get(targetPath + "" + File.separatorChar + file.getName()), force);
                else
                    Files.move(file.toPath(), Paths.get(targetPath + "" + File.separatorChar + file.getName()), StandardCopyOption.REPLACE_EXISTING);
            } else if (targetPath.toFile().exists())
                Files.move(file.toPath(), Paths.get(targetPath + "" + File.separatorChar + file.getName()), StandardCopyOption.REPLACE_EXISTING);
            else if (!Paths.get(targetPath + "" + File.separatorChar + file.getName()).toFile().exists() && force) {
                File newFile = Paths.get(targetPath + "" + File.separatorChar + file.getName()).toFile();
                newFile.mkdirs();
                newFile.createNewFile();
                Files.move(file.toPath(), newFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (Exception exception) {
            new WarningMessage("Failed to move directories for " + file.getName() + "! Tell the dev!");
            exception.printStackTrace();
        }
    }

}
