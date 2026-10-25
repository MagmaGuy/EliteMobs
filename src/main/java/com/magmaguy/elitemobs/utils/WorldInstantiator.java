package com.magmaguy.elitemobs.utils;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.instanced.WorldOperationQueue;
import com.magmaguy.magmacore.util.Logger;
import com.magmaguy.magmacore.util.TemporaryWorldManager;
import com.magmaguy.magmacore.util.WorldFolderResolver;
import com.magmaguy.magmacore.util.WorldBlueprint;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class WorldInstantiator {
    // Reserved for blueprint conversion, so anything found under this name is an earlier conversion's leftover.
    private static final String CONVERSION_WORLD = "elitemobs_blueprint_conversion";
    private static final Set<Path> pendingConversions = ConcurrentHashMap.newKeySet();
    // Copies read blueprints; a conversion replaces one under the write lock, off the main thread.
    private static final ReentrantReadWriteLock blueprintLock = new ReentrantReadWriteLock();
    // Held by whichever conversion step is using the conversion world's folder.
    private static final ReentrantLock conversionWorldLock = new ReentrantLock();

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
            Path root = blueprintFolder(worldName, configurationFolder);
            if (WorldBlueprint.hasInterruptedConversion(root)) {
                blueprintLock.writeLock().lock();
                try {
                    WorldBlueprint.recoverInterruptedConversion(root);
                } finally {
                    blueprintLock.writeLock().unlock();
                }
            }
            blueprintLock.readLock().lock();
            try {
                WorldBlueprint.inspect(root, environment).requireCompatibleServer(WorldFolderResolver.usesModernStorage());
            } finally {
                blueprintLock.readLock().unlock();
            }
            return true;
        } catch (IOException | RuntimeException failure) {
            Logger.warn("Cannot prepare blueprint " + worldName + ": " + failure.getMessage());
            return false;
        }
    }

    /**
     * Rewrites a blueprint once into the format this server saves worlds in, so instance copies load without the
     * server converting each one. The server converts a copy itself, and the blueprint is only replaced when the saved
     * result needs no further conversion. Imports replace blueprints whole, so updated content converts again.
     */
    public static void convertBlueprint(String worldName, String configurationFolder, World.Environment environment) {
        Path root;
        try {
            root = blueprintFolder(worldName, configurationFolder);
            if (!needsConversion(WorldBlueprint.inspect(root, environment))) return;
        } catch (IOException | RuntimeException failure) {
            return; // validateBlueprint reports blueprints that cannot be inspected
        }
        if (!pendingConversions.add(root)) return;
        AtomicReference<WorldBlueprint> prepared = new AtomicReference<>();
        AtomicBoolean adopting = new AtomicBoolean();
        WorldOperationQueue.queueSystemOperation(
                () -> prepareConversion(worldName, root, environment, prepared),
                () -> finishConversion(worldName, environment, prepared.get(), adopting),
                "Blueprint conversion " + worldName,
                () -> {
                    pendingConversions.remove(root);
                    // A scheduled adoption owns the conversion world and removes it when done.
                    if (adopting.get() || Bukkit.getWorld(CONVERSION_WORLD) != null || !conversionWorldLock.tryLock()) return;
                    try {
                        WorldFolderResolver.deleteAllLayouts(CONVERSION_WORLD);
                    } finally {
                        conversionWorldLock.unlock();
                    }
                });
    }

    private static boolean needsConversion(WorldBlueprint blueprint) {
        return blueprint.needsConversion(WorldFolderResolver.usesModernStorage(), Bukkit.getUnsafe().getDataVersion());
    }

    private static boolean prepareConversion(String worldName, Path root, World.Environment environment,
                                             AtomicReference<WorldBlueprint> prepared) {
        conversionWorldLock.lock();
        try {
            WorldFolderResolver.deleteAllLayouts(CONVERSION_WORLD);
            WorldBlueprint blueprint = WorldBlueprint.inspect(root, environment);
            // An earlier queued conversion of the same blueprint may already have finished.
            if (!needsConversion(blueprint)) return false;
            blueprint.copyForConversion(WorldFolderResolver.legacyFolder(CONVERSION_WORLD));
            prepared.set(blueprint);
            return true;
        } catch (IOException | RuntimeException failure) {
            warnConversionFailed(worldName, failure);
            return false;
        } finally {
            conversionWorldLock.unlock();
        }
    }

    /** Lets the server convert and save the prepared copy, then replaces the blueprint off the main thread. */
    private static void finishConversion(String worldName, World.Environment environment, WorldBlueprint blueprint,
                                         AtomicBoolean adopting) {
        boolean modern = WorldFolderResolver.usesModernStorage();
        int dataVersion = Bukkit.getUnsafe().getDataVersion();
        try {
            World world = Bukkit.createWorld(new WorldCreator(CONVERSION_WORLD)
                    .environment(environment).generator(TemporaryWorldManager.voidGenerator()));
            if (world == null) throw new IOException("the server did not load the conversion world");
            // Saving writes the level data in this server's current format.
            if (!Bukkit.unloadWorld(world, true)) throw new IOException("the server did not unload the conversion world");
            Path converted = modern
                    ? WorldFolderResolver.modernFolder(CONVERSION_WORLD) : WorldFolderResolver.legacyFolder(CONVERSION_WORLD);
            adopting.set(true);
            Bukkit.getScheduler().runTaskAsynchronously(MetadataHandler.PLUGIN,
                    () -> adoptConversion(worldName, blueprint, converted, modern, dataVersion));
        } catch (IOException | RuntimeException failure) {
            adopting.set(false);
            warnConversionFailed(worldName, failure);
        }
    }

    private static void adoptConversion(String worldName, WorldBlueprint blueprint, Path converted,
                                        boolean modern, int dataVersion) {
        conversionWorldLock.lock();
        try {
            blueprintLock.writeLock().lock();
            try {
                blueprint.adoptConversion(converted, modern, dataVersion);
            } finally {
                blueprintLock.writeLock().unlock();
            }
            Logger.info("Converted world blueprint " + worldName + " to this server's world format.");
        } catch (IOException | RuntimeException failure) {
            warnConversionFailed(worldName, failure);
        } finally {
            try {
                WorldFolderResolver.deleteAllLayouts(CONVERSION_WORLD);
            } finally {
                conversionWorldLock.unlock();
            }
        }
    }

    private static void warnConversionFailed(String worldName, Throwable failure) {
        Logger.warn("Could not convert world blueprint " + worldName + "; each instance keeps converting it as it loads: "
                + failure.getMessage());
    }

    public static File cloneWorld(String worldName, String targetWorldName, String configurationFolder,
                                  World.Environment environment) {
        blueprintLock.readLock().lock();
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
        } finally {
            blueprintLock.readLock().unlock();
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
