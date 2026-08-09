package com.magmaguy.elitemobs.utils;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.magmacore.util.Logger;
import com.magmaguy.magmacore.util.WorldFolderResolver;
import org.bukkit.Bukkit;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

public class WorldInstantiator {

    public static File cloneWorld(String worldName, String targetWorldName, String dungeonConfigurationFolderName) {
        File blueprintWorld = new File(MetadataHandler.PLUGIN.getDataFolder().getAbsolutePath() + File.separatorChar +
                "world_blueprints" + File.separatorChar + dungeonConfigurationFolderName + File.separatorChar + worldName);
        if (!blueprintWorld.exists()) {
            Logger.warn("Blueprint world " + worldName + " does not exist! Path: " + blueprintWorld.getAbsolutePath());
            return null;
        }
        if (!blueprintWorld.isDirectory()) {
            Logger.warn("Blueprint world " + worldName + " is not a directory!");
            return null;
        }

        // Wipe both legacy and Paper-26.1+ modern paths so the blueprint clone
        // doesn't collide with leftovers from a previous instance of this world.
        WorldFolderResolver.deleteAllLayouts(targetWorldName);
        if (WorldFolderResolver.folderExists(targetWorldName)) {
            Logger.warn("Could not prepare instance world " + targetWorldName +
                    " because an existing world folder could not be removed.");
            return null;
        }

        File destinationWorld = new File(Bukkit.getWorldContainer(), targetWorldName);
        try {
            copyAll(blueprintWorld.toPath(), destinationWorld.toPath());
            return destinationWorld;
        } catch (IOException | RuntimeException exception) {
            Logger.warn("Failed to clone blueprint world " + blueprintWorld + " to " + destinationWorld +
                    ": " + exception.getMessage());
            WorldFolderResolver.deleteAllLayouts(targetWorldName);
            return null;
        }
    }

    private static void copyAll(Path sourceRoot, Path destinationRoot) throws IOException {
        Files.walkFileTree(sourceRoot, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path directory, BasicFileAttributes attributes)
                    throws IOException {
                Files.createDirectories(destinationRoot.resolve(sourceRoot.relativize(directory)));
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) throws IOException {
                Files.copy(file, destinationRoot.resolve(sourceRoot.relativize(file)),
                        StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    public static void recursivelyDelete(File file) {
        if (!file.exists()) {
            Logger.info("Attempted to recursively file " + file.getAbsolutePath() + " which doesn't exist.");
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
        // Picks up world folders at both legacy and Paper-26.1+ modern locations.
        worldNames.addAll(WorldFolderResolver.listAllWorldNames());
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
        highestNumber++;
        return blueprintWorldName + "_" + highestNumber;
    }
}
