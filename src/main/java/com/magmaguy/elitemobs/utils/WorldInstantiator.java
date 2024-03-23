package com.magmaguy.elitemobs.utils;

import com.magmaguy.elitemobs.MetadataHandler;
import org.bukkit.Bukkit;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

public class WorldInstantiator {

    public static File cloneWorld(String worldName, String targetWorldName, String dungeonConfigurationFolderName) {
        File blueprintWorld = new File(MetadataHandler.PLUGIN.getDataFolder().getAbsolutePath() + File.separatorChar +
                "world_blueprints" + File.separatorChar + dungeonConfigurationFolderName + File.separatorChar + worldName);
        if (!blueprintWorld.exists()) {
            new WarningMessage("Blueprint world " + worldName + " does not exist! Path: " + blueprintWorld.getAbsolutePath());
            return null;
        }
        if (!blueprintWorld.isDirectory()) {
            new WarningMessage("Blueprint world " + worldName + " is not a directory!");
            return null;
        }

        File destinationWorld = new File(Bukkit.getWorldContainer().getAbsolutePath().replace("\\.", "\\") + File.separatorChar + targetWorldName);
        if (destinationWorld.exists())
            recursivelyDelete(destinationWorld);

        copyAll(blueprintWorld, destinationWorld);

        return destinationWorld;
    }

    private static void copyAll(File directoryToClone, File targetDirectory) {
        for (File child : directoryToClone.listFiles())
            copy(child, Paths.get(targetDirectory.getPath() + File.separatorChar + child.getName()));
    }

    private static void copy(File file, Path targetPath) {
        try {
            if (!targetPath.getParent().toFile().exists()) targetPath.getParent().toFile().mkdirs();

            if (file.isDirectory()) {
                if (!targetPath.toFile().exists())
                    targetPath.toFile().mkdirs();
                for (File child : file.listFiles())
                    copy(child, Paths.get(targetPath.toString() + File.separatorChar + child.getName()));
            } else
                Files.copy(file.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception ex) {
            new WarningMessage("Failed to copy file " + file.toString() + " to " + targetPath.toString());
            ex.printStackTrace();
        }
    }

    public static void recursivelyDelete(File file) {
        if (!file.exists()) {
            new InfoMessage("Attempted to recursively file " + file.getAbsolutePath() + " which doesn't exist.");
            return;
        }
        if (file.isDirectory()) {
            for (File subFile : file.listFiles())
                recursivelyDelete(subFile);
            file.delete();
        } else file.delete();
    }

    public static String getNewWorldName(String blueprintWorldName) {
        List<String> worldNames = new ArrayList<>();
        Bukkit.getWorlds().forEach(world -> worldNames.add(world.getName()));
        for (File file : Bukkit.getWorldContainer().listFiles()) worldNames.add(file.getName());
        int highestNumber = 0;

        for (String worldName : worldNames) {
            if (worldName.contains(blueprintWorldName)) {
                try {
                    String[] strings = worldName.replace(blueprintWorldName, "").split("_");
                    int worldNumber = Integer.parseInt(strings[strings.length - 1]);
                    if (worldNumber > highestNumber) highestNumber = worldNumber;
                } catch (Exception exception) {
                }
            }
        }
        return blueprintWorldName + "_" + highestNumber;
    }
}
