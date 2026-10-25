package com.magmaguy.elitemobs.utils;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.magmacore.util.Logger;
import com.magmaguy.magmacore.util.WorldBlueprint;
import com.magmaguy.magmacore.util.WorldBlueprints;
import com.magmaguy.magmacore.util.WorldFolderResolver;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class WorldInstantiator {

    public static File cloneWorld(String worldName, String targetWorldName, String dungeonConfigurationFolderName) {
        return cloneWorld(worldName, targetWorldName, dungeonConfigurationFolderName, World.Environment.NORMAL);
    }

    private static Path blueprintFolder(String worldName, String configurationFolder) {
        Path root = MetadataHandler.PLUGIN.getDataFolder().toPath().resolve("world_blueprints").toAbsolutePath().normalize();
        Path result = root.resolve(configurationFolder).resolve(worldName).normalize();
        if (!result.startsWith(root) || result.equals(root)) throw new IllegalArgumentException("Blueprint path escapes world_blueprints");
        return result;
    }

    public static boolean validateBlueprint(String worldName, String configurationFolder, World.Environment environment) {
        try {
            WorldBlueprints.validate(blueprintFolder(worldName, configurationFolder), environment);
            return true;
        } catch (IOException | RuntimeException failure) {
            Logger.warn("Cannot prepare blueprint " + worldName + ": " + failure.getMessage());
            return false;
        }
    }

    /** Converts a blueprint once into this server's world format, so instances load without converting it. */
    public static void convertBlueprint(String worldName, String configurationFolder, World.Environment environment) {
        Path root;
        try {
            root = blueprintFolder(worldName, configurationFolder);
        } catch (RuntimeException failure) {
            return; // validateBlueprint reports blueprints that cannot be resolved
        }
        WorldBlueprints.convert(root, environment, worldName);
    }

    public static File cloneWorld(String worldName, String targetWorldName, String configurationFolder,
                                  World.Environment environment) {
        try {
            if (!targetWorldName.matches("[a-zA-Z0-9_-]+")) throw new IOException("Invalid instance world name");
            return WorldBlueprints.copy(blueprintFolder(worldName, configurationFolder), environment, targetWorldName,
                    WorldBlueprint.CopyListener.NONE).toFile();
        } catch (IOException | RuntimeException failure) {
            Logger.warn("Failed to prepare blueprint " + worldName + " as " + targetWorldName + ": " + failure.getMessage());
            return null;
        }
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
        String instancePrefix = blueprintWorldName.toLowerCase(Locale.ROOT) + "_";
        for (String worldName : worldNames) {
            if (worldName.toLowerCase(Locale.ROOT).startsWith(instancePrefix)) {
                try {
                    int worldNumber = Integer.parseInt(worldName.substring(instancePrefix.length()));
                    if (worldNumber > highestNumber) highestNumber = worldNumber;
                } catch (Exception exception) {
                }
            }
        }
        highestNumber++;
        return blueprintWorldName + "_" + highestNumber;
    }
}
