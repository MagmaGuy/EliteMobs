package com.magmaguy.elitemobs.utils;

import com.magmaguy.elitemobs.MetadataHandler;
import org.bukkit.Bukkit;
import org.bukkit.World;

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
        File destinationWorld = new File(Bukkit.getWorldContainer().getAbsolutePath() + File.separatorChar + targetWorldName);
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
        //new WarningMessage("File: " + file.getAbsolutePath());
        //new WarningMessage("Target path: " + targetPath);
        try {
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
        if (file.isDirectory()) {
            for (File subFile : file.listFiles())
                recursivelyDelete(subFile);
            file.delete();
        } else file.delete();
    }

    public static String getNewWorldName(String blueprintWorldName) {
        List<Integer> numberList = new ArrayList();
        for (World world : Bukkit.getWorlds()) {
            if (world.getName().contains(blueprintWorldName)) {
                try {
                    String[] strings = world.getName().replace(blueprintWorldName, "").split("_");
                    int worldNumber = Integer.parseInt(strings[strings.length - 1]);
                    numberList.add(worldNumber, worldNumber);
                } catch (Exception exception) {
                    continue;
                }
            }
        }
        int worldNumber = 0;
        if (!numberList.isEmpty())
            worldNumber = numberList.get(numberList.size() - 1);
        return blueprintWorldName + "_" + worldNumber;
    }
}
