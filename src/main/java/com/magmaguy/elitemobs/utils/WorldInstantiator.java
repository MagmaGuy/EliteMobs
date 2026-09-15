package com.magmaguy.elitemobs.utils;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.magmacore.util.Logger;
import com.magmaguy.magmacore.util.WorldFolderResolver;
import com.magmaguy.magmacore.util.WorldBlueprint;
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
            WorldBlueprint.inspect(blueprintFolder(worldName, configurationFolder), environment)
                    .requireCompatibleServer(WorldFolderResolver.usesModernStorage());
            return true;
        } catch (IOException | RuntimeException failure) {
            Logger.warn("Cannot prepare blueprint " + worldName + ": " + failure.getMessage());
            return false;
        }
    }

    public static File cloneWorld(String worldName, String targetWorldName, String configurationFolder,
                                  World.Environment environment) {
        try {
            if (!targetWorldName.matches("[a-zA-Z0-9_-]+")) throw new IOException("Invalid instance world name");
            var blueprint = WorldBlueprint.inspect(blueprintFolder(worldName, configurationFolder), environment);
            boolean modern = WorldFolderResolver.usesModernStorage();
            if (Bukkit.getWorld(targetWorldName) != null || WorldFolderResolver.folderExists(targetWorldName))
                throw new IOException("Instance destination already exists: " + targetWorldName);
            Path destination = blueprint.usesModernDestination(modern)
                    ? WorldFolderResolver.modernFolder(targetWorldName) : WorldFolderResolver.legacyFolder(targetWorldName);
            blueprint.copyTo(destination, environment, modern);
            return destination.toFile();
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
